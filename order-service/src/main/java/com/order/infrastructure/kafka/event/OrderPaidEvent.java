package com.order.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Event được publish khi Payment đã thành công cho Order
 *
 * Event này được publish bởi Order Service sau khi nhận PaymentSuccessEvent
 * từ Payment Service. Nó đánh dấu completion của Order-Payment Saga.
 *
 * Flow:
 * 1. Order Service → OrderCreatedEvent → Payment Service
 * 2. Payment Service process payment
 * 3. Payment Service → PaymentSuccessEvent → Order Service
 * 4. Order Service → OrderPaidEvent → Other services (Inventory, Notification, etc.)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidEvent extends OrderEvent {

    /**
     * Event type constant
     */
    public static final String EVENT_TYPE = "ORDER_PAID";

    /**
     * ID của Order
     */
    private String orderId;

    /**
     * Order Number
     */
    private String orderNumber;

    /**
     * ID của payment (từ Payment Service)
     */
    private String paymentId;

    /**
     * Transaction ID từ payment provider
     */
    private String transactionId;

    /**
     * Số tiền đã thanh toán
     */
    private BigDecimal amount;

    /**
     * Currency code
     */
    private String currency;

    /**
     * Payment method đã sử dụng
     */
    private String paymentMethod;

    /**
     * Timestamp khi payment được xử lý
     */
    private java.time.Instant paymentTimestamp;

    /**
     * Constructor đầy đủ
     */
    public OrderPaidEvent(
            String eventId,
            String aggregateId,
            java.time.Instant timestamp,
            String userId,
            String traceId,
            String orderId,
            String orderNumber,
            String paymentId,
            String transactionId,
            BigDecimal amount,
            String currency,
            String paymentMethod
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
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
    }
}
