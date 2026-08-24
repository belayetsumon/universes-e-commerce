package com.ecommerce.app.module.order.services;

import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import com.ecommerce.app.module.order.model.CheckoutPlacementAttempt;
import com.ecommerce.app.module.order.model.CheckoutPlacementAttemptStatus;
import com.ecommerce.app.module.order.repository.CheckoutPlacementAttemptRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class CheckoutPlacementIdempotencyService {

    private static final Duration PROCESSING_LOCK = Duration.ofMinutes(5);

    private final CheckoutPlacementAttemptRepository repository;
    private final TransactionTemplate requiresNew;
    private final TransactionTemplate required;

    public CheckoutPlacementIdempotencyService(
            CheckoutPlacementAttemptRepository repository,
            PlatformTransactionManager transactionManager
    ) {
        this.repository = repository;
        this.requiresNew = new TransactionTemplate(transactionManager);
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.required = new TransactionTemplate(transactionManager);
        this.required.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    }

    public ClaimResult claim(String actorScope, String requestKey, String payload) {
        String actorHash = requireHash(actorScope, "Checkout actor scope is required.");
        String requestHash = requireHash(requestKey, "Checkout request key is required.");
        String payloadHash = requireHash(payload, "Checkout request payload is required.");
        try {
            return requiresNew.execute(status -> claimInTransaction(actorHash, requestHash, payloadHash));
        } catch (DataIntegrityViolationException race) {
            return requiresNew.execute(status -> resolveConcurrentClaim(actorHash, requestHash, payloadHash));
        }
    }

    public ClaimResult findCompleted(String actorScope, String requestKey, String payload) {
        String actorHash = requireHash(actorScope, "Checkout actor scope is required.");
        String requestHash = requireHash(requestKey, "Checkout request key is required.");
        String payloadHash = requireHash(payload, "Checkout request payload is required.");
        return requiresNew.execute(status -> repository.findByActorScopeHashAndRequestKeyHash(actorHash, requestHash)
                .filter(attempt -> attempt.getStatus() == CheckoutPlacementAttemptStatus.COMPLETED
                        || attempt.getOrderGroupUuid() != null)
                .map(attempt -> {
                    requireSamePayload(attempt, payloadHash);
                    return ClaimResult.completed(attempt.getOrderGroupUuid(), safeRedirect(attempt));
                })
                .orElse(null));
    }

    public void linkCompletedOrderGroup(
            String actorScope,
            String requestKey,
            String payload,
            String orderGroupUuid
    ) {
        String actorHash = requireHash(actorScope, "Checkout actor scope is required.");
        String requestHash = requireHash(requestKey, "Checkout request key is required.");
        String payloadHash = requireHash(payload, "Checkout request payload is required.");
        String safeGroupUuid = trim(orderGroupUuid, 36);
        if (safeGroupUuid == null) {
            throw new CheckoutPlacementIdempotencyException("Order group reference is required.");
        }
        required.executeWithoutResult(status -> {
            CheckoutPlacementAttempt attempt = repository.findForUpdate(actorHash, requestHash)
                    .orElseThrow(() -> new CheckoutPlacementIdempotencyException("Checkout attempt claim was not found."));
            requireSamePayload(attempt, payloadHash);
            attempt.setOrderGroupUuid(safeGroupUuid);
            attempt.setRedirectPath(safePlacedRedirect(safeGroupUuid));
            attempt.setStatus(CheckoutPlacementAttemptStatus.COMPLETED);
            attempt.setLockedUntil(null);
            attempt.setFailureReason(null);
            repository.save(attempt);
        });
    }

    public void complete(String actorScope, String requestKey, String payload, String redirectPath) {
        updateOutcome(actorScope, requestKey, payload, CheckoutPlacementAttemptStatus.COMPLETED, redirectPath, null);
    }

    public void fail(String actorScope, String requestKey, String payload, String failureReason) {
        updateOutcome(actorScope, requestKey, payload, CheckoutPlacementAttemptStatus.FAILED, null, failureReason);
    }

    private ClaimResult claimInTransaction(String actorHash, String requestHash, String payloadHash) {
        LocalDateTime now = LocalDateTime.now();
        return repository.findForUpdate(actorHash, requestHash)
                .map(existing -> claimExisting(existing, payloadHash, now))
                .orElseGet(() -> createClaim(actorHash, requestHash, payloadHash, now));
    }

    private ClaimResult resolveConcurrentClaim(String actorHash, String requestHash, String payloadHash) {
        CheckoutPlacementAttempt existing = repository.findForUpdate(actorHash, requestHash)
                .orElseThrow(() -> new CheckoutPlacementIdempotencyException(
                "Checkout request is being claimed concurrently. Please retry."
        ));
        return claimExisting(existing, payloadHash, LocalDateTime.now());
    }

    private ClaimResult claimExisting(
            CheckoutPlacementAttempt attempt,
            String payloadHash,
            LocalDateTime now
    ) {
        requireSamePayload(attempt, payloadHash);
        if (attempt.getStatus() == CheckoutPlacementAttemptStatus.COMPLETED
                || attempt.getOrderGroupUuid() != null) {
            return ClaimResult.completed(attempt.getOrderGroupUuid(), safeRedirect(attempt));
        }
        if (attempt.getStatus() == CheckoutPlacementAttemptStatus.PROCESSING
                && attempt.getLockedUntil() != null
                && attempt.getLockedUntil().isAfter(now)) {
            return ClaimResult.inProgress();
        }

        attempt.setStatus(CheckoutPlacementAttemptStatus.PROCESSING);
        attempt.setLockedUntil(now.plus(PROCESSING_LOCK));
        attempt.setFailureReason(null);
        attempt.setRedirectPath(null);
        repository.save(attempt);
        return ClaimResult.acquired();
    }

    private ClaimResult createClaim(
            String actorHash,
            String requestHash,
            String payloadHash,
            LocalDateTime now
    ) {
        CheckoutPlacementAttempt attempt = new CheckoutPlacementAttempt();
        attempt.setActorScopeHash(actorHash);
        attempt.setRequestKeyHash(requestHash);
        attempt.setPayloadHash(payloadHash);
        attempt.setStatus(CheckoutPlacementAttemptStatus.PROCESSING);
        attempt.setLockedUntil(now.plus(PROCESSING_LOCK));
        repository.saveAndFlush(attempt);
        return ClaimResult.acquired();
    }

    private void updateOutcome(
            String actorScope,
            String requestKey,
            String payload,
            CheckoutPlacementAttemptStatus status,
            String redirectPath,
            String failureReason
    ) {
        String actorHash = requireHash(actorScope, "Checkout actor scope is required.");
        String requestHash = requireHash(requestKey, "Checkout request key is required.");
        String payloadHash = requireHash(payload, "Checkout request payload is required.");
        requiresNew.executeWithoutResult(transaction -> repository.findForUpdate(actorHash, requestHash)
                .ifPresent(attempt -> {
                    if (attempt.getStatus() == CheckoutPlacementAttemptStatus.COMPLETED
                            && status == CheckoutPlacementAttemptStatus.FAILED) {
                        return;
                    }
                    requireSamePayload(attempt, payloadHash);
                    attempt.setStatus(status);
                    attempt.setLockedUntil(null);
                    if (status == CheckoutPlacementAttemptStatus.COMPLETED) {
                        attempt.setRedirectPath(sanitizeRedirect(redirectPath, attempt.getOrderGroupUuid()));
                        attempt.setFailureReason(null);
                    } else {
                        attempt.setFailureReason(trim(failureReason, 500));
                    }
                    repository.save(attempt);
                }));
    }

    private void requireSamePayload(CheckoutPlacementAttempt attempt, String payloadHash) {
        if (attempt.getPayloadHash() != null && !attempt.getPayloadHash().equals(payloadHash)) {
            throw new CheckoutPlacementIdempotencyException(
                    "This checkout request key was already used for different checkout details."
            );
        }
    }

    private String safeRedirect(CheckoutPlacementAttempt attempt) {
        return sanitizeRedirect(attempt.getRedirectPath(), attempt.getOrderGroupUuid());
    }

    private String sanitizeRedirect(String redirectPath, String orderGroupUuid) {
        String clean = trim(redirectPath, 500);
        if (clean != null && (clean.startsWith("redirect:/order/placed")
                || clean.startsWith("redirect:/customerorder/payment/")
                || clean.startsWith("redirect:/customerorder/index")
                || clean.startsWith("redirect:/customer-meritten-emi/"))) {
            return clean;
        }
        return safePlacedRedirect(orderGroupUuid);
    }

    private String safePlacedRedirect(String orderGroupUuid) {
        String safeUuid = trim(orderGroupUuid, 36);
        return safeUuid == null
                ? "redirect:/customerorder/index"
                : "redirect:/order/placed?group=" + safeUuid;
    }

    private String requireHash(String value, String message) {
        String hash = FraudHashingSupport.sha256(value);
        if (hash == null) {
            throw new CheckoutPlacementIdempotencyException(message);
        }
        return hash;
    }

    private String trim(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String clean = value.trim();
        return clean.length() <= maxLength ? clean : clean.substring(0, maxLength);
    }

    public enum ClaimState {
        ACQUIRED,
        COMPLETED,
        IN_PROGRESS
    }

    public record ClaimResult(
            ClaimState state,
            String orderGroupUuid,
            String redirectPath
    ) {

        static ClaimResult acquired() {
            return new ClaimResult(ClaimState.ACQUIRED, null, null);
        }

        static ClaimResult completed(String orderGroupUuid, String redirectPath) {
            return new ClaimResult(ClaimState.COMPLETED, orderGroupUuid, redirectPath);
        }

        static ClaimResult inProgress() {
            return new ClaimResult(ClaimState.IN_PROGRESS, null, null);
        }
    }
}
