package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.enumvalue.PromotionAuditAction;
import com.ecommerce.app.module.ReferralRewards.enumvalue.PromotionAuditEntityType;
import com.ecommerce.app.module.ReferralRewards.model.AuditLogPromotions;
import com.ecommerce.app.module.ReferralRewards.repository.AuditLogPromotionsRepository;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class AuditLogPromotionsService {

    private final AuditLogPromotionsRepository auditLogPromotionsRepository;

    public AuditLogPromotionsService(AuditLogPromotionsRepository auditLogPromotionsRepository) {
        this.auditLogPromotionsRepository = auditLogPromotionsRepository;
    }

    public void log(String entityType, String entityId, String action, String oldValue, String newValue, String performedBy) {
        log(parseRequired(PromotionAuditEntityType.class, entityType, "Audit entity type"),
                entityId,
                parseRequired(PromotionAuditAction.class, action, "Audit action"),
                oldValue,
                newValue,
                performedBy);
    }

    public void log(PromotionAuditEntityType entityType, String entityId, PromotionAuditAction action,
            String oldValue, String newValue, String performedBy) {
        log(entityType, entityId, action, oldValue, newValue, performedBy, null, null, null, null, null);
    }

    public void log(PromotionAuditEntityType entityType, String entityId, PromotionAuditAction action,
            String oldValue, String newValue, String performedBy, String ipAddress, String userAgent,
            String requestId, String correlationId, String reason) {
        if (entityType == null) {
            throw new IllegalArgumentException("Audit entity type is required.");
        }
        if (action == null) {
            throw new IllegalArgumentException("Audit action is required.");
        }

        AuditLogPromotions log = new AuditLogPromotions();
        log.setEntityType(entityType);
        log.setEntityId(required(entityId, "Audit entity ID", 80));
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setPerformedBy(defaulted(performedBy, "system", 100));
        log.setIpAddress(trimToLength(ipAddress, 45));
        log.setUserAgent(trimToLength(userAgent, 500));
        log.setRequestId(trimToLength(requestId, 100));
        log.setCorrelationId(trimToLength(correlationId, 100));
        log.setReason(trimToLength(reason, 255));
        auditLogPromotionsRepository.save(log);
    }

    public void logIfChanged(String entityType, String entityId, String action, String oldValue, String newValue, String performedBy) {
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        log(entityType, entityId, action, oldValue, newValue, performedBy);
    }

    public void logIfChanged(PromotionAuditEntityType entityType, String entityId, PromotionAuditAction action,
            String oldValue, String newValue, String performedBy) {
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        log(entityType, entityId, action, oldValue, newValue, performedBy);
    }

    private <E extends Enum<E>> E parseRequired(Class<E> enumType, String value, String label) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            return Enum.valueOf(enumType, normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(label + " is invalid: " + value, ex);
        }
    }

    private String required(String value, String label, int maxLength) {
        String cleaned = trimToLength(value, maxLength);
        if (cleaned == null) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return cleaned;
    }

    private String defaulted(String value, String fallback, int maxLength) {
        String cleaned = trimToLength(value, maxLength);
        return cleaned == null ? fallback : cleaned;
    }

    private String trimToLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private String normalize(String value) {
        String cleaned = trimToLength(value, 100);
        return cleaned == null ? null : cleaned.replace('-', '_').replace(' ', '_').toUpperCase(Locale.ENGLISH);
    }
}
