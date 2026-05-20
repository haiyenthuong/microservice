package com.cms.domain.model;

public enum UserType {
    ADMIN(0),
    CUSTOMER(1);

    private final int value;

    UserType(int value) {
        this.value = value;
    }

    /**
     * Lấy giá trị số của loại người dùng.
     *
     * @return giá trị số
     */
    public int getValue() {
        return value;
    }

    /**
     * Chuyển đổi giá trị số sang enum UserType.
     *
     * @param value giá trị số cần chuyển đổi
     * @return UserType enum tương ứng
     * @throws IllegalArgumentException nếu giá trị không hợp lệ
     */
    public static UserType fromValue(int value) {
        for (UserType type : UserType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid UserType value: " + value);
    }

    /**
     * Kiểm tra loại người dùng có phải là admin không.
     *
     * @return true nếu là ADMIN
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Kiểm tra loại người dùng có phải là khách hàng không.
     *
     * @return true nếu là CUSTOMER
     */
    public boolean isCustomer() {
        return this == CUSTOMER;
    }
}
