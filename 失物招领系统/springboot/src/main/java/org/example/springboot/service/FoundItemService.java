package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.ClaimApplication;
import org.example.springboot.entity.FoundItem;
import org.example.springboot.entity.ItemCategory;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ClaimApplicationMapper;
import org.example.springboot.mapper.FoundItemMapper;
import org.example.springboot.mapper.ItemCategoryMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.example.springboot.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 招领信息服务类
 */
@Service
public class FoundItemService {
    private static final Logger log = LoggerFactory.getLogger(FoundItemService.class);

    @Resource
    private FoundItemMapper foundItemMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private ItemCategoryMapper itemCategoryMapper;

    @Resource
    private UserService userService;

    @Resource
    private ClaimApplicationMapper claimApplicationMapper;

    @Resource
    private ItemStatusService itemStatusService;
    
    /**
     * 分页查询招领信息
     * @param title 标题（可选）
     * @param categoryId 分类ID（可选）
     * @param status 状态（可选）
     * @param currentPage 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    public Page<FoundItem> queryPage(String title, Long categoryId, Integer status, int currentPage, int size) {
        Page<FoundItem> page = new Page<>(currentPage, size);
        
        LambdaQueryWrapper<FoundItem> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like(FoundItem::getTitle, title);
        }
        
        if (categoryId != null) {
            queryWrapper.eq(FoundItem::getCategoryId, categoryId);
        }
        
        if (status != null) {
            queryWrapper.eq(FoundItem::getStatus, status);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(FoundItem::getCreateTime);
        
        // 执行查询
        foundItemMapper.selectPage(page, queryWrapper);
        
        // 填充关联信息
        fillInfo(page.getRecords());
        
        return page;
    }
    
    /**
     * 获取当前用户的招领列表
     * @return 招领列表
     */
    public List<FoundItem> getCurrentUserFoundItems() {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        LambdaQueryWrapper<FoundItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FoundItem::getUserId, currentUser.getId())
                .orderByDesc(FoundItem::getCreateTime);
        
        List<FoundItem> foundItems = foundItemMapper.selectList(queryWrapper);
        fillInfo(foundItems);
        
