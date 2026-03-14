/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.globalServices.District;
import com.ecommerce.app.module.shipping.dto.ShippingOption;
import com.ecommerce.app.module.shipping.model.Carrier;
import com.ecommerce.app.module.shipping.model.CarrierRate;
import com.ecommerce.app.module.shipping.model.DeliverySpeed;
import com.ecommerce.app.module.shipping.model.ShippingProfile;
import com.ecommerce.app.module.shipping.repository.CarrierRateRepository;
import com.ecommerce.app.module.shipping.repository.ShippingProfileRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class ShippingOptionService {

    @Autowired
    private ShippingProfileRepository profileRepo;

    @Autowired
    private CarrierRateRepository rateRepo;

    public List<ShippingOption> getShippingOptions(
            Long vendorId,
            District customerDistrict,
            BigDecimal totalWeight
    ) {
        List<ShippingOption> options = new ArrayList<>();

        List<ShippingProfile> profiles = profileRepo.findByVendorIdAndActiveTrue(vendorId);
        if (profiles.isEmpty()) {
            return options;
        }

        for (ShippingProfile profile : profiles) {
            List<Carrier> allowedCarriers = profile.getAllowedCarriers();
            if (allowedCarriers == null || allowedCarriers.isEmpty()) {
                continue;
            }

            List<District> allowedDistricts = profile.getAllowedDistricts();
            if (allowedDistricts != null && !allowedDistricts.isEmpty()
                    && !allowedDistricts.contains(customerDistrict)) {
                continue;
            }

            for (Carrier carrier : allowedCarriers) {
                // ✅ Fetch rates for customer district or fallback ALL
                List<CarrierRate> rates = rateRepo.findByCarrierAndDistrictIn(
                        carrier,
                        List.of(customerDistrict, District.All) // Make sure District.ALL exists in your enum
                );

                for (CarrierRate rate : rates) {
                    ShippingOption option = new ShippingOption();
                    option.setCarrierCode(carrier.getCode());   // e.g. 'SA'
                    option.setCode(rate.getUuid());             // use the UUID
                    option.setTitle(carrier.getName() + " - " + rate.getSpeed().name());

                    // ✅ Calculate price = basePrice + perKg * weight
                    BigDecimal weightToUse = (totalWeight == null ? BigDecimal.ONE : totalWeight);
                    BigDecimal weightCost = rate.getPerKg().multiply(weightToUse);
                    BigDecimal price = rate.getBasePrice().add(weightCost);

                    option.setPrice(price);
                    option.setEstimatedDelivery(
                            rate.getSpeed() == DeliverySpeed.EXPRESS ? "1-2 days" : "3-5 days"
                    );

                    options.add(option);
                }
            }
        }
        return options;
    }

}
