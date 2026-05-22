package com.payment.domain.enums;

/**
 * Enum representing the payment method used for a transaction.
 * Enum đại diện cho phương thức thanh toán được sử dụng.
 */
public enum PaymentMethod {

    CREDIT_CARD("CREDIT_CARD", "Credit Card", "Thẻ tín dụng"),
    DEBIT_CARD("DEBIT_CARD", "Debit Card", "Thẻ ghi nợ"),
    PAYPAL("PAYPAL", "PayPal", "PayPal"),
    BANK_TRANSFER("BANK_TRANSFER", "Bank Transfer", "Chuyển khoản ngân hàng"),
    CASH_ON_DELIVERY("CASH_ON_DELIVERY", "Cash on Delivery", "Thanh toán khi nhận hàng"),
    MOBILE_PAYMENT("MOBILE_PAYMENT", "Mobile Payment", "Thanh toán di động"),
    CRYPTO("CRYPTO", "Cryptocurrency", "Tiền mã hóa"),
    VNPAY("VNPAY", "VNPAY", "Ví VNPay"),
    MOMO("MOMO", "MoMo", "Ví MoMo"),
    ZALOPAY("ZALOPAY", "ZaloPay", "Ví ZaloPay"),
    APPLE_PAY("APPLE_PAY", "Apple Pay", "Apple Pay"),
    GOOGLE_PAY("GOOGLE_PAY", "Google Pay", "Google Pay");

    private final String code;
    private final String name;
    private final String description;

    PaymentMethod(String code, String name, String description) {
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
     * Lấy PaymentMethod từ code (String).
     *
     * @param code mã phương thức thanh toán
     * @return PaymentMethod enum
     * @throws IllegalArgumentException nếu code không hợp lệ
     */
    public static PaymentMethod fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method code cannot be null or empty");
        }
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.code.equalsIgnoreCase(code)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown PaymentMethod code: " + code);
    }

    /**
     * Kiểm tra phương thức thanh toán có phải là thẻ không.
     */
    public boolean isCard() {
        return this == CREDIT_CARD || this == DEBIT_CARD;
    }

    /**
     * Kiểm tra phương thức thanh toán có phải là ví điện tử không.
     */
    public boolean isEWallet() {
        return this == VNPAY || this == MOMO || this == ZALOPAY;
    }

    /**
     * Kiểm tra phương thức thanh toán có phải là online payment không.
     */
    public boolean isOnline() {
        return this == CREDIT_CARD || this == DEBIT_CARD ||
               this == PAYPAL || this == VNPAY || this == MOMO ||
               this == ZALOPAY || this == APPLE_PAY || this == GOOGLE_PAY;
    }

    /**
     * Kiểm tra phương thức thanh toán có phải là COD không.
     */
    public boolean isCOD() {
        return this == CASH_ON_DELIVERY;
    }
}
