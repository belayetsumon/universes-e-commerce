package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.FraudAssessment;
import java.util.List;

public interface FraudRuleExecutionLogService {

    void recordExecutions(FraudAssessment assessment, List<FraudSignalResult> signals, FraudContext context);
}
