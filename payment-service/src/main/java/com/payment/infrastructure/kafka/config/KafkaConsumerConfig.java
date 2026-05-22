package com.payment.infrastructure.kafka.config;

import com.payment.infrastructure.kafka.event.OrderCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Cấu hình Kafka Consumer cho Payment Service
 *
 * Consumer này lắng nghe "order-events" topic để nhận OrderCreatedEvent
 * và trigger payment processing flow.
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${kafka.consumer.group-id:payment-service-group}")
    private String groupId;

    @Value("${kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    @Value("${kafka.consumer.max-poll-records:10}")
    private int maxPollRecords;

    @Value("${kafka.consumer.enable-auto-commit:false}")
    private boolean enableAutoCommit;

    /**
     * Consumer Factory cho OrderCreatedEvent
     */
    @Bean
    public ConsumerFactory<String, OrderCreatedEvent> orderConsumerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Kafka broker config
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Deserializer config
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Offset config - earliest để xử lý từ đầu nếu consumer group mới
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

        // Auto-commit config - disable để manual commit (exact-once semantics)
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);

        // Poll config - batch processing
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);

        // Session timeout config
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");

        // Heartbeat interval config
        config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "10000");

        // Max poll interval config - max time between polls before rebalance
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");

        // Trust package cho JsonDeserializer
        JsonDeserializer<OrderCreatedEvent> deserializer = new JsonDeserializer<>(OrderCreatedEvent.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(false);

        return new DefaultKafkaConsumerFactory<>(
            config,
            new StringDeserializer(),
            deserializer
        );
    }

    /**
     * Container Factory cho OrderCreatedEvent Listener
     *
     * Cấu hình batch listener để xử lý nhiều records cùng lúc.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>
            orderKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(orderConsumerFactory());

        // Batch listener - xử lý list của records
        factory.setBatchListener(true);

        // Concurrency - số lượng consumer threads
        factory.setConcurrency(2);

        // Ack mode - MANUAL_IMMEDIATE để acknowledge ngay sau khi xử lý
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }
}
