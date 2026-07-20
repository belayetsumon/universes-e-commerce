package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudAssessmentReviewRequest;
import com.ecommerce.app.module.fraud.dto.FraudAssessmentResponse;
import com.ecommerce.app.module.fraud.model.FraudAction;
import com.ecommerce.app.module.fraud.model.FraudDecision;
import com.ecommerce.app.module.fraud.services.FraudAssessmentService;
import com.ecommerce.app.module.fraud.services.FraudReviewService;
import org.springframework.stereotype.Service;

@Service
public class DefaultFraudReviewService implements FraudReviewService {

    private final FraudAssessmentService fraudAssessmentService;

    public DefaultFraudReviewService(FraudAssessmentService fraudAssessmentService) {
        this.fraudAssessmentService = fraudAssessmentService;
    }

    @Override
    public FraudAssessmentResponse approve(Long assessmentId, FraudAssessmentReviewRequest request) {
        FraudAssessmentReviewRequest normalized = normalize(request, FraudDecision.APPROVE, FraudAction.APPROVE);
        return fraudAssessmentService.review(assessmentId, normalized);
    }

    @Override
    public FraudAssessmentResponse reject(Long assessmentId, FraudAssessmentReviewRequest request) {
        FraudAssessmentReviewRequest normalized = normalize(request, FraudDecision.REJECT, FraudAction.REJECT);
        return fraudAssessmentService.review(assessmentId, normalized);
    }

    @Override
    public FraudAssessmentResponse requestVerification(Long assessmentId, FraudAssessmentReviewRequest request) {
        FraudAssessmentReviewRequest normalized = normalize(request, FraudDecision.REQUIRE_OTP, FraudAction.REQUIRE_OTP);
        return fraudAssessmentService.review(assessmentId, normalized);
    }

    @Override
    public FraudAssessmentResponse hold(Long assessmentId, FraudAssessmentReviewRequest request) {
        FraudAssessmentReviewRequest normalized = normalize(request, FraudDecision.HOLD, FraudAction.HOLD);
        return fraudAssessmentService.review(assessmentId, normalized);
    }

    private FraudAssessmentReviewRequest normalize(FraudAssessmentReviewRequest request, FraudDecision decision, FraudAction action) {
        FraudAssessmentReviewRequest normalized = request == null ? new FraudAssessmentReviewRequest() : request;
        normalized.setDecision(decision);
        normalized.setAction(action);
        return normalized;
    }
}
