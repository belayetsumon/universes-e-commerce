package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.services.FraudSignalCollector;
import com.ecommerce.app.module.fraud.services.evaluator.FraudSignalEvaluator;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DefaultFraudSignalCollector implements FraudSignalCollector {

    private final List<FraudSignalEvaluator> evaluators;

    public DefaultFraudSignalCollector(List<FraudSignalEvaluator> evaluators) {
        this.evaluators = evaluators == null
                ? List.of()
                : evaluators.stream()
                        .sorted(Comparator.comparing(evaluator -> evaluator.category().name()))
                        .toList();
    }

    @Override
    public List<FraudSignalResult> collect(SalesOrder order, FraudContext context) {
        FraudContext safeContext = context == null ? new FraudContext() : context;
        List<FraudSignalResult> signals = new ArrayList<>();
        for (FraudSignalEvaluator evaluator : evaluators) {
            List<FraudSignalResult> evaluated = evaluator.evaluate(order, safeContext);
            if (evaluated != null && !evaluated.isEmpty()) {
                signals.addAll(evaluated);
            }
        }
        return signals;
    }
}
