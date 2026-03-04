package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.springboot.entity.ClaimApplication;
import org.example.springboot.entity.ItemCategory;
import org.example.springboot.entity.LostItem;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ClaimApplicationMapper;
import org.example.springboot.mapper.ItemCategoryMapper;
import org.example.springboot.mapper.LostItemMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.example.springboot.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LostItemService extends ServiceImpl<LostItemMapper, LostItem> {
    @Resource
    private LostItemMapper lostItemMapper;
    
    @Resource
    private ItemCategoryService itemCategoryService;
    
    @Resource
    private UserService userService;
    @Autowired
    private ItemCategoryMapper itemCategoryMapper;
    @Autowired
    private UserMapper userMapper;

    @Resource
    private ClaimApplicationMapper claimApplicationMapper;

    @Resource
    private ItemStatusService itemStatusService;

    /**
     * 添加失物信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void addLostItem(LostItem lostItem) {
        // 获取当前登录用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("未登录或登录已过期");
        }

        // 检查用户状态
        userService.checkUserCanOperate(currentUser);

        // 完整的数据验证
        validateLostItemData(lostItem);

        // 设置用户ID和默认状态
        lostItem.setUserId(currentUser.getId());
        lostItem.setStatus(0); // 待认领状态

        // 验证分类是否存在
        if (lostItem.getCategoryId() != null) {
            itemCategoryService.getCategoryById(lostItem.getCategoryId());
        }

        if (lostItemMapper.insert(lostItem) <= 0) {
            throw new ServiceException("添加失物信息失败");
        }
    }

    /**
     * 验证失物信息数据
     */
    private void validateLostItemData(LostItem lostItem) {
        // 验证标题
        ValidationUtils.validateStringLength(lostItem.getTitle(), "标题", 2, 100);

        // 验证描述
        ValidationUtils.validateDescription(lostItem.getDescription());

        // 验证分类ID
        ValidationUtils.validateId(lostItem.getCategoryId(), "分类");

        // 验证丢失地点
        ValidationUtils.validateLocation(lostItem.getLostPlace(), "丢失地点");

        // 验证丢失时间
        ValidationUtils.validateReasonableTime(lostItem.getLostTime(), "丢失时间", 365);

        // 验证联系人姓名
        ValidationUtils.validateStringLength(lostItem.getContactName(), "联系人姓名", 2, 50);

        // 验证联系电话
        ValidationUtils.validatePhone(lostItem.getContactPhone());

        // 验证图片路径
        ValidationUtils.validateImagePaths(lostItem.getImages());
    }
    
    /**
     * 更新失物信息
     */
    public void updateLostItem(Long id, LostItem lostItem) {
        // 检查失物信息是否存在
        LostItem existItem = getLostItemById(id);
        
        // 验证当前用户是否有权限修改（只有管理员或发布者可以修改）
        checkUpdatePermission(existItem);
        
        // 验证分类是否存在
        if (lostItem.getCategoryId() != null) {
            itemCategoryService.getCategoryById(lostItem.getCategoryId());
        }
        
        // 设置ID，保留创建时间和用户ID
        lostItem.setId(id);
        lostItem.setUserId(existItem.getUserId());
        
        if (lostItemMapper.updateById(lostItem) <= 0) {
            throw new ServiceException("更新失物信息失败");
        }
    }
    
    /**
     * 删除失物信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteLostItem(Long id) {
        deleteLostItem(id, false);
    }

    /**
     * 删除失物信息
     * @param id 失物ID
     * @param force 是否强制删除（级联删除相关申请）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteLostItem(Long id, boolean force) {
        // 检查失物信息是否存在
        LostItem existItem = getLostItemById(id);

        // 验证当前用户是否有权限删除（只有管理员或发布者可以删除）
        checkUpdatePermission(existItem);

        if (force) {
            // 强制删除：先删除相关申请
            deleteRelatedApplications(id, 1);
        } else {
            // 检查关联关系
            checkRelated(id);
        }

        if (lostItemMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除失物信息失败");
        }
    }

    /**
     * 检查关联关系
     * @param lostItemId 失物信息ID
     */
    private void checkRelated(Long lostItemId) {
        // 检查是否有未处理的认领申请
        LambdaQueryWrapper<ClaimApplication> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClaimApplication::getItemId, lostItemId)
                   .eq(ClaimApplication::getItemType, 1) // 失物信息
                   .eq(ClaimApplication::getStatus, 0); // 待审核状态

        long count = claimApplicationMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException("该失物信息还有未处理的认领申请，无法删除。如需强制删除，请使用级联删除功能。");
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
     * 根据ID获取失物信息
     */
    public LostItem getLostItemById(Long id) {
        LostItem lostItem = lostItemMapper.selectById(id);
        if (lostItem == null) {
            throw new ServiceException("失物信息不存在");
        }
        
        // 填充分类名称和用户名
        fillInfo(lostItem);
        
        return lostItem;
    }
    
    /**
     * 分页查询失物信息
     */
    public Page<LostItem> getLostItemByPage(String title, Long categoryId, Integer status, Integer currentPage, Integer size) {
        LambdaQueryWrapper<LostItem> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like(LostItem::getTitle, title);
        }
        
        if (categoryId != null) {
            queryWrapper.eq(LostItem::getCategoryId, categoryId);
        }
        
        if (status != null) {
            queryWrapper.eq(LostItem::getStatus, status);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(LostItem::getCreateTime);
        
        Page<LostItem> page = lostItemMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
        
        // 填充关联信息
        for (LostItem item : page.getRecords()) {
            fillInfo(item);
        }
        
        return page;
    }
    
    /**
     * 获取当前用户发布的失物信息
     */
    public Page<LostItem> getUserLostItems(Integer currentPage, Integer size) {
        // 获取当前登录用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("未登录或登录已过期");
        }
        
        LambdaQueryWrapper<LostItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LostItem::getUserId, currentUser.getId())
                   .orderByDesc(LostItem::getCreateTime);
        
        Page<LostItem> page = lostItemMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
        
        // 填充关联信息
        for (LostItem item : page.getRecords()) {
            fillInfo(item);
        }
        
        return page;
    }
    
    /**
     * 修改失物信息状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status, String reason) {
        itemStatusService.updateLostItemStatus(id, status, reason);
    }

    /**
     * 修改失物信息状态（兼容旧接口）
     */
    public void updateStatus(Long id, Integer status) {
        updateStatus(id, status, null);
    }
    

    
    /**
     * 验证当前用户是否有权限修改/删除失物信息
     */
    private void checkUpdatePermission(LostItem lostItem) {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("未登录或登录已过期");
        }
        
        // 管理员或发布者可以修改
        boolean isAdmin = "ADMIN".equals(currentUser.getRoleCode());
        boolean isOwner = currentUser.getId().equals(lostItem.getUserId());
        
        if (!isAdmin && !isOwner) {
            throw new ServiceException("无权限操作");
        }
    }

    /**
     * 分页查询失物信息
     * @param currentPage 当前页码
     * @param size 每页大小
     * @param title 标题(可选)
     * @param categoryId 分类ID(可选)
     * @param status 状态(可选)
     * @param userId 用户ID(可选)
     * @return 分页结果
     */
    public Page<LostItem> queryByPage(Integer currentPage, Integer size, String title, Long categoryId, Integer status, Long userId) {
        LambdaQueryWrapper<LostItem> queryWrapper = new LambdaQueryWrapper<>();
        
        // 按标题模糊查询
        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like(LostItem::getTitle, title);
        }
        
        // 按分类ID查询
        if (categoryId != null) {
            queryWrapper.eq(LostItem::getCategoryId, categoryId);
        }
        
        // 按状态查询
        if (status != null) {
            queryWrapper.eq(LostItem::getStatus, status);
        }
        
        // 按用户ID查询
        if (userId != null) {
            queryWrapper.eq(LostItem::getUserId, userId);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(LostItem::getCreateTime);
        
        // 分页查询
        Page<LostItem> page = page(new Page<>(currentPage, size), queryWrapper);
        
        // 填充分类名称和用户名
        fillInfoBatch(page.getRecords());
        
        return page;
    }

    /**
     * 根据ID查询失物信息并填充关联信息
     * @param id 失物ID
     * @return 失物信息
     */
    public LostItem getItemById(Long id) {
        LostItem lostItem = getById(id);
        if (lostItem != null) {
            fillInfo(lostItem);
        }
        return lostItem;
    }

    /**
     * 填充失物关联信息(分类名称和用户名)
     * @param lostItem 失物信息
     */
    private void fillInfo(LostItem lostItem) {
        // 填充分类名称
        if (lostItem.getCategoryId() != null) {
            ItemCategory category = itemCategoryMapper.selectById(lostItem.getCategoryId());
            if (category != null) {
                lostItem.setCategoryName(category.getName());
            }
        }
        
        // 填充用户名
        if (lostItem.getUserId() != null) {
            User user = userMapper.selectById(lostItem.getUserId());
            if (user != null) {
                lostItem.setUsername(user.getUsername());
            }
        }
    }
    
    /**
     * 批量填充失物关联信息
     * @param lostItems 失物信息列表
     */
    private void fillInfoBatch(java.util.List<LostItem> lostItems) {
        if (lostItems != null && !lostItems.isEmpty()) {
            for (LostItem item : lostItems) {
                fillInfo(item);
            }
        }
    }
    
    /**
     * 统计指定状态的失物数量
     * @param status 状态值
     * @return 数量
     */
    public long countByStatus(Integer status) {
        LambdaQueryWrapper<LostItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LostItem::getStatus, status);
        return count(queryWrapper);
    }
} 