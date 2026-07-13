package com.ecommerce.app.module.checkout.availability;

public class CheckoutAvailability {

    private final boolean secureCheckoutEnabled;
    private final boolean guestCheckoutEnabled;
    private final boolean authenticated;
    private final boolean checkoutAvailable;
    private final boolean loginRequired;
    private final boolean guestAllowed;
    private final boolean showAuthenticationModal;
    private final String nextUrl;
    private final String message;

    public CheckoutAvailability(
            boolean secureCheckoutEnabled,
            boolean guestCheckoutEnabled,
            boolean authenticated,
            boolean checkoutAvailable,
            boolean loginRequired,
            boolean guestAllowed,
            boolean showAuthenticationModal,
            String nextUrl,
            String message) {
        this.secureCheckoutEnabled = secureCheckoutEnabled;
        this.guestCheckoutEnabled = guestCheckoutEnabled;
        this.authenticated = authenticated;
        this.checkoutAvailable = checkoutAvailable;
        this.loginRequired = loginRequired;
        this.guestAllowed = guestAllowed;
        this.showAuthenticationModal = showAuthenticationModal;
        this.nextUrl = nextUrl;
        this.message = message;
    }

    public boolean isSecureCheckoutEnabled() {
        return secureCheckoutEnabled;
    }

    public boolean isGuestCheckoutEnabled() {
        return guestCheckoutEnabled;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public boolean isCheckoutAvailable() {
        return checkoutAvailable;
    }

    public boolean isLoginRequired() {
        return loginRequired;
    }

    public boolean isGuestAllowed() {
        return guestAllowed;
    }

    public boolean isShowAuthenticationModal() {
        return showAuthenticationModal;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public String getMessage() {
        return message;
    }
}
