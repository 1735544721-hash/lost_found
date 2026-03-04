package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知消息实体类
 */
@Data
@TableName("notification")
@Schema(description = "通知消息实体类")
public class Notification {
    @TableId(type = IdType.AUTO)
    @Schema(description = "通知ID")
    private Long id;
    
    @Schema(description = "接收用户ID")
    private Long userId;
    
    @Schema(description = "标题")
    private String title;
    
    @Schema(description = "内容")
    private String content;
    
    @Schema(description = "类型(0系统消息,1申请消息,2审核消息)")
    private Integer type;
    
    @Schema(description = "关联ID")
    private Long relatedId;
    
    @Schema(description = "是否已读(0未读,1已读)")
    private Integer isRead;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    // 额外字段，用于前端展示
    @TableField(exist = false)
    @Schema(description = "接收用户名")
    private String username;
    
    @TableField(exist = false)
    @Schema(description = "类型描述")
    private String typeDescription;
    
    /**
     * 通知类型枚举
     */
    public enum NotificationType {
        SYSTEM(0, "系统消息"),
        APPLICATION(1, "申请消息"),
        AUDIT(2, "审核消息");
        
        private final Integer value;
        private final String description;
        
        NotificationType(Integer value, String description) {
            this.value = value;
            this.description = description;
        }
        
        public Integer getValue() {
            return value;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static NotificationType fromValue(Integer value) {
            for (NotificationType type : NotificationType.values()) {
                if (type.getValue().equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("未知的通知类型值: " + value);
        }
    }
}
