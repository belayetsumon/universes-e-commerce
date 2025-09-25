/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Component.java to edit this template
 */
package com.ecommerce.app.vendor.user.componant;

import com.ecommerce.app.module.user.componant.UserContext;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.repository.UserVendorRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 *
 * @author libertyerp_local
 */
@Component
public class VendorRoleChecker {

    @Autowired
    private UserVendorRoleRepository repo;
    @Autowired
    private VendorUserContext vendorUserContext;

    public boolean hasVendorRole(Authentication auth, String roleName) {
        String username = auth.getName();
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        return repo.existsByUsers_EmailAndVendor_IdAndVendorRole_Name(
                username, activeVendor.getId(), roleName.toUpperCase());
    }
}
