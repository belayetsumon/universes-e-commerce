package com.ecommerce.app.module.checkout.guest.services;

import com.ecommerce.app.module.checkout.guest.model.FraudRiskLevel;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class GuestCheckoutFraudRiskService {

    public FraudRiskLevel scoreGuestCheckout(BigDecimal payableTotal, boolean cashOnDelivery, boolean newlyCreatedMobile) {
        BigDecimal total = payableTotal == null ? BigDecimal.ZERO : payableTotal;
        int score = 0;
        if (newlyCreatedMobile) {
            score += 10;
        }
        if (cashOnDelivery) {
            score += 10;
        }
        if (total.compareTo(new BigDecimal("25000")) >= 0) {
            score += 30;
        } else if (total.compareTo(new BigDecimal("10000")) >= 0) {
            score += 15;
        }

        if (score >= 60) {
            return FraudRiskLevel.CRITICAL;
        }
        if (score >= 40) {
            return FraudRiskLevel.HIGH;
        }
        if (score >= 20) {
            return FraudRiskLevel.MEDIUM;
        }
        return FraudRiskLevel.LOW;
    }
}
