package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.FraudReasonCode;
import com.ecommerce.app.module.fraud.model.FraudSignalSeverity;
import com.ecommerce.app.module.fraud.services.evaluator.ReferralAbuseSignalEvaluator;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DefaultReferralAbuseSignalEvaluator extends AbstractFraudSignalEvaluator implements ReferralAbuseSignalEvaluator {

    @Override
    public List<FraudSignalResult> evaluate(SalesOrder order, FraudContext context) {
        List<FraudSignalResult> signals = new ArrayList<>();
        Map<String, Object> metadata = context.getMetadata();
        boolean referralUsed = context.getReferralCode() != null && !context.getReferralCode().isBlank();
        signals.add(signal("REFERRAL_USED", category(), referralUsed, 0, FraudSignalSeverity.INFO,
                null, referralUsed ? context.getReferralCode() : null, "referral-abuse", null));

        boolean selfReferral = Boolean.TRUE.equals(metadata.get("selfReferral"));
        signals.add(signal("SELF_REFERRAL", category(), selfReferral, 40, FraudSignalSeverity.HIGH,
                FraudReasonCode.SELF_REFERRAL, String.valueOf(selfReferral), "referral-abuse", null));

        boolean circularReferral = Boolean.TRUE.equals(metadata.get("circularReferral"));
        signals.add(signal("CIRCULAR_REFERRAL", category(), circularReferral, 50, FraudSignalSeverity.HIGH,
                FraudReasonCode.CIRCULAR_REFERRAL, String.valueOf(circularReferral), "referral-abuse", null));

        boolean excessiveReferral = Boolean.TRUE.equals(metadata.get("excessiveReferralUsage"));
        signals.add(signal("EXCESSIVE_REFERRAL_USAGE", category(), excessiveReferral, 20, FraudSignalSeverity.MEDIUM,
                FraudReasonCode.REFERRAL_ABUSE, String.valueOf(excessiveReferral), "referral-abuse", null));

        return signals;
    }
}
