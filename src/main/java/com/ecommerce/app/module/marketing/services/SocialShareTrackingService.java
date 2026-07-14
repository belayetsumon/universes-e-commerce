package com.ecommerce.app.module.marketing.services;

import com.ecommerce.app.module.marketing.dto.SocialShareReportSummary;
import com.ecommerce.app.module.marketing.dto.SocialShareRequest;
import com.ecommerce.app.module.marketing.model.SocialShareEvent;
import com.ecommerce.app.module.marketing.model.SocialShareEventType;
import com.ecommerce.app.module.marketing.repository.SocialShareEventRepository;
import com.ecommerce.app.module.user.model.Users;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SocialShareTrackingService {

    private static final int MAX_SHARE_EVENTS_PER_MINUTE = 30;
    private final SocialShareEventRepository repository;
    private final Map<String, RateWindow> rateWindows = new ConcurrentHashMap<>();

    public SocialShareTrackingService(SocialShareEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SocialShareEvent trackShare(SocialShareRequest request, Users customer, HttpServletRequest servletRequest) {
        validateRateLimit(servletRequest);
        URI publicUri = validatePublicUrl(request.getPublicUrl());
        SocialShareEvent event = new SocialShareEvent();
        event.setEventType(SocialShareEventType.SHARE_INITIATED);
        event.setPageType(clean(request.getPageType(), "PAGE"));
        event.setPublicEntityReference(clean(request.getPublicEntityReference(), null));
        event.setPlatform(request.getPlatform());
        event.setCustomerUserId(customer == null ? null : customer.getId());
        event.setGuestTrackingId(customer == null ? guestTrackingId(servletRequest) : null);
        event.setPublicUrl(publicUri.toString());
        event.setCampaignSource(clean(request.getCampaignSource(), null));
        event.setDeviceCategory(deviceCategory(servletRequest.getHeader("User-Agent")));
        event.setIpHash(hash(clientIp(servletRequest)));
        event.setUserAgentHash(hash(servletRequest.getHeader("User-Agent")));
        String referralCode = referralCode(publicUri);
        event.setReferralCodePresent(referralCode != null);
        event.setReferralCodeHash(hash(referralCode));
        return repository.save(event);
    }

    @Transactional(readOnly = true)
    public SocialShareReportSummary summary(Instant start, Instant end) {
        return new SocialShareReportSummary(
                repository.countByCreatedAtBetween(start, end),
                repository.countByReferralCodePresentTrueAndCreatedAtBetween(start, end),
                repository.countByCustomerUserIdIsNotNullAndCreatedAtBetween(start, end),
                repository.countByCustomerUserIdIsNullAndCreatedAtBetween(start, end)
        );
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> byPlatform(Instant start, Instant end) {
        return rows(repository.countByPlatform(start, end));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> byPageType(Instant start, Instant end) {
        return rows(repository.countByPageType(start, end));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> topEntities(String pageType, Instant start, Instant end) {
        return rows(repository.topEntities(pageType, start, end));
    }

    private List<Map<String, Object>> rows(List<Object[]> rows) {
        return rows.stream().map(row -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("label", row[0] == null ? "Unknown" : row[0].toString());
            map.put("total", row[1]);
            return map;
        }).toList();
    }

    private void validateRateLimit(HttpServletRequest request) {
        String key = hash(clientIp(request) + "|" + request.getHeader("User-Agent"));
        long now = Instant.now().toEpochMilli();
        RateWindow window = rateWindows.compute(key, (ignored, existing) -> {
            if (existing == null || now - existing.windowStartedAt > 60_000L) {
                return new RateWindow(now, 1);
            }
            existing.count++;
            return existing;
        });
        if (window.count > MAX_SHARE_EVENTS_PER_MINUTE) {
            throw new IllegalArgumentException("Too many share events. Please try again shortly.");
        }
    }

    private URI validatePublicUrl(String value) {
        try {
            URI uri = URI.create(value == null ? "" : value.trim());
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null || isPrivateHost(host)) {
                throw new IllegalArgumentException("Public URL must be an HTTPS public URL.");
            }
            return uri;
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Public URL must be an HTTPS public URL.");
        }
    }

    private boolean isPrivateHost(String host) {
        String lowerHost = host.toLowerCase();
        return lowerHost.equals("localhost")
                || lowerHost.startsWith("127.")
                || lowerHost.startsWith("10.")
                || lowerHost.startsWith("192.168.")
                || lowerHost.matches("172\\.(1[6-9]|2[0-9]|3[0-1])\\..*");
    }

    private String referralCode(URI uri) {
        String query = uri.getRawQuery();
        if (query == null || query.isBlank()) {
            return null;
        }
        for (String pair : query.split("&")) {
            if (pair.startsWith("ref=") && pair.length() > 4) {
                return pair.substring(4);
            }
        }
        return null;
    }

    private String guestTrackingId(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object existing = session.getAttribute("guestShareTrackingId");
        if (existing instanceof String value && !value.isBlank()) {
            return value;
        }
        String value = UUID.randomUUID().toString();
        session.setAttribute("guestShareTrackingId", value);
        return value;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String deviceCategory(String userAgent) {
        String ua = userAgent == null ? "" : userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "MOBILE";
        }
        if (ua.contains("tablet") || ua.contains("ipad")) {
            return "TABLET";
        }
        return "DESKTOP";
    }

    private String clean(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private String hash(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable.", ex);
        }
    }

    private static final class RateWindow {
        private final long windowStartedAt;
        private int count;

        private RateWindow(long windowStartedAt, int count) {
            this.windowStartedAt = windowStartedAt;
            this.count = count;
        }
    }
}
