package com.ecommerce.app.vendor.user.services;

import com.ecommerce.app.vendor.user.model.VendorPrivilege;
import com.ecommerce.app.vendor.user.repository.VendorPrivilegeRepository;
import java.util.List;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendorPermissionCatalogueSeedService {

    private static final List<VendorPermissionSeed> REQUIRED_VENDOR_PERMISSIONS = List.of(
            new VendorPermissionSeed("Vendor Stock Read", "vendor.stock.read"),
            new VendorPermissionSeed("Vendor Stock Manage", "vendor.stock.manage"),
            new VendorPermissionSeed("Vendor Shipping Read", "vendor.shipping.read"),
            new VendorPermissionSeed("Vendor Shipping Manage", "vendor.shipping.manage")
    );

    private final VendorPrivilegeRepository vendorPrivilegeRepository;

    public VendorPermissionCatalogueSeedService(VendorPrivilegeRepository vendorPrivilegeRepository) {
        this.vendorPrivilegeRepository = vendorPrivilegeRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedRequiredVendorPermissions() {
        for (VendorPermissionSeed seed : REQUIRED_VENDOR_PERMISSIONS) {
            if (vendorPrivilegeRepository.findFirstBySlugIgnoreCase(seed.slug()).isPresent()) {
                continue;
            }
            VendorPrivilege privilege = new VendorPrivilege();
            privilege.setName(seed.name());
            privilege.setSlug(seed.slug());
            vendorPrivilegeRepository.save(privilege);
        }
    }

    private record VendorPermissionSeed(String name, String slug) {
    }
}
