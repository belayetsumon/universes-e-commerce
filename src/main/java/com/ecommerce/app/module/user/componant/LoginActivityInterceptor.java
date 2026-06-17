package com.ecommerce.app.module.user.componant;

import com.ecommerce.app.module.user.services.LoginEventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginActivityInterceptor implements HandlerInterceptor {

    private static final String LAST_ACTIVITY_SYNC_KEY = "loginHistoryLastActivitySyncAt";
    private static final long MIN_SYNC_INTERVAL_MS = 60_000L;

    @Autowired
    private LoginEventService loginEventService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated()) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || !shouldSync(session)) {
            return true;
        }

        loginEventService.touchLastActivity(authentication.getName(), session.getId());
        session.setAttribute(LAST_ACTIVITY_SYNC_KEY, System.currentTimeMillis());
        return true;
    }

    private boolean shouldSync(HttpSession session) {
        Object lastSyncedAt = session.getAttribute(LAST_ACTIVITY_SYNC_KEY);
        if (lastSyncedAt instanceof Long lastSyncedMillis) {
            return System.currentTimeMillis() - lastSyncedMillis >= MIN_SYNC_INTERVAL_MS;
        }
        if (lastSyncedAt instanceof Number lastSyncedNumber) {
            return System.currentTimeMillis() - lastSyncedNumber.longValue() >= MIN_SYNC_INTERVAL_MS;
        }
        return true;
    }
}
