package com.ecommerce.app.publics.seo;

import java.util.List;
import java.util.Map;

public class PageSeoMetadata {

    private final String title;
    private final String description;
    private final String canonicalUrl;
    private final String ogType;
    private final String ogImageUrl;
    private final String robots;
    private final String sharePageType;
    private final String shareEntityReference;
    private final List<Map<String, Object>> jsonLd;

    public PageSeoMetadata(
            String title,
            String description,
            String canonicalUrl,
            String ogType,
            String ogImageUrl,
            String robots,
            String sharePageType,
            String shareEntityReference,
            List<Map<String, Object>> jsonLd) {
        this.title = title;
        this.description = description;
        this.canonicalUrl = canonicalUrl;
        this.ogType = ogType;
        this.ogImageUrl = ogImageUrl;
        this.robots = robots;
        this.sharePageType = sharePageType;
        this.shareEntityReference = shareEntityReference;
        this.jsonLd = jsonLd == null ? List.of() : List.copyOf(jsonLd);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public String getOgType() {
        return ogType;
    }

    public String getOgImageUrl() {
        return ogImageUrl;
    }

    public String getRobots() {
        return robots;
    }

    public String getSharePageType() {
        return sharePageType;
    }

    public String getShareEntityReference() {
        return shareEntityReference;
    }

    public List<Map<String, Object>> getJsonLd() {
        return jsonLd;
    }
}