        return foundItems;
    }
    
    /**
     * 根据ID获取招领信息
     * @param id ID
     * @return 招领信息
     */
    public FoundItem getById(Long id) {
        FoundItem foundItem = foundItemMapper.selectById(id);
        if (foundItem == null) {
            throw new ServiceException("招领信息不存在");
        }
        
        // 填充关联信息
        fillInfo(List.of(foundItem));
        
        return foundItem;
    }
    
    /**
     * 添加招领信息
     * @param foundItem 招领信息
     * @return 添加结果
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(FoundItem foundItem) {
        // 获取当前用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }

        // 检查用户状态
        userService.checkUserCanOperate(currentUser);

        // 完整的数据验证
        validateFoundItemData(foundItem);

        // 初始化状态
        foundItem.setUserId(currentUser.getId());
        foundItem.setStatus(0); // 初始化为待认领状态

        // 插入数据库
        foundItemMapper.insert(foundItem);
    }

    /**
     * 验证招领信息数据
     */
    private void validateFoundItemData(FoundItem foundItem) {
        // 验证标题
        ValidationUtils.validateStringLength(foundItem.getTitle(), "标题", 2, 100);

        // 验证描述
        ValidationUtils.validateDescription(foundItem.getDescription());

        // 验证分类ID
        ValidationUtils.validateId(foundItem.getCategoryId(), "分类");

        // 验证拾取地点
        ValidationUtils.validateLocation(foundItem.getFoundPlace(), "拾取地点");

        // 验证拾取时间
        ValidationUtils.validateReasonableTime(foundItem.getFoundTime(), "拾取时间", 365);

        // 验证联系人姓名
        ValidationUtils.validateStringLength(foundItem.getContactName(), "联系人姓名", 2, 50);

        // 验证联系电话
        ValidationUtils.validatePhone(foundItem.getContactPhone());

        // 验证图片路径
        ValidationUtils.validateImagePaths(foundItem.getImages());
    }
    
    /**
     * 更新招领信息
     * @param foundItem 招领信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(FoundItem foundItem) {
        // 检查招领信息是否存在
        FoundItem existingFound = foundItemMapper.selectById(foundItem.getId());
        if (existingFound == null) {
            throw new ServiceException("招领信息不存在");
        }
        
        // 获取当前用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        // 只有管理员或者发布者可以修改
        if (!"ADMIN".equals(currentUser.getRoleCode()) && !Objects.equals(existingFound.getUserId(), currentUser.getId())) {
            throw new ServiceException("无权限修改此招领信息");
        }
        
        // 不能修改状态
        foundItem.setStatus(existingFound.getStatus());
        
        // 不能修改用户ID
        foundItem.setUserId(existingFound.getUserId());
        
        // 更新数据库
        foundItemMapper.updateById(foundItem);
    }
    
    /**
     * 更新招领信息状态
     * @param id ID
     * @param status 状态
     * @param reason 原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status, String reason) {
        itemStatusService.updateFoundItemStatus(id, status, reason);
    }

    /**
     * 更新招领信息状态（兼容旧接口）
     * @param id ID
     * @param status 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        updateStatus(id, status, null);
    }
    
    /**
     * 删除招领信息
     * @param id ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        delete(id, false);
    }

    /**
     * 删除招领信息
     * @param id ID
     * @param force 是否强制删除（级联删除相关申请）
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, boolean force) {
        // 检查招领信息是否存在
        FoundItem existingFound = foundItemMapper.selectById(id);
        if (existingFound == null) {
            throw new ServiceException("招领信息不存在");
        }

        // 获取当前用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }

        // 只有管理员或者发布者可以删除
        if (!"ADMIN".equals(currentUser.getRoleCode()) && !Objects.equals(existingFound.getUserId(), currentUser.getId())) {
            throw new ServiceException("无权限删除此招领信息");
        }

        if (force) {
            // 强制删除：先删除相关申请
            deleteRelatedApplications(id, 0);
        } else {
            // 检查关联关系
            checkRelated(id);
        }

        // 删除数据
        foundItemMapper.deleteById(id);
    }
    
    /**
     * 检查关联关系
     * @param foundItemId 招领信息ID
     */
    private void checkRelated(Long foundItemId) {
        // 检查是否有未处理的认领申请
        LambdaQueryWrapper<ClaimApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClaimApplication::getItemId, foundItemId)
                   .eq(ClaimApplication::getItemType, 0) // 招领信息
                   .eq(ClaimApplication::getStatus, 0); // 待审核状态

        long count = claimApplicationMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException("该招领信息还有未处理的认领申请，无法删除。如需强制删除，请使用级联删除功能。");
        }
    }

    /**
     * 删除相关申请
     * @param itemId 物品ID
     * @param itemType 物品类型（0招领信息，1失物信息）
     */
    private void deleteRelatedApplications(Long itemId, Integer itemType) {
        LambdaQueryWrapper<ClaimApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClaimApplication::getItemId, itemId)
                   .eq(ClaimApplication::getItemType, itemType)
                   .eq(ClaimApplication::getStatus, 0); // 只删除待审核的申请

        List<ClaimApplication> applications = claimApplicationMapper.selectList(queryWrapper);

        for (ClaimApplication application : applications) {
            // 设置为已取消状态而不是直接删除，保留数据完整性
            application.setStatus(3); // 已取消
            application.setAuditRemark("因物品被删除而自动取消");
            application.setAuditTime(LocalDateTime.now());
            application.setUpdateTime(LocalDateTime.now());
            claimApplicationMapper.updateById(application);
        }
    }
    
    /**
     * 填充关联信息
     * @param foundItems 招领信息列表
     */
    private void fillInfo(List<FoundItem> foundItems) {
        if (foundItems == null || foundItems.isEmpty()) {
            return;
        }
        
        for (FoundItem foundItem : foundItems) {
            // 填充分类名称
            if (foundItem.getCategoryId() != null) {
                ItemCategory category = itemCategoryMapper.selectById(foundItem.getCategoryId());
                if (category != null) {
                    foundItem.setCategoryName(category.getName());
                }
            }
            
            // 填充用户名
            if (foundItem.getUserId() != null) {
                User user = userMapper.selectById(foundItem.getUserId());
                if (user != null) {
                    foundItem.setUsername(user.getUsername());
                }
            }
        }
    }
} 