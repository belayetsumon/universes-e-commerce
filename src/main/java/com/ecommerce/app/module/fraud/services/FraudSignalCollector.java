package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.util.List;

public interface FraudSignalCollector {

    List<FraudSignalResult> collect(SalesOrder order, FraudContext context);
}
