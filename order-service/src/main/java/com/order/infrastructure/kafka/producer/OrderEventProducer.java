package com.order.infrastructure.kafka.producer;

import com.order.infrastructure.kafka.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka Producer cho Order Events
 *
 * Producer này chịu trách nhiệm publish các order-related events đến Kafka topics.
 * Mỗi event được publish với key là orderId (partition key) để đảm bảo ordering.
 *
 * Key Features:
 * - Async publish với CompletableFuture callback
 * - Transactional publish (khi cần consistency)
 * - Error handling và retry logic
 * - Correlation ID tracking cho distributed tracing
 *
 * Topics:
 * - order-events: Main topic cho order events (OrderCreated, OrderPaid, etc.)
 * - order-events-dlt: Dead Letter Topic cho failed events
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    /**
     * Topic name cho order events
     */
    @Value("${spring.kafka.topics.order.name:order-events}")
    private String orderEventsTopic;

    /**
     * Publish event đến Kafka (Async)
     *
     * Method này publish event asynchronously và trả về CompletableFuture
     * để caller có thể handle success/failure.
     *
     * @param event Order event cần publish
     * @return CompletableFuture<SendResult> để track publish result
     */
    public CompletableFuture<SendResult<String, OrderEvent>> publishEvent(OrderEvent event) {
        // Ensure event has required fields
        validateEvent(event);

        // Key cho Kafka message (dùng để partitioning)
        // Dùng aggregateId (orderId) để đảm bảo cùng order luôn vào cùng partition
        String key = event.getAggregateId();

        log.info("Publishing event: {} for order: {} to topic: {}",
                event.getEventType(), key, orderEventsTopic);

        // Publish event to Kafka
        CompletableFuture<SendResult<String, OrderEvent>> future =
                kafkaTemplate.send(orderEventsTopic, key, event);

        // Handle success/failure callbacks
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                handleSuccess(event, result);
            } else {
                handleFailure(event, ex);
            }
        });

        return future;
    }

    /**
     * Publish event synchronously (blocking)
     *
     * Method này đợi cho đến khi event được successfully published.
     * Ném exception nếu publish failed.
     *
     * @param event Order event cần publish
     * @throws Exception nếu publish failed
     */
    @Transactional
    public void publishEventSync(OrderEvent event) throws Exception {
        // Ensure event has required fields
        validateEvent(event);

        // Key cho Kafka message
        String key = event.getAggregateId();

        log.info("Publishing event SYNC: {} for order: {} to topic: {}",
                event.getEventType(), key, orderEventsTopic);

        // Send and wait for result
        SendResult<String, OrderEvent> result = kafkaTemplate
                .send(orderEventsTopic, key, event)
                .get();

        handleSuccess(event, result);
    }

    /**
     * Publish OrderCreatedEvent
     *
     * @param event OrderCreatedEvent
     * @return CompletableFuture để track result
     */
    public CompletableFuture<SendResult<String, OrderEvent>> publishOrderCreated(OrderEvent event) {
        log.info("Publishing OrderCreatedEvent for order: {}", event.getAggregateId());
        return publishEvent(event);
    }

    /**
     * Publish OrderPaidEvent
     *
     * @param event OrderPaidEvent
     * @return CompletableFuture để track result
     */
    public CompletableFuture<SendResult<String, OrderEvent>> publishOrderPaid(OrderEvent event) {
        log.info("Publishing OrderPaidEvent for order: {}", event.getAggregateId());
        return publishEvent(event);
    }

    /**
     * Publish OrderPaymentFailedEvent
     *
     * @param event OrderPaymentFailedEvent
     * @return CompletableFuture để track result
     */
    public CompletableFuture<SendResult<String, OrderEvent>> publishOrderPaymentFailed(OrderEvent event) {
        log.info("Publishing OrderPaymentFailedEvent for order: {}", event.getAggregateId());
        return publishEvent(event);
    }

    /**
     * Validate event có đủ required fields không
     *
     * @param event Event cần validate
     * @throws IllegalArgumentException nếu event không hợp lệ
     */
    private void validateEvent(OrderEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        if (event.getAggregateId() == null || event.getAggregateId().isEmpty()) {
            throw new IllegalArgumentException("Event must have aggregateId (orderId)");
        }

        if (event.getEventType() == null || event.getEventType().isEmpty()) {
            throw new IllegalArgumentException("Event must have eventType");
        }

        // Auto-generate eventId nếu chưa có
        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            event.setEventId(UUID.randomUUID().toString());
        }

        // Auto-set timestamp nếu chưa có
        if (event.getTimestamp() == null) {
            event.setTimestamp(Instant.now());
        }
    }

    /**
     * Handle successful publish
     *
     * @param event  Event đã publish
     * @param result SendResult từ Kafka
     */
    private void handleSuccess(OrderEvent event, SendResult<String, OrderEvent> result) {
        log.info("Event published successfully: {} for order: {} | Topic: {} | Partition: {} | Offset: {}",
                event.getEventType(),
                event.getAggregateId(),
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
    }

    /**
     * Handle failed publish
     *
     * @param event Event failed to publish
     * @param ex    Exception
     */
    private void handleFailure(OrderEvent event, Throwable ex) {
        log.error("Failed to publish event: {} for order: {} | Error: {}",
                event.getEventType(),
                event.getAggregateId(),
                ex.getMessage(),
                ex);

        // TODO: Implement dead letter queue pattern
        // - Save failed event to database
        // - Or publish to DLT topic
        // - Or alert operations team
    }
}
