package com.order.domain.model;

/**
 * Enum representing the status of an order.
 * Enum đại diện cho trạng thái của đơn hàng.
 */
public enum OrderStatus {
    PENDING(0, "Pending", "Chờ xử lý"),
    CONFIRMED(1, "Confirmed", "Chờ xác nhận"),
    PROCESSING(2, "Processing", "Đang xử lý"),
    SHIPPED(3, "Shipped", "Đang giao"),
    DELIVERED(4, "Delivered", "Đã giao"),
    CANCELLED(5, "Cancelled", "Đã hủy"),
    REFUNDED(6, "Refunded", "Đã hoàn tiền");

    private final Integer value;
    private final String name;
    private final String description;

    OrderStatus(Integer value, String name, String description) {
        this.value = value;
        this.name = name;
        this.description = description;
    }

    public Integer getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Chuyển đổi integer value sang OrderStatus enum.
     *
     * @param value giá trị Integer
     * @return OrderStatus enum
     * @throws IllegalArgumentException nếu giá trị không hợp lệ
     */
    public static OrderStatus fromValue(Integer value) {
        if (value == null) {
            return PENDING;
        }
        for (OrderStatus status : OrderStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus value: " + value);
    }

    /**
     * Kiểm tra đơn hàng có đang ở trạng thái chờ xử lý không.
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * Kiểm tra đơn hàng đã được xác nhận chưa.
     */
    public boolean isConfirmed() {
        return this == CONFIRMED;
    }

    /**
     * Kiểm tra đơn hàng đã được giao chưa.
     */
    public boolean isShipped() {
        return this == SHIPPED;
    }

    /**
     * Kiểm tra đơn hàng đã giao thành công chưa.
     */
    public boolean isDelivered() {
        return this == DELIVERED;
    }

    /**
     * Kiểm tra đơn hàng đã bị hủy chưa.
     */
    public boolean isCancelled() {
        return this == CANCELLED;
    }

    /**
     * Kiểm tra đơn hàng có thể hủy được không.
     * Đơn hàng chỉ có thể hủy khi ở trạng thái PENDING hoặc CONFIRMED.
     */
    public boolean canCancel() {
        return this == PENDING || this == CONFIRMED;
    }

    /**
     * Kiểm tra đơn hàng có ở trạng thái cuối cùng không (không thể sửa đổi).
     */
    public boolean isFinalState() {
        return this == DELIVERED || this == CANCELLED || this == REFUNDED;
    }
}
