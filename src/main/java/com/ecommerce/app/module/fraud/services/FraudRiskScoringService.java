package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import java.util.List;

public interface FraudRiskScoringService {

    int calculate(List<FraudSignalResult> signals, FraudContext context);
}
