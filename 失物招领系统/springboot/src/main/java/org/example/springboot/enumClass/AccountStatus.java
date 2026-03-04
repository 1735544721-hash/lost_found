package org.example.springboot.enumClass;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 账户状态枚举
 * 定义用户账户的各种状态及其转换规则
 */
public enum AccountStatus {
    @Schema(description = "待审核") PENDING_REVIEW(0, "待审核", "新注册用户等待管理员审核"),
    @Schema(description = "正常") ACTIVE(1, "正常", "账户正常，可以正常使用系统"),
    @Schema(description = "审核失败") REVIEW_FAILED(2, "审核失败", "审核未通过，需要修改信息重新提交"),
    @Schema(description = "已禁用") DISABLED(3, "已禁用", "账户被管理员禁用，无法登录"),
    @Schema(description = "已锁定") LOCKED(4, "已锁定", "账户因安全原因被锁定"),
    @Schema(description = "已过期") EXPIRED(5, "已过期", "账户已过期，需要续期");

    private final Integer value;
    private final String description;
    private final String detail;

    AccountStatus(Integer value, String description, String detail) {
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
    public static AccountStatus fromValue(Integer value) {
        for (AccountStatus status : AccountStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的账户状态值: " + value);
    }

    /**
     * 检查状态是否可以登录
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }

    /**
     * 检查状态是否可以操作
     */
    public boolean canOperate() {
        return this == ACTIVE;
    }

    /**
     * 获取状态转换建议
     */
    public String getTransitionAdvice() {
        switch (this) {
            case PENDING_REVIEW:
                return "请等待管理员审核，或联系管理员加快审核进度";
            case REVIEW_FAILED:
                return "请修改个人信息后重新提交审核";
            case DISABLED:
                return "账户已被禁用，请联系管理员了解详情";
            case LOCKED:
                return "账户已被锁定，请联系管理员解锁";
            case EXPIRED:
                return "账户已过期，请联系管理员续期";
            default:
                return "账户状态正常";
        }
    }
}