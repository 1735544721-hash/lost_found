package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 认领申请实体类
 */
@Data
@TableName("claim_application")
@Schema(description = "认领申请实体类")
public class ClaimApplication {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "申请ID")
    private Long id;
    
    @Schema(description = "物品ID")
    private Long itemId;
    
    @Schema(description = "物品类型(0招领信息,1失物信息)")
    private Integer itemType;
    
    @Schema(description = "申请人ID")
    private Long userId;
    
    @Schema(description = "申请说明")
    private String description;
    
    @Schema(description = "状态(0待审核,1已通过,2已拒绝)")
    private Integer status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @Schema(description = "审核人ID")
    private Long auditUserId;
    
    @Schema(description = "审核时间")
    private LocalDateTime auditTime;
    
    @Schema(description = "审核备注")
    private String auditRemark;
    
    // 非数据库字段
    @TableField(exist = false)
    @Schema(description = "物品标题")
    private String itemTitle;
    
    @TableField(exist = false)
    @Schema(description = "申请人用户名")
    private String username;
    
    @TableField(exist = false)
    @Schema(description = "申请人姓名")
    private String name;
    
    @TableField(exist = false)
    @Schema(description = "审核人用户名")
    private String auditUsername;
    
    @TableField(exist = false)
    @Schema(description = "审核人姓名")
    private String auditName;
} 