package com.payment.domain.enums;

/**
 * Enum representing the payment gateway provider.
 * Enum đại diện cho nhà cung cấp cổng thanh toán.
 */
public enum PaymentProvider {

    STRIPE("STRIPE", "Stripe", "Stripe"),
    PAYPAL("PAYPAL", "PayPal", "PayPal"),
    VNPAY("VNPAY", "VNPay", "VNPay"),
    MOMO("MOMO", "MoMo", "MoMo"),
    ZALOPAY("ZALOPAY", "ZaloPay", "ZaloPay"),
    NGAN_LUONG("NGAN_LUONG", "Ngan Luong", "Ngân Lượng"),
    ONE_PAY("ONE_PAY", "OnePay", "OnePay"),
    SIMULATION("SIMULATION", "Simulation", "Mô phỏng (dùng cho test)");

    private final String code;
    private final String name;
    private final String description;

    PaymentProvider(String code, String name, String description) {
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
     * Lấy PaymentProvider từ code (String).
     *
     * @param code mã nhà cung cấp
     * @return PaymentProvider enum
     * @throws IllegalArgumentException nếu code không hợp lệ
     */
    public static PaymentProvider fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return SIMULATION;  // Default cho simulation
        }
        for (PaymentProvider provider : PaymentProvider.values()) {
            if (provider.code.equalsIgnoreCase(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown PaymentProvider code: " + code);
    }

    /**
     * Kiểm tra có phải là provider simulation không.
     */
    public boolean isSimulation() {
        return this == SIMULATION;
    }

    /**
     * Kiểm tra có phải là provider Việt Nam không.
     */
    public boolean isVietnamese() {
        return this == VNPAY || this == MOMO || this == ZALOPAY || this == NGAN_LUONG;
    }
}
