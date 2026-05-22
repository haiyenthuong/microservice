package com.payment.infrastructure.kafka.producer;

import com.payment.infrastructure.kafka.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.CompletableFuture;

/**
 * Producer gửi PaymentProcessedEvent đến Kafka
 *
 * Sử dụng transactional event publishing để đảm bảo
 * event chỉ được gửi sau khi DB commit thành công.
 */
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);

    private final KafkaTemplate<String, PaymentProcessedEvent> paymentKafkaTemplate;

    @Value("${kafka.topics.payment-events:payment-events}")
    private String paymentEventsTopic;

    /**
     * Gửi PaymentProcessedEvent tới Kafka topic "payment-events"
     *
     * Event chỉ được gửi SAU KHI transaction hiện tại commit thành công
     * (sau khi Payment record được lưu vào DB).
     *
     * @param event PaymentProcessedEvent
     */
    public void sendPaymentProcessedEvent(PaymentProcessedEvent event) {
        String status = event.success ? "SUCCESS" : "FAILED";
        log.info("Scheduling PaymentProcessedEvent for after-commit: orderId={}, status={}",
            event.orderId, status);

        // Register transaction synchronization - event sẽ được gửi AFTER_COMMIT
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishEvent(event);
                }
            }
        );
    }

    /**
     * Publish event đến Kafka
     *
     * @param event PaymentProcessedEvent
     */
    private void publishEvent(PaymentProcessedEvent event) {
        try {
            String key = event.orderId; // Partition key = orderId để cùng order luôn vào cùng partition
            String status = event.success ? "SUCCESS" : "FAILED";

            log.info("Publishing PaymentProcessedEvent: orderId={}, paymentId={}, status={}",
                event.orderId, event.paymentId, status);

            CompletableFuture<SendResult<String, PaymentProcessedEvent>> future =
                paymentKafkaTemplate.send(paymentEventsTopic, key, event);

            // Handle async result (non-blocking)
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("PaymentProcessedEvent published successfully: orderId={}, partition={}, offset={}",
                        event.orderId,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish PaymentProcessedEvent: orderId={}, error={}",
                        event.orderId, ex.getMessage(), ex);
                }
            });

        } catch (Exception e) {
            log.error("Error scheduling PaymentProcessedEvent: orderId={}, error={}",
                event.orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to publish payment event", e);
        }
    }

    /**
     * Gửi event ngay lập tức (không đợi transaction commit)
     * Chỉ dùng cho testing hoặc special cases
     *
     * @param event PaymentProcessedEvent
     */
    public void sendPaymentProcessedEventImmediately(PaymentProcessedEvent event) {
        log.warn("Sending PaymentProcessedEvent immediately (without transaction synchronization): orderId={}",
            event.orderId);
        publishEvent(event);
    }
}
