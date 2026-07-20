package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.services.FraudRiskScoringService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DefaultFraudRiskScoringService implements FraudRiskScoringService {

    @Override
    public int calculate(List<FraudSignalResult> signals, FraudContext context) {
        if (signals == null || signals.isEmpty()) {
            return 0;
        }
        int score = signals.stream()
                .filter(signal -> signal != null && signal.isTriggered())
                .mapToInt(FraudSignalResult::getScoreImpact)
                .sum();
        return Math.max(0, Math.min(100, score));
    }
}
