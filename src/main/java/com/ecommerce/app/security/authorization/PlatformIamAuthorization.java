package com.ecommerce.app.security.authorization;

import com.ecommerce.app.module.user.model.Privilege;
import com.ecommerce.app.security.permission.PlatformIamPermissions;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component("platformIamAuthorization")
public class PlatformIamAuthorization {

    private static final Set<String> LEGACY_PLATFORM_ADMIN_AUTHORITIES = Set.of("admin", "ROLE_ADMIN");
    private static final Set<String> PROTECTED_ROLE_SLUGS = Set.of(
            "admin", "platform-admin", "super-admin", "customer", "vendor");
    private static final Set<String> NON_ASSIGNABLE_PREFIXES = Set.of(
            "public.", "internal.", "webhook.", "api.", "vendor.");

    public boolean canRead(Authentication authentication) {
        return isAuthenticated(authentication)
                && (hasAuthority(authentication, PlatformIamPermissions.READ)
                || canManage(authentication));
    }

    public boolean canManage(Authentication authentication) {
        return isAuthenticated(authentication)
                && (hasAuthority(authentication, PlatformIamPermissions.MANAGE)
                || hasAuthority(authentication, PlatformIamPermissions.PROTECTED_MANAGE)
                || isLegacyPlatformAdmin(authentication));
    }

    public boolean canManageProtected(Authentication authentication) {
        return isAuthenticated(authentication)
                && hasAuthority(authentication, PlatformIamPermissions.PROTECTED_MANAGE);
    }

    public boolean isProtectedRole(String roleSlug) {
        String normalizedSlug = normalize(roleSlug);
        return normalizedSlug != null && PROTECTED_ROLE_SLUGS.contains(normalizedSlug);
    }

    public void assertCanGrant(Authentication authentication, Collection<Privilege> privileges) {
        if (!canManage(authentication)) {
            throw new AccessDeniedException("IAM management permission is required.");
        }

        Set<String> actorAuthorities = authorityNames(authentication);

        for (Privilege privilege : privileges) {
            String permission = privilege == null ? null : normalize(privilege.getSlug());
            if (permission == null) {
                throw new AccessDeniedException("Every assigned permission must have an approved catalogue slug.");
            }
            if (isNonAssignablePolicy(permission)) {
                throw new AccessDeniedException("The permission is a protected policy capability and cannot be assigned to a role.");
            }
            if (isProtectedPermission(permission) && !canManageProtected(authentication)) {
                throw new AccessDeniedException("Protected IAM permission management is required.");
            }
            if (!actorAuthorities.contains(permission)) {
                throw new AccessDeniedException("A role cannot receive permissions broader than the actor's effective access.");
            }
        }
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authorityNames(authentication).contains(authority);
    }

    private boolean isLegacyPlatformAdmin(Authentication authentication) {
        return authorityNames(authentication).stream().anyMatch(LEGACY_PLATFORM_ADMIN_AUTHORITIES::contains);
    }

    private Set<String> authorityNames(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return Set.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority != null && !authority.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private boolean isNonAssignablePolicy(String permission) {
        return NON_ASSIGNABLE_PREFIXES.stream().anyMatch(permission::startsWith);
    }

    private boolean isProtectedPermission(String permission) {
        return permission.equals(PlatformIamPermissions.PROTECTED_MANAGE)
                || permission.startsWith("platform.iam.protected.");
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
