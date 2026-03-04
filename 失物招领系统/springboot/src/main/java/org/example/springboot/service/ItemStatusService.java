package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.entity.FoundItem;
import org.example.springboot.entity.LostItem;
import org.example.springboot.entity.User;
import org.example.springboot.enumClass.ItemStatus;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.FoundItemMapper;
import org.example.springboot.mapper.LostItemMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物品状态管理服务
 */
@Service
public class ItemStatusService {
    private static final Logger log = LoggerFactory.getLogger(ItemStatusService.class);
    
    @Resource
    private LostItemMapper lostItemMapper;
    
    @Resource
    private FoundItemMapper foundItemMapper;
    
    @Resource
    private NotificationService notificationService;
    
    /**
     * 更新失物状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateLostItemStatus(Long itemId, Integer newStatus, String reason) {
        LostItem lostItem = lostItemMapper.selectById(itemId);
        if (lostItem == null) {
            throw new ServiceException("失物信息不存在");
        }
        
        // 检查权限
        checkUpdatePermission(lostItem.getUserId());
        
        // 验证状态转换
        ItemStatus currentStatus = ItemStatus.fromValue(lostItem.getStatus());
        ItemStatus targetStatus = ItemStatus.fromValue(newStatus);
        
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new ServiceException(currentStatus.getTransitionAdvice(targetStatus));
        }
        
        // 更新状态
        Integer oldStatus = lostItem.getStatus();
        lostItem.setStatus(newStatus);

        lostItemMapper.updateById(lostItem);
        
        // 发送状态变更通知
        sendStatusChangeNotification(lostItem.getUserId(), lostItem.getTitle(), 
                                    currentStatus.getDescription(), targetStatus.getDescription(), itemId);
        
        log.info("失物状态更新成功: itemId={}, oldStatus={}, newStatus={}, reason={}", 
                itemId, oldStatus, newStatus, reason);
    }
    
    /**
     * 更新招领状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateFoundItemStatus(Long itemId, Integer newStatus, String reason) {
        FoundItem foundItem = foundItemMapper.selectById(itemId);
        if (foundItem == null) {
            throw new ServiceException("招领信息不存在");
        }
        
        // 检查权限
        checkUpdatePermission(foundItem.getUserId());
        
        // 验证状态转换
        ItemStatus currentStatus = ItemStatus.fromValue(foundItem.getStatus());
        ItemStatus targetStatus = ItemStatus.fromValue(newStatus);
        
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new ServiceException(currentStatus.getTransitionAdvice(targetStatus));
        }
        
        // 更新状态
        Integer oldStatus = foundItem.getStatus();
        foundItem.setStatus(newStatus);

        foundItemMapper.updateById(foundItem);
        
        // 发送状态变更通知
        sendStatusChangeNotification(foundItem.getUserId(), foundItem.getTitle(), 
                                    currentStatus.getDescription(), targetStatus.getDescription(), itemId);
        
        log.info("招领状态更新成功: itemId={}, oldStatus={}, newStatus={}, reason={}", 
                itemId, oldStatus, newStatus, reason);
    }
    
    /**
     * 批量处理过期物品
     */
    @Transactional(rollbackFor = Exception.class)
    public void processExpiredItems(int expireDays) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(expireDays);
        
        // 处理过期失物
        LambdaQueryWrapper<LostItem> lostQueryWrapper = new LambdaQueryWrapper<>();
        lostQueryWrapper.eq(LostItem::getStatus, ItemStatus.PENDING.getValue())
                       .lt(LostItem::getCreateTime, expireTime);
        
        List<LostItem> expiredLostItems = lostItemMapper.selectList(lostQueryWrapper);
        for (LostItem item : expiredLostItems) {
            item.setStatus(ItemStatus.EXPIRED.getValue());
            lostItemMapper.updateById(item);
            
            // 发送过期通知
            sendStatusChangeNotification(item.getUserId(), item.getTitle(), 
                                       ItemStatus.PENDING.getDescription(), 
                                       ItemStatus.EXPIRED.getDescription(), item.getId());
        }
        
        // 处理过期招领
        LambdaQueryWrapper<FoundItem> foundQueryWrapper = new LambdaQueryWrapper<>();
        foundQueryWrapper.eq(FoundItem::getStatus, ItemStatus.PENDING.getValue())
                        .lt(FoundItem::getCreateTime, expireTime);
        
