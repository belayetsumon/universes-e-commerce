package com.ecommerce.app.vendor.user.componant;

import com.ecommerce.app.vendor.model.Vendorprofile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class VendorAccessAuthorityChecker {

    private final VendorUserContext vendorUserContext;

    public VendorAccessAuthorityChecker(VendorUserContext vendorUserContext) {
        this.vendorUserContext = vendorUserContext;
    }

    public boolean hasAuthority(Authentication authentication, String privilegeSlug) {
        if (authentication == null || privilegeSlug == null || privilegeSlug.isBlank()) {
            return false;
        }

        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            return false;
        }

        String expectedAuthority = "VENDOR_" + activeVendor.getId() + ":" + privilegeSlug;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority != null && expectedAuthority.equals(authority.getAuthority())) {
                return true;
            }
        }

        return false;
    }
}
