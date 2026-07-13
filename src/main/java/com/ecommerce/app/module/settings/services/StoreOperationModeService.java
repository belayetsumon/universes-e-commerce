package com.ecommerce.app.module.settings.services;

import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.model.SalesOrderMode;
import com.ecommerce.app.module.settings.model.StoreMode;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StoreOperationModeService {

    private final GlobalSettingsService globalSettingsService;
    private final VendorprofileRepository vendorprofileRepository;

    public StoreOperationModeService(
            GlobalSettingsService globalSettingsService,
            VendorprofileRepository vendorprofileRepository
    ) {
        this.globalSettingsService = globalSettingsService;
        this.vendorprofileRepository = vendorprofileRepository;
    }

    public GlobalSettings settings() {
        return globalSettingsService.getActiveSettings();
    }

    public StoreMode storeMode() {
        StoreMode mode = settings().getStoreMode();
        return mode != null ? mode : StoreMode.MARKETPLACE;
    }

    public SalesOrderMode salesOrderMode() {
        SalesOrderMode mode = settings().getSalesOrderMode();
        return mode != null ? mode : SalesOrderMode.SPLIT_BY_VENDOR;
    }

    public boolean isSingleVendorMode() {
        return storeMode() == StoreMode.SINGLE_VENDOR;
    }

    public boolean isMarketplaceMode() {
        return storeMode() == StoreMode.MARKETPLACE;
    }

    public boolean shouldSplitOrdersByVendor() {
        return salesOrderMode() == SalesOrderMode.SPLIT_BY_VENDOR || isMarketplaceMode();
    }

    public boolean shouldUseCustomerOrderGroup() {
        return isMarketplaceMode() && salesOrderMode() == SalesOrderMode.SINGLE_ORDER;
    }

    public boolean isGuestCheckoutAllowed() {
        return Boolean.TRUE.equals(settings().getAllowGuestCheckout());
    }

    public boolean isSecureCheckoutEnabled() {
        return Boolean.TRUE.equals(settings().getSecureCheckoutEnabled());
    }

    public boolean isGuestMobileRequired() {
        return Boolean.TRUE.equals(settings().getGuestMobileRequired());
    }

    public boolean isGuestMobileOtpVerificationEnabled() {
        return Boolean.TRUE.equals(settings().getGuestMobileOtpVerificationEnabled());
    }

    public boolean isGuestAutoCreateCustomerAccountEnabled() {
        return Boolean.TRUE.equals(settings().getGuestAutoCreateCustomerAccount());
    }

    public int guestOtpExpiryMinutes() {
        Integer value = settings().getGuestOtpExpiryMinutes();
        return value != null && value > 0 ? value : 5;
    }

    public int guestOtpMaximumAttempts() {
        Integer value = settings().getGuestOtpMaximumAttempts();
        return value != null && value > 0 ? value : 5;
    }

    public int guestOtpResendCooldownSeconds() {
        Integer value = settings().getGuestOtpResendCooldownSeconds();
        return value != null && value >= 0 ? value : 60;
    }

    public int guestOtpDailySendLimit() {
        Integer value = settings().getGuestOtpDailySendLimit();
        return value != null && value > 0 ? value : 5;
    }

    public Long primaryVendorId() {
        return settings().getPrimaryVendorId();
    }

    public Optional<Vendorprofile> primaryVendor() {
        Long primaryVendorId = primaryVendorId();
        if (primaryVendorId == null) {
            return Optional.empty();
        }
        return vendorprofileRepository.findById(primaryVendorId);
    }

    public Vendorprofile requirePrimaryVendor() {
        return primaryVendor().orElseThrow(
                () -> new IllegalStateException("Primary vendor is required for single-vendor mode.")
        );
    }
}
