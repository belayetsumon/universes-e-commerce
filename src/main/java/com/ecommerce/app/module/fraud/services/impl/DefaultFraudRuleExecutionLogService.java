package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.FraudAssessment;
import com.ecommerce.app.module.fraud.model.FraudRule;
import com.ecommerce.app.module.fraud.model.FraudRuleExecution;
import com.ecommerce.app.module.fraud.repository.FraudRuleExecutionRepository;
import com.ecommerce.app.module.fraud.repository.FraudRuleRepository;
import com.ecommerce.app.module.fraud.services.FraudRuleExecutionLogService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultFraudRuleExecutionLogService implements FraudRuleExecutionLogService {

    private final FraudRuleRepository fraudRuleRepository;
    private final FraudRuleExecutionRepository fraudRuleExecutionRepository;
    private final FraudRuleMatcher fraudRuleMatcher;

    public DefaultFraudRuleExecutionLogService(FraudRuleRepository fraudRuleRepository,
            FraudRuleExecutionRepository fraudRuleExecutionRepository,
            FraudRuleMatcher fraudRuleMatcher) {
        this.fraudRuleRepository = fraudRuleRepository;
        this.fraudRuleExecutionRepository = fraudRuleExecutionRepository;
        this.fraudRuleMatcher = fraudRuleMatcher;
    }

    @Override
    @Transactional
    public void recordExecutions(FraudAssessment assessment, List<FraudSignalResult> signals, FraudContext context) {
        if (assessment == null || assessment.getId() == null) {
            return;
        }
        List<FraudRule> rules = fraudRuleRepository.findByActiveTrueOrderByPriorityAscIdAsc();
        for (FraudRule rule : rules) {
            FraudSignalResult signal = fraudRuleMatcher.findSignal(rule, signals).orElse(null);
            boolean matched = fraudRuleMatcher.matches(rule, signals, context);

            FraudRuleExecution execution = new FraudRuleExecution();
            execution.setAssessment(assessment);
            execution.setRule(rule);
            execution.setRuleCode(rule.getRuleCode());
            execution.setSignalCode(rule.getSignalCode());
            execution.setMatched(matched);
            execution.setScoreImpact(matched ? rule.getScoreImpact() : 0);
            execution.setAction(rule.getAction());
            execution.setHardBlock(rule.isHardBlock());
            execution.setExecutionDetailJson(fraudRuleMatcher.executionDetail(rule, signal, matched));
            execution.setExecutedAt(LocalDateTime.now());
            fraudRuleExecutionRepository.save(execution);
        }
    }
}
