package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.events.FraudOutboxDispatchEvent;
import com.ecommerce.app.module.fraud.model.FraudOutboxEvent;
import com.ecommerce.app.module.fraud.model.FraudOutboxStatus;
import com.ecommerce.app.module.fraud.repository.FraudOutboxEventRepository;
import com.ecommerce.app.module.fraud.services.FraudOutboxDispatcherService;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultFraudOutboxDispatcherService implements FraudOutboxDispatcherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFraudOutboxDispatcherService.class);
    private static final int MAX_FAILURE_LENGTH = 1000;

    private final FraudOutboxEventRepository repository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public DefaultFraudOutboxDispatcherService(FraudOutboxEventRepository repository,
            ApplicationEventPublisher applicationEventPublisher) {
        this.repository = repository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Scheduled(fixedDelayString = "${fraud.outbox.dispatch.fixed-delay-ms:30000}")
    @Transactional
    public int dispatchDueEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<FraudOutboxEvent> events = repository.findTop50ByStatusInAndNextAttemptAtLessThanEqualOrderByIdAsc(
                List.of(FraudOutboxStatus.PENDING, FraudOutboxStatus.FAILED, FraudOutboxStatus.PROCESSING), now);
        int published = 0;
        for (FraudOutboxEvent event : events) {
            if (dispatchOne(event)) {
                published++;
            }
        }
        return published;
    }

    private boolean dispatchOne(FraudOutboxEvent event) {
        LocalDateTime now = LocalDateTime.now();
        try {
            event.setStatus(FraudOutboxStatus.PROCESSING);
            event.setNextAttemptAt(now.plusMinutes(5));
            repository.saveAndFlush(event);

            applicationEventPublisher.publishEvent(new FraudOutboxDispatchEvent(event));

            event.setStatus(FraudOutboxStatus.PUBLISHED);
            event.setPublishedAt(LocalDateTime.now());
            event.setFailureReason(null);
            event.setNextAttemptAt(null);
            repository.save(event);
            return true;
        } catch (Exception ex) {
            event.setStatus(FraudOutboxStatus.FAILED);
            event.setRetryCount(event.getRetryCount() + 1);
            event.setFailureReason(trimFailure(ex.getMessage()));
            event.setNextAttemptAt(LocalDateTime.now().plusMinutes(retryDelayMinutes(event.getRetryCount())));
            repository.save(event);
            LOGGER.warn("Fraud outbox dispatch failed for eventId={} eventType={} retryCount={}",
                    event.getId(), event.getEventType(), event.getRetryCount(), ex);
            return false;
        }
    }

    private int retryDelayMinutes(int retryCount) {
        return Math.min(60, Math.max(1, retryCount * 5));
    }

    private String trimFailure(String failureReason) {
        if (failureReason == null || failureReason.isBlank()) {
            return "Fraud outbox dispatch failed.";
        }
        String cleaned = failureReason.trim();
        return cleaned.length() <= MAX_FAILURE_LENGTH ? cleaned : cleaned.substring(0, MAX_FAILURE_LENGTH);
    }
}
