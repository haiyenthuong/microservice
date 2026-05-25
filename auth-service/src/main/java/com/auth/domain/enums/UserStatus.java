package com.auth.domain.enums;

import lombok.Getter;

/**
 * Enum cho trạng thái người dùng
 *
 * Các trạng thái:
 * - ACTIVE (1): User đang hoạt động bình thường
 * - LOCKED (2): User bị khóa tạm thời
 * - DELETED (3): User đã bị xóa
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Getter
public enum UserStatus {

    /**
     * User đang hoạt động
     */
    ACTIVE(1, "Active", "User đang hoạt động"),

    /**
     * User bị khóa
     */
    LOCKED(2, "Locked", "User bị khóa"),

    /**
     * User đã bị xóa
     */
    DELETED(3, "Deleted", "User đã bị xóa");

    private final Integer value;
    private final String name;
    private final String description;

    UserStatus(Integer value, String name, String description) {
        this.value = value;
        this.name = name;
        this.description = description;
    }

    /**
     * Chuyển đổi từ số sang enum
     *
     * @param value giá trị số
     * @return UserStatus tương ứng, mặc định là ACTIVE
     */
    public static UserStatus fromValue(Integer value) {
        if (value == null) {
            return ACTIVE;
        }
        for (UserStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return ACTIVE;
    }

    /**
     * Kiểm tra user đang hoạt động
     *
     * @return true nếu đang hoạt động
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * Kiểm tra user bị khóa
     *
     * @return true nếu bị khóa
     */
    public boolean isLocked() {
        return this == LOCKED;
    }

    /**
     * Kiểm tra user bị xóa
     *
     * @return true nếu bị xóa
     */
    public boolean isDeleted() {
        return this == DELETED;
    }
}
