package com.order.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Base Event class cho Order-related events
 *
 * Đây là base class cho tất cả events liên quan đến Order được publish/consume qua Kafka.
 * Mỗi event có:
 * - eventId: Unique ID cho event itself (để track và deduplicate)
 * - eventType: Type của event (ORDER_CREATED, ORDER_PAID, etc.)
 * - aggregateId: ID của aggregate root (Order ID)
 * - timestamp: Thời điểm event xảy ra
 * - userId: ID của user tạo event (để audit trail)
 *
 * Pattern: Event-Carried State Transfer
 * Events chứa đầy đủ state cần thiết để consumer xử lý mà không cần gọi lại source service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class OrderEvent {

    /**
     * Unique ID cho event này
     * Dùng để track và deduplicate events trong case of retry
     */
    private String eventId;

    /**
     * Type của event - dùng để routing trong consumer
     * Ví dụ: ORDER_CREATED, ORDER_PAID, ORDER_CANCELLED, etc.
     */
    private String eventType;

    /**
     * ID của aggregate root (Order ID)
     * Đây là correlation ID để track saga flow
     */
    private String aggregateId;

    /**
     * Thời điểm event xảy ra
     */
    private Instant timestamp;

    /**
     * ID của user tạo event
     * Dùng cho audit trail và security context
     */
    private String userId;

    /**
     * Trace ID để track request qua nhiều services
     * Dùng cho distributed tracing (OpenTelemetry, Zipkin, etc.)
     */
    private String traceId;

    /**
     * Kiểm tra xem event có expired không
     *
     * @param ttlSeconds Time-to-live trong giây
     * @return true nếu event đã expired
     */
    public boolean isExpired(long ttlSeconds) {
        if (timestamp == null) {
            return false;
        }
        Instant expiryTime = timestamp.plusSeconds(ttlSeconds);
        return Instant.now().isAfter(expiryTime);
    }

    /**
     * Get correlation ID - ID để track saga flow
     *
     * @return aggregateId (Order ID)
     */
    public String getCorrelationId() {
        return aggregateId;
    }
}
