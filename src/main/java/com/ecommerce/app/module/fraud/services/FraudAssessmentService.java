package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudAssessmentCreateRequest;
import com.ecommerce.app.module.fraud.dto.FraudAssessmentResponse;
import com.ecommerce.app.module.fraud.dto.FraudAssessmentReviewRequest;
import com.ecommerce.app.module.fraud.dto.FraudAssessmentSearchFilter;
import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudDecisionResult;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.util.List;

public interface FraudAssessmentService {

    FraudAssessmentResponse evaluate(FraudAssessmentCreateRequest request);

    FraudAssessmentResponse persistAndApply(SalesOrder order, List<FraudSignalResult> signals,
            FraudDecisionResult decision, FraudContext context);

    FraudAssessmentResponse findById(Long id);

    FraudAssessmentResponse findLatestByOrderId(Long orderId);

    List<FraudAssessmentResponse> search(FraudAssessmentSearchFilter filter);

    FraudAssessmentResponse review(Long assessmentId, FraudAssessmentReviewRequest request);
}
