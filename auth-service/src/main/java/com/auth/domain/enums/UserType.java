package com.auth.domain.enums;

import lombok.Getter;

/**
 * Enum cho loại người dùng
 *
 * Các loại:
 * - ADMIN (0): Administrator - Quản trị viên toàn hệ thống
 * - CUSTOMER (1): Customer - Khách hàng
 *
 * @author Auth Service
 * @version 1.0.0
 */
@Getter
public enum UserType {

    /**
     * Administrator
     */
    ADMIN(0, "Admin", "Quản trị viên"),

    /**
     * Customer
     */
    CUSTOMER(1, "Customer", "Khách hàng");

    private final Integer value;
    private final String name;
    private final String description;

    UserType(Integer value, String name, String description) {
        this.value = value;
        this.name = name;
        this.description = description;
    }

    /**
     * Chuyển đổi từ số sang enum
     *
     * @param value giá trị số
     * @return UserType tương ứng, mặc định là CUSTOMER
     */
    public static UserType fromValue(Integer value) {
        if (value == null) {
            return CUSTOMER;
        }
        for (UserType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return CUSTOMER;
    }

    /**
     * Kiểm tra có phải admin không
     *
     * @return true nếu là admin
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Kiểm tra có phải customer không
     *
     * @return true nếu là customer
     */
    public boolean isCustomer() {
        return this == CUSTOMER;
    }
}
