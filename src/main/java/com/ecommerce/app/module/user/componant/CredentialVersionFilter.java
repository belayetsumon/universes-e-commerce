package com.ecommerce.app.module.user.componant;

import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.SessionCredentialVersionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rejects an authenticated session whose account epoch no longer matches the
 * database. The session marker is created by the login success handler.
 */
public class CredentialVersionFilter extends OncePerRequestFilter {

    private final UsersRepository usersRepository;

    public CredentialVersionFilter(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (usersRepository == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Object sessionVersion = session.getAttribute(
                SessionCredentialVersionService.SESSION_CREDENTIAL_VERSION_ATTRIBUTE);
        // Sessions created by older deployments are not retroactively marked;
        // a new login binds the marker and all changed sessions are still
        // rejected once their marker exists.
        if (sessionVersion == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<Long> currentVersion = usersRepository
                .findCredentialVersionByEmailIgnoreCase(authentication.getName());
        long expectedVersion = toLong(sessionVersion);
        if (currentVersion.isEmpty() || currentVersion.get() != expectedVersion) {
            SecurityContextHolder.clearContext();
            try {
                session.invalidate();
            } catch (IllegalStateException ignored) {
                // The container may already have invalidated the session.
            }
            if (isApiRequest(request)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                response.sendRedirect("/public/member-login?sessionExpired=true");
            }
            return;
        }

        filterChain.doFilter(request, response);
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return Long.MIN_VALUE;
        }
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && (uri.startsWith(request.getContextPath() + "/api/")
                || uri.startsWith(request.getContextPath() + "/actuator/"));
    }
}
