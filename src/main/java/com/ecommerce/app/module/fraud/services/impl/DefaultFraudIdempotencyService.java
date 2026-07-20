package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.exception.FraudIdempotencyException;
import com.ecommerce.app.module.fraud.model.FraudIdempotencyRecord;
import com.ecommerce.app.module.fraud.model.FraudIdempotencyStatus;
import com.ecommerce.app.module.fraud.repository.FraudIdempotencyRecordRepository;
import com.ecommerce.app.module.fraud.services.FraudIdempotencyService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultFraudIdempotencyService implements FraudIdempotencyService {

    private static final int DEFAULT_LOCK_MINUTES = 10;
    private static final int DEFAULT_TTL_HOURS = 72;

    private final FraudIdempotencyRecordRepository repository;

    public DefaultFraudIdempotencyService(FraudIdempotencyRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FraudIdempotencyRecord> findCompleted(String operationScope, String idempotencyKey) {
        if (isBlank(operationScope) || isBlank(idempotencyKey)) {
            return Optional.empty();
        }
        return repository.findByIdempotencyKeyAndOperationScope(idempotencyKey.trim(), operationScope.trim())
                .filter(record -> record.getStatus() == FraudIdempotencyStatus.COMPLETED);
    }

    @Override
    @Transactional
    public FraudIdempotencyRecord start(String operationScope, String idempotencyKey, String requestHash) {
        if (isBlank(operationScope) || isBlank(idempotencyKey)) {
            return null;
        }
        String safeScope = trim(operationScope, 100);
        String safeKey = trim(idempotencyKey, 160);
        LocalDateTime now = LocalDateTime.now();
        Optional<FraudIdempotencyRecord> existing = repository.findByIdempotencyKeyAndOperationScope(safeKey, safeScope);
        if (existing.isPresent()) {
            FraudIdempotencyRecord record = existing.get();
            validateRequestHash(record, requestHash);
            if (record.getStatus() == FraudIdempotencyStatus.COMPLETED) {
                return record;
            }
            if (record.getStatus() == FraudIdempotencyStatus.STARTED
                    && record.getLockedUntil() != null
                    && record.getLockedUntil().isAfter(now)) {
                throw new FraudIdempotencyException("A fraud operation with this idempotency key is already in progress.");
            }
            record.setStatus(FraudIdempotencyStatus.STARTED);
            record.setLockedUntil(now.plusMinutes(DEFAULT_LOCK_MINUTES));
            record.setExpiresAt(now.plusHours(DEFAULT_TTL_HOURS));
            record.setResponseJson(null);
            return repository.save(record);
        }

        FraudIdempotencyRecord record = new FraudIdempotencyRecord();
        record.setOperationScope(safeScope);
        record.setIdempotencyKey(safeKey);
        record.setRequestHash(trim(requestHash, 128));
        record.setStatus(FraudIdempotencyStatus.STARTED);
        record.setLockedUntil(now.plusMinutes(DEFAULT_LOCK_MINUTES));
        record.setExpiresAt(now.plusHours(DEFAULT_TTL_HOURS));
        return repository.save(record);
    }

    @Override
    @Transactional
    public void complete(String operationScope, String idempotencyKey, String responseJson) {
        update(operationScope, idempotencyKey, FraudIdempotencyStatus.COMPLETED, responseJson);
    }

    @Override
    @Transactional
    public void fail(String operationScope, String idempotencyKey, String responseJson) {
        update(operationScope, idempotencyKey, FraudIdempotencyStatus.FAILED, responseJson);
    }

    @Override
    public String hashPayload(String payload) {
        return sha256(payload == null ? "" : payload);
    }

    private void update(String operationScope, String idempotencyKey, FraudIdempotencyStatus status, String responseJson) {
        if (isBlank(operationScope) || isBlank(idempotencyKey)) {
            return;
        }
        repository.findByIdempotencyKeyAndOperationScope(idempotencyKey.trim(), operationScope.trim())
                .ifPresent(record -> {
                    record.setStatus(status);
                    record.setLockedUntil(null);
                    record.setResponseJson(responseJson);
                    repository.save(record);
                });
    }

    private void validateRequestHash(FraudIdempotencyRecord record, String requestHash) {
        if (isBlank(record.getRequestHash()) || isBlank(requestHash)) {
            return;
        }
        if (!record.getRequestHash().equals(requestHash)) {
            throw new FraudIdempotencyException("Idempotency key was already used with a different fraud request.");
        }
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
