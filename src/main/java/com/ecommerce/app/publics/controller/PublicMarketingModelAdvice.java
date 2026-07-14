package com.ecommerce.app.publics.controller;

import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.model.TrackingImplementationMode;
import com.ecommerce.app.module.settings.services.GlobalSettingsService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class PublicMarketingModelAdvice {

    private final GlobalSettingsService globalSettingsService;

    public PublicMarketingModelAdvice(GlobalSettingsService globalSettingsService) {
        this.globalSettingsService = globalSettingsService;
    }

    @ModelAttribute
    public void addMarketingModel(Model model, HttpServletRequest request) {
        if (request == null || isSecuredArea(request.getRequestURI())) {
            return;
        }
        GlobalSettings settings = globalSettingsService.getActiveSettings();
        String baseUrl = safeBaseUrl(settings.getPublicBaseUrl());
        String canonicalUrl = buildCanonicalUrl(baseUrl, request);
        String title = firstNonBlank(settings.getOgTitle(), settings.getMetaTitle(), settings.getSiteTitle(), settings.getSiteName());
        String description = firstNonBlank(settings.getOgDescription(), settings.getMetaDescription(), settings.getSiteTagline());
        String imageUrl = absolutePublicUrl(settings.getOgImage(), baseUrl);

        model.addAttribute("marketingSettings", marketingSettings(settings));
        model.addAttribute("defaultCanonicalUrl", canonicalUrl);
        model.addAttribute("defaultOgImageUrl", imageUrl);
        model.addAttribute("defaultPageTitle", title);
        model.addAttribute("defaultPageDescription", description);
        model.addAttribute("publicTracking", trackingConfig(settings));
    }

    private boolean isSecuredArea(String uri) {
        if (uri == null) {
            return false;
        }
        return uri.startsWith("/admin")
                || uri.startsWith("/vendor")
                || uri.startsWith("/customer")
                || uri.startsWith("/cart/checkout")
                || uri.startsWith("/order")
                || uri.contains("password")
                || uri.contains("payment");
    }

    private Map<String, Object> trackingConfig(GlobalSettings settings) {
        Map<String, Object> config = new LinkedHashMap<>();
        TrackingImplementationMode mode = settings.getTrackingImplementationMode() == null
                ? TrackingImplementationMode.DIRECT
                : settings.getTrackingImplementationMode();
        config.put("enabled", Boolean.TRUE);
        config.put("debug", Boolean.TRUE.equals(settings.getFacebookDebugMode()) || Boolean.TRUE.equals(settings.getGa4DebugMode()));
        config.put("mode", mode.name());
        config.put("cookieConsentEnabled", Boolean.TRUE.equals(settings.getCookieConsentEnabled()));
        config.put("googleConsentModeEnabled", Boolean.TRUE.equals(settings.getGoogleConsentModeEnabled()));
        config.put("googleAnalyticsEnabled", mode == TrackingImplementationMode.DIRECT && Boolean.TRUE.equals(settings.getGoogleAnalyticsEnabled()));
        config.put("ga4MeasurementId", safeClientValue(settings.getGoogleAnalyticsId()));
        config.put("ga4DebugMode", Boolean.TRUE.equals(settings.getGa4DebugMode()));
        config.put("facebookPixelEnabled", mode == TrackingImplementationMode.DIRECT && Boolean.TRUE.equals(settings.getFacebookPixelEnabled()));
        config.put("facebookBrowserTrackingEnabled", Boolean.TRUE.equals(settings.getFacebookBrowserTrackingEnabled()));
        config.put("facebookPixelId", safeClientValue(settings.getFacebookPixelId()));
        config.put("facebookDebugMode", Boolean.TRUE.equals(settings.getFacebookDebugMode()));
        config.put("googleTagManagerEnabled", mode == TrackingImplementationMode.GOOGLE_TAG_MANAGER && Boolean.TRUE.equals(settings.getGoogleTagManagerEnabled()));
        config.put("gtmContainerId", safeClientValue(settings.getGtmContainerId()));
        config.put("serverSideGtmUrl", safeBaseUrl(settings.getServerSideGtmUrl()));
        return config;
    }

    private Map<String, Object> marketingSettings(GlobalSettings settings) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("openGraphEnabled", Boolean.TRUE.equals(settings.getOpenGraphEnabled()));
        config.put("ogSiteName", firstNonBlank(settings.getOgSiteName(), settings.getSiteName()));
        config.put("siteName", settings.getSiteName());
        config.put("facebookAppId", safeClientValue(settings.getFacebookAppId()));
        config.put("twitterCardType", firstNonBlank(settings.getTwitterCardType(), "summary_large_image"));
        config.put("socialSharingEnabled", Boolean.TRUE.equals(settings.getSocialSharingEnabled()));
        config.put("facebookSharingEnabled", Boolean.TRUE.equals(settings.getFacebookSharingEnabled()));
        config.put("messengerSharingEnabled", Boolean.TRUE.equals(settings.getMessengerSharingEnabled()));
        config.put("whatsappSharingEnabled", Boolean.TRUE.equals(settings.getWhatsappSharingEnabled()));
        config.put("linkedinSharingEnabled", Boolean.TRUE.equals(settings.getLinkedinSharingEnabled()));
        config.put("twitterSharingEnabled", Boolean.TRUE.equals(settings.getTwitterSharingEnabled()));
        config.put("emailSharingEnabled", Boolean.TRUE.equals(settings.getEmailSharingEnabled()));
        config.put("copyLinkSharingEnabled", Boolean.TRUE.equals(settings.getCopyLinkSharingEnabled()));
        config.put("nativeShareEnabled", Boolean.TRUE.equals(settings.getNativeShareEnabled()));
        return config;
    }

    private String buildCanonicalUrl(String baseUrl, HttpServletRequest request) {
        if (baseUrl == null) {
            return null;
        }
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return baseUrl + (path == null || path.isBlank() ? "/" : path);
    }

    private String absolutePublicUrl(String value, String baseUrl) {
        String cleanValue = clean(value);
        if (cleanValue == null) {
            return null;
        }
        if (safeBaseUrl(cleanValue) != null) {
            return cleanValue;
        }
        if (cleanValue.startsWith("/") && baseUrl != null) {
            return baseUrl + cleanValue;
        }
        return null;
    }

    private String safeBaseUrl(String value) {
        String cleanValue = clean(value);
        if (cleanValue == null || !cleanValue.startsWith("https://")) {
            return null;
        }
        String lowerValue = cleanValue.toLowerCase(Locale.ROOT);
        if (lowerValue.contains("localhost")
                || lowerValue.contains("127.0.0.1")
                || lowerValue.contains("0.0.0.0")
                || lowerValue.matches("https://10\\..*")
                || lowerValue.matches("https://172\\.(1[6-9]|2[0-9]|3[0-1])\\..*")
                || lowerValue.matches("https://192\\.168\\..*")) {
            return null;
        }
        while (cleanValue.endsWith("/")) {
            cleanValue = cleanValue.substring(0, cleanValue.length() - 1);
        }
        return cleanValue;
    }

    private String safeClientValue(String value) {
        String cleanValue = clean(value);
        if (cleanValue == null || cleanValue.contains("<") || cleanValue.contains(">")) {
            return null;
        }
        return cleanValue;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String cleanValue = clean(value);
            if (cleanValue != null) {
                return cleanValue;
            }
        }
        return null;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleanValue = value.trim();
        return cleanValue.isEmpty() ? null : cleanValue;
    }
}
