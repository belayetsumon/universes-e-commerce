package com.ecommerce.app.security.permission;

public final class PlatformSecurityAuditPermissions {

    public static final String READ = "platform.security.audit.read";

    public static final String CAN_READ = "hasAnyAuthority('" + READ + "', 'admin', 'ROLE_ADMIN')";

    private PlatformSecurityAuditPermissions() {
    }
}
