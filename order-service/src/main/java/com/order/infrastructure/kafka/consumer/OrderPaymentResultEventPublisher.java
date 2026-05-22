package com.order.infrastructure.kafka.consumer;

import com.order.infrastructure.kafka.event.OrderPaidEvent;
import com.order.infrastructure.kafka.event.OrderPaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Publisher cho Order payment result events
 *
 * Publish các events sau khi Order status được cập nhật:
 * - OrderPaidEvent: Khi payment thành công
 * - OrderPaymentFailedEvent: Khi payment thất bại
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentResultEventPublisher {

    private final KafkaTemplate<String, Object> orderKafkaTemplate;

    @Value("${kafka.topics.order-events.name:order-events}")
    private String orderEventsTopic;

    /**
     * Publish OrderPaidEvent tới Kafka
     *
     * @param event OrderPaidEvent
     */
    public void publishOrderPaidEvent(OrderPaidEvent event) {
        try {
            String key = event.getOrderId();

            log.info("Publishing OrderPaidEvent: orderNumber={}, amount={}, transactionId={}",
                event.getOrderNumber(), event.getAmount(), event.getTransactionId());

            CompletableFuture<SendResult<String, Object>> future =
                orderKafkaTemplate.send(orderEventsTopic, key, event);

            // Handle async result (non-blocking)
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("OrderPaidEvent published successfully: orderNumber={}, partition={}, offset={}",
                        event.getOrderNumber(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish OrderPaidEvent: orderNumber={}, error={}",
                        event.getOrderNumber(), ex.getMessage(), ex);
                }
            });

        } catch (Exception e) {
            log.error("Error publishing OrderPaidEvent: orderNumber={}, error={}",
                event.getOrderNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish OrderPaidEvent", e);
        }
    }

    /**
     * Publish OrderPaymentFailedEvent tới Kafka
     *
     * @param event OrderPaymentFailedEvent
     */
    public void publishOrderPaymentFailedEvent(OrderPaymentFailedEvent event) {
        try {
            String key = event.getOrderId();

            log.info("Publishing OrderPaymentFailedEvent: orderNumber={}, reason={}, retryable={}",
                event.getOrderNumber(), event.getReason(), event.isRetryable());

            CompletableFuture<SendResult<String, Object>> future =
                orderKafkaTemplate.send(orderEventsTopic, key, event);

            // Handle async result (non-blocking)
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("OrderPaymentFailedEvent published successfully: orderNumber={}, partition={}, offset={}",
                        event.getOrderNumber(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish OrderPaymentFailedEvent: orderNumber={}, error={}",
                        event.getOrderNumber(), ex.getMessage(), ex);
                }
            });

        } catch (Exception e) {
            log.error("Error publishing OrderPaymentFailedEvent: orderNumber={}, error={}",
                event.getOrderNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish OrderPaymentFailedEvent", e);
        }
    }
}
