package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.order.model.SalesOrder;

public interface CodRiskService {

    boolean requiresFirstOrderOtp(SalesOrder order, FraudContext context);

    boolean requiresPartialPrepayment(SalesOrder order, int riskScore);

    boolean isCodDisabledForCustomer(Long customerId);

    boolean isCodDisabledForVendor(Long vendorId);
}
