package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.FraudReasonCode;
import com.ecommerce.app.module.fraud.model.FraudSignalSeverity;
import com.ecommerce.app.module.fraud.model.PaymentRiskProviderStatus;
import com.ecommerce.app.module.fraud.model.PaymentRiskResult;
import com.ecommerce.app.module.fraud.repository.PaymentRiskResultRepository;
import com.ecommerce.app.module.fraud.services.evaluator.PaymentRiskSignalEvaluator;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DefaultPaymentRiskSignalEvaluator extends AbstractFraudSignalEvaluator implements PaymentRiskSignalEvaluator {

    private final PaymentRiskResultRepository paymentRiskResultRepository;

    public DefaultPaymentRiskSignalEvaluator(PaymentRiskResultRepository paymentRiskResultRepository) {
        this.paymentRiskResultRepository = paymentRiskResultRepository;
    }

    @Override
    public List<FraudSignalResult> evaluate(SalesOrder order, FraudContext context) {
        List<FraudSignalResult> signals = new ArrayList<>();
        PaymentRiskResult providerResult = order == null || order.getId() == null
                ? null
                : paymentRiskResultRepository.findTopByOrderIdOrderByIdDesc(order.getId()).orElse(null);

        boolean highRisk = providerResult != null && (providerResult.getProviderStatus() == PaymentRiskProviderStatus.HIGH_RISK
                || providerResult.getProviderStatus() == PaymentRiskProviderStatus.REJECTED);
        signals.add(signal("PAYMENT_PROVIDER_HIGH_RISK", category(), highRisk, 100, FraudSignalSeverity.CRITICAL,
                FraudReasonCode.PAYMENT_PROVIDER_HIGH_RISK, providerResult == null ? "NO_PROVIDER_RESULT" : providerResult.getProviderStatus().name(),
                "payment-risk-result", null));

        boolean threeDsSuccess = providerResult != null
                && providerResult.getThreeDSecureResult() != null
                && providerResult.getThreeDSecureResult().equalsIgnoreCase("SUCCESS");
        signals.add(signal("THREE_D_SECURE_SUCCESS", category(), threeDsSuccess, -15, FraudSignalSeverity.LOW,
                null, String.valueOf(threeDsSuccess), "payment-risk-result", null));

        String paymentCountry = providerResult != null && providerResult.getPaymentCountry() != null
                ? providerResult.getPaymentCountry()
                : context.getPaymentCountry();
        boolean countryMismatch = paymentCountry != null && context.getShippingCountry() != null
                && !paymentCountry.equalsIgnoreCase(context.getShippingCountry());
        signals.add(signal("PAYMENT_COUNTRY_MISMATCH", category(), countryMismatch, 15, FraudSignalSeverity.MEDIUM,
                FraudReasonCode.PAYMENT_COUNTRY_MISMATCH, String.valueOf(countryMismatch), "payment-risk", null));

        boolean tokenBlacklisted = providerResult != null && providerResult.getPaymentTokenHash() != null
                && paymentRiskResultRepository.findByPaymentTokenHash(providerResult.getPaymentTokenHash()).stream()
                        .anyMatch(result -> result.getProviderStatus() == PaymentRiskProviderStatus.REJECTED);
        signals.add(signal("PAYMENT_TOKEN_BLACKLISTED", category(), tokenBlacklisted, 100, FraudSignalSeverity.CRITICAL,
                FraudReasonCode.PAYMENT_TOKEN_BLACKLISTED, String.valueOf(tokenBlacklisted), "payment-risk-result", null));

        return signals;
    }
}
