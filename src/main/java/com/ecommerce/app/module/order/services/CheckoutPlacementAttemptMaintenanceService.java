package com.ecommerce.app.module.order.services;

import com.ecommerce.app.module.order.model.CheckoutPlacementAttemptStatus;
import com.ecommerce.app.module.order.repository.CheckoutPlacementAttemptRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutPlacementAttemptMaintenanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutPlacementAttemptMaintenanceService.class);
    private static final String STALE_REASON = "Recovered by stale checkout-attempt maintenance.";

    private final CheckoutPlacementAttemptRepository repository;
    private final int retentionDays;
    private final int staleGraceMinutes;

    public CheckoutPlacementAttemptMaintenanceService(
            CheckoutPlacementAttemptRepository repository,
            @Value("${checkout.idempotency.retention-days:30}") int retentionDays,
            @Value("${checkout.idempotency.stale-grace-minutes:15}") int staleGraceMinutes
    ) {
        this.repository = repository;
        this.retentionDays = Math.max(retentionDays, 1);
        this.staleGraceMinutes = Math.max(staleGraceMinutes, 5);
    }

    @Scheduled(cron = "${checkout.idempotency.cleanup-cron:0 15 3 * * *}")
    @Transactional
    public void maintain() {
        LocalDateTime now = LocalDateTime.now();
        int recovered = repository.markStaleProcessingAttemptsFailed(
                CheckoutPlacementAttemptStatus.PROCESSING,
                CheckoutPlacementAttemptStatus.FAILED,
                now.minusMinutes(staleGraceMinutes),
                now,
                STALE_REASON
        );
        int deleted = repository.deleteTerminalAttemptsBefore(
                List.of(
                        CheckoutPlacementAttemptStatus.COMPLETED,
                        CheckoutPlacementAttemptStatus.FAILED
                ),
                now.minusDays(retentionDays)
        );
        if (recovered > 0) {
            LOGGER.warn("Recovered {} stale checkout placement attempt(s).", recovered);
        }
        if (deleted > 0) {
            LOGGER.info("Deleted {} checkout placement attempt(s) past retention.", deleted);
        }
    }
}
