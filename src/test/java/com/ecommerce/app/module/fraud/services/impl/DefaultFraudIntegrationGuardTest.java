package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.fraud.dto.FraudAssessmentResponse;
import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.fraud.model.FraudAssessment;
import com.ecommerce.app.module.fraud.model.FraudAssessmentStatus;
import com.ecommerce.app.module.fraud.model.FraudDecision;
import com.ecommerce.app.module.fraud.repository.FraudAssessmentRepository;
import com.ecommerce.app.module.fraud.repository.FraudBlocklistRepository;
import com.ecommerce.app.module.fraud.services.FraudAssessmentService;
import com.ecommerce.app.module.fraud.services.FraudCaseService;
import com.ecommerce.app.module.order.model.CustomerOrderGroup;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultFraudIntegrationGuardTest {

    private FraudAssessmentRepository assessmentRepository;
    private FraudAssessmentService assessmentService;
    private DefaultFraudIntegrationGuard guard;

    @BeforeEach
    void setUp() {
        assessmentRepository = mock(FraudAssessmentRepository.class);
        assessmentService = mock(FraudAssessmentService.class);
        guard = new DefaultFraudIntegrationGuard(
                assessmentRepository,
                mock(FraudBlocklistRepository.class),
                assessmentService,
                mock(FraudCaseService.class)
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
