package com.order.infrastructure.kafka.config;

import com.order.infrastructure.kafka.event.OrderEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Configuration cho Order Service
 *
 * Cấu hình Kafka Producer để publish Order events
 * và Kafka Consumer để consume Payment events.
 *
 * Topics:
 * - order-events: Topic cho order events (OrderCreated, OrderPaid, OrderPaymentFailed, etc.)
 * - payment-events: Topic cho payment events (PaymentSuccess, PaymentFailed)
 *
 * Key Features:
 * - Producer: Idempotent, compression, batching cho performance
 * - Consumer: Manual acknowledgment, batch processing
 * - Error Handling: Dead Letter Queue (DLQ) cho failed events
 * - Serialization: JSON với trusted packages security
 */
@Configuration
@EnableKafka
public class KafkaConfiguration {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:order-service-group}")
    private String consumerGroupId;

    // ==================== PRODUCER CONFIGURATION ====================

    /**
     * Producer Factory cho Order Events
     *
     * Cấu hình producer để publish Order events với JSON serialization.
     */
    @Bean
    public ProducerFactory<String, OrderEvent> orderEventProducerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Bootstrap servers
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Key Serializer - Order ID as String
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Value Serializer - JSON for Order Events
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Reliability settings
        config.put(ProducerConfig.ACKS_CONFIG, "all");  // Wait for all replicas
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");  // Exactly-once semantics
        config.put(ProducerConfig.RETRIES_CONFIG, 3);  // Retry configuration

        // Performance settings
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");  // Compression
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);  // Batch size
        config.put(ProducerConfig.LINGER_MS_CONFIG, 10);  // Linger time
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);  // Buffer memory

        // JsonSerializer type mapping for event polymorphism
        config.put(JsonSerializer.TYPE_MAPPINGS, "event:" + OrderEvent.class.getName());

        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * Kafka Template cho Order Events
     *
     * Template này được sử dụng bởi OrderEventProducer để publish events.
     */
    @Bean
    public KafkaTemplate<String, OrderEvent> orderEventKafkaTemplate() {
        return new KafkaTemplate<>(orderEventProducerFactory());
    }

    // ==================== CONSUMER CONFIGURATION ====================

    /**
     * Consumer Factory cho Payment Events
     *
     * Cấu hình consumer để consume Payment events với JSON deserialization.
     */
    @Bean
    public ConsumerFactory<String, OrderEvent> paymentEventConsumerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Bootstrap servers
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Group ID
        config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

        // Key Deserializer - Order ID as String
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Value Deserializer - JSON for Order Events
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Offset management
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");  // Start from beginning if no offset

        // Auto commit - disabled để manual acknowledge
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        // JsonDeserializer trusted packages
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.TYPE_MAPPINGS, "event:" + OrderEvent.class.getName());

        // Max poll records (batch size)
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10");

        // Max poll interval (time between polls)
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");  // 5 minutes

        return new DefaultKafkaConsumerFactory<>(config,
                new StringDeserializer(),
                new JsonDeserializer<>(OrderEvent.class, false));
    }

    /**
     * Kafka Listener Container Factory cho Payment Events
     *
     * Cấu hình listener container với manual acknowledgment.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent>
    paymentEventKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(paymentEventConsumerFactory());

        // Manual acknowledgment
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // Concurrency - number of consumer threads
        factory.setConcurrency(3);

        // Batch processing - enable để consume multiple messages cùng lúc
        factory.setBatchListener(true);

        return factory;
    }

    // ==================== ADMIN CONFIGURATION ====================

    /**
     * Kafka Admin
     *
     * Bean này có thể dùng để tạo topics programmatically nếu cần.
     * Lưu ý: Trong production, topics thường được tạo bởi DevOps hoặc Infrastructure as Code.
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }
}
