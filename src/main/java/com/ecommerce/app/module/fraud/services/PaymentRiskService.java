package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.model.PaymentRiskProviderStatus;
import com.ecommerce.app.module.order.model.SalesOrder;

public interface PaymentRiskService {

    PaymentRiskProviderStatus resolveProviderStatus(SalesOrder order, FraudContext context);

    boolean isPaymentTokenBlocked(String paymentToken);
}
