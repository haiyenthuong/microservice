package com.payment.infrastructure.kafka.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event được publish từ Payment Service sau khi xử lý payment
 *
 * Đây là response event trong Order-Payment Saga:
 * - PaymentSuccessEvent: Thanh toán thành công
 * - PaymentFailedEvent: Thanh toán thất bại
 *
 * Event này được consume bởi Order Service để update order status.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class PaymentProcessedEvent extends PaymentEvent {

    /**
     * Event type constants
     */
    public static final String EVENT_TYPE_SUCCESS = "PAYMENT_SUCCESS";
    public static final String EVENT_TYPE_FAILED = "PAYMENT_FAILED";

    /**
     * Constructor đầy đủ cho Success Event
     */
    public PaymentProcessedEvent(
            String eventId,
            String aggregateId,
            Instant timestamp,
            String userId,
            String traceId,
            String orderId,
            String paymentId,
            String transactionId,
            BigDecimal paidAmount,
            String currency,
            String paymentMethod,
            String paymentProvider,
            boolean success
    ) {
        super(eventId, success ? EVENT_TYPE_SUCCESS : EVENT_TYPE_FAILED,
              aggregateId, timestamp, userId, traceId);
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.paidAmount = paidAmount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.paymentProvider = paymentProvider;
        this.success = success;
    }

    /**
     * Order ID - Order cần được update status
     */
    public String orderId;

    /**
     * Payment ID - ID của payment record trong Payment Service
     */
    public String paymentId;

    /**
     * Transaction ID - ID giao dịch từ payment gateway
     */
    public String transactionId;

    /**
     * Số tiền đã thanh toán
     */
    public BigDecimal paidAmount;

    /**
     * Currency code
     */
    public String currency;

    /**
     * Payment method được sử dụng
     */
    public String paymentMethod;

    /**
     * Payment provider đã xử lý (STRIPE, VNPAY, etc.)
     */
    public String paymentProvider;

    /**
     * Payment success hay failed
     */
    public boolean success;

    /**
     * Lý do thất bại (chỉ có khi success = false)
     */
    public String failureReason;

    /**
     * Error code từ payment gateway (chỉ có khi success = false)
     */
    public String errorCode;

    /**
     * Có thể retry hay không (chỉ có khi success = false)
     */
    public boolean retryable;

    /**
     * Timestamp khi payment được xử lý xong
     */
    public Instant processedAt;
}
