package com.payment.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity đại diện cho một thanh toán.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Column(name = "order_id", length = 36, nullable = false)
    private String orderId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @Column(name = "payment_status", nullable = false)
    private Integer paymentStatus = 0; // PaymentStatus.PENDING

    @Column(name = "payment_method", length = 50, nullable = false)
    private String paymentMethod;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    /**
     * Lấy trạng thái thanh toán dưới dạng enum.
     */
    public com.payment.domain.model.PaymentStatus getPaymentStatusEnum() {
        return com.payment.domain.model.PaymentStatus.fromValue(paymentStatus);
    }

    /**
     * Thiết lập trạng thái thanh toán từ enum.
     */
    public void setPaymentStatusEnum(com.payment.domain.model.PaymentStatus status) {
        if (status != null) {
            this.paymentStatus = status.getValue();
        }
    }

    /**
     * Đánh dấu thanh toán thành công.
     */
    public void markAsPaid(String transactionId) {
        setPaymentStatusEnum(com.payment.domain.model.PaymentStatus.PAID);
        this.transactionId = transactionId;
        this.paymentDate = LocalDateTime.now();
    }

    /**
     * Đánh dấu thanh toán thất bại.
     */
    public void markAsFailed(String reason) {
        setPaymentStatusEnum(com.payment.domain.model.PaymentStatus.FAILED);
        this.failureReason = reason;
    }
}