        List<FoundItem> expiredFoundItems = foundItemMapper.selectList(foundQueryWrapper);
        for (FoundItem item : expiredFoundItems) {
            item.setStatus(ItemStatus.EXPIRED.getValue());
            foundItemMapper.updateById(item);
            
            // 发送过期通知
            sendStatusChangeNotification(item.getUserId(), item.getTitle(), 
                                       ItemStatus.PENDING.getDescription(), 
                                       ItemStatus.EXPIRED.getDescription(), item.getId());
        }
        
        log.info("处理过期物品完成: 失物{}个, 招领{}个", expiredLostItems.size(), expiredFoundItems.size());
    }
    
    /**
     * 获取物品状态统计
     */
    public ItemStatusStatistics getStatusStatistics() {
        ItemStatusStatistics statistics = new ItemStatusStatistics();
        
        // 统计失物状态
        for (ItemStatus status : ItemStatus.values()) {
            LambdaQueryWrapper<LostItem> lostWrapper = new LambdaQueryWrapper<>();
            lostWrapper.eq(LostItem::getStatus, status.getValue());
            long lostCount = lostItemMapper.selectCount(lostWrapper);
            
            LambdaQueryWrapper<FoundItem> foundWrapper = new LambdaQueryWrapper<>();
            foundWrapper.eq(FoundItem::getStatus, status.getValue());
            long foundCount = foundItemMapper.selectCount(foundWrapper);
            
            statistics.addStatusCount(status, lostCount, foundCount);
        }
        
        return statistics;
    }
    
    /**
     * 检查更新权限
     */
    private void checkUpdatePermission(Long itemUserId) {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        boolean isAdmin = "ADMIN".equals(currentUser.getRoleCode());
        boolean isOwner = currentUser.getId().equals(itemUserId);
        
        if (!isAdmin && !isOwner) {
            throw new ServiceException("无权限操作");
        }
    }
    
    /**
     * 发送状态变更通知
     */
    private void sendStatusChangeNotification(Long userId, String itemTitle, 
                                            String oldStatus, String newStatus, Long itemId) {
        try {
            notificationService.sendStatusChangeNotification(userId, itemTitle, oldStatus, newStatus, itemId);
        } catch (Exception e) {
            log.error("发送状态变更通知失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 状态统计内部类
     */
    public static class ItemStatusStatistics {
        private long pendingLost = 0;
        private long claimedLost = 0;
        private long completedLost = 0;
        private long closedLost = 0;
        private long expiredLost = 0;
        
        private long pendingFound = 0;
        private long claimedFound = 0;
        private long completedFound = 0;
        private long closedFound = 0;
        private long expiredFound = 0;
        
        public void addStatusCount(ItemStatus status, long lostCount, long foundCount) {
            switch (status) {
                case PENDING:
                    this.pendingLost = lostCount;
                    this.pendingFound = foundCount;
                    break;
                case CLAIMED:
                    this.claimedLost = lostCount;
                    this.claimedFound = foundCount;
                    break;
                case COMPLETED:
                    this.completedLost = lostCount;
                    this.completedFound = foundCount;
                    break;
                case CLOSED:
                    this.closedLost = lostCount;
                    this.closedFound = foundCount;
                    break;
                case EXPIRED:
                    this.expiredLost = lostCount;
                    this.expiredFound = foundCount;
                    break;
            }
        }
        
        // Getters
        public long getPendingLost() { return pendingLost; }
        public long getClaimedLost() { return claimedLost; }
        public long getCompletedLost() { return completedLost; }
        public long getClosedLost() { return closedLost; }
        public long getExpiredLost() { return expiredLost; }
        
        public long getPendingFound() { return pendingFound; }
        public long getClaimedFound() { return claimedFound; }
        public long getCompletedFound() { return completedFound; }
        public long getClosedFound() { return closedFound; }
        public long getExpiredFound() { return expiredFound; }
        
        public long getTotalLost() {
            return pendingLost + claimedLost + completedLost + closedLost + expiredLost;
        }
        
        public long getTotalFound() {
            return pendingFound + claimedFound + completedFound + closedFound + expiredFound;
        }
    }
}
