/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.model.Carrier;
import com.ecommerce.app.module.shipping.model.ShippingProfile;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.shipping.repository.ShippingProfileRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author libertyerp_local
 */
@Service
public class ShippingProfileService {

    private final ShippingProfileRepository repo;
    private final CarrierService carrierService;
    private final ShippingLocationService locationService;

    public ShippingProfileService(
            ShippingProfileRepository repo,
            CarrierService carrierService,
            ShippingLocationService locationService) {
        this.repo = repo;
        this.carrierService = carrierService;
        this.locationService = locationService;
    }

    public List<ShippingProfile> getAll() {
        return repo.findAll();
    }

    public ShippingProfile getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public ShippingProfile getByVendor(Long vendorId) {
        if (vendorId == null) {
            return null;
        }
        return repo.findByVendorIdOrderByIdAsc(vendorId).stream().findFirst().orElse(null);
    }

    public ShippingProfile save(ShippingProfile p) {
        return repo.save(p);
    }

    public boolean hasAnotherProfileForVendor(Long vendorId, Long currentProfileId) {
        if (vendorId == null) {
            return false;
        }
        return repo.findByVendorIdOrderByIdAsc(vendorId).stream()
                .anyMatch(profile -> currentProfileId == null || !currentProfileId.equals(profile.getId()));
    }

    @Transactional
    public ShippingProfile saveVendorProfile(
            Long vendorId,
            ShippingProfile submittedProfile,
            Collection<Long> allowedCarrierIds,
            Collection<Long> allowedLocationIds) {
        if (vendorId == null) {
            throw new IllegalArgumentException("Active vendor is required.");
        }
        if (submittedProfile == null) {
            throw new IllegalArgumentException("Shipping profile is required.");
        }
        if (submittedProfile.getName() == null || submittedProfile.getName().trim().isBlank()) {
            throw new IllegalArgumentException("Profile name is required.");
        }
        if (submittedProfile.getType() == null) {
            throw new IllegalArgumentException("Profile type is required.");
        }

        List<Carrier> selectedCarriers = carrierService.findAllById(
                allowedCarrierIds == null ? List.of() : allowedCarrierIds.stream().distinct().toList()
        );
        if (selectedCarriers.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one allowed carrier.");
        }

        List<ShippingLocation> selectedLocations = locationService.findAllById(
                allowedLocationIds == null ? List.of() : allowedLocationIds.stream().distinct().toList()
        );
        if (selectedLocations.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one allowed location.");
        }

        ShippingProfile profile = getByVendor(vendorId);
        if (profile == null) {
            profile = new ShippingProfile();
            profile.setVendorId(vendorId);
        }

        profile.setName(submittedProfile.getName().trim());
        profile.setType(submittedProfile.getType());
        profile.setAllowedCarriers(selectedCarriers);
        profile.setAllowedDistricts(selectedLocations);
        profile.setActive(submittedProfile.isActive());
        profile.setVendorId(vendorId);

        return repo.save(profile);
    }

    public void delete(Long id) {
        repo.deleteById(id);

    }
}
