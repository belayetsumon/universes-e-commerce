/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.vendor.user.services;

import com.ecommerce.app.vendor.user.model.VendorPrivilege;
import com.ecommerce.app.vendor.user.repository.VendorPrivilegeRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class VendorPrivilegeService {

    @Autowired
    private VendorPrivilegeRepository repository;

    public List<VendorPrivilege> findAll() {
        return repository.findAll();
    }

    public VendorPrivilege findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid ID: " + id));
    }

    public VendorPrivilege save(VendorPrivilege vendorPrivilege) {
        return repository.save(vendorPrivilege);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
