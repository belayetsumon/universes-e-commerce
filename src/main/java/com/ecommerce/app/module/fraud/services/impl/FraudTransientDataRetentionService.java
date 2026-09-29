package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.checkout.guest.model.OtpStatus;
import com.ecommerce.app.module.checkout.guest.repository.OtpVerificationRepository;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.communication.repository.MessageJobRepository;
import com.ecommerce.app.module.communication.repository.MessageLogRepository;
import com.ecommerce.app.module.fraud.model.FraudOutboxStatus;
import com.ecommerce.app.module.fraud.repository.FraudIdempotencyRecordRepository;
import com.ecommerce.app.module.fraud.repository.FraudOutboxEventRepository;
import com.ecommerce.app.module.fraud.repository.VelocityCounterRepository;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** Bounded, race-safe cleanup for transient OTP and fraud operational data. */
@Service
public class FraudTransientDataRetentionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FraudTransientDataRetentionService.class);
    private static final EnumSet<OtpStatus> TERMINAL_OTP_STATUSES = EnumSet.of(
            OtpStatus.VERIFIED,
            OtpStatus.USED,
            OtpStatus.EXPIRED,
            OtpStatus.FAILED,
            OtpStatus.BLOCKED
    );
    private static final EnumSet<MessageEventType> COD_OTP_MESSAGE_EVENTS = EnumSet.of(
            MessageEventType.GUEST_CHECKOUT_OTP,
            MessageEventType.CUSTOMER_COD_OTP
    );

    private final OtpVerificationRepository otpRepository;
    private final MessageJobRepository messageJobRepository;
    private final MessageLogRepository messageLogRepository;
    private final VelocityCounterRepository velocityRepository;
    private final FraudIdempotencyRecordRepository idempotencyRepository;
    private final FraudOutboxEventRepository outboxRepository;
    private final int otpRetentionDays;
    private final int velocityRetentionDays;
    private final int outboxPublishedRetentionDays;
    private final int batchSize;
    private final int maxBatches;

    public FraudTransientDataRetentionService(
            OtpVerificationRepository otpRepository,
            MessageJobRepository messageJobRepository,
            MessageLogRepository messageLogRepository,
            VelocityCounterRepository velocityRepository,
            FraudIdempotencyRecordRepository idempotencyRepository,
            FraudOutboxEventRepository outboxRepository,
            @Value("${checkout.otp.retention-days:30}") int otpRetentionDays,
            @Value("${fraud.retention.velocity-days:7}") int velocityRetentionDays,
            @Value("${fraud.retention.outbox-published-days:30}") int outboxPublishedRetentionDays,
            @Value("${fraud.retention.batch-size:500}") int batchSize,
            @Value("${fraud.retention.max-batches-per-run:20}") int maxBatches
    ) {
        this.otpRepository = otpRepository;
        this.messageJobRepository = messageJobRepository;
        this.messageLogRepository = messageLogRepository;
        this.velocityRepository = velocityRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.outboxRepository = outboxRepository;
        this.otpRetentionDays = Math.max(otpRetentionDays, 2);
        this.velocityRetentionDays = Math.max(velocityRetentionDays, 1);
        this.outboxPublishedRetentionDays = Math.max(outboxPublishedRetentionDays, 1);
        this.batchSize = Math.min(Math.max(batchSize, 50), 1000);
        this.maxBatches = Math.min(Math.max(maxBatches, 1), 100);
    }

    @Scheduled(cron = "${fraud.retention.cleanup-cron:0 45 3 * * *}")
    public void runRetention() {
        RetentionResult result = runRetentionAt(LocalDateTime.now());
        if (result.totalDeleted() > 0) {
            LOGGER.info(
                    "Transient security retention deleted otp={}, otpMessageJobs={}, otpMessageLogs={}, velocity={}, idempotency={}, publishedOutbox={} rows.",
                    result.otpDeleted(),
                    result.otpMessageJobsDeleted(),
                    result.otpMessageLogsDeleted(),
                    result.velocityDeleted(),
                    result.idempotencyDeleted(),
                    result.publishedOutboxDeleted()
            );
        }
    }

    RetentionResult runRetentionAt(LocalDateTime now) {
        LocalDateTime safeNow = now == null ? LocalDateTime.now() : now;
        Pageable page = PageRequest.of(0, batchSize);
        int otpDeleted = purgeTerminalOtp(safeNow.minusDays(otpRetentionDays), page);
        otpDeleted += purgeExpiredPendingOtp(safeNow, safeNow.minusDays(otpRetentionDays), page);
        int otpMessageJobsDeleted = purgeCodOtpMessageJobs(page);
        int otpMessageLogsDeleted = purgeCodOtpMessageLogs(safeNow.minusDays(otpRetentionDays), page);
        int velocityDeleted = purgeVelocity(safeNow.minusDays(velocityRetentionDays), page);
        int idempotencyDeleted = purgeIdempotency(safeNow, page);
        int outboxDeleted = purgePublishedOutbox(safeNow.minusDays(outboxPublishedRetentionDays), page);
        return new RetentionResult(
                otpDeleted,
                otpMessageJobsDeleted,
                otpMessageLogsDeleted,
                velocityDeleted,
                idempotencyDeleted,
                outboxDeleted
        );
    }

    private int purgeTerminalOtp(LocalDateTime cutoff, Pageable page) {
        int deleted = 0;
        for (int batch = 0; batch < maxBatches; batch++) {
            List<Long> ids = otpRepository.findTerminalRetentionCandidateIds(TERMINAL_OTP_STATUSES, cutoff, page);
            if (ids.isEmpty()) {
                break;
            }
            deleted += otpRepository.deleteTerminalRetentionCandidates(ids, TERMINAL_OTP_STATUSES, cutoff);
        }
        return deleted;
    }

    private int purgeExpiredPendingOtp(LocalDateTime now, LocalDateTime cutoff, Pageable page) {
        int deleted = 0;
        for (int batch = 0; batch < maxBatches; batch++) {
            List<Long> ids = otpRepository.findExpiredPendingRetentionCandidateIds(
                    OtpStatus.PENDING, now, cutoff, page);
            if (ids.isEmpty()) {
                break;
            }
            deleted += otpRepository.deleteExpiredPendingRetentionCandidates(
                    ids, OtpStatus.PENDING, now, cutoff);
        }
        return deleted;
    }

    private int purgeCodOtpMessageJobs(Pageable page) {
        int deleted = 0;
        for (int batch = 0; batch < maxBatches; batch++) {
            List<Long> ids = messageJobRepository.findCodOtpRetentionCandidateIds(
                    COD_OTP_MESSAGE_EVENTS, page);
            if (ids.isEmpty()) {
                break;
            }
            deleted += messageJobRepository.deleteCodOtpRetentionCandidates(
                    ids, COD_OTP_MESSAGE_EVENTS);
        }
        return deleted;
    }

    private int purgeCodOtpMessageLogs(LocalDateTime cutoff, Pageable page) {
        int deleted = 0;
        for (int batch = 0; batch < maxBatches; batch++) {
            List<Long> ids = messageLogRepository.findCodOtpRetentionCandidateIds(
                    COD_OTP_MESSAGE_EVENTS, cutoff, page);
            if (ids.isEmpty()) {
                break;
            }
            deleted += messageLogRepository.deleteCodOtpRetentionCandidates(
                    ids, COD_OTP_MESSAGE_EVENTS, cutoff);
        }
        return deleted;
    }

    private int purgeVelocity(LocalDateTime cutoff, Pageable page) {
        int deleted = 0;
        for (int batch = 0; batch < maxBatches; batch++) {
            List<Long> ids = velocityRepository.findRetentionCandidateIds(cutoff, page);
            if (ids.isEmpty()) {
                break;
            }
            deleted += velocityRepository.deleteRetentionCandidates(ids, cutoff);
        }
        return deleted;
    }

    private int purgeIdempotency(LocalDateTime now, Pageable page) {
        int deleted = 0;
        for (int batch = 0; batch < maxBatches; batch++) {
            List<Long> ids = idempotencyRepository.findExpiredRetentionCandidateIds(now, page);
            if (ids.isEmpty()) {
                break;
            }
            deleted += idempotencyRepository.deleteExpiredRetentionCandidates(ids, now);
        }
        return deleted;
    }

    private int purgePublishedOutbox(LocalDateTime cutoff, Pageable page) {
        int deleted = 0;
        for (int batch = 0; batch < maxBatches; batch++) {
            List<Long> ids = outboxRepository.findPublishedRetentionCandidateIds(
                    FraudOutboxStatus.PUBLISHED, cutoff, page);
            if (ids.isEmpty()) {
                break;
            }
            deleted += outboxRepository.deletePublishedRetentionCandidates(
                    ids, FraudOutboxStatus.PUBLISHED, cutoff);
        }
        return deleted;
    }

    record RetentionResult(
            int otpDeleted,
            int otpMessageJobsDeleted,
            int otpMessageLogsDeleted,
            int velocityDeleted,
            int idempotencyDeleted,
            int publishedOutboxDeleted
    ) {
        int totalDeleted() {
            return otpDeleted + otpMessageJobsDeleted + otpMessageLogsDeleted
                    + velocityDeleted + idempotencyDeleted + publishedOutboxDeleted;
        }
    }
}
