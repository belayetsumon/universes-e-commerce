package com.ecommerce.app.vendor.user.componant;

import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.repository.UserVendorRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class VendorPrivilegeChecker {

    @Autowired
    private UserVendorRoleRepository repository;

    @Autowired
    private VendorUserContext vendorUserContext;

    public boolean hasPrivilege(Authentication auth, String privilegeSlug) {
        if (auth == null || privilegeSlug == null || privilegeSlug.isBlank()) {
            return false;
        }

        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null) {
            return false;
        }

        return repository.hasVendorPrivilege(auth.getName(), activeVendor.getId(), privilegeSlug);
    }
}
