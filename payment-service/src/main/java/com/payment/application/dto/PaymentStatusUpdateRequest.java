package com.payment.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để cập nhật trạng thái thanh toán.
 * DTO dùng cho callback từ payment-service sang order-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusUpdateRequest {

    @NotBlank(message = "Payment status is required")
    @Size(max = 50, message = "Payment status must not exceed 50 characters")
    private String paymentStatus; // PAID, FAILED

    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    private String transactionId;

    @Size(max = 500, message = "Failure reason must not exceed 500 characters")
    private String failureReason;
}
