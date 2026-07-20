package com.ecommerce.app.module.fraud.services.evaluator;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.FraudSignalCategory;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.util.List;

public interface FraudSignalEvaluator {

    FraudSignalCategory category();

    List<FraudSignalResult> evaluate(SalesOrder order, FraudContext context);
}
