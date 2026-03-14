/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.model.ShippingProfile;
import com.ecommerce.app.module.shipping.repository.ShippingProfileRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class ShippingProfileService {

    private final ShippingProfileRepository repo;

    public ShippingProfileService(ShippingProfileRepository repo) {
        this.repo = repo;
    }

    public List<ShippingProfile> getAll() {
        return repo.findAll();
    }

    public ShippingProfile getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public ShippingProfile getByVendor(Long vendorId) {
        return repo.findByVendorId(vendorId);
    }

    public ShippingProfile save(ShippingProfile p) {
        return repo.save(p);
    }

    public void delete(Long id) {
        repo.deleteById(id);

    }
}
