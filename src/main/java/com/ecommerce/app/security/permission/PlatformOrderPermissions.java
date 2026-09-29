package com.ecommerce.app.security.permission;

/**
 * Named platform capability for the marketplace-wide order administration view.
 */
public final class PlatformOrderPermissions {

    public static final String READ = "platform.order.payment.read";
    public static final String CAN_READ = "hasAnyAuthority('" + READ + "', 'admin', 'ROLE_ADMIN')";

    private PlatformOrderPermissions() {
    }
}
