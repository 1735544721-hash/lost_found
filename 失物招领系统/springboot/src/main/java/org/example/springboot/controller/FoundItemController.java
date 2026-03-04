package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.FoundItem;
import org.example.springboot.service.FoundItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 招领信息控制器
 */
@Tag(name = "招领信息管理")
@RestController
@RequestMapping("/found-item")
public class FoundItemController {
    private static final Logger log = LoggerFactory.getLogger(FoundItemController.class);
    
    @Resource
    private FoundItemService foundItemService;
    
    /**
     * 分页查询招领信息
     */
    @Operation(summary = "分页查询招领信息")
    @GetMapping("/page")
    public Result<Page<FoundItem>> page(
            @Parameter(description = "标题") @RequestParam(required = false) String title,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "状态(0待认领,1已认领,2已关闭)") @RequestParam(required = false) Integer status,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("分页查询招领信息: title={}, categoryId={}, status={}, page={}, size={}",
                title, categoryId, status, currentPage, size);
        
        Page<FoundItem> page = foundItemService.queryPage(title, categoryId, status, currentPage, size);
        return Result.success(page);
    }
    
    /**
     * 获取当前用户的招领信息列表
     */
    @Operation(summary = "获取当前用户的招领信息列表")
    @GetMapping("/my")
    public Result<List<FoundItem>> myFoundItems() {
        List<FoundItem> items = foundItemService.getCurrentUserFoundItems();
        return Result.success(items);
    }
    
    /**
     * 根据ID获取招领信息
     */
    @Operation(summary = "根据ID获取招领信息")
    @GetMapping("/{id}")
    public Result<FoundItem> getById(@Parameter(description = "招领信息ID") @PathVariable Long id) {
        FoundItem foundItem = foundItemService.getById(id);
        return Result.success(foundItem);
    }
    
    /**
     * 添加招领信息
     */
    @Operation(summary = "添加招领信息")
    @PostMapping
    public Result<?> add(@RequestBody FoundItem foundItem) {
        foundItemService.add(foundItem);
        return Result.success();
    }
    
    /**
     * 更新招领信息
     */
    @Operation(summary = "更新招领信息")
    @PutMapping
    public Result<?> update(@RequestBody FoundItem foundItem) {
        foundItemService.update(foundItem);
        return Result.success();
    }
    
    /**
     * 更新招领信息状态
     */
    @Operation(summary = "更新招领信息状态")
    @PutMapping("/status/{id}/{status}")
    public Result<?> updateStatus(
            @Parameter(description = "招领信息ID") @PathVariable Long id,
            @Parameter(description = "状态(0待认领,1已认领,2已关闭)") @PathVariable Integer status) {
        
        foundItemService.updateStatus(id, status);
        return Result.success();
    }
    
    /**
     * 删除招领信息
     */
    @Operation(summary = "删除招领信息")
    @DeleteMapping("/{id}")
    public Result<?> delete(@Parameter(description = "招领信息ID") @PathVariable Long id) {
        foundItemService.delete(id);
        return Result.success();
    }
} 