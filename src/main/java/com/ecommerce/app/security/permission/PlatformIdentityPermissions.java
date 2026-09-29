package com.ecommerce.app.security.permission;

public final class PlatformIdentityPermissions {

    public static final String USER_READ = "platform.identity.user.read";
    public static final String USER_MANAGE = "platform.identity.user.manage";
    public static final String PASSWORD_RESET = "platform.identity.password.reset";

    public static final String CAN_READ_USERS = "@platformIdentityAuthorization.canReadUsers(authentication)";
    public static final String CAN_READ_USER = "@platformIdentityAuthorization.canReadUser(authentication, #uid)";
    public static final String CAN_MANAGE_USERS = "@platformIdentityAuthorization.canManageUsers(authentication)";
    public static final String CAN_RESET_PASSWORDS = "@platformIdentityAuthorization.canResetPasswords(authentication)";

    private PlatformIdentityPermissions() {
    }
}
