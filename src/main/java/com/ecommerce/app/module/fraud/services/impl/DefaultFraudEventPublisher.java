package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.exception.FraudValidationException;
import com.ecommerce.app.module.fraud.model.FraudEventType;
import com.ecommerce.app.module.fraud.model.FraudOutboxEvent;
import com.ecommerce.app.module.fraud.model.FraudOutboxStatus;
import com.ecommerce.app.module.fraud.repository.FraudOutboxEventRepository;
import com.ecommerce.app.module.fraud.services.FraudEventPublisher;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultFraudEventPublisher implements FraudEventPublisher {

    private final FraudOutboxEventRepository repository;

    public DefaultFraudEventPublisher(FraudOutboxEventRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void publish(FraudEventType eventType, String aggregateType, Long aggregateId,
            String payloadJson, String correlationId, String idempotencyKey) {
        if (eventType == null) {
            throw new FraudValidationException("Fraud event type is required.");
        }
        String safeAggregateType = aggregateType == null || aggregateType.isBlank() ? "FRAUD_EVENT" : aggregateType.trim();
        Long safeAggregateId = aggregateId == null ? 0L : aggregateId;
        String eventIdempotencyKey = eventIdempotencyKey(eventType, safeAggregateType, safeAggregateId, payloadJson, idempotencyKey);
        if (repository.findByIdempotencyKey(eventIdempotencyKey).isPresent()) {
            return;
        }

        FraudOutboxEvent event = new FraudOutboxEvent();
        event.setEventType(eventType);
        event.setAggregateType(trim(safeAggregateType, 80));
        event.setAggregateId(safeAggregateId);
        event.setCorrelationId(trim(correlationId, 120));
        event.setIdempotencyKey(eventIdempotencyKey);
        event.setPayloadJson(payloadJson == null || payloadJson.isBlank() ? "{}" : payloadJson);
        event.setStatus(FraudOutboxStatus.PENDING);
        event.setRetryCount(0);
        event.setNextAttemptAt(LocalDateTime.now());
        repository.save(event);
    }

    private String eventIdempotencyKey(FraudEventType eventType, String aggregateType, Long aggregateId,
            String payloadJson, String idempotencyKey) {
        String source = (idempotencyKey == null || idempotencyKey.isBlank() ? "" : idempotencyKey.trim()) + "|"
                + eventType + "|" + aggregateType + "|" + aggregateId + "|" + (payloadJson == null ? "" : payloadJson);
        return "FRAUD:OUTBOX:" + sha256(source);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte current : encoded) {
                builder.append(String.format("%02x", current));
            }
            return builder.toString();
        } catch (Exception ex) {
            return Integer.toHexString(value.hashCode());
        }
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength);
    }
}
