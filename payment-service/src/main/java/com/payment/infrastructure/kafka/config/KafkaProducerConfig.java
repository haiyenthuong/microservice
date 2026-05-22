package com.payment.infrastructure.kafka.config;

import com.payment.infrastructure.kafka.event.PaymentProcessedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Cấu hình Kafka Producer cho Payment Service
 *
 * Producer này gửi PaymentProcessedEvent tới "payment-events" topic
 * để thông báo kết quả thanh toán (thành công hoặc thất bại).
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${kafka.producer.acks:all}")
    private String acks;

    @Value("${kafka.producer.retries:3}")
    private int retries;

    @Value("${kafka.producer.enable-idempotence:true}")
    private boolean enableIdempotence;

    /**
     * Producer Factory cho PaymentProcessedEvent
     */
    @Bean
    public ProducerFactory<String, PaymentProcessedEvent> paymentProducerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Kafka broker config
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Serializer config
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Reliability config
        config.put(ProducerConfig.ACKS_CONFIG, acks);  // "all" = all replicas must acknowledge
        config.put(ProducerConfig.RETRIES_CONFIG, retries);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);

        // Delivery timeout config
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "30000");  // 30 seconds

        // Request timeout config
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "10000");  // 10 seconds

        // Batch config - optimize throughput
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384");  // 16KB
        config.put(ProducerConfig.LINGER_MS_CONFIG, "10");  // 10ms

        // Buffer config
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "33554432");  // 32MB

        // Compression config
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        return new DefaultKafkaProducerFactory<>(
            config,
            new StringSerializer(),
            new JsonSerializer<PaymentProcessedEvent>()
        );
    }

    /**
     * KafkaTemplate cho PaymentProcessedEvent
     *
     * Được inject vào PaymentEventProducer để gửi events.
     */
    @Bean
    public KafkaTemplate<String, PaymentProcessedEvent> paymentKafkaTemplate() {
        return new KafkaTemplate<>(paymentProducerFactory());
    }
}
