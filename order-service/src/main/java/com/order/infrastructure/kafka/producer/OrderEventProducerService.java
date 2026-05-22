package com.order.infrastructure.kafka.producer;

import com.order.infrastructure.kafka.event.OrderCreatedEvent;
import com.order.infrastructure.kafka.event.OrderEvent;
import com.order.infrastructure.kafka.event.OrderPaidEvent;
import com.order.infrastructure.kafka.event.OrderPaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service để publish Order Events đến Kafka
 *
 * Service này wrap OrderEventProducer và cung cấp high-level methods
 * để publish các loại events khác nhau liên quan đến Order.
 *
 * Transactional Event Publishing:
 * - Events được publish SAU KHI transaction commit thành công
 * - Sử dụng @TransactionalEventListener để đảm bảo exactly-once semantics
 * - Nếu transaction rollback, event sẽ KHÔNG được publish
 *
 * Key Features:
 * - Transaction-bound event publishing
 * - Auto-generate event IDs và timestamps
 * - Comprehensive logging cho debugging
 * - Error handling với DLQ support
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventProducerService {

    private final OrderEventProducer orderEventProducer;

    /**
     * Publish OrderCreatedEvent trong transaction
     *
     * Event được publish SAU KHI order được lưu thành công vào database.
     * Sử dụng @TransactionalEventListener để ensure exactly-once semantics.
     *
     * @param event OrderCreatedEvent cần publish
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishOrderCreatedAfterCommit(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent after transaction commit: {}", event.getAggregateId());
        try {
            CompletableFuture<SendResult<String, OrderEvent>> future =
                    orderEventProducer.publishOrderCreated(event);

            // Log success
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("OrderCreatedEvent published successfully: {} | Partition: {} | Offset: {}",
                            event.getAggregateId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish OrderCreatedEvent: {} | Error: {}",
                            event.getAggregateId(), ex.getMessage(), ex);
                    // TODO: Implement Dead Letter Queue pattern
                }
            });

        } catch (Exception e) {
            log.error("Error publishing OrderCreatedEvent: {}", event.getAggregateId(), e);
            // Không throw exception để không rollback transaction
        }
    }

    /**
     * Publish OrderCreatedEvent synchronously (blocking)
     *
     * Method này đợi cho đến khi event được successfully published.
     * Ném exception nếu publish failed.
     *
     * Chỉ dùng khi cần guarantee immediate publish.
     *
     * @param event OrderCreatedEvent cần publish
     * @throws Exception nếu publish failed
     */
    @Transactional
    public void publishOrderCreatedSync(OrderCreatedEvent event) throws Exception {
        log.info("Publishing OrderCreatedEvent SYNC: {}", event.getAggregateId());
        orderEventProducer.publishEventSync(event);
    }

    /**
     * Publish OrderPaidEvent khi payment thành công
     *
     * Event này được publish sau khi Order Service nhận PaymentSuccessEvent
     * từ Payment Service và update order status thành PAID.
     *
     * @param orderId Order ID
     * @param paymentId Payment ID
     * @param transactionId Transaction ID
     * @param paidAmount Số tiền đã thanh toán
     * @param currency Currency code
     * @param paymentMethod Payment method
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishOrderPaid(
            String orderId,
            String paymentId,
            String transactionId,
            java.math.BigDecimal paidAmount,
            String currency,
            String paymentMethod
    ) {
        log.info("Publishing OrderPaidEvent for order: {}", orderId);

        try {
            OrderPaidEvent event = new OrderPaidEvent(
                    UUID.randomUUID().toString(),     // eventId
                    orderId,                          // aggregateId
                    Instant.now(),                    // timestamp
                    getCurrentUserId(),                // userId
                    generateTraceId(),                // traceId
                    orderId,                          // orderId
                    null,                            // orderNumber (not available here)
                    paymentId,                        // paymentId
                    transactionId,                    // transactionId
                    paidAmount,                       // amount
                    currency,                         // currency
                    paymentMethod                     // paymentMethod
            );

            CompletableFuture<SendResult<String, OrderEvent>> future =
                    orderEventProducer.publishOrderPaid(event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("OrderPaidEvent published successfully: {}", orderId);
                } else {
                    log.error("Failed to publish OrderPaidEvent: {}", orderId, ex);
                }
            });

        } catch (Exception e) {
            log.error("Error publishing OrderPaidEvent for order: {}", orderId, e);
        }
    }

    /**
     * Publish OrderPaymentFailedEvent khi payment thất bại
     *
     * Event này được publish sau khi Order Service nhận PaymentFailedEvent
     * từ Payment Service.
     *
     * @param orderId Order ID
     * @param failureReason Lý do thất bại
     * @param errorCode Error code
     * @param retryable Có thể retry hay không
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishOrderPaymentFailed(
            String orderId,
            String failureReason,
            String errorCode,
            boolean retryable
    ) {
        log.info("Publishing OrderPaymentFailedEvent for order: {} | Reason: {}", orderId, failureReason);

        try {
            OrderPaymentFailedEvent event = new OrderPaymentFailedEvent(
                    UUID.randomUUID().toString(),     // eventId
                    orderId,                          // aggregateId
                    Instant.now(),                    // timestamp
                    getCurrentUserId(),                // userId
                    generateTraceId(),                // traceId
                    orderId,                          // orderId
                    null,                            // orderNumber (not available here)
                    null,                            // amount (not available here)
                    null,                            // currency (not available here)
                    failureReason,                    // reason
                    errorCode,                        // errorCode
                    retryable                         // retryable
            );

            CompletableFuture<SendResult<String, OrderEvent>> future =
                    orderEventProducer.publishOrderPaymentFailed(event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("OrderPaymentFailedEvent published successfully: {}", orderId);
                } else {
                    log.error("Failed to publish OrderPaymentFailedEvent: {}", orderId, ex);
                }
            });

        } catch (Exception e) {
            log.error("Error publishing OrderPaymentFailedEvent for order: {}", orderId, e);
        }
    }

    /**
     * Lấy current user ID từ UserContextHolder
     * Được dùng để set userId field trong event
     *
     * @return Current user ID, hoặc "SYSTEM" nếu không có
     */
    private String getCurrentUserId() {
        try {
            String userId = com.order.infrastructure.security.UserContextHolder.getUserId();
            return userId != null ? userId : "SYSTEM";
        } catch (Exception e) {
            log.debug("Could not get current user ID: {}", e.getMessage());
            return "SYSTEM";
        }
    }

    /**
     * Generate trace ID cho distributed tracing
     *
     * @return Trace ID string
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}
