package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.fraud.model.FraudIdempotencyRecord;
import com.ecommerce.app.module.fraud.model.FraudIdempotencyStatus;
import com.ecommerce.app.module.fraud.repository.FraudIdempotencyRecordRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DefaultFraudIdempotencyServiceTest {

    @Test
    void expiredCompletedResultIsNotReplayed() {
        FraudIdempotencyRecordRepository repository = mock(FraudIdempotencyRecordRepository.class);
        FraudIdempotencyRecord record = new FraudIdempotencyRecord();
        record.setStatus(FraudIdempotencyStatus.COMPLETED);
        record.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(repository.findByIdempotencyKeyAndOperationScope("key", "scope"))
                .thenReturn(Optional.of(record));

        assertTrue(new DefaultFraudIdempotencyService(repository)
                .findCompleted("scope", "key").isEmpty());
    }

    @Test
    void expiredKeyCanStartWithANewRequestHash() {
        FraudIdempotencyRecordRepository repository = mock(FraudIdempotencyRecordRepository.class);
        FraudIdempotencyRecord record = new FraudIdempotencyRecord();
        record.setStatus(FraudIdempotencyStatus.COMPLETED);
        record.setRequestHash("old-hash");
        record.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(repository.findByIdempotencyKeyAndOperationScope("key", "scope"))
                .thenReturn(Optional.of(record));
        when(repository.save(record)).thenReturn(record);

        FraudIdempotencyRecord restarted = new DefaultFraudIdempotencyService(repository)
                .start("scope", "key", "new-hash");

        assertEquals(FraudIdempotencyStatus.STARTED, restarted.getStatus());
        assertEquals("new-hash", restarted.getRequestHash());
    }
}
