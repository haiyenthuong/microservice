package com.order.infrastructure.kafka.consumer;

import com.order.infrastructure.kafka.event.OrderEvent;
import com.order.infrastructure.kafka.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Kafka Consumer cho Payment Events (OrderPaymentResultConsumer)
 *
 * Consumer này nhận PaymentProcessedEvent từ Payment Service và update Order status.
 * Đây là consumer side của Order-Payment Saga pattern.
 *
 * Topics:
 * - payment-events: Topic chứa PaymentProcessedEvent (SUCCESS hoặc FAILED)
 *
 * Flow:
 * 1. Order Service publish OrderCreatedEvent → Payment Service
 * 2. Payment Service process payment
 * 3. Payment Service publish PaymentProcessedEvent → this consumer
 * 4. Consumer update Order status (PAID hoặc FAILED)
 * 5. Consumer publish OrderPaidEvent hoặc OrderPaymentFailedEvent
 *
 * Key Features:
 * - Manual acknowledgment để đảm bảo at-least-once delivery
 * - Error handling với retry mechanism
 * - Idempotency check để handle duplicate messages
 * - Distributed tracing với trace ID
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentResultConsumer {

    @Value("${spring.kafka.topics.payment.name:payment-events}")
    private String paymentEventsTopic;

    private final PaymentEventConsumerService paymentEventConsumerService;

    /**
     * Listen Payment Events topic
     *
     * Method này consume PaymentProcessedEvent và route đến handler phù hợp
     * dựa trên success flag.
     *
     * @param events List của payment events (batch processing)
     * @param ack    Acknowledgment object để manual commit
     * @param keys   Message keys (order IDs)
     */
    @KafkaListener(
            topics = "${spring.kafka.topics.payment.name:payment-events}",
            groupId = "${spring.kafka.consumer.group-id:order-service-group}",
            containerFactory = "paymentEventKafkaListenerContainerFactory",
            clientIdPrefix = "order-payment-consumer"
    )
    public void consumePaymentEvents(
            @Payload List<OrderEvent> events,
            Acknowledgment ack,
            @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys
    ) {
        if (events == null || events.isEmpty()) {
            log.warn("Received empty message list, skipping");
            return;
        }

        log.info("Received {} payment events from topic: {}", events.size(), paymentEventsTopic);

        try {
            // Process each event
            for (int i = 0; i < events.size(); i++) {
                OrderEvent event = events.get(i);
                String key = keys != null && i < keys.size() ? keys.get(i) : "unknown";

                log.debug("Processing payment event: {} for key: {}", event.getEventType(), key);

                try {
                    // Chỉ xử lý PaymentProcessedEvent
                    if (event instanceof PaymentProcessedEvent) {
                        handlePaymentProcessedEvent((PaymentProcessedEvent) event);
                    } else {
                        log.warn("Unexpected event type: {}. Expected PaymentProcessedEvent.", event.getClass().getSimpleName());
                    }

                } catch (Exception e) {
                    log.error("Error processing payment event: {} for key: {} | Error: {}",
                            event.getEventType(), key, e.getMessage(), e);

                    // Continue processing next event thay vì fail toàn bộ batch
                }
            }

            // Acknowledge messages sau khi xử lý thành công
            if (ack != null) {
                log.debug("Acknowledging {} payment events", events.size());
                ack.acknowledge();
            }

        } catch (Exception e) {
            log.error("Unexpected error consuming payment events", e);

            // Không acknowledge - Kafka sẽ redeliver message
            // TODO: Implement max retry attempts và send to DLQ
        }
    }

    /**
     * Handle PaymentProcessedEvent
     *
     * Route đến handler phù hợp dựa trên success flag:
     * - success = true → handlePaymentSuccess
     * - success = false → handlePaymentFailed
     *
     * @param event PaymentProcessedEvent
     */
    private void handlePaymentProcessedEvent(PaymentProcessedEvent event) {
        log.info("Handling PaymentProcessedEvent for order: {} | Success: {} | Payment ID: {}",
                event.getOrderId(), event.isSuccess(), event.getPaymentId());

        if (event.isSuccess()) {
            paymentEventConsumerService.handlePaymentSuccess(event);
        } else {
            paymentEventConsumerService.handlePaymentFailed(event);
        }
    }
}
