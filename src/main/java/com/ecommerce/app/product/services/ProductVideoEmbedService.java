package com.ecommerce.app.product.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ProductVideoEmbedService {

    private static final String YOUTUBE_EMBED_BASE_URL = "https://www.youtube.com/embed/";

    public String toYoutubeEmbedUrl(Object videoValue) {
        if (videoValue == null) {
            return null;
        }

        String rawUrl = videoValue.toString().trim();
        if (rawUrl.isEmpty()) {
            return null;
        }

        String videoId = extractVideoId(rawUrl);
        if (videoId == null) {
            return null;
        }
        return YOUTUBE_EMBED_BASE_URL + videoId + "?rel=0";
    }

    private String extractVideoId(String rawUrl) {
        if (isValidYoutubeId(rawUrl)) {
            return rawUrl;
        }

        URI uri = parseUri(rawUrl);
        if (uri == null || uri.getHost() == null) {
            return null;
        }

        String host = normalizeHost(uri.getHost());
        String path = uri.getPath() == null ? "" : uri.getPath();

        if ("youtu.be".equals(host)) {
            return validSegment(firstPathSegment(path));
        }

        if (!"youtube.com".equals(host) && !host.endsWith(".youtube.com")
                && !"youtube-nocookie.com".equals(host) && !host.endsWith(".youtube-nocookie.com")) {
            return null;
        }

        String queryVideoId = queryParameter(uri.getRawQuery(), "v");
        if (queryVideoId != null) {
            return validSegment(queryVideoId);
        }

        String[] pathSegments = path.replaceFirst("^/+", "").split("/");
        for (int i = 0; i < pathSegments.length - 1; i++) {
            String segment = pathSegments[i].toLowerCase(Locale.ROOT);
            if ("embed".equals(segment) || "shorts".equals(segment) || "live".equals(segment)) {
                return validSegment(decode(pathSegments[i + 1]));
            }
        }

        return null;
    }

    private URI parseUri(String rawUrl) {
        String candidate = rawUrl;
        if (!candidate.contains("://")) {
            candidate = "https://" + candidate;
        }
        try {
            return new URI(candidate);
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    private String normalizeHost(String host) {
        String normalized = host.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("www.")) {
            normalized = normalized.substring(4);
        }
        if (normalized.startsWith("m.")) {
            normalized = normalized.substring(2);
        }
        return normalized;
    }

    private String queryParameter(String query, String name) {
        if (query == null || query.isBlank()) {
            return null;
        }
        for (String pair : query.split("&")) {
            int separator = pair.indexOf('=');
            String key = separator >= 0 ? pair.substring(0, separator) : pair;
            if (name.equals(decode(key))) {
                String value = separator >= 0 ? pair.substring(separator + 1) : "";
                return decode(value);
            }
        }
        return null;
    }

    private String firstPathSegment(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String[] segments = path.replaceFirst("^/+", "").split("/");
        return segments.length == 0 ? null : decode(segments[0]);
    }

    private String validSegment(String value) {
        if (value == null) {
            return null;
        }
        String cleanValue = value.trim();
        return isValidYoutubeId(cleanValue) ? cleanValue : null;
    }

    private boolean isValidYoutubeId(String value) {
        return value != null && value.matches("[A-Za-z0-9_-]{6,64}");
    }

    private String decode(String value) {
        return URLDecoder.decode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
