package com.order.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Event được consume từ Payment Service khi Payment thất bại
 *
 * Event này được PUBLISH bởi Payment Service và CONSUME bởi Order Service.
 * Đây là failure response trong Order-Payment Saga pattern.
 *
 * Flow:
 * 1. Order Service publishes OrderCreatedEvent
 * 2. Payment Service consumes OrderCreatedEvent và process payment
 * 3. Payment failed → Payment Service publishes PaymentFailedEvent (event này)
 * 4. Order Service consumes PaymentFailedEvent và update order status
 * 5. Order Service có thể publish OrderPaymentFailedEvent để notify other
 * services
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent extends OrderEvent {

    /**
     * Event type constant
     */
    public static final String EVENT_TYPE = "PAYMENT_FAILED";

    /**
     * Constructor đầy đủ
     */
    public PaymentFailedEvent(
            String eventId,
            String aggregateId,
            java.time.Instant timestamp,
            String userId,
            String traceId,
            String orderId,
            String failureReason,
            String errorCode,
            boolean retryable) {
        super();
        setEventId(eventId);
        setEventType(EVENT_TYPE);
        setAggregateId(aggregateId);
        setTimestamp(timestamp);
        setUserId(userId);
        setTraceId(traceId);
        this.orderId = orderId;
        this.failureReason = failureReason;
        this.errorCode = errorCode;
        this.retryable = retryable;
    }

    /**
     * ID của order (để map back)
     */
    private String orderId;

    /**
     * Lý do payment failed
     */
    private String failureReason;

    /**
     * Error code từ payment provider
     */
    private String errorCode;

    /**
     * Có thể retry payment hay không
     */
    private boolean retryable;

    /**
     * Số lần retry đã thực hiện
     */
    private int retryCount;

    /**
     * Timestamp khi payment failed
     */
    private java.time.Instant failedTimestamp;
}
