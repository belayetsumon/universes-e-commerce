package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.fraud.model.VelocityCounter;
import com.ecommerce.app.module.fraud.model.VelocityCounterScope;
import com.ecommerce.app.module.fraud.repository.VelocityCounterRepository;
import com.ecommerce.app.module.fraud.services.VelocityLimitClaim;
import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

class DefaultOrderVelocityServiceTest {

    private VelocityCounterRepository repository;
    private DefaultOrderVelocityService service;

    @BeforeEach
    void setUp() {
        repository = mock(VelocityCounterRepository.class);
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenAnswer(invocation -> new SimpleTransactionStatus());
        service = new DefaultOrderVelocityService(repository, transactionManager);
    }

    @Test
    void usesDeterministicFifteenMinuteBuckets() {
        assertEquals(
                LocalDateTime.of(2026, 8, 24, 10, 15, 0),
                DefaultOrderVelocityService.currentBucketStart(
                        LocalDateTime.of(2026, 8, 24, 10, 29, 59, 999)
                )
        );
    }

    @Test
    void incrementsExistingBucketUnderWriteLock() {
        VelocityCounter existing = new VelocityCounter();
        existing.setCounterCount(4L);
        when(repository.findBucketForUpdate(any(), any(), any(), any())).thenReturn(Optional.of(existing));
        when(repository.saveAndFlush(existing)).thenReturn(existing);

        service.increment(VelocityCounterScope.CUSTOMER, "42");

        assertEquals(5L, existing.getCounterCount());
        verify(repository).saveAndFlush(existing);
    }

