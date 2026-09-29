package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.checkout.guest.model.OtpStatus;
import com.ecommerce.app.module.checkout.guest.repository.OtpVerificationRepository;
import com.ecommerce.app.module.communication.repository.MessageJobRepository;
import com.ecommerce.app.module.communication.repository.MessageLogRepository;
import com.ecommerce.app.module.fraud.model.FraudOutboxStatus;
import com.ecommerce.app.module.fraud.repository.FraudIdempotencyRecordRepository;
import com.ecommerce.app.module.fraud.repository.FraudOutboxEventRepository;
import com.ecommerce.app.module.fraud.repository.VelocityCounterRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

class FraudTransientDataRetentionServiceTest {

    @Test
    void retentionUsesBoundedConditionalBatchesAndConfiguredCutoffs() {
        OtpVerificationRepository otp = mock(OtpVerificationRepository.class);
        MessageJobRepository messageJobs = mock(MessageJobRepository.class);
        MessageLogRepository messageLogs = mock(MessageLogRepository.class);
        VelocityCounterRepository velocity = mock(VelocityCounterRepository.class);
        FraudIdempotencyRecordRepository idempotency = mock(FraudIdempotencyRecordRepository.class);
        FraudOutboxEventRepository outbox = mock(FraudOutboxEventRepository.class);
        LocalDateTime now = LocalDateTime.of(2026, 8, 24, 3, 45);
        LocalDateTime otpCutoff = now.minusDays(30);
        LocalDateTime velocityCutoff = now.minusDays(7);
        LocalDateTime outboxCutoff = now.minusDays(30);

        when(otp.findTerminalRetentionCandidateIds(any(), eq(otpCutoff), any(Pageable.class)))
                .thenReturn(List.of(1L, 2L), List.of());
        when(otp.deleteTerminalRetentionCandidates(
                eq(List.of(1L, 2L)), any(Collection.class), eq(otpCutoff))).thenReturn(2);
        when(otp.findExpiredPendingRetentionCandidateIds(
                eq(OtpStatus.PENDING), eq(now), eq(otpCutoff), any(Pageable.class))).thenReturn(List.of());
        when(messageJobs.findCodOtpRetentionCandidateIds(any(Collection.class), any(Pageable.class)))
                .thenReturn(List.of(6L), List.of());
        when(messageJobs.deleteCodOtpRetentionCandidates(
                eq(List.of(6L)), any(Collection.class))).thenReturn(1);
        when(messageLogs.findCodOtpRetentionCandidateIds(
                any(Collection.class), eq(otpCutoff), any(Pageable.class)))
                .thenReturn(List.of(7L, 8L), List.of());
        when(messageLogs.deleteCodOtpRetentionCandidates(
                eq(List.of(7L, 8L)), any(Collection.class), eq(otpCutoff))).thenReturn(2);
        when(velocity.findRetentionCandidateIds(eq(velocityCutoff), any(Pageable.class)))
                .thenReturn(List.of(3L), List.of());
        when(velocity.deleteRetentionCandidates(List.of(3L), velocityCutoff)).thenReturn(1);
        when(idempotency.findExpiredRetentionCandidateIds(eq(now), any(Pageable.class)))
                .thenReturn(List.of(4L), List.of());
        when(idempotency.deleteExpiredRetentionCandidates(List.of(4L), now)).thenReturn(1);
        when(outbox.findPublishedRetentionCandidateIds(
                eq(FraudOutboxStatus.PUBLISHED), eq(outboxCutoff), any(Pageable.class)))
                .thenReturn(List.of(5L), List.of());
        when(outbox.deletePublishedRetentionCandidates(
                List.of(5L), FraudOutboxStatus.PUBLISHED, outboxCutoff)).thenReturn(1);
        FraudTransientDataRetentionService service = new FraudTransientDataRetentionService(
                otp, messageJobs, messageLogs, velocity, idempotency, outbox, 30, 7, 30, 500, 20);

        FraudTransientDataRetentionService.RetentionResult result = service.runRetentionAt(now);

        assertEquals(2, result.otpDeleted());
        assertEquals(1, result.otpMessageJobsDeleted());
        assertEquals(2, result.otpMessageLogsDeleted());
        assertEquals(1, result.velocityDeleted());
        assertEquals(1, result.idempotencyDeleted());
        assertEquals(1, result.publishedOutboxDeleted());
        assertEquals(8, result.totalDeleted());
    }
}
