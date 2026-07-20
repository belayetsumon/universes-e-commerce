package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.model.FraudAction;

public interface FraudAuditService {

    void record(String entityType, Long entityId, FraudAction action, String reason, String metadataJson);
}
