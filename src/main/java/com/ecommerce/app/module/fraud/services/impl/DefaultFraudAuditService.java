package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.model.FraudAction;
import com.ecommerce.app.module.fraud.model.FraudEventLog;
import com.ecommerce.app.module.fraud.model.FraudEventType;
import com.ecommerce.app.module.fraud.repository.FraudEventLogRepository;
import com.ecommerce.app.module.fraud.services.FraudAuditService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultFraudAuditService implements FraudAuditService {

    private final FraudEventLogRepository fraudEventLogRepository;

    public DefaultFraudAuditService(FraudEventLogRepository fraudEventLogRepository) {
        this.fraudEventLogRepository = fraudEventLogRepository;
    }

    @Override
    @Transactional
    public void record(String entityType, Long entityId, FraudAction action, String reason, String metadataJson) {
        FraudEventLog eventLog = new FraudEventLog();
        eventLog.setEventType(FraudEventType.FRAUD_AUDIT_RECORDED);
        eventLog.setAggregateType(entityType);
        eventLog.setAggregateId(entityId);
        eventLog.setPayloadJson("{\"action\":\"" + safe(action == null ? null : action.name())
                + "\",\"reason\":\"" + safe(reason)
                + "\",\"metadata\":" + (metadataJson == null || metadataJson.isBlank() ? "{}" : metadataJson) + "}");
        eventLog.setEventTime(LocalDateTime.now());
        fraudEventLogRepository.save(eventLog);
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
