package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.ItemCategory;
import org.example.springboot.service.ItemCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "物品分类管理")
@RestController
@RequestMapping("/category")
public class ItemCategoryController {
    private static final Logger log = LoggerFactory.getLogger(ItemCategoryController.class);
    
    @Resource
    private ItemCategoryService itemCategoryService;
    
    @Operation(summary = "获取分类列表")
    @GetMapping("/list")
    public Result<List<ItemCategory>> getAllCategories() {
        log.info("获取所有分类列表");
        List<ItemCategory> categories = itemCategoryService.getAllCategories();
        return Result.success(categories);
    }
    
    @Operation(summary = "分页查询分类")
    @GetMapping("/page")
    public Result<Page<ItemCategory>> getCategoryByPage(
            @Parameter(description = "分类名称") @RequestParam(required = false) String name,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("分页查询分类: name={}, currentPage={}, size={}", name, currentPage, size);
        Page<ItemCategory> page = itemCategoryService.getCategoryByPage(name, currentPage, size);
        return Result.success(page);
    }
    
    @Operation(summary = "根据ID获取分类")
    @GetMapping("/{id}")
    public Result<ItemCategory> getCategoryById(@PathVariable Long id) {
        log.info("根据ID获取分类: id={}", id);
        ItemCategory category = itemCategoryService.getCategoryById(id);
        return Result.success(category);
    }
    
    @Operation(summary = "添加分类")
    @PostMapping
    public Result<?> addCategory(@RequestBody ItemCategory category) {
        log.info("添加分类: {}", category);
        itemCategoryService.addCategory(category);
        return Result.success();
    }
    
    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody ItemCategory category) {
        log.info("更新分类: id={}, category={}", id, category);
        itemCategoryService.updateCategory(id, category);
        return Result.success();
    }
    
    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        log.info("删除分类: id={}", id);
        itemCategoryService.deleteCategory(id);
        return Result.success();
    }
} 