package com.ecommerce.app.module.marketing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "social_share_event",
        indexes = {
            @Index(name = "idx_social_share_event_created_at", columnList = "created_at"),
            @Index(name = "idx_social_share_event_platform", columnList = "platform"),
            @Index(name = "idx_social_share_event_page_type", columnList = "page_type"),
            @Index(name = "idx_social_share_event_entity", columnList = "public_entity_reference"),
            @Index(name = "idx_social_share_event_customer", columnList = "customer_user_id")
        }
)
public class SocialShareEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String uuid = UUID.randomUUID().toString();

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private SocialShareEventType eventType = SocialShareEventType.SHARE_INITIATED;

    @Column(name = "page_type", nullable = false, length = 50)
    private String pageType;

    @Column(name = "public_entity_reference", length = 120)
    private String publicEntityReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SocialSharePlatform platform;

    @Column(name = "customer_user_id")
    private Long customerUserId;

    @Column(name = "guest_tracking_id", length = 80)
    private String guestTrackingId;

    @Column(name = "referral_code_present", nullable = false)
    private Boolean referralCodePresent = false;

    @Column(name = "referral_code_hash", length = 128)
    private String referralCodeHash;

    @Column(name = "public_url", nullable = false, length = 1000)
    private String publicUrl;

    @Column(name = "campaign_source", length = 120)
    private String campaignSource;

    @Column(name = "device_category", length = 40)
    private String deviceCategory;

    @Column(name = "ip_hash", length = 128)
    private String ipHash;

    @Column(name = "user_agent_hash", length = 128)
    private String userAgentHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (uuid == null || uuid.isBlank()) {
            uuid = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public SocialShareEventType getEventType() {
        return eventType;
    }

    public void setEventType(SocialShareEventType eventType) {
        this.eventType = eventType;
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

    public SocialSharePlatform getPlatform() {
        return platform;
    }

    public void setPlatform(SocialSharePlatform platform) {
        this.platform = platform;
    }

    public Long getCustomerUserId() {
        return customerUserId;
    }

    public void setCustomerUserId(Long customerUserId) {
        this.customerUserId = customerUserId;
    }

    public String getGuestTrackingId() {
        return guestTrackingId;
    }

    public void setGuestTrackingId(String guestTrackingId) {
        this.guestTrackingId = guestTrackingId;
    }

    public Boolean getReferralCodePresent() {
        return referralCodePresent;
    }

    public void setReferralCodePresent(Boolean referralCodePresent) {
        this.referralCodePresent = referralCodePresent;
    }

    public String getReferralCodeHash() {
        return referralCodeHash;
    }

    public void setReferralCodeHash(String referralCodeHash) {
        this.referralCodeHash = referralCodeHash;
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

    public String getDeviceCategory() {
        return deviceCategory;
    }

    public void setDeviceCategory(String deviceCategory) {
        this.deviceCategory = deviceCategory;
    }

    public String getIpHash() {
        return ipHash;
    }

    public void setIpHash(String ipHash) {
        this.ipHash = ipHash;
    }

    public String getUserAgentHash() {
        return userAgentHash;
    }

    public void setUserAgentHash(String userAgentHash) {
        this.userAgentHash = userAgentHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
