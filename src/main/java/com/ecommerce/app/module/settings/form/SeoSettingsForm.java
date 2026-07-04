package com.ecommerce.app.module.settings.form;

import com.ecommerce.app.module.settings.model.GlobalSettings;
import jakarta.validation.constraints.Size;

public class SeoSettingsForm {

    private Long version;

    @Size(max = 200, message = "Meta title must be 200 characters or fewer.")
    private String metaTitle;

    @Size(max = 500, message = "Meta description must be 500 characters or fewer.")
    private String metaDescription;

    @Size(max = 500, message = "Meta keywords must be 500 characters or fewer.")
    private String metaKeywords;

    @Size(max = 200, message = "OG title must be 200 characters or fewer.")
    private String ogTitle;

    @Size(max = 500, message = "OG description must be 500 characters or fewer.")
    private String ogDescription;

    @Size(max = 100, message = "Google Analytics ID must be 100 characters or fewer.")
    private String googleAnalyticsId;

    @Size(max = 100, message = "Facebook Pixel ID must be 100 characters or fewer.")
    private String facebookPixelId;

    public static SeoSettingsForm from(GlobalSettings settings) {
        SeoSettingsForm form = new SeoSettingsForm();
        form.setVersion(settings.getVersion());
        form.setMetaTitle(settings.getMetaTitle());
        form.setMetaDescription(settings.getMetaDescription());
        form.setMetaKeywords(settings.getMetaKeywords());
        form.setOgTitle(settings.getOgTitle());
        form.setOgDescription(settings.getOgDescription());
        form.setGoogleAnalyticsId(settings.getGoogleAnalyticsId());
        form.setFacebookPixelId(settings.getFacebookPixelId());
        return form;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getMetaKeywords() {
        return metaKeywords;
    }

    public void setMetaKeywords(String metaKeywords) {
        this.metaKeywords = metaKeywords;
    }

    public String getOgTitle() {
        return ogTitle;
    }

    public void setOgTitle(String ogTitle) {
        this.ogTitle = ogTitle;
    }

    public String getOgDescription() {
        return ogDescription;
    }

    public void setOgDescription(String ogDescription) {
        this.ogDescription = ogDescription;
    }

    public String getGoogleAnalyticsId() {
        return googleAnalyticsId;
    }

    public void setGoogleAnalyticsId(String googleAnalyticsId) {
        this.googleAnalyticsId = googleAnalyticsId;
    }

    public String getFacebookPixelId() {
        return facebookPixelId;
    }

    public void setFacebookPixelId(String facebookPixelId) {
        this.facebookPixelId = facebookPixelId;
    }
}
