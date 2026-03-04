package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物品分类实体类
 */
@Data
@TableName("item_category")
@Schema(description = "物品分类实体类")
public class ItemCategory {
    @TableId(type = IdType.AUTO)
    @Schema(description = "分类ID")
    private Long id;
    
    @Schema(description = "分类名称")
    private String name;
    
    @Schema(description = "排序号")
    private Integer sort;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 