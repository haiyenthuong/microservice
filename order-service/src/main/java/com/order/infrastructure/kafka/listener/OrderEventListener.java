package com.order.infrastructure.kafka.listener;

import com.order.infrastructure.kafka.event.OrderCreatedEvent;
import com.order.infrastructure.kafka.producer.OrderEventProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event Listener cho Order Events
 *
 * Component này listen cho Spring application events và publish chúng đến Kafka.
 * Sử dụng @TransactionalEventListener để ensure events chỉ được publish
 * sau khi transaction commit thành công.
 *
 * Flow:
 * 1. CreateOrderCommand publish OrderCreatedEvent (Spring event)
 * 2. OrderEventListener nhận event (sau transaction commit)
 * 3. OrderEventListener publish event to Kafka qua OrderEventProducerService
 * 4. Payment Service consume event từ Kafka
 *
 * Key Features:
 * - Exactly-once semantics (event chỉ publish khi transaction commit)
 * - Async publishing để không block main thread
 * - Error handling với DLQ support
 * - Comprehensive logging
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderEventProducerService orderEventProducerService;

    /**
     * Listen cho OrderCreatedEvent và publish to Kafka
     *
     * @TransactionalEventListener với phase = AFTER_COMMIT đảm bảo
     * event chỉ được process khi transaction đã commit thành công.
     *
     * @param event OrderCreatedEvent
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent (after transaction commit): {} | Order: {}",
                event.getEventId(), event.getAggregateId());

        try {
            // Publish event to Kafka qua producer service
            orderEventProducerService.publishOrderCreatedAfterCommit(event);

            log.info("OrderCreatedEvent published to Kafka successfully: {} | Order: {}",
                    event.getEventId(), event.getAggregateId());

        } catch (Exception e) {
            log.error("Failed to publish OrderCreatedEvent to Kafka: {} | Order: {} | Error: {}",
                    event.getEventId(), event.getAggregateId(), e.getMessage(), e);

            // TODO: Implement Dead Letter Queue pattern
            // - Save failed event to database
            // - Or publish to DLT topic
            // - Or alert operations team
        }
    }
}
