package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.model.VelocityCounter;
import com.ecommerce.app.module.fraud.model.VelocityCounterScope;
import com.ecommerce.app.module.fraud.repository.VelocityCounterRepository;
import com.ecommerce.app.module.fraud.services.OrderVelocityService;
import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class DefaultOrderVelocityService implements OrderVelocityService {

    static final int BUCKET_MINUTES = 15;

    private final VelocityCounterRepository repository;
    private final TransactionTemplate requiresNew;

    public DefaultOrderVelocityService(
            VelocityCounterRepository repository,
            PlatformTransactionManager transactionManager
    ) {
        this.repository = repository;
        this.requiresNew = new TransactionTemplate(transactionManager);
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public long count(VelocityCounterScope scope, String value, Duration window) {
        String valueHash = hash(scope, value);
        if (valueHash == null) {
            return 0;
        }
        Duration safeWindow = window == null || window.isNegative() || window.isZero()
                ? Duration.ofMinutes(BUCKET_MINUTES)
                : window;
        LocalDateTime windowStart = LocalDateTime.now().minus(safeWindow);
        Long result = requiresNew.execute(status -> repository.sumCountWithinWindow(scope, valueHash, windowStart));
        return result == null ? 0 : result;
    }

    @Override
    public void increment(VelocityCounterScope scope, String value) {
        String valueHash = hash(scope, value);
        if (valueHash == null) {
            return;
        }
        LocalDateTime windowStart = currentBucketStart(LocalDateTime.now());
        LocalDateTime windowEnd = windowStart.plusMinutes(BUCKET_MINUTES);
        try {
            requiresNew.executeWithoutResult(status -> incrementBucket(
                    scope,
                    valueHash,
                    mask(value),
                    windowStart,
                    windowEnd
            ));
        } catch (DataIntegrityViolationException concurrentInsert) {
            requiresNew.executeWithoutResult(status -> incrementBucket(
                    scope,
                    valueHash,
                    mask(value),
                    windowStart,
                    windowEnd
            ));
        }
    }

    private void incrementBucket(
            VelocityCounterScope scope,
            String valueHash,
            String maskedValue,
            LocalDateTime windowStart,
            LocalDateTime windowEnd
    ) {
        VelocityCounter counter = repository.findBucketForUpdate(scope, valueHash, windowStart, windowEnd)
                .orElseGet(() -> {
                    VelocityCounter created = new VelocityCounter();
                    created.setCounterScope(scope);
                    created.setCounterValueHash(valueHash);
                    created.setMaskedValue(maskedValue);
                    created.setWindowStartAt(windowStart);
                    created.setWindowEndAt(windowEnd);
                    return created;
                });
        counter.setCounterCount(counter.getCounterCount() + 1);
        repository.saveAndFlush(counter);
    }

    static LocalDateTime currentBucketStart(LocalDateTime now) {
        LocalDateTime safeNow = now == null ? LocalDateTime.now() : now;
        int bucketMinute = (safeNow.getMinute() / BUCKET_MINUTES) * BUCKET_MINUTES;
        return safeNow.withMinute(bucketMinute).withSecond(0).withNano(0);
    }

    private String hash(VelocityCounterScope scope, String value) {
        if (scope == null || value == null || value.isBlank()) {
            return null;
        }
        return FraudHashingSupport.sha256(scope.name() + ":" + value);
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String clean = value.trim();
        if (clean.length() <= 4) {
            return "****";
        }
        return "****" + clean.substring(clean.length() - 4);
    }
}
