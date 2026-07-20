package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.order.model.SalesOrder;

public interface FraudPaymentCaptureGuard {

    FraudGuardResult checkPaymentCaptureAllowed(SalesOrder order);

    FraudGuardResult checkRefundAllowed(SalesOrder order);
}
