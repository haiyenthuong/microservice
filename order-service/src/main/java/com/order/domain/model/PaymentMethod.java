package com.order.domain.model;

/**
 * Enum representing the payment method.
 * Enum đại diện cho phương thức thanh toán.
 */
public enum PaymentMethod {
    CREDIT_CARD("Credit Card", "Thẻ tín dụng"),
    BANK_TRANSFER("Bank Transfer", "Chuyển khoản"),
    CASH("Cash", "Tiền mặt"),
    PAYPAL("PayPal", "Ví PayPal"),
    MOMO("MoMo", "Ví MoMo"),
    VNPAY("VNPay", "VNPay");

    private final String name;
    private final String description;

    PaymentMethod(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Lấy PaymentMethod từ tên.
     *
     * @param name tên của payment method
     * @return PaymentMethod enum
     * @throws IllegalArgumentException nếu tên không hợp lệ
     */
    public static PaymentMethod fromName(String name) {
        if (name == null || name.isEmpty()) {
            return CASH;
        }
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.name().equalsIgnoreCase(name)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown PaymentMethod name: " + name);
    }

    /**
     * Kiểm tra có phải là thanh toán trực tuyến không.
     */
    public boolean isOnline() {
        return this == CREDIT_CARD || this == PAYPAL || this == MOMO || this == VNPAY;
    }

    /**
     * Kiểm tra có phải là thanh toán tiền mặt không.
     */
    public boolean isCash() {
        return this == CASH;
    }

    /**
     * Kiểm tra có phải là chuyển khoản ngân hàng không.
     */
    public boolean isBankTransfer() {
        return this == BANK_TRANSFER;
    }
}