    @Test
    void retriesAfterConcurrentUniqueBucketInsert() {
        VelocityCounter concurrentlyInserted = new VelocityCounter();
        concurrentlyInserted.setCounterCount(1L);
        when(repository.findBucketForUpdate(any(), any(), any(), any()))
                .thenReturn(Optional.empty(), Optional.of(concurrentlyInserted));
        when(repository.saveAndFlush(any(VelocityCounter.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate bucket"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.increment(VelocityCounterScope.DEVICE, "device-hash");

        assertEquals(2L, concurrentlyInserted.getCounterCount());
    }

    @Test
    void countSumsCounterValuesRatherThanCountingRows() {
        String expectedHash = FraudHashingSupport.sha256("CUSTOMER:42");
        when(repository.sumCountWithinWindow(
                eq(VelocityCounterScope.CUSTOMER),
                eq(expectedHash),
                any(LocalDateTime.class)
        )).thenReturn(7L);

        long result = service.count(VelocityCounterScope.CUSTOMER, "42", Duration.ofMinutes(15));

        assertEquals(7L, result);
    }

    @Test
    void mobileVelocityCountIncludesCanonicalAndLegacyFormatBuckets() {
        String canonicalHash = FraudHashingSupport.sha256("MOBILE_NUMBER:8801712345678");
        String legacyLocalHash = FraudHashingSupport.sha256("MOBILE_NUMBER:01712345678");
        when(repository.sumCountWithinWindow(
                eq(VelocityCounterScope.MOBILE_NUMBER),
                eq(canonicalHash),
                any(LocalDateTime.class)
        )).thenReturn(2L);
        when(repository.sumCountWithinWindow(
                eq(VelocityCounterScope.MOBILE_NUMBER),
                eq(legacyLocalHash),
                any(LocalDateTime.class)
        )).thenReturn(3L);

        long result = service.count(
                VelocityCounterScope.MOBILE_NUMBER,
                "+8801712345678",
                Duration.ofHours(1)
        );

        assertEquals(5L, result);
    }

    @Test
    void atomicClaimStopsAtLimitAndCountsSameSecondEventsInOneBucket() {
        VelocityCounter mutex = new VelocityCounter();
        AtomicLong recorded = new AtomicLong();
        VelocityCounter[] eventBucket = new VelocityCounter[1];
        when(repository.findBucketForUpdate(any(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    VelocityCounterScope scope = invocation.getArgument(0);
                    if (scope == VelocityCounterScope.OTP_CLAIM_MUTEX) {
                        return Optional.of(mutex);
                    }
                    return Optional.ofNullable(eventBucket[0]);
                });
        when(repository.sumCountWithinWindow(any(), any(), any()))
                .thenAnswer(invocation -> recorded.get());
        when(repository.saveAndFlush(any(VelocityCounter.class))).thenAnswer(invocation -> {
            VelocityCounter saved = invocation.getArgument(0);
            if (saved.getCounterScope() != VelocityCounterScope.OTP_CLAIM_MUTEX) {
                eventBucket[0] = saved;
                recorded.set(saved.getCounterCount());
            }
            return saved;
        });

        boolean first = service.claimWithinLimit(
                VelocityCounterScope.OTP_GUEST_MOBILE_DAILY,
                "8801712345678",
                2,
                Duration.ofDays(1),
                0L
        );
        boolean second = service.claimWithinLimit(
                VelocityCounterScope.OTP_GUEST_MOBILE_DAILY,
                "8801712345678",
                2,
                Duration.ofDays(1),
                0L
        );
        boolean third = service.claimWithinLimit(
                VelocityCounterScope.OTP_GUEST_MOBILE_DAILY,
                "8801712345678",
                2,
                Duration.ofDays(1),
                0L
        );

        assertTrue(first);
        assertTrue(second);
        assertFalse(third);
        assertEquals(2L, eventBucket[0].getCounterCount());
    }

    @Test
    void atomicBatchRejectsEveryDimensionWithoutPartialCounterMutation() {
        VelocityCounter mutex = new VelocityCounter();
        when(repository.findBucketForUpdate(any(), any(), any(), any()))
                .thenReturn(Optional.of(mutex));
        when(repository.sumCountWithinWindow(
                eq(VelocityCounterScope.OTP_GUEST_MOBILE_DAILY), any(), any()))
                .thenReturn(0L);
        when(repository.sumCountWithinWindow(
                eq(VelocityCounterScope.OTP_IP_DAILY), any(), any()))
                .thenReturn(20L);

        boolean claimed = service.claimAllWithinLimits(List.of(
                new VelocityLimitClaim(
                        VelocityCounterScope.OTP_GUEST_MOBILE_DAILY,
                        "8801712345678",
                        5,
                        Duration.ofDays(1),
                        0L),
                new VelocityLimitClaim(
                        VelocityCounterScope.OTP_IP_DAILY,
                        "ip-hash",
                        20,
                        Duration.ofDays(1),
                        0L)));

        assertFalse(claimed);
        verify(repository, never()).saveAndFlush(any(VelocityCounter.class));
    }

    @Test
    void atomicClaimSeedsHistoricalBaselineBeforeAddingNewAttempt() {
        VelocityCounter mutex = new VelocityCounter();
        when(repository.findBucketForUpdate(any(), any(), any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0) == VelocityCounterScope.OTP_CLAIM_MUTEX
                        ? Optional.of(mutex)
                        : Optional.empty());
        when(repository.sumCountWithinWindow(any(), any(), any())).thenReturn(0L);
        when(repository.saveAndFlush(any(VelocityCounter.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean claimed = service.claimWithinLimit(
                VelocityCounterScope.OTP_SESSION_DAILY,
                "session-1",
                3,
                Duration.ofDays(1),
                1L);

        assertTrue(claimed);
        org.mockito.ArgumentCaptor<VelocityCounter> captor =
                org.mockito.ArgumentCaptor.forClass(VelocityCounter.class);
        verify(repository).saveAndFlush(captor.capture());
        assertEquals(2L, captor.getValue().getCounterCount());
        assertEquals(
                captor.getValue().getWindowStartAt().plusSeconds(1),
                captor.getValue().getWindowEndAt());
    }
}
