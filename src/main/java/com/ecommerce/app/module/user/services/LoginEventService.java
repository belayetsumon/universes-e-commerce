/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.user.services;

import com.ecommerce.app.module.user.model.LoginHistory;
import com.ecommerce.app.module.user.model.LoginStatus;
import com.ecommerce.app.module.user.model.LogoutReason;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.LoginHistoryRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 *
 * @author libertyerp_local
 */
@Service
public class LoginEventService {

    private static final int MAX_FAILURE_REASON_LENGTH = 500;

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    @Autowired
    UsersRepository usersRepository;

    @Transactional
    public void logLoginTime(String username) {
        String attemptedUsername = trimToNull(username);
        Users user = resolveUser(attemptedUsername);
        if (user == null) {
            return;
        }

        LocalDateTime loginTime = LocalDateTime.now();
        user.setLastLogin(new Date());
        usersRepository.save(user);

        LoginHistory loginHistory = buildLoginHistory(user, attemptedUsername, loginTime);
        loginHistory.setLoginTime(loginTime);
        loginHistory.setLastActivityAt(loginTime);
        loginHistory.setLoginStatus(LoginStatus.ACTIVE);
        loginHistoryRepository.save(loginHistory);
    }

    @Transactional
    public void logFailedLogin(String attemptedUsername, String failureReason) {
        String normalizedAttemptedUsername = trimToNull(attemptedUsername);
        LocalDateTime failureTime = LocalDateTime.now();

        LoginHistory loginHistory = buildLoginHistory(resolveUser(normalizedAttemptedUsername), normalizedAttemptedUsername, failureTime);
        loginHistory.setLoginTime(failureTime);
        loginHistory.setLastActivityAt(failureTime);
        loginHistory.setLoginStatus(LoginStatus.FAILED);
        loginHistory.setFailureReason(limit(failureReason, MAX_FAILURE_REASON_LENGTH));
        loginHistoryRepository.save(loginHistory);
    }

    @Transactional
    public void logLogoutTime(String username) {
        logLogoutTime(username, resolveSessionId(), LogoutReason.MANUAL_LOGOUT, false);
    }

    @Transactional
    public void logLogoutTime(String username, boolean updateUserTimestampWithoutOpenSession) {
        logLogoutTime(username, resolveSessionId(), LogoutReason.MANUAL_LOGOUT, updateUserTimestampWithoutOpenSession);
    }

    @Transactional
    public void logSessionExpired(String username, String sessionId) {
        logLogoutTime(username, trimToNull(sessionId), LogoutReason.SESSION_EXPIRED, false);
    }

    @Transactional
    public void touchLastActivity(String username, String sessionId) {
        String normalizedSessionId = trimToNull(sessionId);
        Users user = resolveUser(username);
        Optional<LoginHistory> openHistory = findOpenLoginHistory(user, normalizedSessionId);

        openHistory.ifPresent(loginHistory -> {
            loginHistory.setLastActivityAt(LocalDateTime.now());
            loginHistoryRepository.save(loginHistory);
        });
    }

    @Transactional
    private void logLogoutTime(String username, String sessionId, LogoutReason logoutReason, boolean updateUserTimestampWithoutOpenSession) {
        Users user = resolveUser(username);
        if (user == null) {
            return;
        }

        LocalDateTime logoutTime = LocalDateTime.now();
        Optional<LoginHistory> openHistory = findOpenLoginHistory(user, sessionId);

        boolean loginHistoryClosed = openHistory
                .map(loginHistory -> {
                    loginHistory.setLogoutTime(logoutTime);
                    loginHistory.setLastActivityAt(logoutTime);
                    loginHistory.setLogoutReason(logoutReason);
                    loginHistory.setLoginStatus(resolveClosedStatus(logoutReason));
                    loginHistoryRepository.save(loginHistory);
                    return true;
                })
                .orElse(false);

        if (loginHistoryClosed || updateUserTimestampWithoutOpenSession) {
            user.setLastLogOut(new Date());
            usersRepository.save(user);
        }
    }

    private Optional<LoginHistory> findOpenLoginHistory(Users user, String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            return loginHistoryRepository.findTopBySessionIdAndLoginStatusAndLogoutTimeIsNullOrderByLoginTimeDesc(sessionId, LoginStatus.ACTIVE);
        }

        if (user == null) {
            return Optional.empty();
        }

        return loginHistoryRepository.findTopByUserAndLoginStatusAndLogoutTimeIsNullOrderByLoginTimeDesc(user, LoginStatus.ACTIVE);
    }

    private LoginStatus resolveClosedStatus(LogoutReason logoutReason) {
        if (logoutReason == LogoutReason.SESSION_EXPIRED) {
            return LoginStatus.SESSION_EXPIRED;
        }
        return LoginStatus.LOGGED_OUT;
    }

    private String resolveIpAddress() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }

        String forwardedFor = extractFirstValue(request.getHeader("X-Forwarded-For"));
        if (forwardedFor != null) {
            return forwardedFor;
        }

        String realIp = trimToNull(request.getHeader("X-Real-IP"));
        if (realIp != null) {
            return realIp;
        }

        return trimToNull(request.getRemoteAddr());
    }

    private String resolveSessionId() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        return trimToNull(session.getId());
    }

    private String resolveUserAgent() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }

        return trimToNull(request.getHeader("User-Agent"));
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }

        return servletRequestAttributes.getRequest();
    }

    private LoginHistory buildLoginHistory(Users user, String attemptedUsername, LocalDateTime activityTime) {
        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setUser(user);
        loginHistory.setAttemptedUsername(trimToNull(attemptedUsername));
        loginHistory.setIpAddress(resolveIpAddress());
        loginHistory.setSessionId(resolveSessionId());
        loginHistory.setUserAgent(resolveUserAgent());
        loginHistory.setLastActivityAt(activityTime);
        return loginHistory;
    }

    private Users resolveUser(String username) {
        String normalizedUsername = trimToNull(username);
        if (normalizedUsername == null) {
            return null;
        }

        return usersRepository.findByEmail(normalizedUsername).orElse(null);
    }

    private String extractFirstValue(String value) {
        String trimmedValue = trimToNull(value);
        if (trimmedValue == null) {
            return null;
        }

        int commaIndex = trimmedValue.indexOf(',');
        if (commaIndex < 0) {
            return trimmedValue;
        }

        return trimToNull(trimmedValue.substring(0, commaIndex));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String limit(String value, int maxLength) {
        String normalizedValue = trimToNull(value);
        if (normalizedValue == null || maxLength <= 0) {
            return normalizedValue;
        }

        if (normalizedValue.length() <= maxLength) {
            return normalizedValue;
        }

        return normalizedValue.substring(0, maxLength);
    }

}
