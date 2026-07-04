package com.ecommerce.app.module.settings.form;

import com.ecommerce.app.module.settings.model.GlobalSettings;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class BasicSiteSettingsForm {

    private Long version;

    @NotBlank(message = "Site name is required.")
    @Size(max = 150, message = "Site name must be 150 characters or fewer.")
    private String siteName;

    @Size(max = 200, message = "Site title must be 200 characters or fewer.")
    private String siteTitle;

    @Size(max = 300, message = "Site tagline must be 300 characters or fewer.")
    private String siteTagline;

    @Size(max = 300, message = "Site URL must be 300 characters or fewer.")
    @Pattern(regexp = "^(|https?://.*)$", message = "Site URL must start with http:// or https://.")
    private String siteUrl;

    @NotBlank(message = "Admin email is required.")
    @Email(message = "Admin email must be a valid email address.")
    @Size(max = 150, message = "Admin email must be 150 characters or fewer.")
    private String adminEmail;

    @Email(message = "Support email must be a valid email address.")
    @Size(max = 150, message = "Support email must be 150 characters or fewer.")
    private String supportEmail;

    @Size(max = 50, message = "Support phone must be 50 characters or fewer.")
    private String supportPhone;

    private String address;

    @Size(max = 80, message = "Timezone must be 80 characters or fewer.")
    private String timezone;

    @Size(max = 10, message = "Currency must be 10 characters or fewer.")
    private String currency;

    @Size(max = 10, message = "Language must be 10 characters or fewer.")
    private String language;

    public static BasicSiteSettingsForm from(GlobalSettings settings) {
        BasicSiteSettingsForm form = new BasicSiteSettingsForm();
        form.setVersion(settings.getVersion());
        form.setSiteName(settings.getSiteName());
        form.setSiteTitle(settings.getSiteTitle());
        form.setSiteTagline(settings.getSiteTagline());
        form.setSiteUrl(settings.getSiteUrl());
        form.setAdminEmail(settings.getAdminEmail());
        form.setSupportEmail(settings.getSupportEmail());
        form.setSupportPhone(settings.getSupportPhone());
        form.setAddress(settings.getAddress());
        form.setTimezone(settings.getTimezone());
        form.setCurrency(settings.getCurrency());
        form.setLanguage(settings.getLanguage());
        return form;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteTitle() {
        return siteTitle;
    }

    public void setSiteTitle(String siteTitle) {
        this.siteTitle = siteTitle;
    }

    public String getSiteTagline() {
        return siteTagline;
    }

    public void setSiteTagline(String siteTagline) {
        this.siteTagline = siteTagline;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public String getSupportPhone() {
        return supportPhone;
    }

    public void setSupportPhone(String supportPhone) {
        this.supportPhone = supportPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
