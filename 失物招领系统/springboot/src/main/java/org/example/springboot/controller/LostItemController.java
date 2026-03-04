package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.LostItem;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.service.LostItemService;
import org.example.springboot.service.UserService;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "失物信息管理")
@RestController
@RequestMapping("/lost-item")
public class LostItemController {
    private static final Logger log = LoggerFactory.getLogger(LostItemController.class);
    
    @Resource
    private LostItemService lostItemService;
    
    @Resource
    private UserService userService;
    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "分页查询失物信息")
    @GetMapping("/page")
    public Result<Page<LostItem>> page(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页记录数") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "标题") @RequestParam(required = false) String title,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "状态: 0-待认领, 1-已认领, 2-已关闭") @RequestParam(required = false) Integer status,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {
        Page<LostItem> page = lostItemService.queryByPage(currentPage, size, title, categoryId, status, userId);
        return Result.success(page);
    }
    
    @Operation(summary = "根据ID查询失物信息")
    @GetMapping("/{id}")
    public Result<LostItem> getById(@PathVariable Long id) {
        LostItem lostItem = lostItemService.getItemById(id);
        return Result.success(lostItem);
    }
    
    @Operation(summary = "新增失物信息")
    @PostMapping
    public Result<String> save(@RequestBody LostItem lostItem) {
        try {
            lostItemService.addLostItem(lostItem);
            return Result.success("失物信息发布成功");
        } catch (ServiceException e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "修改失物信息")
    @PutMapping("/{id}")
    public Result<LostItem> update(@PathVariable Long id, @RequestBody LostItem lostItem) {
        // 获取当前登录用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        LostItem existItem = lostItemService.getById(id);
        if (existItem == null) {
            return Result.error("失物信息不存在");
        }
        
        // 验证操作权限（创建者或管理员可以修改）
        if (!existItem.getUserId().equals(currentUser.getId()) && !"ADMIN".equals(currentUser.getRoleCode())) {
            return Result.error("无权修改该失物信息");
        }
        
        lostItem.setId(id);
        lostItemService.updateById(lostItem);
        return Result.success(lostItem);
    }
    
    @Operation(summary = "删除失物信息")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        // 获取当前登录用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        LostItem existItem = lostItemService.getById(id);
        if (existItem == null) {
            return Result.error("失物信息不存在");
        }
        
        // 验证操作权限（创建者或管理员可以删除）
        if (!existItem.getUserId().equals(currentUser.getId()) && !"ADMIN".equals(currentUser.getRoleCode())) {
            return Result.error("无权删除该失物信息");
        }
        
        lostItemService.removeById(id);
        return Result.success(Boolean.TRUE);
    }
    
    @Operation(summary = "修改失物认领状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, Integer> params) {
        
        Integer status = params.get("status");
        if (status == null) {
            return Result.error("状态参数不能为空");
        }
        
        // 获取当前登录用户
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        LostItem existItem = lostItemService.getById(id);
        if (existItem == null) {
            return Result.error("失物信息不存在");
        }
        
        // 验证操作权限（创建者或管理员可以修改状态）
        if (!existItem.getUserId().equals(currentUser.getId()) && !"ADMIN".equals(currentUser.getRoleCode())) {
            return Result.error("无权修改该失物信息状态");
        }
        
        LostItem updateItem = new LostItem();
        updateItem.setId(id);
        updateItem.setStatus(status);
        lostItemService.updateById(updateItem);
        
        return Result.success(Boolean.TRUE);
    }
    
    @Operation(summary = "获取失物信息统计数据")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取失物总数
        long totalItems = lostItemService.count();
        
        // 获取待认领数量
        long totalPending = lostItemService.countByStatus(0);
        
        // 获取已认领数量
        long totalClaimed = lostItemService.countByStatus(1);
        
        // 获取用户总数
        long totalUsers = userMapper.selectCount(null);
        
        statistics.put("totalItems", totalItems);
        statistics.put("totalPending", totalPending);
        statistics.put("totalClaimed", totalClaimed);
        statistics.put("totalUsers", totalUsers);
        
        return Result.success(statistics);
    }

} 