package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.order.model.SalesOrder;

public interface FraudFulfilmentGuard {

    FraudGuardResult checkPackingAllowed(SalesOrder order);

    FraudGuardResult checkShipmentCreationAllowed(SalesOrder order);

    FraudGuardResult checkFulfilmentAllowed(SalesOrder order);
}
