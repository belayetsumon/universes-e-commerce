package com.ecommerce.app.module.settings.form;

import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.model.TrackingImplementationMode;
import jakarta.validation.constraints.Min;
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

    private Boolean openGraphEnabled;

    @Size(max = 150, message = "Open Graph site name must be 150 characters or fewer.")
    private String ogSiteName;

    @Size(max = 500, message = "Public website base URL must be 500 characters or fewer.")
    private String publicBaseUrl;

    @Size(max = 100, message = "Facebook App ID must be 100 characters or fewer.")
    private String facebookAppId;

    @Size(max = 50, message = "Twitter card type must be 50 characters or fewer.")
    private String twitterCardType;

    private Boolean socialSharingEnabled;
    private Boolean facebookSharingEnabled;
    private Boolean messengerSharingEnabled;
    private Boolean whatsappSharingEnabled;
    private Boolean linkedinSharingEnabled;
    private Boolean twitterSharingEnabled;
    private Boolean emailSharingEnabled;
    private Boolean copyLinkSharingEnabled;
    private Boolean nativeShareEnabled;
    private Boolean referralLinksEnabled;

    @Min(value = 1, message = "Referral cookie expiry must be at least 1 day.")
    private Integer referralCookieExpiryDays;

    private Boolean facebookPixelEnabled;
    private Boolean facebookBrowserTrackingEnabled;
    private Boolean facebookConversionApiEnabled;

    @Size(max = 500, message = "Conversion API access token must be 500 characters or fewer.")
    private String facebookConversionApiAccessToken;

    @Size(max = 100, message = "Facebook test event code must be 100 characters or fewer.")
    private String facebookTestEventCode;

    private Boolean facebookDebugMode;
    private Boolean googleAnalyticsEnabled;
    private Boolean ga4EnhancedEcommerceEnabled;
    private Boolean ga4DebugMode;
    private Boolean googleConsentModeEnabled;
    private Boolean googleTagManagerEnabled;

    @Size(max = 50, message = "GTM container ID must be 50 characters or fewer.")
    private String gtmContainerId;

    @Size(max = 500, message = "Server-side GTM URL must be 500 characters or fewer.")
    private String serverSideGtmUrl;

    private TrackingImplementationMode trackingImplementationMode;
    private Boolean cookieConsentEnabled;

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
        form.setOpenGraphEnabled(settings.getOpenGraphEnabled());
        form.setOgSiteName(settings.getOgSiteName());
        form.setPublicBaseUrl(settings.getPublicBaseUrl());
        form.setFacebookAppId(settings.getFacebookAppId());
        form.setTwitterCardType(settings.getTwitterCardType());
        form.setSocialSharingEnabled(settings.getSocialSharingEnabled());
        form.setFacebookSharingEnabled(settings.getFacebookSharingEnabled());
        form.setMessengerSharingEnabled(settings.getMessengerSharingEnabled());
        form.setWhatsappSharingEnabled(settings.getWhatsappSharingEnabled());
        form.setLinkedinSharingEnabled(settings.getLinkedinSharingEnabled());
        form.setTwitterSharingEnabled(settings.getTwitterSharingEnabled());
        form.setEmailSharingEnabled(settings.getEmailSharingEnabled());
        form.setCopyLinkSharingEnabled(settings.getCopyLinkSharingEnabled());
        form.setNativeShareEnabled(settings.getNativeShareEnabled());
        form.setReferralLinksEnabled(settings.getReferralLinksEnabled());
        form.setReferralCookieExpiryDays(settings.getReferralCookieExpiryDays());
        form.setFacebookPixelEnabled(settings.getFacebookPixelEnabled());
        form.setFacebookBrowserTrackingEnabled(settings.getFacebookBrowserTrackingEnabled());
        form.setFacebookConversionApiEnabled(settings.getFacebookConversionApiEnabled());
        form.setFacebookConversionApiAccessToken(maskSecret(settings.getFacebookConversionApiAccessToken()));
        form.setFacebookTestEventCode(settings.getFacebookTestEventCode());
        form.setFacebookDebugMode(settings.getFacebookDebugMode());
        form.setGoogleAnalyticsEnabled(settings.getGoogleAnalyticsEnabled());
        form.setGa4EnhancedEcommerceEnabled(settings.getGa4EnhancedEcommerceEnabled());
        form.setGa4DebugMode(settings.getGa4DebugMode());
        form.setGoogleConsentModeEnabled(settings.getGoogleConsentModeEnabled());
        form.setGoogleTagManagerEnabled(settings.getGoogleTagManagerEnabled());
        form.setGtmContainerId(settings.getGtmContainerId());
        form.setServerSideGtmUrl(settings.getServerSideGtmUrl());
        form.setTrackingImplementationMode(settings.getTrackingImplementationMode());
        form.setCookieConsentEnabled(settings.getCookieConsentEnabled());
        return form;
    }

    private static String maskSecret(String value) {
        return value == null || value.isBlank() ? "" : "********";
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

    public Boolean getOpenGraphEnabled() {
        return openGraphEnabled;
    }

    public void setOpenGraphEnabled(Boolean openGraphEnabled) {
        this.openGraphEnabled = openGraphEnabled;
    }

    public String getOgSiteName() {
        return ogSiteName;
    }

    public void setOgSiteName(String ogSiteName) {
        this.ogSiteName = ogSiteName;
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public String getFacebookAppId() {
        return facebookAppId;
    }

    public void setFacebookAppId(String facebookAppId) {
        this.facebookAppId = facebookAppId;
    }

    public String getTwitterCardType() {
        return twitterCardType;
    }

    public void setTwitterCardType(String twitterCardType) {
        this.twitterCardType = twitterCardType;
    }

    public Boolean getSocialSharingEnabled() {
        return socialSharingEnabled;
    }

    public void setSocialSharingEnabled(Boolean socialSharingEnabled) {
        this.socialSharingEnabled = socialSharingEnabled;
    }

    public Boolean getFacebookSharingEnabled() {
        return facebookSharingEnabled;
    }

    public void setFacebookSharingEnabled(Boolean facebookSharingEnabled) {
        this.facebookSharingEnabled = facebookSharingEnabled;
    }

    public Boolean getMessengerSharingEnabled() {
        return messengerSharingEnabled;
    }

    public void setMessengerSharingEnabled(Boolean messengerSharingEnabled) {
        this.messengerSharingEnabled = messengerSharingEnabled;
    }

    public Boolean getWhatsappSharingEnabled() {
        return whatsappSharingEnabled;
    }

    public void setWhatsappSharingEnabled(Boolean whatsappSharingEnabled) {
        this.whatsappSharingEnabled = whatsappSharingEnabled;
    }

    public Boolean getLinkedinSharingEnabled() {
        return linkedinSharingEnabled;
    }

    public void setLinkedinSharingEnabled(Boolean linkedinSharingEnabled) {
        this.linkedinSharingEnabled = linkedinSharingEnabled;
    }

    public Boolean getTwitterSharingEnabled() {
        return twitterSharingEnabled;
    }

    public void setTwitterSharingEnabled(Boolean twitterSharingEnabled) {
        this.twitterSharingEnabled = twitterSharingEnabled;
    }

    public Boolean getEmailSharingEnabled() {
        return emailSharingEnabled;
    }

    public void setEmailSharingEnabled(Boolean emailSharingEnabled) {
        this.emailSharingEnabled = emailSharingEnabled;
    }

    public Boolean getCopyLinkSharingEnabled() {
        return copyLinkSharingEnabled;
    }

    public void setCopyLinkSharingEnabled(Boolean copyLinkSharingEnabled) {
        this.copyLinkSharingEnabled = copyLinkSharingEnabled;
    }

    public Boolean getNativeShareEnabled() {
        return nativeShareEnabled;
    }

    public void setNativeShareEnabled(Boolean nativeShareEnabled) {
        this.nativeShareEnabled = nativeShareEnabled;
    }

    public Boolean getReferralLinksEnabled() {
        return referralLinksEnabled;
    }

    public void setReferralLinksEnabled(Boolean referralLinksEnabled) {
        this.referralLinksEnabled = referralLinksEnabled;
    }

    public Integer getReferralCookieExpiryDays() {
        return referralCookieExpiryDays;
    }

    public void setReferralCookieExpiryDays(Integer referralCookieExpiryDays) {
        this.referralCookieExpiryDays = referralCookieExpiryDays;
    }

    public Boolean getFacebookPixelEnabled() {
        return facebookPixelEnabled;
    }

    public void setFacebookPixelEnabled(Boolean facebookPixelEnabled) {
        this.facebookPixelEnabled = facebookPixelEnabled;
    }

    public Boolean getFacebookBrowserTrackingEnabled() {
        return facebookBrowserTrackingEnabled;
    }

    public void setFacebookBrowserTrackingEnabled(Boolean facebookBrowserTrackingEnabled) {
        this.facebookBrowserTrackingEnabled = facebookBrowserTrackingEnabled;
    }

    public Boolean getFacebookConversionApiEnabled() {
        return facebookConversionApiEnabled;
    }

    public void setFacebookConversionApiEnabled(Boolean facebookConversionApiEnabled) {
        this.facebookConversionApiEnabled = facebookConversionApiEnabled;
    }

    public String getFacebookConversionApiAccessToken() {
        return facebookConversionApiAccessToken;
    }

    public void setFacebookConversionApiAccessToken(String facebookConversionApiAccessToken) {
        this.facebookConversionApiAccessToken = facebookConversionApiAccessToken;
    }

    public String getFacebookTestEventCode() {
        return facebookTestEventCode;
    }

    public void setFacebookTestEventCode(String facebookTestEventCode) {
        this.facebookTestEventCode = facebookTestEventCode;
    }

    public Boolean getFacebookDebugMode() {
        return facebookDebugMode;
    }

    public void setFacebookDebugMode(Boolean facebookDebugMode) {
        this.facebookDebugMode = facebookDebugMode;
    }

    public Boolean getGoogleAnalyticsEnabled() {
        return googleAnalyticsEnabled;
    }

    public void setGoogleAnalyticsEnabled(Boolean googleAnalyticsEnabled) {
        this.googleAnalyticsEnabled = googleAnalyticsEnabled;
    }

    public Boolean getGa4EnhancedEcommerceEnabled() {
        return ga4EnhancedEcommerceEnabled;
    }

    public void setGa4EnhancedEcommerceEnabled(Boolean ga4EnhancedEcommerceEnabled) {
        this.ga4EnhancedEcommerceEnabled = ga4EnhancedEcommerceEnabled;
    }

    public Boolean getGa4DebugMode() {
        return ga4DebugMode;
    }

    public void setGa4DebugMode(Boolean ga4DebugMode) {
        this.ga4DebugMode = ga4DebugMode;
    }

    public Boolean getGoogleConsentModeEnabled() {
        return googleConsentModeEnabled;
    }

    public void setGoogleConsentModeEnabled(Boolean googleConsentModeEnabled) {
        this.googleConsentModeEnabled = googleConsentModeEnabled;
    }

    public Boolean getGoogleTagManagerEnabled() {
        return googleTagManagerEnabled;
    }

    public void setGoogleTagManagerEnabled(Boolean googleTagManagerEnabled) {
        this.googleTagManagerEnabled = googleTagManagerEnabled;
    }

    public String getGtmContainerId() {
        return gtmContainerId;
    }

    public void setGtmContainerId(String gtmContainerId) {
        this.gtmContainerId = gtmContainerId;
    }

    public String getServerSideGtmUrl() {
        return serverSideGtmUrl;
    }

    public void setServerSideGtmUrl(String serverSideGtmUrl) {
        this.serverSideGtmUrl = serverSideGtmUrl;
    }

    public TrackingImplementationMode getTrackingImplementationMode() {
        return trackingImplementationMode;
    }

    public void setTrackingImplementationMode(TrackingImplementationMode trackingImplementationMode) {
        this.trackingImplementationMode = trackingImplementationMode;
    }

    public Boolean getCookieConsentEnabled() {
        return cookieConsentEnabled;
    }

    public void setCookieConsentEnabled(Boolean cookieConsentEnabled) {
        this.cookieConsentEnabled = cookieConsentEnabled;
    }
}
