package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.ClaimApplication;
import org.example.springboot.entity.FoundItem;
import org.example.springboot.entity.LostItem;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ClaimApplicationMapper;
import org.example.springboot.mapper.FoundItemMapper;
import org.example.springboot.mapper.LostItemMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认领申请Service
 */
@Service
public class ClaimApplicationService {
    private static final Logger log = LoggerFactory.getLogger(ClaimApplicationService.class);
    
    @Resource
    private ClaimApplicationMapper claimApplicationMapper;
    
    @Resource
    private FoundItemMapper foundItemMapper;
    
    @Resource
    private LostItemMapper lostItemMapper;
    
    @Resource
    private UserMapper userMapper;

    @Resource
    private NotificationService notificationService;
    
    /**
     * 添加认领申请
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(ClaimApplication claimApplication) {
        // 获取当前用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        // 设置申请人ID
        claimApplication.setUserId(currentUser.getId());
        
        // 检查物品是否存在
        if (claimApplication.getItemType() == 0) {
            // 招领信息
            FoundItem foundItem = foundItemMapper.selectById(claimApplication.getItemId());
            if (foundItem == null) {
                throw new ServiceException("招领信息不存在");
            }
            
            // 检查物品状态是否为待认领
            if (foundItem.getStatus() != 0) {
                throw new ServiceException("该物品不可认领，可能已被认领或已关闭");
            }
            
            // 检查是否是自己发布的物品
            if (foundItem.getUserId().equals(currentUser.getId())) {
                throw new ServiceException("不能认领自己发布的物品");
            }
        } else if (claimApplication.getItemType() == 1) {
            // 失物信息
            LostItem lostItem = lostItemMapper.selectById(claimApplication.getItemId());
            if (lostItem == null) {
                throw new ServiceException("失物信息不存在");
            }
            
            // 检查物品状态是否为待认领
            if (lostItem.getStatus() != 0) {
                throw new ServiceException("该物品不可认领，可能已被认领或已关闭");
            }
            
            // 检查是否是自己发布的物品
            if (lostItem.getUserId().equals(currentUser.getId())) {
                throw new ServiceException("不能认领自己发布的物品");
            }
        } else {
            throw new ServiceException("物品类型错误");
        }
        
        // 检查是否已经申请过该物品
        LambdaQueryWrapper<ClaimApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClaimApplication::getItemId, claimApplication.getItemId())
                .eq(ClaimApplication::getItemType, claimApplication.getItemType())
                .eq(ClaimApplication::getUserId, currentUser.getId())
                .ne(ClaimApplication::getStatus, 2); // 不等于已拒绝的状态
        
        long count = claimApplicationMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException("您已申请过该物品，请勿重复申请");
        }
        
        // 设置初始状态为待审核
        claimApplication.setStatus(0);
        claimApplication.setCreateTime(LocalDateTime.now());
        claimApplication.setUpdateTime(LocalDateTime.now());
        
        // 保存申请记录
        claimApplicationMapper.insert(claimApplication);

        // 发送申请通知给物品发布者
        sendApplicationNotification(claimApplication, currentUser);
    }
    
    /**
     * 分页查询认领申请
     */
    public Page<ClaimApplication> page(Integer currentPage, Integer size, Integer status, Integer itemType, String itemTitle) {
        Page<ClaimApplication> page = new Page<>(currentPage, size);
        LambdaQueryWrapper<ClaimApplication> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (status != null) {
            queryWrapper.eq(ClaimApplication::getStatus, status);
        }
        
        if (itemType != null) {
            queryWrapper.eq(ClaimApplication::getItemType, itemType);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(ClaimApplication::getCreateTime);
        
        Page<ClaimApplication> resultPage = claimApplicationMapper.selectPage(page, queryWrapper);
        
        // 填充关联信息
        fillInfo(resultPage.getRecords(), itemTitle);
        
        return resultPage;
    }
    
    /**
     * 查询我的申请
     */
    public Page<ClaimApplication> pageMyApplications(Integer currentPage, Integer size, Integer status) {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        Page<ClaimApplication> page = new Page<>(currentPage, size);
        LambdaQueryWrapper<ClaimApplication> queryWrapper = new LambdaQueryWrapper<>();
        
        // 查询当前用户的申请
        queryWrapper.eq(ClaimApplication::getUserId, currentUser.getId());
        
        // 添加状态查询条件
        if (status != null) {
            queryWrapper.eq(ClaimApplication::getStatus, status);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(ClaimApplication::getCreateTime);
        
        Page<ClaimApplication> resultPage = claimApplicationMapper.selectPage(page, queryWrapper);
        
        // 填充关联信息
        fillInfo(resultPage.getRecords(), null);
        
        return resultPage;
    }
    
    /**
     * 查询待我审核的申请
     */
    public Page<ClaimApplication> pageMyAudit(Integer currentPage, Integer size, Integer status) {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        Page<ClaimApplication> page = new Page<>(currentPage, size);
        LambdaQueryWrapper<ClaimApplication> queryWrapper = new LambdaQueryWrapper<>();
        
        // 查询当前用户发布的物品的申请
        queryWrapper.and(wrapper -> {
            wrapper.exists("SELECT 1 FROM found_item WHERE found_item.id = claim_application.item_id AND claim_application.item_type = 0 AND found_item.user_id = " + currentUser.getId())
                .or()
                .exists("SELECT 1 FROM lost_item WHERE lost_item.id = claim_application.item_id AND claim_application.item_type = 1 AND lost_item.user_id = " + currentUser.getId());
        });
        
        // 添加状态查询条件
        if (status != null) {
            queryWrapper.eq(ClaimApplication::getStatus, status);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(ClaimApplication::getCreateTime);
        
        Page<ClaimApplication> resultPage = claimApplicationMapper.selectPage(page, queryWrapper);
        
        // 填充关联信息
        fillInfo(resultPage.getRecords(), null);
        
        return resultPage;
    }
    
    /**
     * 审核认领申请
     */
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, Integer status, String auditRemark) {
        // 获取当前用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }

        // 获取申请记录 - 使用悲观锁防止并发
        ClaimApplication claimApplication = claimApplicationMapper.selectById(id);
        if (claimApplication == null) {
            throw new ServiceException("申请记录不存在");
        }

        // 检查申请状态
        if (claimApplication.getStatus() != 0) {
            throw new ServiceException("该申请已被处理，无法重复审核");
        }
        
        // 检查是否有权限审核
        boolean hasPermission = false;
        
        // 管理员或物品发布者可以审核
        if ("ADMIN".equals(currentUser.getRoleCode())) {
            hasPermission = true;
        } else {
            // 检查是否是物品发布者
            if (claimApplication.getItemType() == 0) {
                // 招领信息
                FoundItem foundItem = foundItemMapper.selectById(claimApplication.getItemId());
                if (foundItem != null && foundItem.getUserId().equals(currentUser.getId())) {
                    hasPermission = true;
                }
            } else if (claimApplication.getItemType() == 1) {
                // 失物信息
                LostItem lostItem = lostItemMapper.selectById(claimApplication.getItemId());
                if (lostItem != null && lostItem.getUserId().equals(currentUser.getId())) {
                    hasPermission = true;
                }
            }
        }
        
        if (!hasPermission) {
            throw new ServiceException("您没有权限审核该申请");
        }
        
        // 更新申请状态
        claimApplication.setStatus(status);
        claimApplication.setAuditUserId(currentUser.getId());
        claimApplication.setAuditTime(LocalDateTime.now());
        claimApplication.setAuditRemark(auditRemark);
        claimApplication.setUpdateTime(LocalDateTime.now());
        
        claimApplicationMapper.updateById(claimApplication);

        // 发送审核结果通知
        sendAuditResultNotification(claimApplication, status == 1, auditRemark);

        // 如果审核通过，更新物品状态为已认领
        if (status == 1) {
            if (claimApplication.getItemType() == 0) {
                // 招领信息
                FoundItem foundItem = foundItemMapper.selectById(claimApplication.getItemId());
                if (foundItem != null) {
                    // 再次检查物品状态，防止并发问题
                    if (foundItem.getStatus() != 0) {
                        throw new ServiceException("该物品已被认领或已关闭，无法继续处理");
                    }

                    foundItem.setStatus(1); // 设置为已认领
                    foundItemMapper.updateById(foundItem);

                    // 拒绝其他申请
                    rejectOtherApplications(claimApplication.getItemId(), claimApplication.getItemType(), id);
                }
            } else if (claimApplication.getItemType() == 1) {
                // 失物信息
                LostItem lostItem = lostItemMapper.selectById(claimApplication.getItemId());
                if (lostItem != null) {
                    // 再次检查物品状态，防止并发问题
                    if (lostItem.getStatus() != 0) {
                        throw new ServiceException("该物品已被认领或已关闭，无法继续处理");
                    }

                    lostItem.setStatus(1); // 设置为已认领
                    lostItemMapper.updateById(lostItem);

                    // 拒绝其他申请
                    rejectOtherApplications(claimApplication.getItemId(), claimApplication.getItemType(), id);
                }
            }
        }
    }
    
    /**
     * 拒绝其他申请
     */
    private void rejectOtherApplications(Long itemId, Integer itemType, Long acceptedApplicationId) {
        LambdaQueryWrapper<ClaimApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClaimApplication::getItemId, itemId)
                .eq(ClaimApplication::getItemType, itemType)
                .eq(ClaimApplication::getStatus, 0) // 待审核的申请
                .ne(ClaimApplication::getId, acceptedApplicationId); // 不包括已接受的申请
        
        List<ClaimApplication> applications = claimApplicationMapper.selectList(queryWrapper);
        
        for (ClaimApplication application : applications) {
            application.setStatus(2); // 设置为已拒绝
            application.setAuditRemark("已有其他申请被接受");
            application.setUpdateTime(LocalDateTime.now());
            claimApplicationMapper.updateById(application);
        }
    }
    
    /**
     * 填充关联信息
     */
    private void fillInfo(List<ClaimApplication> records, String itemTitle) {
        if (records == null || records.isEmpty()) {
            return;
        }
        
        for (ClaimApplication record : records) {
            // 填充物品标题
            if (record.getItemType() == 0) {
                // 招领信息
                FoundItem foundItem = foundItemMapper.selectById(record.getItemId());
                if (foundItem != null) {
                    record.setItemTitle(foundItem.getTitle());
                    
                    // 如果需要过滤物品标题
                    if (StringUtils.isNotBlank(itemTitle) && !foundItem.getTitle().contains(itemTitle)) {
                        continue;
                    }
                }
            } else if (record.getItemType() == 1) {
                // 失物信息
                LostItem lostItem = lostItemMapper.selectById(record.getItemId());
                if (lostItem != null) {
                    record.setItemTitle(lostItem.getTitle());
                    
                    // 如果需要过滤物品标题
                    if (StringUtils.isNotBlank(itemTitle) && !lostItem.getTitle().contains(itemTitle)) {
                        continue;
                    }
                }
            }
            
            // 填充申请人信息
            User user = userMapper.selectById(record.getUserId());
            if (user != null) {
                record.setUsername(user.getUsername());
                record.setName(user.getName());
            }
            
            // 填充审核人信息
            if (record.getAuditUserId() != null) {
                User auditUser = userMapper.selectById(record.getAuditUserId());
                if (auditUser != null) {
                    record.setAuditUsername(auditUser.getUsername());
                    record.setAuditName(auditUser.getName());
                }
            }
        }
    }
    
    /**
     * 取消认领申请
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelClaim(Long id) {
        // 获取当前用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        // 获取申请记录
        ClaimApplication claimApplication = claimApplicationMapper.selectById(id);
        if (claimApplication == null) {
            throw new ServiceException("申请记录不存在");
        }
        
        // 验证是否是自己的申请
        if (!claimApplication.getUserId().equals(currentUser.getId())) {
            throw new ServiceException("无法取消他人的申请");
        }
        
        // 检查申请状态
        if (claimApplication.getStatus() != 0) {
            throw new ServiceException("该申请已处理，无法取消");
        }
        
        // 更新申请状态为已取消(3)，而不是已拒绝(2)
        claimApplication.setStatus(3); // 设置为已取消
        claimApplication.setAuditRemark("用户主动取消申请");
        claimApplication.setAuditTime(LocalDateTime.now());
        claimApplication.setUpdateTime(LocalDateTime.now());
        
        claimApplicationMapper.updateById(claimApplication);
    }

    /**
     * 发送申请通知
     */
    private void sendApplicationNotification(ClaimApplication claimApplication, User applicant) {
        try {
            String itemTitle = "";
            Long publisherUserId = null;

            if (claimApplication.getItemType() == 0) {
                // 招领信息
                FoundItem foundItem = foundItemMapper.selectById(claimApplication.getItemId());
                if (foundItem != null) {
                    itemTitle = foundItem.getTitle();
                    publisherUserId = foundItem.getUserId();
                }
            } else if (claimApplication.getItemType() == 1) {
                // 失物信息
                LostItem lostItem = lostItemMapper.selectById(claimApplication.getItemId());
                if (lostItem != null) {
                    itemTitle = lostItem.getTitle();
                    publisherUserId = lostItem.getUserId();
                }
            }

            if (publisherUserId != null) {
                notificationService.sendApplicationNotification(
                    publisherUserId, itemTitle, applicant.getName() != null ? applicant.getName() : applicant.getUsername(),
                    claimApplication.getId());
            }
        } catch (Exception e) {
            // 通知发送失败不影响主流程
            log.error("发送申请通知失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送审核结果通知
     */
    private void sendAuditResultNotification(ClaimApplication claimApplication, boolean approved, String auditRemark) {
        try {
            String itemTitle = "";

            if (claimApplication.getItemType() == 0) {
                // 招领信息
                FoundItem foundItem = foundItemMapper.selectById(claimApplication.getItemId());
                if (foundItem != null) {
                    itemTitle = foundItem.getTitle();
                }
            } else if (claimApplication.getItemType() == 1) {
                // 失物信息
                LostItem lostItem = lostItemMapper.selectById(claimApplication.getItemId());
                if (lostItem != null) {
                    itemTitle = lostItem.getTitle();
                }
            }

            notificationService.sendAuditResultNotification(
                claimApplication.getUserId(), itemTitle, approved, auditRemark, claimApplication.getId());
        } catch (Exception e) {
            // 通知发送失败不影响主流程
            log.error("发送审核结果通知失败: {}", e.getMessage(), e);
        }
    }
} 