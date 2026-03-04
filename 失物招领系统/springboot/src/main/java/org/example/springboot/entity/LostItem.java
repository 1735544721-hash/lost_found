package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 失物信息实体类
 */
@Data
@TableName("lost_item")
@Schema(description = "失物信息实体类")
public class LostItem {
    @TableId(type = IdType.AUTO)
    @Schema(description = "失物ID")
    private Long id;
    
    @Schema(description = "标题")
    private String title;
    
    @Schema(description = "描述")
    private String description;
    
    @Schema(description = "分类ID")
    private Long categoryId;
    
    @Schema(description = "丢失地点")
    private String lostPlace;
    
    @Schema(description = "丢失时间")
    private LocalDateTime lostTime;
    
    @Schema(description = "联系人姓名")
    private String contactName;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "图片(多张用逗号分隔)")
    private String images;
    
    @Schema(description = "发布用户ID")
    private Long userId;
    
    @Schema(description = "状态(0待认领,1已认领,2已关闭)")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    // 额外字段，用于前端展示
    @TableField(exist = false)
    @Schema(description = "分类名称")
    private String categoryName;
    
    @TableField(exist = false)
    @Schema(description = "用户名")
    private String username;
} 