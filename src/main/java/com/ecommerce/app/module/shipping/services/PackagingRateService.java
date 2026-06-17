/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.exception.UniqueConstraintViolationException;
import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.module.shipping.model.PackagingRate;
import com.ecommerce.app.module.shipping.repository.PackagingRateRepository;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author libertyerp_local
 */
@Service
public class PackagingRateService {

    @Autowired
    private PackagingRateRepository repository;

    public List<PackagingRate> getAll() {
        return repository.findAll();
    }

    public PackagingRate getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Packaging Rate not found"));
    }

    @Transactional
    public void save(PackagingRate rate) {
        boolean exists = repository.existsByPackagingTypeAndVendorId(rate.getPackagingType(), rate.getVendor().getId());
        if (exists && rate.getId() == null) {
            throw new UniqueConstraintViolationException("Rate for this packaging type already exists!");
        }
        repository.save(rate);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<PackagingRate> getByVendor(Long vendorId) {
        return repository.findByVendorId(vendorId);
    }

    public List<PackagingRate> getByVendorUuid(String vendorUuid) {
        if (vendorUuid == null || vendorUuid.isBlank()) {
            return List.of();
        }
        return repository.findByVendor_Uuid(vendorUuid);
    }

    public BigDecimal calculatePackaging(Long packagingId, List<CartItem> vendorCart, HttpSession session) {
        if (packagingId == null) {
            return BigDecimal.ZERO;
        }

        Map<Long, PackagingRate> packagingMap = (Map<Long, PackagingRate>) session.getAttribute("packagingOptions");
        if (packagingMap != null && packagingMap.containsKey(packagingId)) {
            PackagingRate pack = packagingMap.get(packagingId);
            BigDecimal totalWeight = vendorCart.stream()
                    .map(CartItem::getWeight)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return pack.getBasePrice().add(pack.getAdditionalPrice().multiply(totalWeight));
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calculateRateOne(Long rateId, double baseWeightLimit, double totalWeight) {
        PackagingRate rate = repository.findById(rateId)
                .orElseThrow(() -> new RuntimeException("Packaging Rate not found"));

        BigDecimal basePrice = rate.getBasePrice();
        BigDecimal additionalPrice = rate.getAdditionalPrice();
        BigDecimal baseLimit = BigDecimal.valueOf(baseWeightLimit);
        BigDecimal weight = BigDecimal.valueOf(totalWeight);

        // If within base weight, return only base price
        if (weight.compareTo(baseLimit) <= 0) {
            return basePrice;
        }

        // Otherwise calculate extra cost
        BigDecimal extraWeight = weight.subtract(baseLimit);
        return basePrice.add(additionalPrice.multiply(extraWeight));
    }

    public BigDecimal calculateRateOneByUuid(String rateUuid, double baseWeightLimit, double totalWeight) {
        PackagingRate rate = repository.findByUuid(rateUuid)
                .orElseThrow(() -> new RuntimeException("Packaging Rate not found"));

        BigDecimal basePrice = rate.getBasePrice();
        BigDecimal additionalPrice = rate.getAdditionalPrice();
        BigDecimal baseLimit = BigDecimal.valueOf(baseWeightLimit);
        BigDecimal weight = BigDecimal.valueOf(totalWeight);

        if (weight.compareTo(baseLimit) <= 0) {
            return basePrice;
        }

        BigDecimal extraWeight = weight.subtract(baseLimit);
        return basePrice.add(additionalPrice.multiply(extraWeight));
    }

    public BigDecimal calculateRateByVendor(Long vendorId,
            double baseWeightLimit,
            double totalWeight) {

        PackagingRate rate = repository
                .findFirstByVendorIdAndActiveTrue(vendorId)
                .orElseThrow(() -> new RuntimeException("No active rate found for this vendor"));

        BigDecimal basePrice = rate.getBasePrice();
        BigDecimal additionalPrice = rate.getAdditionalPrice();
        BigDecimal baseLimit = BigDecimal.valueOf(baseWeightLimit);
        BigDecimal weight = BigDecimal.valueOf(totalWeight);

        if (weight.compareTo(baseLimit) <= 0) {
            return basePrice;
        }

        BigDecimal extraWeight = weight.subtract(baseLimit);
        return basePrice.add(additionalPrice.multiply(extraWeight));
    }

    public BigDecimal calculateRates(Long vendorId,
            Long rateId,
            double baseWeightLimit,
            double totalWeight) {

        // First confirm the rate belongs to the vendor
        PackagingRate rate = repository.findById(rateId)
                .orElseThrow(() -> new RuntimeException("Rate not found"));

        if (!rate.getVendor().getId().equals(vendorId)) {
            throw new RuntimeException("This rate does not belong to the given vendor");
        }

        BigDecimal basePrice = rate.getBasePrice();
        BigDecimal additionalPrice = rate.getAdditionalPrice();
        BigDecimal baseLimit = BigDecimal.valueOf(baseWeightLimit);
        BigDecimal weight = BigDecimal.valueOf(totalWeight);

        if (weight.compareTo(baseLimit) <= 0) {
            return basePrice;
        }

        BigDecimal extraWeight = weight.subtract(baseLimit);
        return basePrice.add(additionalPrice.multiply(extraWeight));
    }

    public BigDecimal calculateRate(Long vendorId,
            Long rateId,
            double baseWeightLimit,
            double totalWeight, HttpSession session) {

        // First confirm the rate belongs to the vendor
        PackagingRate rate = repository.findById(rateId)
                .orElseThrow(() -> new RuntimeException("Rate not found"));

        if (!rate.getVendor().getId().equals(vendorId)) {
            throw new RuntimeException("This rate does not belong to the given vendor");
        }

        BigDecimal basePrice = rate.getBasePrice();
        BigDecimal additionalPrice = rate.getAdditionalPrice();
        BigDecimal baseLimit = BigDecimal.valueOf(baseWeightLimit);
        BigDecimal weight = BigDecimal.valueOf(totalWeight);

        if (weight.compareTo(baseLimit) <= 0) {
            return basePrice;
        }

        BigDecimal extraWeight = weight.subtract(baseLimit);
        return basePrice.add(additionalPrice.multiply(extraWeight));
    }

}
