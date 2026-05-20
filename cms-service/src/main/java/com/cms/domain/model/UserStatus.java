package com.cms.domain.model;

public enum UserStatus {
    ACTIVE(1),
    LOCKED(0),
    DELETED(2);

    private final int value;

    UserStatus(int value) {
        this.value = value;
    }

    /**
     * Lấy giá trị số của trạng thái.
     *
     * @return giá trị số
     */
    public int getValue() {
        return value;
    }

    /**
     * Chuyển đổi giá trị số sang enum UserStatus.
     *
     * @param value giá trị số cần chuyển đổi
     * @return UserStatus enum tương ứng
     * @throws IllegalArgumentException nếu giá trị không hợp lệ
     */
    public static UserStatus fromValue(int value) {
        for (UserStatus status : UserStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid UserStatus value: " + value);
    }

    /**
     * Kiểm tra trạng thái có đang hoạt động không.
     *
     * @return true nếu trạng thái là ACTIVE
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * Kiểm tra trạng thái có bị khóa không.
     *
     * @return true nếu trạng thái là LOCKED
     */
    public boolean isLocked() {
        return this == LOCKED;
    }

    /**
     * Kiểm tra trạng thái đã bị xóa chưa.
     *
     * @return true nếu trạng thái là DELETED
     */
    public boolean isDeleted() {
        return this == DELETED;
    }
}
