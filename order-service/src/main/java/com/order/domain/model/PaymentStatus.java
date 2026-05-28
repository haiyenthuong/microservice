package com.order.domain.model;

/**
 * Enum representing the payment status of an order.
 * Enum đại diện cho trạng thái thanh toán của đơn hàng.
 */
public enum PaymentStatus {
    PENDING(0L, "Pending", "Chờ thanh toán"),
    PROCESSING(1L, "Processing", "Đang xử lý"),
    PAID(2L, "Paid", "Đã thanh toán"),
    FAILED(3L, "Failed", "Thanh toán thất bại"),
    REFUNDED(4L, "Refunded", "Đã hoàn tiền");

    private final Long value;
    private final String name;
    private final String description;

    PaymentStatus(Long value, String name, String description) {
        this.value = value;
        this.name = name;
        this.description = description;
    }

    public Long getValue() {
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
     *
     * @param value giá trị Integer
     * @return PaymentStatus enum
     * @throws IllegalArgumentException nếu giá trị không hợp lệ
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
     * Lấy PaymentStatus từ tên (String).
     *
     * @param name tên của payment status
     * @return PaymentStatus enum
     * @throws IllegalArgumentException nếu tên không hợp lệ
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

    /**
     * Lấy PaymentStatus từ tên (String).
     *
     * @param status tên của payment status
     * @return PaymentStatus enum
     * @throws IllegalArgumentException nếu tên không hợp lệ
     */
    public static PaymentStatus fromValue(Long status) {
        if (status == null) {
            return PENDING;
        }
        for (PaymentStatus paymentStatus : PaymentStatus.values()) {
            if (paymentStatus.value.equals(status)) {
                return paymentStatus;
            }
        }
        throw new IllegalArgumentException("Unknown PaymentStatus name: " + status);
    }

    /**
     * Kiểm tra thanh toán có thành công không.
     */
    public boolean isPaid() {
        return this == PAID;
    }

    /**
     * Kiểm tra thanh toán có đang chờ xử lý không.
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * Kiểm tra thanh toán có đang được xử lý không.
     */
    public boolean isProcessing() {
        return this == PROCESSING;
    }

    /**
     * Kiểm tra thanh toán có thất bại không.
     */
    public boolean isFailed() {
        return this == FAILED;
    }

    /**
     * Kiểm tra có thể thực hiện thanh toán lại không.
     * Chỉ có thể retry khi PENDING hoặc FAILED.
     */
    public boolean canRetry() {
        return this == PENDING || this == FAILED;
    }

    /**
     * Kiểm tra có thể hoàn tiền không.
     * Chỉ có thể refund khi PAID.
     */
    public boolean canRefund() {
        return this == PAID;
    }
}
