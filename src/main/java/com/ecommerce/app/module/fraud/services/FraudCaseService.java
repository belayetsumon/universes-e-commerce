package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudCaseAssignRequest;
import com.ecommerce.app.module.fraud.dto.FraudCaseResolveRequest;
import com.ecommerce.app.module.fraud.dto.FraudCaseResponse;
import com.ecommerce.app.module.fraud.model.FraudAssessment;
import java.util.List;

public interface FraudCaseService {

    FraudCaseResponse findById(Long id);

    List<FraudCaseResponse> findOpenCases();

    FraudCaseResponse assign(Long caseId, FraudCaseAssignRequest request);

    FraudCaseResponse resolve(Long caseId, FraudCaseResolveRequest request);

    FraudCaseResponse openForAssessment(FraudAssessment assessment, String reason);

    boolean hasOpenCaseForOrder(Long orderId);

    boolean hasOpenCaseForVendor(Long vendorId);
}
