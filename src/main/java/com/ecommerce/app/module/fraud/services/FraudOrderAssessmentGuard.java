package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.order.model.SalesOrder;

public interface FraudOrderAssessmentGuard {

    FraudGuardResult checkOrderAllowed(SalesOrder order);

    FraudGuardResult checkOrderAllowed(SalesOrder order, FraudContext context);
}
