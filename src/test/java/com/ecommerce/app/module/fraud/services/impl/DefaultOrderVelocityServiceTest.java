package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.fraud.model.VelocityCounter;
import com.ecommerce.app.module.fraud.model.VelocityCounterScope;
import com.ecommerce.app.module.fraud.repository.VelocityCounterRepository;
import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
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
}
