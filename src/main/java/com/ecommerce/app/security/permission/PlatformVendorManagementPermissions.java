package com.ecommerce.app.security.permission;

public final class PlatformVendorManagementPermissions {

    public static final String READ = "platform.vendor.management.read";
    public static final String MANAGE = "platform.vendor.management.manage";
    public static final String DELETE = "platform.vendor.management.delete";
    public static final String PRIVILEGE = "platform.vendor.management.privilege";

    public static final String CAN_ACCESS = "hasAnyAuthority('" + READ + "', '" + MANAGE + "', '"
            + DELETE + "', '" + PRIVILEGE + "', 'admin', 'ROLE_ADMIN')";
    public static final String CAN_READ = CAN_ACCESS;
    public static final String CAN_MANAGE = "hasAnyAuthority('" + MANAGE + "', 'admin', 'ROLE_ADMIN')";
    public static final String CAN_DELETE = "hasAnyAuthority('" + DELETE + "', 'admin', 'ROLE_ADMIN')";
    public static final String CAN_MANAGE_PRIVILEGES = "hasAnyAuthority('" + PRIVILEGE
            + "', 'admin', 'ROLE_ADMIN')";

    private PlatformVendorManagementPermissions() {
    }
}
