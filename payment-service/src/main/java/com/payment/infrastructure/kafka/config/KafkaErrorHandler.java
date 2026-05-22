package com.payment.infrastructure.kafka.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Error Handler cho Kafka Consumer
 *
 * Xử lý lỗi khi consume messages từ Kafka.
 * Thay vì fail toàn bộ consumer batch, log lỗi và continue processing.
 */
@Component("kafkaErrorHandler")
public class KafkaErrorHandler implements ConsumerAwareListenerErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(KafkaErrorHandler.class);

    /**
     * Handle error khi consume message
     *
     * Strategy:
     * - Log lỗi chi tiết
     * - Continue processing các messages khác trong batch
     * - Không throw exception để tránh consumer stop
     *
     * @param message Message gây lỗi
     * @param exception Exception xảy ra
     * @param consumer Kafka Consumer instance
     * @return null để skip message gây lỗi
     */
    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception, Consumer<?, ?> consumer) {
        log.error("Kafka Consumer Error: {}", exception.getMessage(), exception);

        if (message != null) {
            log.error("Failed message details:");
            log.error("  - Topic: {}", message.getHeaders().get("kafka_receivedTopic"));
            log.error("  - Partition: {}", message.getHeaders().get("kafka_receivedPartitionId"));
            log.error("  - Offset: {}", message.getHeaders().get("kafka_offset"));
            log.error("  - Payload: {}", message.getPayload());
        }

        // Return null để skip message này và continue processing
        return null;
    }
}
