package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Notification;
import org.example.springboot.service.NotificationService;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@Tag(name = "通知管理接口")
@RestController
@RequestMapping("/notification")
public class NotificationController {
    
    @Resource
    private NotificationService notificationService;
    
    @Operation(summary = "获取用户通知列表")
    @GetMapping("/list")
    public Result<Page<Notification>> getUserNotifications(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type) {
        try {
            Page<Notification> page = notificationService.getUserNotifications(currentPage, size, type);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        try {
            long count = notificationService.getUnreadCount();
            return Result.success(count);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "标记通知为已读")
    @PutMapping("/{id}/read")
    public Result<String> markAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return Result.success("标记成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "标记所有通知为已读")
    @PutMapping("/read-all")
    public Result<String> markAllAsRead() {
        try {
            notificationService.markAllAsRead();
            return Result.success("全部标记成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    public Result<String> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
