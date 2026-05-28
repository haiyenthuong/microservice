package com.payment.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình VNPAY Payment Gateway
 *
 * @author Payment Service
 * @version 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "vnpay")
public class VnpayConfig {

    /**
     * Mã website đăng ký với VNPAY
     */
    private String tmnCode;

    /**
     * Mã checksum (hash secret key)
     */
    private String hashSecret;

    /**
     * URL API thanh toán VNPAY
     * Production: https://pay.vnpay.vn/vpcpay.html
     * Sandbox: https://sandbox.vnpay.vn/vpcpay.html
     */
    private String paymentUrl = "https://sandbox.vnpay.vn/vpcpay.html";

    /**
     * URL API query transaction VNPAY
     */
    private String queryUrl = "https://sandbox.vnpay.vn/transaction.html";

    /**
     * Return URL sau khi thanh toán xong
     * Gateway sẽ redirect về URL này
     */
    private String returnUrl;

    /**
     * URL nhận IPN (Instant Payment Notification)
     * VNPAY sẽ gọi URL này để notify về trạng thái thanh toán
     */
    private String ipnUrl;

    /**
     * Thời gian timeout cho giao dịch (phút)
     * Default: 15 phút
     */
    private Integer timeoutMinutes = 15;

    /**
     * Ngôn ngữ: vn hoặc en
     */
    private String locale = "vn";

    /**
     * Currency code: VND
     */
    private String currency = "VND";

    /**
     * Version của API VNPAY
     */
    private String version = "2.1.0";

    /**
     * Command tạo URL thanh toán
     */
    private String command = "pay";

    /**
     * Bật/tắt chế độ sandbox
     */
    private Boolean sandbox = true;
}
