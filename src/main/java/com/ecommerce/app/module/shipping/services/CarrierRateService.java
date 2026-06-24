/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.model.CarrierRate;
import com.ecommerce.app.module.shipping.model.CarrierRateSlab;
import com.ecommerce.app.module.shipping.repository.CarrierRateRepository;
import com.ecommerce.app.module.shipping.repository.CarrierRateSlabRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class CarrierRateService {

    @Autowired
    private CarrierRateRepository carrierRateRepository;
    @Autowired
    private CarrierRateSlabRepository carrierRateSlabRepository;

    public BigDecimal calculateShippingRateByUuid(String uuid, BigDecimal totalWeight, boolean isCod) {
        CarrierRate rate = carrierRateRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Rate not found for UUID: " + uuid));

        BigDecimal weight = totalWeight == null || totalWeight.compareTo(BigDecimal.ZERO) <= 0
                ? BigDecimal.ONE
                : totalWeight;
        BigDecimal baseWeight = rate.getBaseWeight() == null || rate.getBaseWeight().compareTo(BigDecimal.ZERO) <= 0
                ? BigDecimal.ONE
                : rate.getBaseWeight();
        BigDecimal additionalWeightUnit = rate.getAdditionalWeightUnit() == null || rate.getAdditionalWeightUnit().compareTo(BigDecimal.ZERO) <= 0
                ? BigDecimal.ONE
                : rate.getAdditionalWeightUnit();

        BigDecimal price = calculateSlabPrice(rate, weight);
        if (price == null) {
            price = rate.getBasePrice();

            // 2026-04-22: Apply base price up to base weight, then charge one slab at a time.
            if (weight.compareTo(baseWeight) > 0) {
                BigDecimal extraWeight = weight.subtract(baseWeight);
                BigDecimal extraSlabs = extraWeight.divide(additionalWeightUnit, 0, RoundingMode.CEILING);
                price = price.add(rate.getPerKg().multiply(extraSlabs));
            }
        }

        // 2026-04-22: Add COD fee only when the selected rate allows COD.
        if (isCod && rate.isCodAvailable()) {
            price = price.add(rate.getCodFee());
        }

        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSlabPrice(CarrierRate rate, BigDecimal weight) {
        List<CarrierRateSlab> slabs = carrierRateSlabRepository.findByCarrierRateAndActiveTrueOrderByPriorityAscMinWeightAsc(rate);
        if (slabs == null || slabs.isEmpty()) {
            return null;
        }
        return slabs.stream()
                .filter(slab -> slab.matchesWeight(weight))
                .map(CarrierRateSlab::getPrice)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

}
