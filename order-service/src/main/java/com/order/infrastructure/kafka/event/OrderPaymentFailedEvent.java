package com.order.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Event được publish khi Payment thất bại cho Order
 *
 * Event này được publish bởi Order Service sau khi nhận PaymentFailedEvent
 * từ Payment Service. Nó đánh dấu failure trong Order-Payment Saga.
 *
 * Flow:
 * 1. Order Service → OrderCreatedEvent → Payment Service
 * 2. Payment Service process payment nhưng failed
 * 3. Payment Service → PaymentFailedEvent → Order Service
 * 4. Order Service → OrderPaymentFailedEvent → Other services (Notification, etc.)
 *
 * Saga Compensation:
 * - Order status được cập nhật thành PAYMENT_FAILED
 * - Có thể trigger retry hoặc manual intervention
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentFailedEvent extends OrderEvent {

    /**
     * Event type constant
     */
    public static final String EVENT_TYPE = "ORDER_PAYMENT_FAILED";

    /**
     * ID của Order
     */
    private String orderId;

    /**
     * Order Number
     */
    private String orderNumber;

    /**
     * Số tiền thanh toán thất bại
     */
    private BigDecimal amount;

    /**
     * Currency code
     */
    private String currency;

    /**
     * Lý do payment failed
     */
    private String reason;

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
     * Max retry attempts allowed
     */
    private int maxRetryAttempts;

    /**
     * Constructor đầy đủ với order info
     */
    public OrderPaymentFailedEvent(
            String eventId,
            String aggregateId,
            java.time.Instant timestamp,
            String userId,
            String traceId,
            String orderId,
            String orderNumber,
            BigDecimal amount,
            String currency,
            String reason,
            String errorCode,
            boolean retryable
    ) {
        super();
        setEventId(eventId);
        setEventType(EVENT_TYPE);
        setAggregateId(aggregateId);
        setTimestamp(timestamp);
        setUserId(userId);
        setTraceId(traceId);
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.amount = amount;
        this.currency = currency;
        this.reason = reason;
        this.errorCode = errorCode;
        this.retryable = retryable;
    }
}
