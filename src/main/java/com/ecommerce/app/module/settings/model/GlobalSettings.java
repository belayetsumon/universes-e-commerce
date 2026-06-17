/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.settings.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author libertyerp_local
 */
@Entity
@Table(
        name = "global_settings",
        indexes = {
            @Index(name = "idx_global_settings_uuid", columnList = "uuid"),
            @Index(name = "idx_global_settings_active", columnList = "active")
        },
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_global_settings_single_row", columnNames = {"id"})
        }
)
@org.hibernate.annotations.Check(constraints = "id = 1")
@EntityListeners(AuditingEntityListener.class)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Cacheable
@Scope("singleton")
public class GlobalSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id = 1;

    @Column(name = "uuid", nullable = false, updatable = false, unique = true, length = 36)
    private String uuid = UUID.randomUUID().toString();

    // BASIC SITE SETTINGS
    @NotBlank
    @Size(max = 150)
    @Column(name = "site_name", nullable = false, length = 150)
    private String siteName;

    @Size(max = 200)
    @Column(name = "site_title", length = 200)
    private String siteTitle;

    @Size(max = 300)
    @Column(name = "site_tagline", length = 300)
    private String siteTagline;

    @Size(max = 500)
    @Column(name = "site_logo", length = 500)
    private String siteLogo;

    @Size(max = 500)
    @Column(name = "site_logo_desktop", length = 500)
    private String siteLogoDesktop;

    @Size(max = 500)
    @Column(name = "site_logo_mobile", length = 500)
    private String siteLogoMobile;

    @Size(max = 500)
    @Column(name = "site_logo_square", length = 500)
    private String siteLogoSquare;

    @Size(max = 500)
    @Column(name = "favicon", length = 500)
    private String favicon;

    @Size(max = 300)
    @Column(name = "site_url", length = 300)
    private String siteUrl;

    @Email
    @Size(max = 150)
    @Column(name = "admin_email", length = 150)
    private String adminEmail;

    @Email
    @Size(max = 150)
    @Column(name = "support_email", length = 150)
    private String supportEmail;

    @Size(max = 50)
    @Column(name = "support_phone", length = 50)
    private String supportPhone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Size(max = 80)
    @Column(name = "timezone", length = 80)
    private String timezone = "Asia/Dhaka";

    @Size(max = 10)
    @Column(name = "currency", length = 10)
    private String currency = "BDT";

    @Size(max = 10)
    @Column(name = "language", length = 10)
    private String language = "en";

    // SEO SETTINGS
    @Size(max = 200)
    @Column(name = "meta_title", length = 200)
    private String metaTitle;

    @Size(max = 500)
    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Size(max = 500)
    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;

    @Size(max = 200)
    @Column(name = "og_title", length = 200)
    private String ogTitle;

    @Size(max = 500)
    @Column(name = "og_description", length = 500)
    private String ogDescription;

    @Size(max = 500)
    @Column(name = "og_image", length = 500)
    private String ogImage;

    @Size(max = 100)
    @Column(name = "google_analytics_id", length = 100)
    private String googleAnalyticsId;

    @Size(max = 100)
    @Column(name = "facebook_pixel_id", length = 100)
    private String facebookPixelId;

    // STORE SETTINGS
    @Size(max = 10)
    @Column(name = "default_currency", length = 10)
    private String defaultCurrency = "BDT";

    @Column(name = "tax_enabled", nullable = false)
    private Boolean taxEnabled = false;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(name = "tax_percentage", precision = 5, scale = 2)
    private BigDecimal taxPercentage = BigDecimal.ZERO;

    @Column(name = "stock_management_enabled", nullable = false)
    private Boolean stockManagementEnabled = true;

    @Min(0)
    @Column(name = "low_stock_alert_qty")
    private Integer lowStockAlertQty = 5;

    @Column(name = "allow_guest_checkout", nullable = false)
    private Boolean allowGuestCheckout = true;

    @DecimalMin("0.00")
    @Column(name = "minimum_order_amount", precision = 19, scale = 2)
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Column(name = "maximum_order_amount", precision = 19, scale = 2)
    private BigDecimal maximumOrderAmount;

    // PAYMENT SETTINGS
    @Column(name = "cod_enabled", nullable = false)
    private Boolean codEnabled = true;

    @Column(name = "online_payment_enabled", nullable = false)
    private Boolean onlinePaymentEnabled = false;

    @Column(name = "partial_payment_enabled", nullable = false)
    private Boolean partialPaymentEnabled = false;

    @Column(name = "emi_enabled", nullable = false)
    private Boolean emiEnabled = false;

    // DELIVERY SETTINGS
    @Column(name = "delivery_enabled", nullable = false)
    private Boolean deliveryEnabled = true;

    @Column(name = "free_delivery_enabled", nullable = false)
    private Boolean freeDeliveryEnabled = false;

    @DecimalMin("0.00")
    @Column(name = "free_delivery_min_amount", precision = 19, scale = 2)
    private BigDecimal freeDeliveryMinAmount = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Column(name = "inside_dhaka_delivery_charge", precision = 19, scale = 2)
    private BigDecimal insideDhakaDeliveryCharge = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Column(name = "outside_dhaka_delivery_charge", precision = 19, scale = 2)
    private BigDecimal outsideDhakaDeliveryCharge = BigDecimal.ZERO;

    @Size(max = 150)
    @Column(name = "delivery_time_text", length = 150)
    private String deliveryTimeText;

    @DecimalMin("0.00")
    @Column(name = "cash_on_delivery_charge", precision = 19, scale = 2)
    private BigDecimal cashOnDeliveryCharge = BigDecimal.ZERO;

    // ORDER SETTINGS
    @Size(max = 20)
    @Column(name = "order_prefix", length = 20)
    private String orderPrefix = "ORD";

    @Size(max = 20)
    @Column(name = "invoice_prefix", length = 20)
    private String invoicePrefix = "INV";

    @Column(name = "auto_confirm_order", nullable = false)
    private Boolean autoConfirmOrder = false;

    @Column(name = "auto_cancel_unpaid_order", nullable = false)
    private Boolean autoCancelUnpaidOrder = true;

    @Min(0)
    @Column(name = "cancel_order_after_minutes")
    private Integer cancelOrderAfterMinutes = 60;

    @Min(0)
    @Column(name = "return_allowed_days")
    private Integer returnAllowedDays = 7;

    @Min(0)
    @Column(name = "refund_allowed_days")
    private Integer refundAllowedDays = 7;

    // SOCIAL MEDIA
    @Size(max = 300)
    @Column(name = "facebook_url", length = 300)
    private String facebookUrl;

    @Size(max = 300)
    @Column(name = "youtube_url", length = 300)
    private String youtubeUrl;

    @Size(max = 300)
    @Column(name = "instagram_url", length = 300)
    private String instagramUrl;

    @Size(max = 300)
    @Column(name = "linkedin_url", length = 300)
    private String linkedinUrl;

    @Size(max = 300)
    @Column(name = "twitter_url", length = 300)
    private String twitterUrl;

    @Size(max = 50)
    @Column(name = "whatsapp_number", length = 50)
    private String whatsappNumber;

    // POLICY PAGES
    @Lob
    @Column(name = "about_us", columnDefinition = "LONGTEXT")
    private String aboutUs;

    @Lob
    @Column(name = "contact_us_content", columnDefinition = "LONGTEXT")
    private String contactUsContent;

    @Lob
    @Column(name = "help_page_content", columnDefinition = "LONGTEXT")
    private String helpPageContent;

    @Lob
    @Column(name = "terms_of_use_content", columnDefinition = "LONGTEXT")
    private String termsOfUseContent;

    @Lob
    @Column(name = "terms_and_conditions", columnDefinition = "LONGTEXT")
    private String termsAndConditions;

    @Lob
    @Column(name = "privacy_policy", columnDefinition = "LONGTEXT")
    private String privacyPolicy;

    @Lob
    @Column(name = "payment_methods_content", columnDefinition = "LONGTEXT")
    private String paymentMethodsContent;

    @Lob
    @Column(name = "return_policy", columnDefinition = "LONGTEXT")
    private String returnPolicy;

    @Lob
    @Column(name = "refund_policy", columnDefinition = "LONGTEXT")
    private String refundPolicy;

    @Lob
    @Column(name = "shipping_policy", columnDefinition = "LONGTEXT")
    private String shippingPolicy;

    // MAINTENANCE
    @Column(name = "maintenance_mode", nullable = false)
    private Boolean maintenanceMode = false;

    @Column(name = "maintenance_message", columnDefinition = "TEXT")
    private String maintenanceMessage;

    @Column(name = "registration_enabled", nullable = false)
    private Boolean registrationEnabled = true;

    @Column(name = "vendor_registration_enabled", nullable = false)
    private Boolean vendorRegistrationEnabled = true;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // AUDIT & VERSIONING
    @Version
    @Column(name = "version")
    private Long version;

    @CreatedDate
    @Column(name = "created_on", nullable = false, updatable = false)
    private Instant createdOn;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_on")
    private Instant updatedOn;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public GlobalSettings() {
    }

    public GlobalSettings(int id, String siteName, String siteTitle, String siteTagline, String siteLogo, String favicon, String siteUrl, String adminEmail, String supportEmail, String supportPhone, String address, String metaTitle, String metaDescription, String metaKeywords, String ogTitle, String ogDescription, String ogImage, String googleAnalyticsId, String facebookPixelId, String storeName, BigDecimal maximumOrderAmount, String deliveryTimeText, String facebookUrl, String youtubeUrl, String instagramUrl, String linkedinUrl, String twitterUrl, String whatsappNumber, String aboutUs, String termsAndConditions, String privacyPolicy, String returnPolicy, String refundPolicy, String shippingPolicy, String maintenanceMessage, Long version, Instant createdOn, String createdBy, Instant updatedOn, String updatedBy) {
        this.id = 1;
        this.siteName = siteName;
        this.siteTitle = siteTitle;
        this.siteTagline = siteTagline;
        this.siteLogo = siteLogo;
        this.favicon = favicon;
        this.siteUrl = siteUrl;
        this.adminEmail = adminEmail;
        this.supportEmail = supportEmail;
        this.supportPhone = supportPhone;
        this.address = address;
        this.metaTitle = metaTitle;
        this.metaDescription = metaDescription;
        this.metaKeywords = metaKeywords;
        this.ogTitle = ogTitle;
        this.ogDescription = ogDescription;
        this.ogImage = ogImage;
        this.googleAnalyticsId = googleAnalyticsId;
        this.facebookPixelId = facebookPixelId;

        this.maximumOrderAmount = maximumOrderAmount;
        this.deliveryTimeText = deliveryTimeText;
        this.facebookUrl = facebookUrl;
        this.youtubeUrl = youtubeUrl;
        this.instagramUrl = instagramUrl;
        this.linkedinUrl = linkedinUrl;
        this.twitterUrl = twitterUrl;
        this.whatsappNumber = whatsappNumber;
        this.aboutUs = aboutUs;
        this.termsAndConditions = termsAndConditions;
        this.privacyPolicy = privacyPolicy;
        this.returnPolicy = returnPolicy;
        this.refundPolicy = refundPolicy;
        this.shippingPolicy = shippingPolicy;
        this.maintenanceMessage = maintenanceMessage;
        this.version = version;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
        this.updatedOn = updatedOn;
        this.updatedBy = updatedBy;
    }

    public int getId() {
        return id;
    }

    // ID setter is intentionally disabled - ID is always 1 for singleton pattern
    public void setId(Integer id) {
        // Intentionally do nothing - ID is always forced to 1
        // This prevents accidental or malicious ID changes
    }

    // Force update method for when you need to explicitly update settings
    public void forceUpdate() {
        // This method can be used to trigger entity update through JPA
        // The version field will be incremented automatically
        this.setUpdatedOn(Instant.now());
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getSiteLogo() {
        return siteLogo;
    }

    public void setSiteLogo(String siteLogo) {
        this.siteLogo = siteLogo;
    }

    public String getSiteLogoDesktop() {
        return siteLogoDesktop;
    }

    public void setSiteLogoDesktop(String siteLogoDesktop) {
        this.siteLogoDesktop = siteLogoDesktop;
    }

    public String getSiteLogoMobile() {
        return siteLogoMobile;
    }

    public void setSiteLogoMobile(String siteLogoMobile) {
        this.siteLogoMobile = siteLogoMobile;
    }

    public String getSiteLogoSquare() {
        return siteLogoSquare;
    }

    public void setSiteLogoSquare(String siteLogoSquare) {
        this.siteLogoSquare = siteLogoSquare;
    }

    public String getResolvedSiteLogoDesktop() {
        return siteLogoDesktop != null ? siteLogoDesktop : siteLogo;
    }

    public String getResolvedSiteLogoMobile() {
        if (siteLogoMobile != null) {
            return siteLogoMobile;
        }
        if (siteLogoDesktop != null) {
            return siteLogoDesktop;
        }
        return siteLogo;
    }

    public String getResolvedSiteLogoSquare() {
        if (siteLogoSquare != null) {
            return siteLogoSquare;
        }
        if (siteLogoDesktop != null) {
            return siteLogoDesktop;
        }
        return siteLogo;
    }

    public String getFavicon() {
        return favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
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

    public String getOgImage() {
        return ogImage;
    }

    public void setOgImage(String ogImage) {
        this.ogImage = ogImage;
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

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public Boolean getTaxEnabled() {
        return taxEnabled;
    }

    public void setTaxEnabled(Boolean taxEnabled) {
        this.taxEnabled = taxEnabled;
    }

    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(BigDecimal taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public Boolean getStockManagementEnabled() {
        return stockManagementEnabled;
    }

    public void setStockManagementEnabled(Boolean stockManagementEnabled) {
        this.stockManagementEnabled = stockManagementEnabled;
    }

    public Integer getLowStockAlertQty() {
        return lowStockAlertQty;
    }

    public void setLowStockAlertQty(Integer lowStockAlertQty) {
        this.lowStockAlertQty = lowStockAlertQty;
    }

    public Boolean getAllowGuestCheckout() {
        return allowGuestCheckout;
    }

    public void setAllowGuestCheckout(Boolean allowGuestCheckout) {
        this.allowGuestCheckout = allowGuestCheckout;
    }

    public BigDecimal getMinimumOrderAmount() {
        return minimumOrderAmount;
    }

    public void setMinimumOrderAmount(BigDecimal minimumOrderAmount) {
        this.minimumOrderAmount = minimumOrderAmount;
    }

    public BigDecimal getMaximumOrderAmount() {
        return maximumOrderAmount;
    }

    public void setMaximumOrderAmount(BigDecimal maximumOrderAmount) {
        this.maximumOrderAmount = maximumOrderAmount;
    }

    public Boolean getCodEnabled() {
        return codEnabled;
    }

    public void setCodEnabled(Boolean codEnabled) {
        this.codEnabled = codEnabled;
    }

    public Boolean getOnlinePaymentEnabled() {
        return onlinePaymentEnabled;
    }

    public void setOnlinePaymentEnabled(Boolean onlinePaymentEnabled) {
        this.onlinePaymentEnabled = onlinePaymentEnabled;
    }

    public Boolean getPartialPaymentEnabled() {
        return partialPaymentEnabled;
    }

    public void setPartialPaymentEnabled(Boolean partialPaymentEnabled) {
        this.partialPaymentEnabled = partialPaymentEnabled;
    }

    public Boolean getEmiEnabled() {
        return emiEnabled;
    }

    public void setEmiEnabled(Boolean emiEnabled) {
        this.emiEnabled = emiEnabled;
    }

    public Boolean getDeliveryEnabled() {
        return deliveryEnabled;
    }

    public void setDeliveryEnabled(Boolean deliveryEnabled) {
        this.deliveryEnabled = deliveryEnabled;
    }

    public Boolean getFreeDeliveryEnabled() {
        return freeDeliveryEnabled;
    }

    public void setFreeDeliveryEnabled(Boolean freeDeliveryEnabled) {
        this.freeDeliveryEnabled = freeDeliveryEnabled;
    }

    public BigDecimal getFreeDeliveryMinAmount() {
        return freeDeliveryMinAmount;
    }

    public void setFreeDeliveryMinAmount(BigDecimal freeDeliveryMinAmount) {
        this.freeDeliveryMinAmount = freeDeliveryMinAmount;
    }

    public BigDecimal getInsideDhakaDeliveryCharge() {
        return insideDhakaDeliveryCharge;
    }

    public void setInsideDhakaDeliveryCharge(BigDecimal insideDhakaDeliveryCharge) {
        this.insideDhakaDeliveryCharge = insideDhakaDeliveryCharge;
    }

    public BigDecimal getOutsideDhakaDeliveryCharge() {
        return outsideDhakaDeliveryCharge;
    }

    public void setOutsideDhakaDeliveryCharge(BigDecimal outsideDhakaDeliveryCharge) {
        this.outsideDhakaDeliveryCharge = outsideDhakaDeliveryCharge;
    }

    public String getDeliveryTimeText() {
        return deliveryTimeText;
    }

    public void setDeliveryTimeText(String deliveryTimeText) {
        this.deliveryTimeText = deliveryTimeText;
    }

    public BigDecimal getCashOnDeliveryCharge() {
        return cashOnDeliveryCharge;
    }

    public void setCashOnDeliveryCharge(BigDecimal cashOnDeliveryCharge) {
        this.cashOnDeliveryCharge = cashOnDeliveryCharge;
    }

    public String getOrderPrefix() {
        return orderPrefix;
    }

    public void setOrderPrefix(String orderPrefix) {
        this.orderPrefix = orderPrefix;
    }

    public String getInvoicePrefix() {
        return invoicePrefix;
    }

    public void setInvoicePrefix(String invoicePrefix) {
        this.invoicePrefix = invoicePrefix;
    }

    public Boolean getAutoConfirmOrder() {
        return autoConfirmOrder;
    }

    public void setAutoConfirmOrder(Boolean autoConfirmOrder) {
        this.autoConfirmOrder = autoConfirmOrder;
    }

    public Boolean getAutoCancelUnpaidOrder() {
        return autoCancelUnpaidOrder;
    }

    public void setAutoCancelUnpaidOrder(Boolean autoCancelUnpaidOrder) {
        this.autoCancelUnpaidOrder = autoCancelUnpaidOrder;
    }

    public Integer getCancelOrderAfterMinutes() {
        return cancelOrderAfterMinutes;
    }

    public void setCancelOrderAfterMinutes(Integer cancelOrderAfterMinutes) {
        this.cancelOrderAfterMinutes = cancelOrderAfterMinutes;
    }

    public Integer getReturnAllowedDays() {
        return returnAllowedDays;
    }

    public void setReturnAllowedDays(Integer returnAllowedDays) {
        this.returnAllowedDays = returnAllowedDays;
    }

    public Integer getRefundAllowedDays() {
        return refundAllowedDays;
    }

    public void setRefundAllowedDays(Integer refundAllowedDays) {
        this.refundAllowedDays = refundAllowedDays;
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

    public String getAboutUs() {
        return aboutUs;
    }

    public void setAboutUs(String aboutUs) {
        this.aboutUs = aboutUs;
    }

    public String getContactUsContent() {
        return contactUsContent;
    }

    public void setContactUsContent(String contactUsContent) {
        this.contactUsContent = contactUsContent;
    }

    public String getHelpPageContent() {
        return helpPageContent;
    }

    public void setHelpPageContent(String helpPageContent) {
        this.helpPageContent = helpPageContent;
    }

    public String getTermsOfUseContent() {
        return termsOfUseContent;
    }

    public void setTermsOfUseContent(String termsOfUseContent) {
        this.termsOfUseContent = termsOfUseContent;
    }

    public String getTermsAndConditions() {
        return termsAndConditions;
    }

    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }

    public String getPrivacyPolicy() {
        return privacyPolicy;
    }

    public void setPrivacyPolicy(String privacyPolicy) {
        this.privacyPolicy = privacyPolicy;
    }

    public String getPaymentMethodsContent() {
        return paymentMethodsContent;
    }

    public void setPaymentMethodsContent(String paymentMethodsContent) {
        this.paymentMethodsContent = paymentMethodsContent;
    }

    public String getReturnPolicy() {
        return returnPolicy;
    }

    public void setReturnPolicy(String returnPolicy) {
        this.returnPolicy = returnPolicy;
    }

    public String getRefundPolicy() {
        return refundPolicy;
    }

    public void setRefundPolicy(String refundPolicy) {
        this.refundPolicy = refundPolicy;
    }

    public String getShippingPolicy() {
        return shippingPolicy;
    }

    public void setShippingPolicy(String shippingPolicy) {
        this.shippingPolicy = shippingPolicy;
    }

    public Boolean getMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(Boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

    public String getMaintenanceMessage() {
        return maintenanceMessage;
    }

    public void setMaintenanceMessage(String maintenanceMessage) {
        this.maintenanceMessage = maintenanceMessage;
    }

    public Boolean getRegistrationEnabled() {
        return registrationEnabled;
    }

    public void setRegistrationEnabled(Boolean registrationEnabled) {
        this.registrationEnabled = registrationEnabled;
    }

    public Boolean getVendorRegistrationEnabled() {
        return vendorRegistrationEnabled;
    }

    public void setVendorRegistrationEnabled(Boolean vendorRegistrationEnabled) {
        this.vendorRegistrationEnabled = vendorRegistrationEnabled;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Instant updatedOn) {
        this.updatedOn = updatedOn;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @PrePersist
    @PreUpdate
    private void enforceSingletonPattern() {
        // Force ID to always be 1
        this.id = 1;

        // Ensure UUID exists
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
    }
}
