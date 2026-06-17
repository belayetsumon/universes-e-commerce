/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.shipping.services;

import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service

public class ShippingServices {

//    private final CarrierRateRuleRepository ruleRepo;
//    private final CarrierRateRepository rateRepo;
//
//    public ShippingQuote calculate(Long carrierId,
//            District from,
//            District to,
//            BigDecimal weight) {
//
//        // 1. RULE MATCH (TOP PRIORITY)
//        Optional<CarrierRateRule> rule
//                = ruleRepo.findTopByCarrierIdAndFromDistrictAndToDistrictOrderByPriorityDesc(
//                        carrierId, from, to
//                );
//
//        if (rule.isPresent()) {
//            CarrierRateRule r = rule.get();
//
//            return new ShippingQuote(
//                    r.getFixedPrice(),
//                    r.getMinDays(),
//                    r.getMaxDays(),
//                    r.isCodAvailable(),
//                    r.getCodFee()
//            );
//        }
//
//        // 2. FALLBACK (WEIGHT SYSTEM)
//        CarrierRate rate = rateRepo.findByCarrierId(carrierId)
//                .stream()
//                .findFirst()
//                .orElseThrow();
//
//        BigDecimal price = calculateWeight(rate, weight);
//
//        return new ShippingQuote(
//                price,
//                rate.getEstimatedMinDays(),
//                rate.getEstimatedMaxDays(),
//                rate.isCodAvailable(),
//                rate.getCodFee()
//        );
//    }
//
//    private BigDecimal calculateWeight(CarrierRate rate, BigDecimal weight) {
//
//        if (weight.compareTo(rate.getBaseWeight()) <= 0) {
//            return rate.getBasePrice();
//        }
//
//        BigDecimal extra = weight.subtract(rate.getBaseWeight());
//
//        BigDecimal units = extra.divide(
//                rate.getAdditionalWeightUnit(),
//                0,
//                RoundingMode.CEILING
//        );
//
//        return rate.getBasePrice()
//                .add(units.multiply(rate.getPerKg()));
//    }
}
