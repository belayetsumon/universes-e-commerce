/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.vendor.user.services;

import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.model.VendorRole;
import com.ecommerce.app.vendor.user.repository.VendorRoleRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class VendorRoleService {

    @Autowired
    private VendorRoleRepository repository;

    public List<VendorRole> findAll() {
        return repository.findAll();
    }

    public VendorRole findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid ID: " + id));
    }

    public List<VendorRole> findAllByVendor(Vendorprofile vendor) {
        return repository.findByVendorOrderByNameAsc(vendor);
    }

    public VendorRole findByIdAndVendor(Long id, Vendorprofile vendor) {
        return repository.findByIdAndVendor(id, vendor)
                .orElseThrow(() -> new IllegalArgumentException("Invalid vendor role ID: " + id));
    }

    public boolean slugExistsForVendor(Vendorprofile vendor, String slug, Long currentRoleId) {
        if (slug == null || slug.isBlank()) {
            return false;
        }

        if (currentRoleId == null) {
            return repository.existsByVendorAndSlugIgnoreCase(vendor, slug);
        }

        return repository.existsByVendorAndSlugIgnoreCaseAndIdNot(vendor, slug, currentRoleId);
    }

    public VendorRole save(VendorRole role) {
        return repository.save(role);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
