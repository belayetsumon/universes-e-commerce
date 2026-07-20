package com.ecommerce.app.module.fraud.services.evaluator;

import com.ecommerce.app.module.fraud.model.FraudSignalCategory;

public interface PromotionAbuseSignalEvaluator extends FraudSignalEvaluator {

    @Override
    default FraudSignalCategory category() {
        return FraudSignalCategory.PROMOTION;
    }
}
