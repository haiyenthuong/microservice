package com.payment.domain.model;

/**
 * Enum representing the payment status.
 * Enum đại diện cho trạng thái thanh toán.
 */
public enum PaymentStatus {
    PENDING(0, "Pending", "Chờ thanh toán"),
    PROCESSING(1, "Processing", "Đang xử lý"),
    PAID(2, "Paid", "Đã thanh toán"),
    FAILED(3, "Failed", "Thanh toán thất bại"),
    REFUNDED(4, "Refunded", "Đã hoàn tiền");

    private final Integer value;
    private final String name;
    private final String description;

    PaymentStatus(Integer value, String name, String description) {
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
     * Chuyển đổi integer value sang PaymentStatus enum.
     */
    public static PaymentStatus fromValue(Integer value) {
        if (value == null) {
            return PENDING;
        }
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown PaymentStatus value: " + value);
    }

    /**
     * Lấy PaymentStatus từ tên.
     */
    public static PaymentStatus fromName(String name) {
        if (name == null || name.isEmpty()) {
            return PENDING;
        }
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown PaymentStatus name: " + name);
    }
}
