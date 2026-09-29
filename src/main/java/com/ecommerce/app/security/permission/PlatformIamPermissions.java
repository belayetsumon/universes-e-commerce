package com.ecommerce.app.security.permission;

public final class PlatformIamPermissions {

    public static final String READ = "platform.iam.read";
    public static final String MANAGE = "platform.iam.manage";
    public static final String PROTECTED_MANAGE = "platform.iam.protected.manage";

    public static final String CAN_READ = "@platformIamAuthorization.canRead(authentication)";
    public static final String CAN_MANAGE = "@platformIamAuthorization.canManage(authentication)";
    public static final String CAN_MANAGE_PROTECTED = "@platformIamAuthorization.canManageProtected(authentication)";
    public static final String CAN_SEED_PERMISSION_CATALOGUE = "@platformIamAuthorization.canSeedPermissionCatalogue(authentication)";

    private PlatformIamPermissions() {
    }
}
