package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Notification;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.NotificationMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知服务类
 */
@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    
    @Resource
    private NotificationMapper notificationMapper;
    
    @Resource
    private UserMapper userMapper;
    
    /**
     * 发送通知
     */
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(Long userId, String title, String content, 
                               Notification.NotificationType type, Long relatedId) {
        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType(type.getValue());
            notification.setRelatedId(relatedId);
            notification.setIsRead(0); // 未读
            notification.setCreateTime(LocalDateTime.now());
            
            notificationMapper.insert(notification);
            log.info("通知发送成功: userId={}, title={}", userId, title);
        } catch (Exception e) {
            log.error("通知发送失败: userId={}, title={}, error={}", userId, title, e.getMessage(), e);
            // 通知发送失败不应该影响主业务流程，所以这里只记录日志
        }
    }
    
    /**
     * 发送申请通知
     */
    public void sendApplicationNotification(Long publisherUserId, String itemTitle, 
                                          String applicantName, Long applicationId) {
        String title = "新的认领申请";
        String content = String.format("用户 %s 申请认领您发布的物品：%s，请及时处理。", 
                                      applicantName, itemTitle);
        sendNotification(publisherUserId, title, content, 
                        Notification.NotificationType.APPLICATION, applicationId);
    }
    
    /**
     * 发送审核结果通知
     */
    public void sendAuditResultNotification(Long applicantUserId, String itemTitle, 
                                          boolean approved, String auditRemark, Long applicationId) {
        String title = approved ? "认领申请已通过" : "认领申请被拒绝";
        String content = String.format("您申请认领的物品：%s，审核结果：%s。%s", 
                                      itemTitle, 
                                      approved ? "通过" : "拒绝",
                                      auditRemark != null ? "备注：" + auditRemark : "");
        sendNotification(applicantUserId, title, content, 
                        Notification.NotificationType.AUDIT, applicationId);
    }
    
    /**
     * 发送状态变更通知
     */
    public void sendStatusChangeNotification(Long userId, String itemTitle, 
                                           String oldStatus, String newStatus, Long itemId) {
        String title = "物品状态变更";
        String content = String.format("您发布的物品：%s，状态已从 %s 变更为 %s。", 
                                      itemTitle, oldStatus, newStatus);
        sendNotification(userId, title, content, 
                        Notification.NotificationType.SYSTEM, itemId);
    }
    
    /**
     * 获取用户通知列表
     */
    public Page<Notification> getUserNotifications(Integer currentPage, Integer size, Integer type) {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, currentUser.getId());
        
        if (type != null) {
            queryWrapper.eq(Notification::getType, type);
        }
        
        queryWrapper.orderByDesc(Notification::getCreateTime);
        
        Page<Notification> page = notificationMapper.selectPage(
            new Page<>(currentPage, size), queryWrapper);
        
        // 填充类型描述
        fillTypeDescription(page.getRecords());
        
        return page;
    }
    
    /**
     * 获取未读通知数量
     */
    public long getUnreadCount() {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            return 0;
        }
        
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, currentUser.getId())
                   .eq(Notification::getIsRead, 0);
        
        return notificationMapper.selectCount(queryWrapper);
    }
    
    /**
     * 标记通知为已读
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId) {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new ServiceException("通知不存在");
        }
        
        if (!notification.getUserId().equals(currentUser.getId())) {
            throw new ServiceException("无权限操作此通知");
        }
        
        if (notification.getIsRead() == 0) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }
    }
    
    /**
     * 标记所有通知为已读
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead() {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, currentUser.getId())
                   .eq(Notification::getIsRead, 0);
        
        List<Notification> unreadNotifications = notificationMapper.selectList(queryWrapper);
        
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }
    }
    
    /**
     * 删除通知
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotification(Long notificationId) {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new ServiceException("通知不存在");
        }
        
        if (!notification.getUserId().equals(currentUser.getId())) {
            throw new ServiceException("无权限删除此通知");
        }
        
        notificationMapper.deleteById(notificationId);
    }
    
    /**
     * 填充类型描述
     */
    private void fillTypeDescription(List<Notification> notifications) {
        for (Notification notification : notifications) {
            try {
                Notification.NotificationType type = 
                    Notification.NotificationType.fromValue(notification.getType());
                notification.setTypeDescription(type.getDescription());
            } catch (Exception e) {
                notification.setTypeDescription("未知类型");
            }
        }
    }
}
