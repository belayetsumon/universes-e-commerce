package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.model.FraudEventType;

public interface FraudEventPublisher {

    void publish(FraudEventType eventType, String aggregateType, Long aggregateId,
            String payloadJson, String correlationId, String idempotencyKey);
}
