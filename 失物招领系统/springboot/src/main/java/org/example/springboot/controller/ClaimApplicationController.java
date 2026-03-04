package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.ClaimApplication;
import org.example.springboot.service.ClaimApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 认领申请控制器
 */
@Tag(name = "认领申请管理")
@RestController
@RequestMapping("/claim")
public class ClaimApplicationController {
    
    private static final Logger log = LoggerFactory.getLogger(ClaimApplicationController.class);
    
    @Resource
    private ClaimApplicationService claimApplicationService;
    
    /**
     * 提交认领申请
     */
    @Operation(summary = "提交认领申请")
    @PostMapping
    public Result<Void> add(@RequestBody ClaimApplication claimApplication) {
        log.info("提交认领申请: {}", claimApplication);
        claimApplicationService.add(claimApplication);
        return Result.success();
    }
    
    /**
     * 分页查询认领申请（管理员）
     */
    @Operation(summary = "分页查询认领申请")
    @GetMapping("/page")
    public Result<Page<ClaimApplication>> page(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "物品类型") @RequestParam(required = false) Integer itemType,
            @Parameter(description = "物品标题") @RequestParam(required = false) String itemTitle
    ) {
        log.info("分页查询认领申请: currentPage={}, size={}, status={}, itemType={}, itemTitle={}", 
                currentPage, size, status, itemType, itemTitle);
        Page<ClaimApplication> page = claimApplicationService.page(currentPage, size, status, itemType, itemTitle);
        return Result.success(page);
    }
    
    /**
     * 分页查询我的申请
     */
    @Operation(summary = "分页查询我的申请")
    @GetMapping("/my")
    public Result<Page<ClaimApplication>> pageMyApplications(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status
    ) {
        log.info("分页查询我的申请: currentPage={}, size={}, status={}", currentPage, size, status);
        Page<ClaimApplication> page = claimApplicationService.pageMyApplications(currentPage, size, status);
        return Result.success(page);
    }
    
    /**
     * 分页查询待我审核的申请
     */
    @Operation(summary = "分页查询待我审核的申请")
    @GetMapping("/audit")
    public Result<Page<ClaimApplication>> pageMyAudit(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status
    ) {
        log.info("分页查询待我审核的申请: currentPage={}, size={}, status={}", currentPage, size, status);
        Page<ClaimApplication> page = claimApplicationService.pageMyAudit(currentPage, size, status);
        return Result.success(page);
    }
    
    /**
     * 审核认领申请
     */
    @Operation(summary = "审核认领申请")
    @PutMapping("/audit")
    public Result<Void> audit(@RequestBody ClaimAuditRequest auditRequest) {
        log.info("审核认领申请: id={}, status={}, auditRemark={}", 
                auditRequest.getId(), auditRequest.getStatus(), auditRequest.getAuditRemark());
        claimApplicationService.audit(auditRequest.getId(), auditRequest.getStatus(), auditRequest.getAuditRemark());
        return Result.success();
    }
    
    /**
     * 取消认领申请
     */
    @Operation(summary = "取消认领申请")
    @PutMapping("/cancel/{id}")
    public Result<Void> cancelClaim(@PathVariable Long id) {
        log.info("取消认领申请: id={}", id);
        claimApplicationService.cancelClaim(id);
        return Result.success();
    }
    
    /**
     * 审核请求DTO
     */
    public static class ClaimAuditRequest {
        private Long id;
        private Integer status;
        private String auditRemark;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public Integer getStatus() {
            return status;
        }
        
        public void setStatus(Integer status) {
            this.status = status;
        }
        
        public String getAuditRemark() {
            return auditRemark;
        }
        
        public void setAuditRemark(String auditRemark) {
            this.auditRemark = auditRemark;
        }
    }
} 