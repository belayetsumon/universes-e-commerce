package com.ecommerce.app.module.checkout.availability;

import com.ecommerce.app.module.settings.services.StoreOperationModeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CheckoutAvailabilityService {

    public static final String CHECKOUT_UNAVAILABLE_MESSAGE =
            "Checkout is currently unavailable. Purchasing has been temporarily disabled by the store administrator. Please try again later or contact customer support for assistance.";

    private final StoreOperationModeService storeOperationModeService;

    public CheckoutAvailabilityService(StoreOperationModeService storeOperationModeService) {
        this.storeOperationModeService = storeOperationModeService;
    }

    public CheckoutAvailability availability(boolean authenticated) {
        boolean secureCheckoutEnabled = storeOperationModeService.isSecureCheckoutEnabled();
        boolean guestCheckoutEnabled = storeOperationModeService.isGuestCheckoutAllowed();
        boolean checkoutAvailable = secureCheckoutEnabled || guestCheckoutEnabled;

        if (!checkoutAvailable) {
            return new CheckoutAvailability(
                    false,
                    false,
                    authenticated,
                    false,
                    false,
                    false,
                    false,
                    null,
                    CHECKOUT_UNAVAILABLE_MESSAGE
            );
        }

        if (authenticated) {
            return new CheckoutAvailability(
                    secureCheckoutEnabled,
                    guestCheckoutEnabled,
                    true,
                    true,
                    false,
                    guestCheckoutEnabled,
                    false,
                    "/order/create",
                    null
            );
        }

        if (secureCheckoutEnabled) {
            return new CheckoutAvailability(
                    true,
                    guestCheckoutEnabled,
                    false,
                    true,
                    true,
                    guestCheckoutEnabled,
                    true,
                    null,
                    null
            );
        }

        return new CheckoutAvailability(
                false,
                true,
                false,
                true,
                false,
                true,
                false,
                "/cart/checkout",
                null
        );
    }

    public void requireCheckoutAvailable() {
        if (!storeOperationModeService.isSecureCheckoutEnabled()
                && !storeOperationModeService.isGuestCheckoutAllowed()) {
            throw new CheckoutUnavailableException(CHECKOUT_UNAVAILABLE_MESSAGE);
        }
    }

    public void requireCheckoutAccess(boolean authenticated) {
        CheckoutAvailability availability = availability(authenticated);
        if (!availability.isCheckoutAvailable()) {
            throw new CheckoutUnavailableException(CHECKOUT_UNAVAILABLE_MESSAGE);
        }
        if (!authenticated && availability.isLoginRequired() && !availability.isGuestAllowed()) {
            throw new CheckoutUnavailableException("Please login before checkout.");
        }
    }
}
