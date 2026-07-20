package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudDecisionResult;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import java.util.List;
import java.util.Optional;

public interface FraudRuleEngine {

    Optional<FraudDecisionResult> evaluateHardRules(List<FraudSignalResult> signals, FraudContext context);

    List<FraudSignalResult> applyScoringRules(List<FraudSignalResult> signals, FraudContext context);
}
