package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.order.model.SalesOrder;

public interface FraudContextRecorderService {

    void recordOrderAttempt(SalesOrder order, FraudContext context);
}
