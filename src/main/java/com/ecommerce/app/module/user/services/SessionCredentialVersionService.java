package com.ecommerce.app.module.user.services;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns the account credential/access epoch and the in-process session registry
 * used to revoke sessions after sensitive account changes.
 */
@Service
public class SessionCredentialVersionService {

    public static final String SESSION_CREDENTIAL_VERSION_ATTRIBUTE =
            "com.ecommerce.app.security.credentialVersion";

    private final UsersRepository usersRepository;
    private final SessionRegistry sessionRegistry;

    public SessionCredentialVersionService(
            UsersRepository usersRepository,
            SessionRegistry sessionRegistry) {
        this.usersRepository = usersRepository;
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * Applies a password change and advances the account epoch in one locked
     * transaction. The encoded password is never logged or returned.
     */
    @Transactional
    public boolean updatePassword(Long userId, String encodedPassword, boolean passwordConfigured) {
        if (userId == null || encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }

        Users user = usersRepository.findByIdForUpdate(userId).orElse(null);
        if (user == null) {
            return false;
        }

        user.setPassword(encodedPassword);
        user.setPasswordConfigured(passwordConfigured);
        advanceCredentialVersion(user);
        usersRepository.save(user);
        invalidateSessionsForUsername(user.getEmail());
        return true;
    }

    /**
     * Advances the epoch for account-access changes such as blocking an
     * account, then expires every known session for that account.
     */
    @Transactional
    public boolean bumpCredentialVersion(Long userId) {
        if (userId == null) {
            return false;
        }

        Users user = usersRepository.findByIdForUpdate(userId).orElse(null);
        if (user == null) {
            return false;
        }

        advanceCredentialVersion(user);
        usersRepository.save(user);
        invalidateSessionsForUsername(user.getEmail());
        return true;
    }

    @Transactional(readOnly = true)
    public void invalidateSessionsForUserId(Long userId) {
        if (userId == null) {
            return;
        }
        usersRepository.findById(userId)
                .map(Users::getEmail)
                .ifPresent(this::invalidateSessionsForUsername);
    }

    /**
     * Captures the current epoch in the authenticated HTTP session immediately
     * after login. A missing database row deliberately does not create a
     * session marker.
     */
    @Transactional(readOnly = true)
    public void captureCurrentVersion(HttpServletRequest request, Authentication authentication) {
        if (request == null || authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || authentication.getName().isBlank()) {
            return;
        }

        Optional<Long> version = usersRepository.findCredentialVersionByEmailIgnoreCase(authentication.getName());
        if (version.isEmpty()) {
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_CREDENTIAL_VERSION_ATTRIBUTE, version.get());
    }

    /**
     * Expires sessions registered for a username. SessionRegistry is local to
     * a node; the database epoch remains the cross-node source of truth.
     */
    public void invalidateSessionsForUsername(String username) {
        if (username == null || username.isBlank() || sessionRegistry == null) {
            return;
        }

        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (!Objects.equals(normalizeUsername(principal), normalizeUsername(username))) {
                continue;
            }
            for (SessionInformation session : sessionRegistry.getAllSessions(principal, false)) {
                if (session != null) {
                    session.expireNow();
                }
            }
        }
    }

    private void advanceCredentialVersion(Users user) {
        long current = user.getCredentialVersion();
        user.setCredentialVersion(current == Long.MAX_VALUE ? 1L : current + 1L);
    }

    private String normalizeUsername(Object principal) {
        if (principal instanceof UserDetails userDetails) {
            return normalizeUsername(userDetails.getUsername());
        }
        return normalizeUsername(principal == null ? null : principal.toString());
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
