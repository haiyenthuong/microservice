package com.payment.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO cho trạng thái giao dịch thanh toán.
 *
 * @author Payment Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatusResponse {

    /**
     * Mã đơn hàng
     */
    private String orderCode;

    /**
     * ID đơn hàng
     */
    private String orderId;

    /**
     * Trạng thái giao dịch
     * - PENDING: Chờ thanh toán
     * - PAID: Đã thanh toán thành công
     * - FAILED: Thanh toán thất bại
     * - EXPIRED: Đã hết hạn
     * - REFUNDED: Đã hoàn tiền
     */
    private String status;

    /**
     * Mô tả trạng thái (tiếng Việt)
     */
    private String statusDescription;

    /**
     * Số tiền thanh toán
     */
    private BigDecimal amount;

    /**
     * Số tiền theo định dạng VNPAY
     */
    private BigDecimal vnpAmount;

    /**
     * Mã ngân hàng
     */
    private String bankCode;

    /**
     * Mã giao dịch VNPAY
     */
    private String vnpTransactionNo;

    /**
     * Thời gian thanh toán thành công
     */
    private LocalDateTime paidAt;

    /**
     * Thời gian hết hạn thanh toán
     */
    private LocalDateTime expiredAt;

    /**
     * Thời gian tạo giao dịch
     */
    private LocalDateTime createdAt;

    /**
     * Lý do thất bại (nếu có)
     */
    private String failureReason;
}
