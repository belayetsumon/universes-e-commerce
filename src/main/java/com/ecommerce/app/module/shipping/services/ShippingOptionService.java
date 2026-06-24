/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.dto.ShippingOption;
import com.ecommerce.app.module.shipping.model.Carrier;
import com.ecommerce.app.module.shipping.model.CarrierRate;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.shipping.model.ShippingProfile;
import com.ecommerce.app.module.shipping.repository.CarrierRateRepository;
import com.ecommerce.app.module.shipping.repository.ShippingProfileRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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

    @Autowired
    private CarrierRateService carrierRateService;

    @Autowired
    private ShippingLocationService shippingLocationService;

    public List<ShippingOption> getShippingOptions(
            Long vendorId,
            ShippingLocation customerLocation,
            BigDecimal totalWeight
    ) {
        List<ShippingOption> options = new ArrayList<>();

        if (customerLocation == null) {
            return options;
        }

        List<ShippingProfile> profiles = profileRepo.findByVendorIdAndActiveTrue(vendorId);
        if (profiles.isEmpty()) {
            return options;
        }

        for (ShippingProfile profile : profiles) {
            List<Carrier> allowedCarriers = profile.getAllowedCarriers();
            if (allowedCarriers == null || allowedCarriers.isEmpty()) {
                continue;
            }

            List<ShippingLocation> allowedLocations = profile.getAllowedDistricts();
            if (allowedLocations != null && !allowedLocations.isEmpty()
                    && !shippingLocationService.appliesToAnyLocation(allowedLocations, customerLocation)) {
                continue;
            }

            for (Carrier carrier : allowedCarriers) {
                // Fetch rates that cover the selected shipping location.
                List<CarrierRate> rates = rateRepo.findByCarrier(carrier).stream()
                        .filter(rate -> rate.appliesToLocation(customerLocation)
                        || shippingLocationService.rateAppliesToLocation(rate, customerLocation))
                        .toList();

                for (CarrierRate rate : rates) {
                    ShippingOption option = new ShippingOption();
                    String rateUuid = rate.getUuid();
                    BigDecimal weightToUse = (totalWeight == null || totalWeight.compareTo(BigDecimal.ZERO) <= 0)
                            ? BigDecimal.ONE
                            : totalWeight;

                    option.setCarrierCode(carrier.getCode());
                    option.setCarrierName(carrier.getName());
                    option.setCarrierMode(carrier.getMode());
                    option.setSettlementMode(carrier.getSettlementMode());
                    option.setShippingChargeOwner(carrier.getShippingChargeOwner());
                    option.setCodCollectionMode(carrier.getCodCollectionMode());
                    option.setCode(rateUuid);
                    option.setRateUuid(rateUuid);
                    option.setSpeed(rate.getSpeed());
                    option.setDeliveryType(rate.getDeliveryType());
                    option.setCodFee(rate.getCodFee());
                    option.setCodAvailable(rate.isCodAvailable());
                    option.setTitle(buildOptionTitle(carrier, rate));
                    option.setPrice(carrierRateService.calculateShippingRateByUuid(rateUuid, weightToUse, false));
                    option.setEstimatedDelivery(buildEstimatedDelivery(rate));

                    options.add(option);
                }
            }
        }

        // 2026-04-22: Stable ordering keeps shipping options predictable in the cart UI.
        options.sort(Comparator
                .comparing(ShippingOption::getCarrierName, Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(opt -> opt.getSpeed() != null ? opt.getSpeed().name() : "")
                .thenComparing(opt -> opt.getDeliveryType() != null ? opt.getDeliveryType().name() : ""));

        return options;
    }

    private String buildOptionTitle(Carrier carrier, CarrierRate rate) {
        String carrierName = carrier.getName();
        String speed = rate.getSpeed() != null ? rate.getSpeed().name() : "STANDARD";
        String deliveryType = rate.getDeliveryType() != null ? rate.getDeliveryType().name().replace('_', ' ') : "DELIVERY";
        return carrierName + " - " + speed + " - " + deliveryType;
    }

    private String buildEstimatedDelivery(CarrierRate rate) {
        if (rate.getEstimatedMinDays() != null && rate.getEstimatedMaxDays() != null) {
            if (rate.getEstimatedMinDays().equals(rate.getEstimatedMaxDays())) {
                return rate.getEstimatedMinDays() + " day";
            }
            return rate.getEstimatedMinDays() + "-" + rate.getEstimatedMaxDays() + " days";
        }

        if (rate.getSpeed() == null) {
            return "Delivery time unavailable";
        }

        return switch (rate.getSpeed()) {
            case EXPRESS ->
                "1-2 days";
            case STANDARD ->
                "3-5 days";
            default ->
                "Delivery time unavailable";
        };
    }

}
