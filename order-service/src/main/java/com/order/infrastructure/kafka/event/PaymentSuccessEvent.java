package com.order.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Event được consume từ Payment Service khi Payment thành công
 *
 * Event này được PUBLISH bởi Payment Service và CONSUME bởi Order Service.
 * Đây là response trong Order-Payment Saga pattern.
 *
 * Flow:
 * 1. Order Service publishes OrderCreatedEvent
 * 2. Payment Service consumes OrderCreatedEvent và process payment
 * 3. Payment Service publishes PaymentSuccessEvent (event này)
 * 4. Order Service consumes PaymentSuccessEvent và update order status
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent extends OrderEvent {

    /**
     * Event type constant
     */
    public static final String EVENT_TYPE = "PAYMENT_SUCCESS";

    /**
     * Constructor đầy đủ
     */
    public PaymentSuccessEvent(
            String eventId,
            String aggregateId,
            java.time.Instant timestamp,
            String userId,
            String traceId,
            String paymentId,
            String transactionId,
            String orderId,
            BigDecimal paidAmount,
            String currency,
            String paymentMethod,
            String provider
    ) {
        super();
        setEventId(eventId);
        setEventType(EVENT_TYPE);
        setAggregateId(aggregateId);
        setTimestamp(timestamp);
        setUserId(userId);
        setTraceId(traceId);
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.paidAmount = paidAmount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.provider = provider;
    }

    /**
     * ID của payment (từ Payment Service)
     */
    private String paymentId;

    /**
     * Transaction ID từ payment provider
     */
    private String transactionId;

    /**
     * ID của order (để map back)
     */
    private String orderId;

    /**
     * Số tiền đã thanh toán
     */
    private BigDecimal paidAmount;

    /**
     * Currency code
     */
    private String currency;

    /**
     * Payment method đã sử dụng
     */
    private String paymentMethod;

    /**
     * Payment provider (paypal, stripe, etc.)
     */
    private String provider;

    /**
     * Timestamp khi payment được xử lý thành công
     */
    private java.time.Instant paymentTimestamp;
}
