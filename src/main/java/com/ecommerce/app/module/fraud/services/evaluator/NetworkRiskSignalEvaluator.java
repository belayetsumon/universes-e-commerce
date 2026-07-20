package com.ecommerce.app.module.fraud.services.evaluator;

import com.ecommerce.app.module.fraud.model.FraudSignalCategory;

public interface NetworkRiskSignalEvaluator extends FraudSignalEvaluator {

    @Override
    default FraudSignalCategory category() {
        return FraudSignalCategory.NETWORK;
    }
}
