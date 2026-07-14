package com.ecommerce.app.module.blog.dto;

import com.ecommerce.app.module.blog.model.BlogPublicationStatus;
import com.ecommerce.app.module.blog.model.BlogVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class BlogForm {

    private Long id;
    private Long version;

    @NotBlank(message = "Title is required.")
    @Size(max = 220, message = "Title cannot exceed 220 characters.")
    private String title;

    @NotBlank(message = "Slug is required.")
    @Size(max = 240, message = "Slug cannot exceed 240 characters.")
    private String slug;

    @Size(max = 500, message = "Excerpt cannot exceed 500 characters.")
    private String excerpt;

    @NotBlank(message = "Content is required.")
    private String contentHtml;

    @NotNull(message = "Status is required.")
    private BlogPublicationStatus status = BlogPublicationStatus.DRAFT;

    @NotNull(message = "Visibility is required.")
    private BlogVisibility visibility = BlogVisibility.PUBLIC;

    private Long categoryId;
    private Long authorId;
    private Long seriesId;
    private String tags;
    private String featuredImageUrl;
    private String featuredImageAlt;
    private String languageCode = "en";
    private String countryCodes;
    private String deviceRules;
    private String customerSegment;
    private boolean stickyPost;
    private boolean featuredPost;
    private boolean allowComments = true;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime scheduledAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime expiresAt;

    private String templateKey;
    private String utmCampaign;
    private String seoTitle;
    private String metaDescription;
    private String metaKeywords;
    private String canonicalUrl;
    private String robotsMeta = "index,follow";
    private String openGraphTitle;
    private String openGraphDescription;
    private String openGraphImage;
    private String twitterCard = "summary_large_image";
    private String jsonLd;
    private String changeSummary;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }

    public BlogPublicationStatus getStatus() {
        return status;
    }

    public void setStatus(BlogPublicationStatus status) {
        this.status = status;
    }

    public BlogVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(BlogVisibility visibility) {
        this.visibility = visibility;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Long seriesId) {
        this.seriesId = seriesId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getFeaturedImageUrl() {
        return featuredImageUrl;
    }

    public void setFeaturedImageUrl(String featuredImageUrl) {
        this.featuredImageUrl = featuredImageUrl;
    }

    public String getFeaturedImageAlt() {
        return featuredImageAlt;
    }

    public void setFeaturedImageAlt(String featuredImageAlt) {
        this.featuredImageAlt = featuredImageAlt;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getCountryCodes() {
        return countryCodes;
    }

    public void setCountryCodes(String countryCodes) {
        this.countryCodes = countryCodes;
    }

    public String getDeviceRules() {
        return deviceRules;
    }

    public void setDeviceRules(String deviceRules) {
        this.deviceRules = deviceRules;
    }

    public String getCustomerSegment() {
        return customerSegment;
    }

    public void setCustomerSegment(String customerSegment) {
        this.customerSegment = customerSegment;
    }

    public boolean isStickyPost() {
        return stickyPost;
    }

    public void setStickyPost(boolean stickyPost) {
        this.stickyPost = stickyPost;
    }

    public boolean isFeaturedPost() {
        return featuredPost;
    }

    public void setFeaturedPost(boolean featuredPost) {
        this.featuredPost = featuredPost;
    }

    public boolean isAllowComments() {
        return allowComments;
    }

    public void setAllowComments(boolean allowComments) {
        this.allowComments = allowComments;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }

    public String getUtmCampaign() {
        return utmCampaign;
    }

    public void setUtmCampaign(String utmCampaign) {
        this.utmCampaign = utmCampaign;
    }

    public String getSeoTitle() {
        return seoTitle;
    }

    public void setSeoTitle(String seoTitle) {
        this.seoTitle = seoTitle;
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

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public void setCanonicalUrl(String canonicalUrl) {
        this.canonicalUrl = canonicalUrl;
    }

    public String getRobotsMeta() {
        return robotsMeta;
    }

    public void setRobotsMeta(String robotsMeta) {
        this.robotsMeta = robotsMeta;
    }

    public String getOpenGraphTitle() {
        return openGraphTitle;
    }

    public void setOpenGraphTitle(String openGraphTitle) {
        this.openGraphTitle = openGraphTitle;
    }

    public String getOpenGraphDescription() {
        return openGraphDescription;
    }

    public void setOpenGraphDescription(String openGraphDescription) {
        this.openGraphDescription = openGraphDescription;
    }

    public String getOpenGraphImage() {
        return openGraphImage;
    }

    public void setOpenGraphImage(String openGraphImage) {
        this.openGraphImage = openGraphImage;
    }

    public String getTwitterCard() {
        return twitterCard;
    }

    public void setTwitterCard(String twitterCard) {
        this.twitterCard = twitterCard;
    }

    public String getJsonLd() {
        return jsonLd;
    }

    public void setJsonLd(String jsonLd) {
        this.jsonLd = jsonLd;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }
}
