package com.payment.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho Payment responses.
 * DTO dùng cho phản hồi thông tin thanh toán.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String id;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private Integer paymentStatus;
    private String paymentStatusName;
    private String paymentStatusDescription;
    private String paymentMethod;
    private String transactionId;
    private LocalDateTime paymentDate;
    private String failureReason;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
