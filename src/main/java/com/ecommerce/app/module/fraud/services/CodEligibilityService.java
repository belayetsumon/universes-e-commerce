package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import java.math.BigDecimal;

public interface CodEligibilityService {

    FraudGuardResult checkCodCheckoutEligibility(Long customerId, Long vendorId, BigDecimal orderTotal,
            String paymentPlan, boolean mobileVerified, FraudContext context);
}
