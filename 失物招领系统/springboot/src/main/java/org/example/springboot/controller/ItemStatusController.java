package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.enumClass.ItemStatus;
import org.example.springboot.service.ItemStatusService;
import org.springframework.web.bind.annotation.*;

/**
 * 物品状态管理控制器
 */
@Tag(name = "物品状态管理接口")
@RestController
@RequestMapping("/item-status")
public class ItemStatusController {
    
    @Resource
    private ItemStatusService itemStatusService;
    
    @Operation(summary = "更新失物状态")
    @PutMapping("/lost/{id}")
    public Result<String> updateLostItemStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason) {
        try {
            itemStatusService.updateLostItemStatus(id, status, reason);
            return Result.success("状态更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "更新招领状态")
    @PutMapping("/found/{id}")
    public Result<String> updateFoundItemStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason) {
        try {
            itemStatusService.updateFoundItemStatus(id, status, reason);
            return Result.success("状态更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取物品状态统计")
    @GetMapping("/statistics")
    public Result<ItemStatusService.ItemStatusStatistics> getStatusStatistics() {
        try {
            ItemStatusService.ItemStatusStatistics statistics = itemStatusService.getStatusStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取所有状态枚举")
    @GetMapping("/enum")
    public Result<ItemStatus[]> getStatusEnum() {
        return Result.success(ItemStatus.values());
    }
    
    @Operation(summary = "获取状态转换规则")
    @GetMapping("/transitions/{currentStatus}")
    public Result<ItemStatus[]> getStatusTransitions(@PathVariable Integer currentStatus) {
        try {
            ItemStatus status = ItemStatus.fromValue(currentStatus);
            ItemStatus[] nextStatuses = status.getNextPossibleStatuses();
            return Result.success(nextStatuses);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "处理过期物品")
    @PostMapping("/process-expired")
    public Result<String> processExpiredItems(@RequestParam(defaultValue = "30") int expireDays) {
        try {
            itemStatusService.processExpiredItems(expireDays);
            return Result.success("过期物品处理完成");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
