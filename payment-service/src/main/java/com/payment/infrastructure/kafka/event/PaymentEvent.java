package com.payment.infrastructure.kafka.event;

import java.time.Instant;

/**
 * Base Event class cho Payment-related events
 */
public abstract class PaymentEvent {

    public String eventId;
    public String eventType;
    public String aggregateId;
    public Instant timestamp;
    public String userId;
    public String traceId;

    /**
     * Constructor với đầy đủ parameters (protected để subclasses có thể gọi)
     */
    protected PaymentEvent(String eventId, String eventType, String aggregateId,
                          Instant timestamp, String userId, String traceId) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.timestamp = timestamp;
        this.userId = userId;
        this.traceId = traceId;
    }

    public boolean isExpired(long ttlSeconds) {
        if (timestamp == null) {
            return false;
        }
        Instant expiryTime = timestamp.plusSeconds(ttlSeconds);
        return Instant.now().isAfter(expiryTime);
    }

    public String getCorrelationId() {
        return aggregateId;
    }
}
