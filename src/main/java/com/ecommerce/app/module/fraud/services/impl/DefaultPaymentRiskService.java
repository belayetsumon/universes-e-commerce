package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.model.FraudBlockType;
import com.ecommerce.app.module.fraud.model.PaymentRiskProviderStatus;
import com.ecommerce.app.module.fraud.model.PaymentRiskResult;
import com.ecommerce.app.module.fraud.repository.FraudBlocklistRepository;
import com.ecommerce.app.module.fraud.repository.PaymentRiskResultRepository;
import com.ecommerce.app.module.fraud.security.FraudPrivacySupport;
import com.ecommerce.app.module.fraud.services.PaymentRiskService;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultPaymentRiskService implements PaymentRiskService {

    private final PaymentRiskResultRepository paymentRiskResultRepository;
    private final FraudBlocklistRepository fraudBlocklistRepository;

    public DefaultPaymentRiskService(PaymentRiskResultRepository paymentRiskResultRepository,
            FraudBlocklistRepository fraudBlocklistRepository) {
        this.paymentRiskResultRepository = paymentRiskResultRepository;
        this.fraudBlocklistRepository = fraudBlocklistRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentRiskProviderStatus resolveProviderStatus(SalesOrder order, FraudContext context) {
        if (order == null || order.getId() == null) {
            return PaymentRiskProviderStatus.NOT_REQUESTED;
        }
        return paymentRiskResultRepository.findTopByOrderIdOrderByIdDesc(order.getId())
                .map(PaymentRiskResult::getProviderStatus)
                .orElse(PaymentRiskProviderStatus.NOT_REQUESTED);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPaymentTokenBlocked(String paymentToken) {
        String tokenHash = FraudPrivacySupport.hashIdentifier(paymentToken);
        return tokenHash != null
                && fraudBlocklistRepository.existsByBlockTypeAndHashedValueAndActiveTrue(FraudBlockType.PAYMENT_TOKEN, tokenHash);
    }

    @Transactional
    public PaymentRiskResult saveProviderResult(Long orderId, Long paymentId, String providerName,
            PaymentRiskProviderStatus providerStatus, Integer providerRiskScore, String paymentToken,
            String providerReference, String metadataJson) {
        PaymentRiskResult result = new PaymentRiskResult();
        result.setOrderId(orderId);
        result.setPaymentId(paymentId);
        result.setProviderName(trim(providerName, 80));
        result.setProviderStatus(providerStatus == null ? PaymentRiskProviderStatus.NOT_REQUESTED : providerStatus);
        result.setProviderRiskScore(providerRiskScore);
        result.setPaymentTokenHash(FraudPrivacySupport.hashIdentifier(paymentToken));
        result.setProviderReference(trim(providerReference, 160));
        result.setMetadataJson(FraudPrivacySupport.redactJson(metadataJson));
        result.setReceivedAt(LocalDateTime.now());
        return paymentRiskResultRepository.save(result);
    }

    private String trim(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength);
    }
}
