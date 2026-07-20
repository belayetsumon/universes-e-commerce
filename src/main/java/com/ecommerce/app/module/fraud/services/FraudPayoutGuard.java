package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudGuardResult;

public interface FraudPayoutGuard {

    FraudGuardResult checkVendorPayoutAllowed(Long vendorId);
}
