package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudAssessmentReviewRequest;
import com.ecommerce.app.module.fraud.dto.FraudAssessmentResponse;

public interface FraudReviewService {

    FraudAssessmentResponse approve(Long assessmentId, FraudAssessmentReviewRequest request);

    FraudAssessmentResponse reject(Long assessmentId, FraudAssessmentReviewRequest request);

    FraudAssessmentResponse requestVerification(Long assessmentId, FraudAssessmentReviewRequest request);

    FraudAssessmentResponse hold(Long assessmentId, FraudAssessmentReviewRequest request);
}
