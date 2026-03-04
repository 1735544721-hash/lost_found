package org.example.springboot.enumClass;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 物品状态枚举
 * 定义失物和招领物品的各种状态及其转换规则
 */
public enum ItemStatus {
    @Schema(description = "待认领") PENDING(0, "待认领", "物品已发布，等待认领"),
    @Schema(description = "已认领") CLAIMED(1, "已认领", "物品已被认领，等待交接"),
    @Schema(description = "已交接") COMPLETED(2, "已交接", "物品已成功交接完成"),
    @Schema(description = "已关闭") CLOSED(3, "已关闭", "物品信息已关闭"),
    @Schema(description = "已过期") EXPIRED(4, "已过期", "物品信息已过期");

    private final Integer value;
    private final String description;
    private final String detail;

    ItemStatus(Integer value, String description, String detail) {
        this.value = value;
        this.description = description;
        this.detail = detail;
    }

    public Integer getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public String getDetail() {
        return detail;
    }

    /**
     * 根据值获取枚举
     */
    public static ItemStatus fromValue(Integer value) {
        for (ItemStatus status : ItemStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的物品状态值: " + value);
    }

    /**
     * 检查状态转换是否合法
     */
    public boolean canTransitionTo(ItemStatus targetStatus) {
        switch (this) {
            case PENDING:
                return targetStatus == CLAIMED || targetStatus == CLOSED || targetStatus == EXPIRED;
            case CLAIMED:
                return targetStatus == COMPLETED || targetStatus == PENDING || targetStatus == CLOSED;
            case COMPLETED:
                return targetStatus == CLOSED; // 已完成的只能关闭
            case CLOSED:
                return targetStatus == PENDING; // 已关闭的可以重新开放
            case EXPIRED:
                return targetStatus == PENDING || targetStatus == CLOSED; // 过期的可以重新开放或关闭
            default:
                return false;
        }
    }

    /**
     * 获取状态转换建议
     */
    public String getTransitionAdvice(ItemStatus targetStatus) {
        if (!canTransitionTo(targetStatus)) {
            return "不允许从 " + this.description + " 转换到 " + targetStatus.description;
        }
        
        switch (targetStatus) {
            case CLAIMED:
                return "物品已被认领，请联系认领人进行交接";
            case COMPLETED:
                return "物品交接完成，感谢使用失物招领系统";
            case CLOSED:
                return "物品信息已关闭，不再接受新的认领申请";
            case PENDING:
                return "物品重新开放认领";
            case EXPIRED:
                return "物品信息已过期，建议重新发布或关闭";
            default:
                return "状态已更新";
        }
    }

    /**
     * 检查状态是否可以被认领
     */
    public boolean canBeClaimed() {
        return this == PENDING;
    }

    /**
     * 检查状态是否可以编辑
     */
    public boolean canBeEdited() {
        return this == PENDING || this == EXPIRED;
    }

    /**
     * 检查状态是否为最终状态
     */
    public boolean isFinalStatus() {
        return this == COMPLETED || this == CLOSED;
    }

    /**
     * 获取下一个可能的状态列表
     */
    public ItemStatus[] getNextPossibleStatuses() {
        switch (this) {
            case PENDING:
                return new ItemStatus[]{CLAIMED, CLOSED, EXPIRED};
            case CLAIMED:
                return new ItemStatus[]{COMPLETED, PENDING, CLOSED};
            case COMPLETED:
                return new ItemStatus[]{CLOSED};
            case CLOSED:
                return new ItemStatus[]{PENDING};
            case EXPIRED:
                return new ItemStatus[]{PENDING, CLOSED};
            default:
                return new ItemStatus[0];
        }
    }
}
