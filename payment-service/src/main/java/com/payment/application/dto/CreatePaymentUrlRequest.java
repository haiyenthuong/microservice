package com.payment.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO để tạo URL thanh toán VNPAY.
 *
 * @author Payment Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentUrlRequest {

    /**
     * ID đơn hàng (bắt buộc)
     */
    @NotNull(message = "orderId is required")
    private String orderId;

    /**
     * ID người dùng (tùy chọn)
     */
    private String userId;

    /**
     * Số tiền thanh toán (bắt buộc)
     */
    @NotNull(message = "amount is required")
    private BigDecimal amount;

    /**
     * Mô tả đơn hàng (tùy chọn)
     */
    private String description;

    /**
     * IP address của khách hàng (bắt buộc cho VNPAY)
     */
    @NotNull(message = "ipAddress is required")
    private String ipAddress;

    /**
     * Ngôn ngữ: vn, en (tùy chọn, mặc định theo config)
     */
    private String locale;

    /**
     * Thông tin ngân hàng (tùy chọn)
     */
    private String bankCode;
}
