package com.payment.domain.enums;

/**
 * Enum representing the status of a VNPAY payment transaction.
 * Enum đại diện cho trạng thái giao dịch thanh toán VNPAY.
 */
public enum PaymentTransactionStatus {

    // Payment statuses
    PENDING("PENDING", "Chờ xử lý", "Pending payment"),
    PAID("PAID", "Đã thanh toán", "Payment successful"),
    FAILED("FAILED", "Thất bại", "Payment failed"),
    EXPIRED("EXPIRED", "Hết hạn", "Payment expired"),

    // Refund statuses
    REFUND_PENDING("PENDING", "Chờ xử lý", "Refund pending"),
    REFUND_SUCCESS("SUCCESS", "Thành công", "Refund successful"),
    REFUND_FAILED("FAILED", "Thất bại", "Refund failed");

    private final String code;
    private final String name;
    private final String description;

    PaymentTransactionStatus(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Chuyển đổi code sang PaymentTransactionStatus enum.
     *
     * @param code mã trạng thái
     * @return PaymentTransactionStatus enum
     * @throws IllegalArgumentException nếu mã không hợp lệ
     */
    public static PaymentTransactionStatus fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return PENDING;
        }
        for (PaymentTransactionStatus status : PaymentTransactionStatus.values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown PaymentTransactionStatus code: " + code);
    }

    /**
     * Kiểm tra giao dịch đã thành công chưa.
     */
    public boolean isSuccessful() {
        return this == PAID || this == REFUND_SUCCESS;
    }

    /**
     * Kiểm tra giao dịch đang chờ xử lý.
     */
    public boolean isPending() {
        return this == PENDING || this == REFUND_PENDING;
    }

    /**
     * Kiểm tra giao dịch thất bại.
     */
    public boolean isFailed() {
        return this == FAILED || this == REFUND_FAILED || this == EXPIRED;
    }

    /**
     * Kiểm tra giao dịch đã hoàn thành (thành công hoặc thất bại).
     */
    public boolean isCompleted() {
        return !isPending();
    }
}
