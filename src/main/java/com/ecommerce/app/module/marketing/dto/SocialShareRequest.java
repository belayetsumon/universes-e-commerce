package com.ecommerce.app.module.marketing.dto;

import com.ecommerce.app.module.marketing.model.SocialSharePlatform;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SocialShareRequest {

    @NotNull(message = "Share platform is required.")
    private SocialSharePlatform platform;

    @Size(max = 50, message = "Page type must be 50 characters or fewer.")
    @Pattern(regexp = "^[A-Z_]{2,50}$", message = "Page type is invalid.")
    private String pageType;

    @Size(max = 120, message = "Entity reference must be 120 characters or fewer.")
    @Pattern(regexp = "^$|^[A-Za-z0-9._:-]{1,120}$", message = "Entity reference is invalid.")
    private String publicEntityReference;

    @Size(max = 1000, message = "Public URL must be 1000 characters or fewer.")
    private String publicUrl;

    @Size(max = 120, message = "Campaign source must be 120 characters or fewer.")
    @Pattern(regexp = "^$|^[A-Za-z0-9._:-]{1,120}$", message = "Campaign source is invalid.")
    private String campaignSource;

    public SocialSharePlatform getPlatform() {
        return platform;
    }

    public void setPlatform(SocialSharePlatform platform) {
        this.platform = platform;
    }

    public String getPageType() {
        return pageType;
    }

    public void setPageType(String pageType) {
        this.pageType = pageType;
    }

    public String getPublicEntityReference() {
        return publicEntityReference;
    }

    public void setPublicEntityReference(String publicEntityReference) {
        this.publicEntityReference = publicEntityReference;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public String getCampaignSource() {
        return campaignSource;
    }

    public void setCampaignSource(String campaignSource) {
        this.campaignSource = campaignSource;
    }
}
