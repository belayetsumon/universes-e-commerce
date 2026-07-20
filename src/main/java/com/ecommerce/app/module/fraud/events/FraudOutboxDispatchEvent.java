package com.ecommerce.app.module.fraud.events;

import com.ecommerce.app.module.fraud.model.FraudEventType;
import com.ecommerce.app.module.fraud.model.FraudOutboxEvent;
import java.time.LocalDateTime;

public class FraudOutboxDispatchEvent {

    private final Long outboxEventId;
    private final FraudEventType eventType;
    private final String aggregateType;
    private final Long aggregateId;
    private final String correlationId;
    private final String idempotencyKey;
    private final String payloadJson;
    private final LocalDateTime occurredAt;

    public FraudOutboxDispatchEvent(FraudOutboxEvent event) {
        this.outboxEventId = event.getId();
        this.eventType = event.getEventType();
        this.aggregateType = event.getAggregateType();
        this.aggregateId = event.getAggregateId();
        this.correlationId = event.getCorrelationId();
        this.idempotencyKey = event.getIdempotencyKey();
        this.payloadJson = event.getPayloadJson();
        this.occurredAt = event.getCreatedAt();
    }

    public Long getOutboxEventId() { return outboxEventId; }
    public FraudEventType getEventType() { return eventType; }
    public String getAggregateType() { return aggregateType; }
    public Long getAggregateId() { return aggregateId; }
    public String getCorrelationId() { return correlationId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getPayloadJson() { return payloadJson; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
