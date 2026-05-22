package com.payment.domain.enums;

/**
 * Enum representing the status of a payment transaction.
 * Enum đại diện cho trạng thái của giao dịch thanh toán.
 */
public enum PaymentStatus {

    PROCESSING(0, "Processing", "Đang xử lý"),
    PAID(1, "Paid", "Đã thanh toán"),
    FAILED(2, "Failed", "Thất bại"),
    REFUNDED(3, "Refunded", "Đã hoàn tiền"),
    CANCELLED(4, "Cancelled", "Đã hủy");

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
     *
     * @param value giá trị Integer
     * @return PaymentStatus enum
     * @throws IllegalArgumentException nếu giá trị không hợp lệ
     */
    public static PaymentStatus fromValue(Integer value) {
        if (value == null) {
            return PROCESSING;
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
            return PROCESSING;
        }
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown PaymentStatus name: " + name);
    }

    /**
     * Kiểm tra thanh toán có thành công không.
     */
    public boolean isPaid() {
        return this == PAID;
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
     * Kiểm tra có thể thực hiện lại thanh toán không.
     * Chỉ có thể retry khi FAILED hoặc CANCELLED.
     */
    public boolean canRetry() {
        return this == FAILED || this == CANCELLED;
    }

    /**
     * Kiểm tra có thể hoàn tiền không.
     * Chỉ có thể refund khi PAID.
     */
    public boolean canRefund() {
        return this == PAID;
    }
}
