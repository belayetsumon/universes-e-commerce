package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudGuardResult;

public interface FraudPreOrderGuard {

    FraudGuardResult checkCheckoutEligibility(Long customerId, FraudContext context);
}
