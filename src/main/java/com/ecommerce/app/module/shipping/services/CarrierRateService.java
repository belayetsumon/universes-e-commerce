/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.model.CarrierRate;
import com.ecommerce.app.module.shipping.repository.CarrierRateRepository;
import java.math.BigDecimal;
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

    public BigDecimal calculateShippingRateByUuid(String uuid, BigDecimal totalWeight, boolean isCod) {
        CarrierRate rate = carrierRateRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Rate not found for UUID: " + uuid));

        BigDecimal baseWeight = BigDecimal.ONE; // ✅ or replace with a field in CarrierRate if exists
        BigDecimal price;

        // ✅ If totalWeight is less than or equal to base weight → only base price
        if (totalWeight.compareTo(baseWeight) <= 0) {
            price = rate.getBasePrice();
        } // ✅ Otherwise → base price + extra weight * perKg
        else {
            BigDecimal extraWeight = totalWeight.subtract(baseWeight);  // weight over baseWeight
            price = rate.getBasePrice().add(rate.getPerKg().multiply(extraWeight));
        }

        // ✅ Add COD fee if needed
        if (isCod) {
            price = price.add(rate.getCodFee());
        }

        return price;
    }

}
