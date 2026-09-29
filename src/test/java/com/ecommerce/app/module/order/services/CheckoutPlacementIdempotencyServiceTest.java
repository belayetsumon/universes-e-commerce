package com.ecommerce.app.module.order.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import com.ecommerce.app.module.order.model.CheckoutPlacementAttempt;
import com.ecommerce.app.module.order.model.CheckoutPlacementAttemptStatus;
import com.ecommerce.app.module.order.repository.CheckoutPlacementAttemptRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

class CheckoutPlacementIdempotencyServiceTest {

    private static final String ACTOR = "CUSTOMER:17";
    private static final String REQUEST_ID = "8be483e3-af51-4aef-98fa-b05092a54a67";
    private static final String PAYLOAD = "savebyvendor|cart-hash|FULL_COD";

    private CheckoutPlacementAttemptRepository repository;
    private CheckoutPlacementIdempotencyService service;

    @BeforeEach
    void setUp() {
        repository = mock(CheckoutPlacementAttemptRepository.class);
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenAnswer(invocation -> new SimpleTransactionStatus());
        service = new CheckoutPlacementIdempotencyService(repository, transactionManager);
    }

    @Test
    void newClaimPersistsOnlyHashesAndAcquiresProcessingLock() {
        when(repository.findForUpdate(anyString(), anyString())).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(CheckoutPlacementAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutPlacementIdempotencyService.ClaimResult result = service.claim(ACTOR, REQUEST_ID, PAYLOAD);

        assertEquals(CheckoutPlacementIdempotencyService.ClaimState.ACQUIRED, result.state());
        ArgumentCaptor<CheckoutPlacementAttempt> captor = ArgumentCaptor.forClass(CheckoutPlacementAttempt.class);
        verify(repository).saveAndFlush(captor.capture());
        CheckoutPlacementAttempt attempt = captor.getValue();
        assertNotEquals(ACTOR, attempt.getActorScopeHash());
        assertNotEquals(REQUEST_ID, attempt.getRequestKeyHash());
        assertNotEquals(PAYLOAD, attempt.getPayloadHash());
        assertEquals(CheckoutPlacementAttemptStatus.PROCESSING, attempt.getStatus());
        assertTrue(attempt.getLockedUntil().isAfter(LocalDateTime.now()));
    }

    @Test
    void activeSamePayloadClaimReturnsInProgress() {
        CheckoutPlacementAttempt attempt = attempt(CheckoutPlacementAttemptStatus.PROCESSING, PAYLOAD);
        attempt.setLockedUntil(LocalDateTime.now().plusMinutes(1));
        when(repository.findForUpdate(anyString(), anyString())).thenReturn(Optional.of(attempt));

        CheckoutPlacementIdempotencyService.ClaimResult result = service.claim(ACTOR, REQUEST_ID, PAYLOAD);

        assertEquals(CheckoutPlacementIdempotencyService.ClaimState.IN_PROGRESS, result.state());
    }

    @Test
    void completedReplayRequiresTheOriginalPayload() {
        CheckoutPlacementAttempt attempt = attempt(CheckoutPlacementAttemptStatus.COMPLETED, PAYLOAD);
        attempt.setOrderGroupUuid("986ef48e-9e3d-4a4d-b8b9-147daedf1374");
        attempt.setRedirectPath("redirect:/order/placed?group=986ef48e-9e3d-4a4d-b8b9-147daedf1374");
        when(repository.findByActorScopeHashAndRequestKeyHash(anyString(), anyString()))
                .thenReturn(Optional.of(attempt));

        CheckoutPlacementIdempotencyService.ClaimResult replay = service.findCompleted(
                ACTOR,
                REQUEST_ID,
                PAYLOAD
        );

        assertEquals(CheckoutPlacementIdempotencyService.ClaimState.COMPLETED, replay.state());
        assertThrows(
                CheckoutPlacementIdempotencyException.class,
                () -> service.findCompleted(ACTOR, REQUEST_ID, PAYLOAD + "|CHANGED")
        );
    }

    @Test
    void failedAttemptCanOnlyBeReacquiredWithTheSamePayload() {
        CheckoutPlacementAttempt attempt = attempt(CheckoutPlacementAttemptStatus.FAILED, PAYLOAD);
        when(repository.findForUpdate(anyString(), anyString())).thenReturn(Optional.of(attempt));

        CheckoutPlacementIdempotencyService.ClaimResult retry = service.claim(ACTOR, REQUEST_ID, PAYLOAD);

        assertEquals(CheckoutPlacementIdempotencyService.ClaimState.ACQUIRED, retry.state());
        assertEquals(CheckoutPlacementAttemptStatus.PROCESSING, attempt.getStatus());
        assertThrows(
                CheckoutPlacementIdempotencyException.class,
                () -> service.claim(ACTOR, REQUEST_ID, PAYLOAD + "|CHANGED")
        );
    }

    private CheckoutPlacementAttempt attempt(CheckoutPlacementAttemptStatus status, String payload) {
        CheckoutPlacementAttempt attempt = new CheckoutPlacementAttempt();
        attempt.setActorScopeHash(FraudHashingSupport.sha256Exact(ACTOR));
        attempt.setRequestKeyHash(FraudHashingSupport.sha256Exact(REQUEST_ID));
        attempt.setPayloadHash(FraudHashingSupport.sha256Exact(payload));
        attempt.setStatus(status);
        return attempt;
    }
}
