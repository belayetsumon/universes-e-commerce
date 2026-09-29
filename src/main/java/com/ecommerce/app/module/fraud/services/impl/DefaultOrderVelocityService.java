package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.model.VelocityCounter;
import com.ecommerce.app.module.fraud.model.VelocityCounterScope;
import com.ecommerce.app.module.fraud.repository.VelocityCounterRepository;
import com.ecommerce.app.module.fraud.services.OrderVelocityService;
import com.ecommerce.app.module.fraud.services.VelocityLimitClaim;
import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class DefaultOrderVelocityService implements OrderVelocityService {

    static final int BUCKET_MINUTES = 15;
    private static final int CLAIM_RETRIES = 3;
    private static final LocalDateTime CLAIM_MUTEX_START = LocalDateTime.of(2000, 1, 1, 0, 0);
    private static final LocalDateTime CLAIM_MUTEX_END = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
    private static final String CLAIM_MUTEX_HASH = FraudHashingSupport.sha256("OTP_CLAIM_MUTEX:GLOBAL");

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
        List<String> valueHashes = hashes(scope, value);
        if (valueHashes.isEmpty()) {
            return 0;
        }
        Duration safeWindow = window == null || window.isNegative() || window.isZero()
                ? Duration.ofMinutes(BUCKET_MINUTES)
                : window;
        LocalDateTime windowStart = LocalDateTime.now().minus(safeWindow);
        Long result = requiresNew.execute(status -> valueHashes.stream()
                .mapToLong(valueHash -> repository.sumCountWithinWindow(scope, valueHash, windowStart))
                .sum());
        return result == null ? 0 : result;
    }

    @Override
    public void increment(VelocityCounterScope scope, String value) {
        List<String> valueHashes = hashes(scope, value);
        if (valueHashes.isEmpty()) {
            return;
        }
        String valueHash = valueHashes.get(0);
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

    @Override
    public boolean claimWithinLimit(
            VelocityCounterScope scope,
            String value,
            int limit,
            Duration window,
            long baselineCount) {
        return claimAllWithinLimits(List.of(
                new VelocityLimitClaim(scope, value, limit, window, baselineCount)));
    }

    @Override
    public boolean claimAllWithinLimits(List<VelocityLimitClaim> claims) {
        List<PreparedClaim> prepared = prepareClaims(claims);
        if (prepared.isEmpty()) {
            return false;
        }
        ensureClaimMutex();
        for (int attempt = 0; attempt < CLAIM_RETRIES; attempt++) {
            try {
                Boolean claimed = requiresNew.execute(status -> claimBatch(prepared, LocalDateTime.now()));
                return Boolean.TRUE.equals(claimed);
            } catch (DataIntegrityViolationException concurrentInsert) {
                if (attempt == CLAIM_RETRIES - 1) {
                    throw concurrentInsert;
                }
            }
        }
        return false;
    }

    private List<PreparedClaim> prepareClaims(List<VelocityLimitClaim> claims) {
        if (claims == null || claims.isEmpty()) {
            return List.of();
        }
        List<PreparedClaim> prepared = new ArrayList<>();
        Set<String> lockKeys = new HashSet<>();
        for (VelocityLimitClaim claim : claims) {
            if (claim == null || claim.scope() == null || claim.limit() <= 0) {
                return List.of();
            }
            List<String> valueHashes = hashes(claim.scope(), claim.value());
            if (valueHashes.isEmpty()) {
                return List.of();
            }
            Duration window = claim.window() == null || claim.window().isZero() || claim.window().isNegative()
                    ? Duration.ofDays(1)
                    : claim.window();
            PreparedClaim item = new PreparedClaim(
                    claim.scope(),
                    claim.value(),
                    valueHashes.get(0),
                    claim.limit(),
                    window,
                    Math.max(claim.baselineCount(), 0L));
            if (!lockKeys.add(item.lockKey())) {
                throw new IllegalArgumentException("Duplicate velocity claim dimension.");
            }
            prepared.add(item);
        }
        prepared.sort(Comparator.comparing(PreparedClaim::lockKey));
        return prepared;
    }

    private void ensureClaimMutex() {
        for (int attempt = 0; attempt < CLAIM_RETRIES; attempt++) {
            try {
                requiresNew.executeWithoutResult(status -> {
                    if (repository.findBucketForUpdate(
                            VelocityCounterScope.OTP_CLAIM_MUTEX,
                            CLAIM_MUTEX_HASH,
                            CLAIM_MUTEX_START,
                            CLAIM_MUTEX_END).isPresent()) {
                        return;
                    }
                    VelocityCounter mutex = new VelocityCounter();
                    mutex.setCounterScope(VelocityCounterScope.OTP_CLAIM_MUTEX);
                    mutex.setCounterValueHash(CLAIM_MUTEX_HASH);
                    mutex.setMaskedValue(null);
                    mutex.setCounterCount(0L);
                    mutex.setWindowStartAt(CLAIM_MUTEX_START);
                    mutex.setWindowEndAt(CLAIM_MUTEX_END);
                    repository.saveAndFlush(mutex);
                });
                return;
            } catch (DataIntegrityViolationException concurrentInsert) {
                if (attempt == CLAIM_RETRIES - 1) {
                    throw concurrentInsert;
                }
            }
        }
    }

    private boolean claimBatch(List<PreparedClaim> claims, LocalDateTime now) {
        repository.findBucketForUpdate(
                VelocityCounterScope.OTP_CLAIM_MUTEX,
                CLAIM_MUTEX_HASH,
                CLAIM_MUTEX_START,
                CLAIM_MUTEX_END)
                .orElseThrow(() -> new IllegalStateException("Velocity claim mutex is unavailable."));

        List<ClaimEvaluation> evaluations = new ArrayList<>();
        for (PreparedClaim claim : claims) {
            long recordedCount = repository.sumCountWithinWindow(
                    claim.scope(), claim.valueHash(), now.minus(claim.window()));
            long effectiveCount = Math.max(recordedCount, claim.baselineCount());
            if (effectiveCount >= claim.limit()) {
                return false;
            }
            evaluations.add(new ClaimEvaluation(
                    claim,
                    effectiveCount - recordedCount + 1L));
        }

        LocalDateTime eventBucketStart = now.withNano(0);
        LocalDateTime eventBucketEnd = eventBucketStart.plusSeconds(1);
        for (ClaimEvaluation evaluation : evaluations) {
            VelocityCounter eventBucket = repository.findBucketForUpdate(
                    evaluation.claim().scope(),
                    evaluation.claim().valueHash(),
                    eventBucketStart,
                    eventBucketEnd)
                    .orElseGet(() -> counter(
                            evaluation.claim(), 0L, eventBucketStart, eventBucketEnd));
            eventBucket.setCounterCount(eventBucket.getCounterCount() + evaluation.incrementBy());
            repository.saveAndFlush(eventBucket);
        }
        return true;
    }

    private VelocityCounter counter(
            PreparedClaim claim,
            long count,
            LocalDateTime windowStart,
            LocalDateTime windowEnd) {
        VelocityCounter counter = new VelocityCounter();
        counter.setCounterScope(claim.scope());
        counter.setCounterValueHash(claim.valueHash());
        counter.setMaskedValue(mask(claim.value()));
        counter.setWindowStartAt(windowStart);
        counter.setWindowEndAt(windowEnd);
        counter.setCounterCount(count);
        return counter;
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
        return bucketStart(now, BUCKET_MINUTES);
    }

    private static LocalDateTime bucketStart(LocalDateTime now, int bucketMinutes) {
        LocalDateTime safeNow = now == null ? LocalDateTime.now() : now;
        int safeBucketMinutes = Math.min(Math.max(bucketMinutes, 1), 60);
        int bucketMinute = (safeNow.getMinute() / safeBucketMinutes) * safeBucketMinutes;
        return safeNow.withMinute(bucketMinute).withSecond(0).withNano(0);
    }

    private List<String> hashes(VelocityCounterScope scope, String value) {
        if (scope == null || value == null || value.isBlank()) {
            return List.of();
        }
        List<String> values = scope == VelocityCounterScope.MOBILE_NUMBER
                ? FraudHashingSupport.bangladeshMobileValueCandidates(value)
                : List.of(value);
        return values.stream()
                .map(candidate -> FraudHashingSupport.sha256(scope.name() + ":" + candidate))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
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

    private record PreparedClaim(
            VelocityCounterScope scope,
            String value,
            String valueHash,
            int limit,
            Duration window,
            long baselineCount) {

        String lockKey() {
            return scope.name() + ":" + valueHash;
        }
    }

    private record ClaimEvaluation(PreparedClaim claim, long incrementBy) {
    }
}
