package com.ecommerce.app.module.blog.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "blogs",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_blog_slug_language", columnNames = {"slug", "language_code"})
        },
        indexes = {
            @Index(name = "idx_blog_status_publish", columnList = "status,published_at,scheduled_at"),
            @Index(name = "idx_blog_category", columnList = "category_id"),
            @Index(name = "idx_blog_author", columnList = "author_id"),
            @Index(name = "idx_blog_visibility", columnList = "visibility"),
            @Index(name = "idx_blog_featured_sticky", columnList = "featured_post,sticky_post,sort_order"),
            @Index(name = "idx_blog_deleted", columnList = "deleted_flag,active_flag")
        }
)
public class Blog extends BaseBlogEntity {

    @NotBlank(message = "Blog title is required.")
    @Size(max = 220, message = "Title cannot exceed 220 characters.")
    @Column(name = "title", nullable = false, length = 220)
    private String title;

    @NotBlank(message = "Blog slug is required.")
    @Size(max = 240, message = "Slug cannot exceed 240 characters.")
    @Column(name = "slug", nullable = false, length = 240)
    private String slug;

    @Size(max = 500, message = "Excerpt cannot exceed 500 characters.")
    @Column(name = "excerpt", length = 500)
    private String excerpt;

    @NotBlank(message = "Blog content is required.")
    @Lob
    @Column(name = "content_html", nullable = false, columnDefinition = "TEXT")
    private String contentHtml;

    @Lob
    @Column(name = "content_plain_text", columnDefinition = "TEXT")
    private String contentPlainText;

    @NotNull(message = "Publication status is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private BlogPublicationStatus status = BlogPublicationStatus.DRAFT;

    @NotNull(message = "Visibility is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 40)
    private BlogVisibility visibility = BlogVisibility.PUBLIC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private BlogCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private BlogAuthor author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private BlogSeries series;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "blog_tag_map",
            joinColumns = @JoinColumn(name = "blog_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_blog_tag_map", columnNames = {"blog_id", "tag_id"}),
            indexes = {
                @Index(name = "idx_blog_tag_map_blog", columnList = "blog_id"),
                @Index(name = "idx_blog_tag_map_tag", columnList = "tag_id")
            }
    )
    private Set<BlogTag> tags = new HashSet<>();

    @OneToOne(mappedBy = "blog", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private BlogSeo seo;

    @Column(name = "featured_image_url", length = 500)
    private String featuredImageUrl;

    @Column(name = "featured_image_alt", length = 220)
    private String featuredImageAlt;

    @Column(name = "password_hash", length = 120)
    private String passwordHash;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode = "en";

    @Column(name = "country_codes", length = 500)
    private String countryCodes;

    @Column(name = "device_rules", length = 250)
    private String deviceRules;

    @Column(name = "customer_segment", length = 120)
    private String customerSegment;

    @Column(name = "sticky_post", nullable = false)
    private boolean stickyPost = false;

    @Column(name = "featured_post", nullable = false)
    private boolean featuredPost = false;

    @Column(name = "allow_comments", nullable = false)
    private boolean allowComments = true;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "reading_time_minutes", nullable = false)
    private Integer readingTimeMinutes = 1;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "comment_count", nullable = false)
    private Long commentCount = 0L;

    @Column(name = "share_count", nullable = false)
    private Long shareCount = 0L;

    @Column(name = "template_key", length = 120)
    private String templateKey;

    @Column(name = "utm_campaign", length = 160)
    private String utmCampaign;

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

    public String getContentPlainText() {
        return contentPlainText;
    }

    public void setContentPlainText(String contentPlainText) {
        this.contentPlainText = contentPlainText;
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

    public BlogCategory getCategory() {
        return category;
    }

    public void setCategory(BlogCategory category) {
        this.category = category;
    }

    public BlogAuthor getAuthor() {
        return author;
    }

    public void setAuthor(BlogAuthor author) {
        this.author = author;
    }

    public BlogSeries getSeries() {
        return series;
    }

    public void setSeries(BlogSeries series) {
        this.series = series;
    }

    public Set<BlogTag> getTags() {
        return tags;
    }

    public void setTags(Set<BlogTag> tags) {
        this.tags = tags;
    }

    public BlogSeo getSeo() {
        return seo;
    }

    public void setSeo(BlogSeo seo) {
        this.seo = seo;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Integer getReadingTimeMinutes() {
        return readingTimeMinutes;
    }

    public void setReadingTimeMinutes(Integer readingTimeMinutes) {
        this.readingTimeMinutes = readingTimeMinutes;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Long getShareCount() {
        return shareCount;
    }

    public void setShareCount(Long shareCount) {
        this.shareCount = shareCount;
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
}
