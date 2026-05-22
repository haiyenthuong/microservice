package com.order.infrastructure.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Kafka Error Handler cho Order Service
 *
 * Handler này xử lý các lỗi xảy ra trong quá trình consume Kafka messages.
 * Nó được gọi khi có exception trong @KafkaListener method.
 *
 * Error Handling Strategy:
 * - Log chi tiết error
 * - Publish failed message to Dead Letter Queue (DLQ)
 * - Update metrics/monitoring
 * - Không throw exception để consumer có thể tiếp tục processing các messages khác
 */
@Slf4j
@Component("kafkaErrorHandler")
public class KafkaErrorHandler implements ConsumerAwareListenerErrorHandler {

    /**
     * Handle error trong Kafka listener
     *
     * @param message Message caused error
     * @param exception Exception occurred
     * @param consumer Kafka consumer
     * @return null để không throw exception tiếp
     */
    @Override
    public Object handleError(
            Message<?> message,
            ListenerExecutionFailedException exception,
            Consumer<?, ?> consumer) {

        // Extract message info
        Object payload = message != null ? message.getPayload() : null;
        Object key = message != null ? message.getHeaders().get("k_receivedKey") : "unknown";

        // Log error details
        log.error("=== KAFKA CONSUMER ERROR ===");
        log.error("Message Key: {}", key);
        log.error("Message Payload: {}", payload);
        log.error("Exception Type: {}", exception.getClass().getSimpleName());
        log.error("Exception Message: {}", exception.getMessage());

        // Log root cause nếu có
        Throwable rootCause = exception.getRootCause();
        if (rootCause != null && rootCause != exception) {
            log.error("Root Cause: {} - {}", rootCause.getClass().getSimpleName(), rootCause.getMessage());
        }

        log.error("Consumer Info:");
        log.error("  Group ID: {}", consumer != null ? consumer.groupMetadata().groupId() : "unknown");
        log.error("==========================");

        // TODO: Implement Dead Letter Queue pattern
        // - Publish failed message to DLQ topic
        // - Or save to database for manual review
        // - Send alert to operations team

        // TODO: Update metrics
        // - Increment error counter in Prometheus
        // - Track error rate by event type
        // - Alert if error rate exceeds threshold

        // Return null để không throw exception
        // Consumer sẽ continue processing các messages khác
        return null;
    }
}
