package com.payment.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO cho kết quả tạo URL thanh toán VNPAY.
 *
 * @author Payment Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentUrlResponse {

    /**
     * ID giao dịch thanh toán
     */
    private String transactionId;

    /**
     * Mã đơn hàng (order code)
     */
    private String orderCode;

    /**
     * URL thanh toán VNPAY
     */
    private String paymentUrl;

    /**
     * Thời gian hết hạn thanh toán
     */
    private LocalDateTime expiredAt;

    /**
     * Số tiền thanh toán
     */
    private BigDecimal amount;

    /**
     * Số tiền theo định dạng VNPAY (amount * 100)
     */
    private BigDecimal vnpAmount;
}
