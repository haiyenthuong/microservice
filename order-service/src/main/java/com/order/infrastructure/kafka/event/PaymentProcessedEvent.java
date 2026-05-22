package com.order.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event được consume từ Payment Service sau khi xử lý payment
 *
 * Đây là unified event cho cả SUCCESS và FAILED cases,
 * được gửi bởi Payment Service qua Kafka topic "payment-events".
 *
 * Event này được consume bởi Order Service để update order status.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent extends OrderEvent {

    /**
     * Event type constants
     */
    public static final String EVENT_TYPE_SUCCESS = "PAYMENT_SUCCESS";
    public static final String EVENT_TYPE_FAILED = "PAYMENT_FAILED";

    /**
     * Constructor đầy đủ cho Success/Failed Event
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
        super();
        setEventId(eventId);
        setEventType(success ? EVENT_TYPE_SUCCESS : EVENT_TYPE_FAILED);
        setAggregateId(aggregateId);
        setTimestamp(timestamp);
        setUserId(userId);
        setTraceId(traceId);
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
    private String orderId;

    /**
     * Payment ID - ID của payment record trong Payment Service
     */
    private String paymentId;

    /**
     * Transaction ID - ID giao dịch từ payment gateway
     */
    private String transactionId;

    /**
     * Số tiền đã thanh toán
     */
    private BigDecimal paidAmount;

    /**
     * Currency code
     */
    private String currency;

    /**
     * Payment method được sử dụng
     */
    private String paymentMethod;

    /**
     * Payment provider đã xử lý (STRIPE, VNPAY, etc.)
     */
    private String paymentProvider;

    /**
     * Payment success hay failed
     */
    private boolean success;

    /**
     * Lý do thất bại (chỉ có khi success = false)
     */
    private String failureReason;

    /**
     * Error code từ payment gateway (chỉ có khi success = false)
     */
    private String errorCode;

    /**
     * Có thể retry hay không (chỉ có khi success = false)
     */
    private boolean retryable;

    /**
     * Timestamp khi payment được xử lý xong
     */
    private Instant processedAt;
}
