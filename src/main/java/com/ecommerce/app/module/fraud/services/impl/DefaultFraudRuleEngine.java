package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudDecisionResult;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.FraudAction;
import com.ecommerce.app.module.fraud.model.FraudAssessmentStatus;
import com.ecommerce.app.module.fraud.model.FraudDecision;
import com.ecommerce.app.module.fraud.model.FraudRiskLevel;
import com.ecommerce.app.module.fraud.model.FraudRule;
import com.ecommerce.app.module.fraud.model.FraudRuleType;
import com.ecommerce.app.module.fraud.repository.FraudRuleRepository;
import com.ecommerce.app.module.fraud.services.FraudRuleEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultFraudRuleEngine implements FraudRuleEngine {

    private final FraudRuleRepository fraudRuleRepository;
    private final FraudRuleMatcher fraudRuleMatcher;

    public DefaultFraudRuleEngine(FraudRuleRepository fraudRuleRepository, FraudRuleMatcher fraudRuleMatcher) {
        this.fraudRuleRepository = fraudRuleRepository;
        this.fraudRuleMatcher = fraudRuleMatcher;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FraudDecisionResult> evaluateHardRules(List<FraudSignalResult> signals, FraudContext context) {
        List<FraudRule> hardRules = fraudRuleRepository.findByRuleTypeAndActiveTrueOrderByPriorityAscIdAsc(FraudRuleType.HARD_BLOCK);
        for (FraudRule rule : hardRules) {
            if (fraudRuleMatcher.matches(rule, signals, context)) {
                return Optional.of(toHardDecision(rule, signals));
            }
        }
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudSignalResult> applyScoringRules(List<FraudSignalResult> signals, FraudContext context) {
        if (signals == null || signals.isEmpty()) {
            return List.of();
        }
        List<FraudSignalResult> adjustedSignals = new ArrayList<>(signals);
        List<FraudRule> scoringRules = new ArrayList<>(
                fraudRuleRepository.findByRuleTypeAndActiveTrueOrderByPriorityAscIdAsc(FraudRuleType.SCORING));
        scoringRules.addAll(fraudRuleRepository.findByRuleTypeAndActiveTrueOrderByPriorityAscIdAsc(FraudRuleType.COD_CONTROL));
        scoringRules.addAll(fraudRuleRepository.findByRuleTypeAndActiveTrueOrderByPriorityAscIdAsc(FraudRuleType.PAYMENT_CONTROL));
        scoringRules.addAll(fraudRuleRepository.findByRuleTypeAndActiveTrueOrderByPriorityAscIdAsc(FraudRuleType.PROMOTION_CONTROL));
        scoringRules.addAll(fraudRuleRepository.findByRuleTypeAndActiveTrueOrderByPriorityAscIdAsc(FraudRuleType.REFERRAL_CONTROL));
        scoringRules.addAll(fraudRuleRepository.findByRuleTypeAndActiveTrueOrderByPriorityAscIdAsc(FraudRuleType.VENDOR_CONTROL));

        for (FraudRule rule : scoringRules) {
            fraudRuleMatcher.findSignal(rule, adjustedSignals)
                    .filter(signal -> fraudRuleMatcher.matches(rule, adjustedSignals, context))
                    .ifPresent(signal -> signal.setScoreImpact(rule.getScoreImpact()));
        }
        return adjustedSignals;
    }

    private FraudDecisionResult toHardDecision(FraudRule rule, List<FraudSignalResult> signals) {
        FraudDecisionResult result = new FraudDecisionResult();
        result.setHardBlock(true);
        result.setRiskScore(100);
        result.setRiskLevel(FraudRiskLevel.CRITICAL);
        result.setAction(rule.getAction() == null ? FraudAction.BLOCK : rule.getAction());
        result.setDecision(toDecision(result.getAction()));
        result.setStatus(toStatus(result.getDecision()));
        result.setAutomaticDecision(true);
        result.setManualReviewRequired(result.getDecision() == FraudDecision.HOLD || result.getDecision() == FraudDecision.MANUAL_REVIEW);
        result.setDecisionReason("Hard fraud rule matched: " + rule.getRuleCode());
        result.setPrimaryReasonCode(fraudRuleMatcher.findSignal(rule, signals)
                .map(FraudSignalResult::getReasonCode)
                .orElse(null));
        return result;
    }

    private FraudDecision toDecision(FraudAction action) {
        if (action == null) {
            return FraudDecision.BLOCK;
        }
        return switch (action) {
            case APPROVE, ALLOW -> FraudDecision.APPROVE;
            case VERIFY -> FraudDecision.VERIFY;
            case REQUIRE_OTP -> FraudDecision.REQUIRE_OTP;
            case REQUIRE_PREPAID -> FraudDecision.REQUIRE_PREPAID;
            case REQUIRE_PARTIAL_PREPAYMENT -> FraudDecision.REQUIRE_PARTIAL_PREPAYMENT;
            case MANUAL_REVIEW -> FraudDecision.MANUAL_REVIEW;
            case HOLD -> FraudDecision.HOLD;
            case REJECT -> FraudDecision.REJECT;
            case CANCEL -> FraudDecision.CANCEL;
            case DISABLE_COD -> FraudDecision.DISABLE_COD;
            case HOLD_REFUND -> FraudDecision.HOLD_REFUND;
            case HOLD_REWARD -> FraudDecision.HOLD_REWARD;
            case HOLD_VENDOR_PAYOUT -> FraudDecision.HOLD_VENDOR_PAYOUT;
            case BLOCK, TEMPORARILY_BLOCK_ACCOUNT, PERMANENTLY_BLOCK_ACCOUNT -> FraudDecision.BLOCK;
        };
    }

    private FraudAssessmentStatus toStatus(FraudDecision decision) {
        if (decision == null) {
            return FraudAssessmentStatus.FRAUD_HOLD;
        }
        return switch (decision) {
            case APPROVE -> FraudAssessmentStatus.APPROVED;
            case VERIFY, REQUIRE_OTP, REQUIRE_PREPAID, REQUIRE_PARTIAL_PREPAYMENT -> FraudAssessmentStatus.VERIFICATION_REQUIRED;
            case MANUAL_REVIEW -> FraudAssessmentStatus.MANUAL_REVIEW;
            case HOLD, DISABLE_COD, HOLD_REFUND, HOLD_REWARD, HOLD_VENDOR_PAYOUT -> FraudAssessmentStatus.FRAUD_HOLD;
            case REJECT, BLOCK, CANCEL -> FraudAssessmentStatus.FRAUD_REJECTED;
        };
    }
}
