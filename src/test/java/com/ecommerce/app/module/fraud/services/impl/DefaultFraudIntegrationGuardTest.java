package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.fraud.dto.FraudAssessmentResponse;
import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.fraud.model.FraudAssessment;
import com.ecommerce.app.module.fraud.model.FraudAssessmentStatus;
import com.ecommerce.app.module.fraud.model.FraudBlockType;
import com.ecommerce.app.module.fraud.model.FraudDecision;
import com.ecommerce.app.module.fraud.repository.FraudAssessmentRepository;
import com.ecommerce.app.module.fraud.repository.FraudBlocklistRepository;
import com.ecommerce.app.module.fraud.services.FraudAssessmentService;
import com.ecommerce.app.module.fraud.services.FraudCaseService;
import com.ecommerce.app.module.fraud.services.FraudModuleControlService;
import com.ecommerce.app.module.fraud.model.FraudModuleMode;
import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import com.ecommerce.app.module.order.model.CustomerOrderGroup;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultFraudIntegrationGuardTest {

    private FraudAssessmentRepository assessmentRepository;
    private FraudBlocklistRepository blocklistRepository;
    private FraudAssessmentService assessmentService;
    private DefaultFraudIntegrationGuard guard;
    private FraudModuleControlService modeControl;

    @BeforeEach
    void setUp() {
        assessmentRepository = mock(FraudAssessmentRepository.class);
        blocklistRepository = mock(FraudBlocklistRepository.class);
        assessmentService = mock(FraudAssessmentService.class);
        modeControl = mock(FraudModuleControlService.class);
        when(modeControl.currentMode()).thenReturn(FraudModuleMode.ACTIVE);
        guard = new DefaultFraudIntegrationGuard(
                assessmentRepository,
                blocklistRepository,
                assessmentService,
                mock(FraudCaseService.class),
                modeControl
        );
    }

    @Test
    void nonApprovedAssessmentIsHeldDuringPlacement() {
        when(assessmentRepository.findTopByOrderIdOrderByIdDesc(1L)).thenReturn(Optional.empty());
        when(assessmentService.evaluate(any())).thenReturn(response(
                91L,
                FraudAssessmentStatus.MANUAL_REVIEW,
                FraudDecision.MANUAL_REVIEW
        ));

        FraudGuardResult result = guard.checkOrderAllowed(order(1L));

        assertFalse(result.isAllowed());
        assertEquals(91L, result.getAssessmentId());
        assertEquals(FraudAssessmentStatus.MANUAL_REVIEW, result.getAssessmentStatus());
    }

    @Test
    void onlyExplicitApprovalCanContinuePlacement() {
        when(assessmentRepository.findTopByOrderIdOrderByIdDesc(1L)).thenReturn(Optional.empty());
        when(assessmentService.evaluate(any())).thenReturn(response(
                92L,
                FraudAssessmentStatus.APPROVED,
                FraudDecision.APPROVE
        ));

        FraudGuardResult result = guard.checkOrderAllowed(order(1L));

        assertTrue(result.isAllowed());
        assertEquals(FraudDecision.APPROVE, result.getDecision());
    }

    @Test
    void blockingSiblingAssessmentStopsApprovedOrderFulfilment() {
        SalesOrder order = order(1L);
        CustomerOrderGroup group = new CustomerOrderGroup();
        group.setId(50L);
        order.setOrderGroup(group);

        FraudAssessment existing = new FraudAssessment();
        existing.setId(92L);
        when(assessmentRepository.findTopByOrderIdOrderByIdDesc(1L)).thenReturn(Optional.of(existing));
        when(assessmentService.findById(92L)).thenReturn(response(
                92L,
                FraudAssessmentStatus.APPROVED,
                FraudDecision.APPROVE
        ));
        when(assessmentRepository.countByOrderGroupIdAndStatusIn(anyLong(), any())).thenReturn(1L);

        FraudGuardResult result = guard.checkFulfilmentAllowed(order);

        assertFalse(result.isAllowed());
        assertTrue(result.getReason().contains("fraud verification"));
    }

    @Test
    void preHashedDeviceBlocklistEntryIsNotHashedAgain() {
        String deviceHash = FraudHashingSupport.sha256("raw-browser-token");
        when(blocklistRepository.existsEffectiveBlock(
                eq(FraudBlockType.DEVICE),
                eq(java.util.List.of(deviceHash)),
                any()
        )).thenReturn(true);
        FraudContext context = new FraudContext();
        context.setDeviceIdentifier(deviceHash.toUpperCase());

        FraudGuardResult result = guard.checkCheckoutEligibility(42L, context);

        assertFalse(result.isAllowed());
        verify(blocklistRepository).existsEffectiveBlock(
                eq(FraudBlockType.DEVICE),
                eq(java.util.List.of(deviceHash)),
                any()
        );
    }

    @Test
    void canonicalMobileLookupAlsoFindsLegacyLocalFormatHash() {
        String legacyLocalHash = FraudHashingSupport.sha256("01712345678");
        when(blocklistRepository.existsEffectiveBlock(
                eq(FraudBlockType.MOBILE_NUMBER),
                org.mockito.ArgumentMatchers.argThat(hashes -> hashes.contains(legacyLocalHash)),
                any()
        )).thenReturn(true);
        FraudContext context = new FraudContext();
        context.getMetadata().put("mobileNumber", "+8801712345678");

        FraudGuardResult result = guard.checkCheckoutEligibility(null, context);

        assertFalse(result.isAllowed());
    }

    @Test
    void inactiveModeSkipsNewFraudAssessments() {
        when(modeControl.currentMode()).thenReturn(FraudModuleMode.INACTIVE);
        when(assessmentRepository.findTopByOrderIdOrderByIdDesc(1L)).thenReturn(Optional.empty());

        assertTrue(guard.checkOrderAllowed(order(1L)).isAllowed());
        org.mockito.Mockito.verifyNoInteractions(assessmentService);
    }

    @Test
    void inactiveModeStillBlocksExistingFraudHolds() {
        when(modeControl.currentMode()).thenReturn(FraudModuleMode.INACTIVE);
        FraudAssessment held = new FraudAssessment();
        held.setId(12L);
        when(assessmentRepository.findTopByOrderIdOrderByIdDesc(1L)).thenReturn(Optional.of(held));
        when(assessmentService.findById(12L)).thenReturn(response(12L,
                FraudAssessmentStatus.FRAUD_HOLD, FraudDecision.HOLD));

        assertFalse(guard.checkOrderAllowed(order(1L)).isAllowed());
    }

    @Test
    void monitorOnlyScoresWithoutRestrictingNewOrders() {
        when(modeControl.currentMode()).thenReturn(FraudModuleMode.MONITOR_ONLY);
        when(assessmentRepository.findTopByOrderIdOrderByIdDesc(1L)).thenReturn(Optional.empty());
        FraudAssessmentResponse observed = response(13L, FraudAssessmentStatus.APPROVED, FraudDecision.APPROVE);
        observed.setDecisionReason("[MONITOR_ONLY] Observed HOLD");
        when(assessmentService.evaluate(any())).thenReturn(observed);

        assertTrue(guard.checkOrderAllowed(order(1L)).isAllowed());
        verify(assessmentService).evaluate(any());
    }

    @Test
    void returningToActiveRequiresNewAssessmentAfterMonitoring() {
        FraudAssessment monitored = new FraudAssessment();
        monitored.setId(14L);
        monitored.setEvaluationVersion("fraud-v1-monitor");
        when(assessmentRepository.findTopByOrderIdOrderByIdDesc(1L)).thenReturn(Optional.of(monitored));
        when(assessmentService.evaluate(any())).thenReturn(response(15L,
                FraudAssessmentStatus.FRAUD_HOLD, FraudDecision.HOLD));

        assertFalse(guard.checkOrderAllowed(order(1L)).isAllowed());
        verify(assessmentService).evaluate(any());
    }

    private SalesOrder order(Long id) {
        SalesOrder order = new SalesOrder();
        order.setId(id);
        return order;
    }

    private FraudAssessmentResponse response(
            Long id,
            FraudAssessmentStatus status,
            FraudDecision decision
    ) {
        FraudAssessmentResponse response = new FraudAssessmentResponse();
        response.setId(id);
        response.setStatus(status);
        response.setDecision(decision);
        return response;
    }
}
