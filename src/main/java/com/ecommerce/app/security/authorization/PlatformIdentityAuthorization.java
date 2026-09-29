package com.ecommerce.app.security.authorization;

import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.security.permission.PlatformIdentityPermissions;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component("platformIdentityAuthorization")
public class PlatformIdentityAuthorization {

    private static final Set<String> LEGACY_PLATFORM_ADMIN_AUTHORITIES = Set.of("admin", "ROLE_ADMIN");

    private final UsersRepository usersRepository;

    public PlatformIdentityAuthorization(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public boolean canReadUsers(Authentication authentication) {
        return isAuthenticated(authentication)
                && (hasAuthority(authentication, PlatformIdentityPermissions.USER_READ)
                || canManageUsers(authentication));
    }

    public boolean canReadUser(Authentication authentication, Long userId) {
        if (canReadUsers(authentication)) {
            return true;
        }
        return isAuthenticated(authentication)
                && userId != null
                && authentication.getName() != null
                && usersRepository.existsByIdAndEmail(userId, authentication.getName());
    }

    public boolean canManageUsers(Authentication authentication) {
        return isAuthenticated(authentication)
                && (hasAuthority(authentication, PlatformIdentityPermissions.USER_MANAGE)
                || isLegacyPlatformAdmin(authentication));
    }

    public boolean canResetPasswords(Authentication authentication) {
        return isAuthenticated(authentication)
                && (hasAuthority(authentication, PlatformIdentityPermissions.PASSWORD_RESET)
                || isLegacyPlatformAdmin(authentication));
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
}
