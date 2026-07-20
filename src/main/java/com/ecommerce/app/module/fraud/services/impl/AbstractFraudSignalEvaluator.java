package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.FraudReasonCode;
import com.ecommerce.app.module.fraud.model.FraudSignalCategory;
import com.ecommerce.app.module.fraud.model.FraudSignalSeverity;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

abstract class AbstractFraudSignalEvaluator {

    protected FraudSignalResult signal(String code, FraudSignalCategory category, boolean triggered,
            int scoreImpact, FraudSignalSeverity severity, FraudReasonCode reasonCode,
            String value, String source, String metadataJson) {
        FraudSignalResult result = new FraudSignalResult();
        result.setSignalCode(code);
        result.setCategory(category);
        result.setTriggered(triggered);
        result.setScoreImpact(scoreImpact);
        result.setSeverity(severity);
        result.setReasonCode(reasonCode);
        result.setSignalValue(value);
        result.setSource(source);
        result.setDetectedAt(LocalDateTime.now());
        result.setMetadataJson(metadataJson);
        return result;
    }

    protected Long customerId(SalesOrder order) {
        return order != null && order.getCustomer() != null ? order.getCustomer().getId() : null;
    }

    protected BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    protected String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    protected String normalize(String value) {
        String normalized = blankToNull(value);
        return normalized == null ? null : normalized.toLowerCase();
    }

    protected String sha256(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(normalized.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 hashing is not available.", ex);
        }
    }
}
