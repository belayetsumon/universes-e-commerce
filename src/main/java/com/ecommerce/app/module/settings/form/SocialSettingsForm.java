package com.ecommerce.app.module.settings.form;

import com.ecommerce.app.module.settings.model.GlobalSettings;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SocialSettingsForm {

    private Long version;

    @Size(max = 300, message = "Facebook URL must be 300 characters or fewer.")
    @Pattern(regexp = "^(|https?://.*)$", message = "Facebook URL must start with http:// or https://.")
    private String facebookUrl;

    @Size(max = 300, message = "YouTube URL must be 300 characters or fewer.")
    @Pattern(regexp = "^(|https?://.*)$", message = "YouTube URL must start with http:// or https://.")
    private String youtubeUrl;

    @Size(max = 300, message = "Instagram URL must be 300 characters or fewer.")
    @Pattern(regexp = "^(|https?://.*)$", message = "Instagram URL must start with http:// or https://.")
    private String instagramUrl;

    @Size(max = 300, message = "LinkedIn URL must be 300 characters or fewer.")
    @Pattern(regexp = "^(|https?://.*)$", message = "LinkedIn URL must start with http:// or https://.")
    private String linkedinUrl;

    @Size(max = 300, message = "Twitter/X URL must be 300 characters or fewer.")
    @Pattern(regexp = "^(|https?://.*)$", message = "Twitter/X URL must start with http:// or https://.")
    private String twitterUrl;

    @Size(max = 50, message = "WhatsApp number must be 50 characters or fewer.")
    private String whatsappNumber;

    public static SocialSettingsForm from(GlobalSettings settings) {
        SocialSettingsForm form = new SocialSettingsForm();
        form.setVersion(settings.getVersion());
        form.setFacebookUrl(settings.getFacebookUrl());
        form.setYoutubeUrl(settings.getYoutubeUrl());
        form.setInstagramUrl(settings.getInstagramUrl());
        form.setLinkedinUrl(settings.getLinkedinUrl());
        form.setTwitterUrl(settings.getTwitterUrl());
        form.setWhatsappNumber(settings.getWhatsappNumber());
        return form;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public String getInstagramUrl() {
        return instagramUrl;
    }

    public void setInstagramUrl(String instagramUrl) {
        this.instagramUrl = instagramUrl;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getTwitterUrl() {
        return twitterUrl;
    }

    public void setTwitterUrl(String twitterUrl) {
        this.twitterUrl = twitterUrl;
    }

    public String getWhatsappNumber() {
        return whatsappNumber;
    }

    public void setWhatsappNumber(String whatsappNumber) {
        this.whatsappNumber = whatsappNumber;
    }
}
