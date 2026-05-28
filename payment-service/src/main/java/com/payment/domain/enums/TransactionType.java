package com.payment.domain.enums;

/**
 * Enum representing the type of payment transaction.
 * Enum đại diện cho loại giao dịch thanh toán.
 */
public enum TransactionType {

    PAYMENT("PAYMENT", "Thanh toán", "Payment transaction"),
    REFUND("REFUND", "Hoàn tiền", "Refund transaction");

    private final String code;
    private final String name;
    private final String description;

    TransactionType(String code, String name, String description) {
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
     * Chuyển đổi code sang TransactionType enum.
     *
     * @param code mã giao dịch
     * @return TransactionType enum
     * @throws IllegalArgumentException nếu mã không hợp lệ
     */
    public static TransactionType fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return PAYMENT;
        }
        for (TransactionType type : TransactionType.values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown TransactionType code: " + code);
    }

    /**
     * Kiểm tra có phải giao dịch thanh toán không.
     */
    public boolean isPayment() {
        return this == PAYMENT;
    }

    /**
     * Kiểm tra có phải giao dịch hoàn tiền không.
     */
    public boolean isRefund() {
        return this == REFUND;
    }
}
