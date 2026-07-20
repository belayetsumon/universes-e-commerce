package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudDecisionResult;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.FraudAction;
import com.ecommerce.app.module.fraud.model.FraudAssessmentStatus;
import com.ecommerce.app.module.fraud.model.FraudDecision;
import com.ecommerce.app.module.fraud.model.FraudReasonCode;
import com.ecommerce.app.module.fraud.model.FraudRiskLevel;
import com.ecommerce.app.module.fraud.services.FraudConfigurationService;
import com.ecommerce.app.module.fraud.services.FraudDecisionService;
import com.ecommerce.app.module.order.model.OrderPaymentPlan;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DefaultFraudDecisionService implements FraudDecisionService {

    private final FraudConfigurationService fraudConfigurationService;

    public DefaultFraudDecisionService(FraudConfigurationService fraudConfigurationService) {
        this.fraudConfigurationService = fraudConfigurationService;
    }

    @Override
    public FraudDecisionResult decide(SalesOrder order, int riskScore, List<FraudSignalResult> signals, FraudContext context) {
        int lowMax = fraudConfigurationService.getInt("fraud.score.low.max", 29);
        int mediumMax = fraudConfigurationService.getInt("fraud.score.medium.max", 59);
        int highMax = fraudConfigurationService.getInt("fraud.score.high.max", 79);
        FraudRiskLevel riskLevel = riskLevel(riskScore, lowMax, mediumMax, highMax);

        FraudDecisionResult result = new FraudDecisionResult();
        result.setRiskScore(riskScore);
        result.setRiskLevel(riskLevel);
        result.setAutomaticDecision(true);
        result.setPrimaryReasonCode(primaryReason(signals));
        result.setReasonCodes(signals == null ? List.of() : signals.stream()
                .filter(signal -> signal != null && signal.isTriggered() && signal.getReasonCode() != null)
                .map(FraudSignalResult::getReasonCode)
                .distinct()
                .toList());

        switch (riskLevel) {
            case LOW -> approve(result);
            case MEDIUM -> verify(order, context, result);
            case HIGH -> manualReview(result);
            case CRITICAL -> critical(order, context, result);
            default -> manualReview(result);
        }
        return result;
    }

    private FraudRiskLevel riskLevel(int riskScore, int lowMax, int mediumMax, int highMax) {
        if (riskScore <= lowMax) {
            return FraudRiskLevel.LOW;
        }
        if (riskScore <= mediumMax) {
            return FraudRiskLevel.MEDIUM;
        }
        if (riskScore <= highMax) {
            return FraudRiskLevel.HIGH;
        }
        return FraudRiskLevel.CRITICAL;
    }

    private FraudReasonCode primaryReason(List<FraudSignalResult> signals) {
        if (signals == null) {
            return null;
        }
        return signals.stream()
                .filter(signal -> signal != null && signal.isTriggered() && signal.getReasonCode() != null)
                .sorted((left, right) -> Integer.compare(Math.abs(right.getScoreImpact()), Math.abs(left.getScoreImpact())))
                .map(FraudSignalResult::getReasonCode)
                .findFirst()
                .orElse(null);
    }

    private void approve(FraudDecisionResult result) {
        result.setDecision(FraudDecision.APPROVE);
        result.setAction(FraudAction.APPROVE);
        result.setStatus(FraudAssessmentStatus.APPROVED);
        result.setDecisionReason("Risk score is within the auto-approval threshold.");
    }

    private void verify(SalesOrder order, FraudContext context, FraudDecisionResult result) {
        result.setDecision(isCod(order, context) ? FraudDecision.REQUIRE_OTP : FraudDecision.VERIFY);
        result.setAction(isCod(order, context) ? FraudAction.REQUIRE_OTP : FraudAction.VERIFY);
        result.setStatus(FraudAssessmentStatus.VERIFICATION_REQUIRED);
        result.setDecisionReason("Risk score requires additional verification.");
    }

    private void manualReview(FraudDecisionResult result) {
        result.setDecision(FraudDecision.MANUAL_REVIEW);
        result.setAction(FraudAction.MANUAL_REVIEW);
        result.setStatus(FraudAssessmentStatus.MANUAL_REVIEW);
        result.setManualReviewRequired(true);
        result.setDecisionReason("Risk score requires manual fraud review.");
    }

    private void critical(SalesOrder order, FraudContext context, FraudDecisionResult result) {
        if (isCod(order, context)) {
            result.setDecision(FraudDecision.REQUIRE_PREPAID);
            result.setAction(FraudAction.REQUIRE_PREPAID);
            result.setStatus(FraudAssessmentStatus.VERIFICATION_REQUIRED);
            result.setDecisionReason("Critical COD risk requires prepaid verification.");
            return;
        }
        result.setDecision(FraudDecision.REJECT);
        result.setAction(FraudAction.REJECT);
        result.setStatus(FraudAssessmentStatus.FRAUD_REJECTED);
        result.setDecisionReason("Critical risk score requires rejection or fraud block.");
    }

    private boolean isCod(SalesOrder order, FraudContext context) {
        if (context != null && context.getPaymentMethod() != null && "COD".equalsIgnoreCase(context.getPaymentMethod())) {
            return true;
        }
        return order != null && (order.getPaymentPlan() == OrderPaymentPlan.FULL_COD
                || order.getPaymentPlan() == OrderPaymentPlan.PARTIAL_ADVANCE_COD);
    }
}
