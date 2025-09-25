/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.model.ShippingChargeRule;
import com.ecommerce.app.module.shipping.repository.ShippingChargeRuleRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class ShippingChargeService {

    @Autowired
    private ShippingChargeRuleRepository repository;

    public BigDecimal calculateCharge(String zoneType, BigDecimal weightKg) {
        List<ShippingChargeRule> rules = repository.findByZoneType(zoneType);

        for (ShippingChargeRule rule : rules) {
            BigDecimal min = rule.getMinWeight();
            BigDecimal max = rule.getMaxWeight();

            if (weightKg.compareTo(min) >= 0 && weightKg.compareTo(max) <= 0) {
                return rule.getCharge();
            }
        }

        Optional<ShippingChargeRule> baseRule = rules.stream()
                .filter(rule -> rule.getMaxWeight().compareTo(BigDecimal.valueOf(2.0)) == 0)
                .findFirst();

        if (baseRule.isPresent()) {
            ShippingChargeRule rule = baseRule.get();
            BigDecimal baseCharge = rule.getCharge();
            BigDecimal extraPerKg = rule.getExtraPerKg();

            BigDecimal extraWeight = weightKg.subtract(rule.getMaxWeight())
                    .setScale(0, RoundingMode.CEILING);

            return baseCharge.add(extraPerKg.multiply(extraWeight));
        }

        return BigDecimal.ZERO;
    }
}
