package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.AuditLogPromotions;
import com.ecommerce.app.module.ReferralRewards.repository.AuditLogPromotionsRepository;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class AuditLogPromotionsService {

    private final AuditLogPromotionsRepository auditLogPromotionsRepository;

    public AuditLogPromotionsService(AuditLogPromotionsRepository auditLogPromotionsRepository) {
        this.auditLogPromotionsRepository = auditLogPromotionsRepository;
    }

    public void log(String entityType, String entityId, String action, String oldValue, String newValue, String performedBy) {
        AuditLogPromotions log = new AuditLogPromotions();
        log.setEntityType(clean(entityType));
        log.setEntityId(clean(entityId));
        log.setAction(clean(action));
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setPerformedBy(clean(performedBy));
        auditLogPromotionsRepository.save(log);
    }

    public void logIfChanged(String entityType, String entityId, String action, String oldValue, String newValue, String performedBy) {
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        log(entityType, entityId, action, oldValue, newValue, performedBy);
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}

