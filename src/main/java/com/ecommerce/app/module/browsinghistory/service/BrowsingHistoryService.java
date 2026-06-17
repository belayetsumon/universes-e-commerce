package com.ecommerce.app.module.browsinghistory.service;

import com.ecommerce.app.module.browsinghistory.model.BrowsingHistory;
import com.ecommerce.app.module.browsinghistory.model.BrowsingHistoryViewType;
import com.ecommerce.app.module.browsinghistory.repository.BrowsingHistoryRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.Productcategory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrowsingHistoryService {

    private static final String BROWSER_ID_COOKIE = "browser_id";
    private static final int BROWSER_ID_MAX_AGE_SECONDS = 60 * 60 * 24 * 365;
    private static final int MAX_IP_LENGTH = 120;
    private static final int MAX_USER_AGENT_LENGTH = 1200;

    @Autowired
    private BrowsingHistoryRepository browsingHistoryRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Transactional
    public void recordProductView(Product product, HttpServletRequest request, HttpServletResponse response) {
        if (product == null || product.getId() == null) {
            return;
        }

        BrowsingHistory browsingHistory = buildBaseHistory(request, response);
        browsingHistory.setViewType(BrowsingHistoryViewType.PRODUCT);
        browsingHistory.setProduct(product);
        browsingHistoryRepository.save(browsingHistory);
    }

    @Transactional
    public void recordCategoryView(Productcategory category, HttpServletRequest request, HttpServletResponse response) {
        if (category == null || category.getId() == null) {
            return;
        }

        BrowsingHistory browsingHistory = buildBaseHistory(request, response);
        browsingHistory.setViewType(BrowsingHistoryViewType.CATEGORY);
        browsingHistory.setCategory(category);
        browsingHistoryRepository.save(browsingHistory);
    }

    @Transactional
    public void mergeGuestHistoryToUser(String username, HttpServletRequest request, HttpServletResponse response) {
        Users user = resolveUser(username);
        if (user == null) {
            return;
        }

        String browserId = resolveOrCreateBrowserId(request, response);
        List<BrowsingHistory> guestEntries = browsingHistoryRepository.findByBrowserIdAndUserIsNullOrderByViewedAtAscIdAsc(browserId);
        if (guestEntries.isEmpty()) {
            return;
        }

        guestEntries.forEach(entry -> entry.setUser(user));
        browsingHistoryRepository.saveAll(guestEntries);
    }

    @Transactional(readOnly = true)
    public List<BrowsingHistory> getCurrentBrowsingHistory(HttpServletRequest request, HttpServletResponse response) {
        Users user = resolveAuthenticatedUser();
        if (user != null) {
            return browsingHistoryRepository.findByUserOrderByViewedAtDescIdDesc(user);
        }

        String browserId = resolveOrCreateBrowserId(request, response);
        return browsingHistoryRepository.findByBrowserIdAndUserIsNullOrderByViewedAtDescIdDesc(browserId);
    }

    private BrowsingHistory buildBaseHistory(HttpServletRequest request, HttpServletResponse response) {
        BrowsingHistory browsingHistory = new BrowsingHistory();
        browsingHistory.setUser(resolveAuthenticatedUser());
        browsingHistory.setBrowserId(resolveOrCreateBrowserId(request, response));
        browsingHistory.setSessionId(resolveSessionId(request));
        browsingHistory.setIpAddress(limit(resolveIpAddress(request), MAX_IP_LENGTH));
        browsingHistory.setUserAgent(limit(request.getHeader("User-Agent"), MAX_USER_AGENT_LENGTH));
        browsingHistory.setViewedAt(LocalDateTime.now());
        return browsingHistory;
    }

    private String resolveOrCreateBrowserId(HttpServletRequest request, HttpServletResponse response) {
        String browserId = resolveBrowserId(request);
        if (browserId != null) {
            return browserId;
        }

        browserId = UUID.randomUUID().toString();
        Cookie cookie = new Cookie(BROWSER_ID_COOKIE, browserId);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(BROWSER_ID_MAX_AGE_SECONDS);
        cookie.setSecure(request != null && request.isSecure());
        response.addCookie(cookie);
        return browserId;
    }

    private String resolveBrowserId(HttpServletRequest request) {
        if (request == null || request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (cookie != null && BROWSER_ID_COOKIE.equals(cookie.getName())) {
                String value = trimToNull(cookie.getValue());
                if (value != null) {
                    return value;
                }
            }
        }

        return null;
    }

    private String resolveSessionId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        HttpSession session = request.getSession(false);
        return session == null ? null : trimToNull(session.getId());
    }

    private String resolveIpAddress(HttpServletRequest request) {
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

    private Users resolveAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        return resolveUser(authentication.getName());
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

    private String limit(String value, int maxLength) {
        String normalizedValue = trimToNull(value);
        if (normalizedValue == null || maxLength <= 0 || normalizedValue.length() <= maxLength) {
            return normalizedValue;
        }
        return normalizedValue.substring(0, maxLength);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Transactional(readOnly = true)
    public long getProductViewCount(Product product) {
        if (product == null || product.getId() == null) {
            return 0L;
        }
        return browsingHistoryRepository.countByProductAndViewType(
                product, BrowsingHistoryViewType.PRODUCT);
    }
}
