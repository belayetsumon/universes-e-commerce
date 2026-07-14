package com.ecommerce.app.module.blog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
        name = "blog_media",
        indexes = {
            @Index(name = "idx_blog_media_blog_type", columnList = "blog_id,media_type"),
            @Index(name = "idx_blog_media_sort", columnList = "sort_order")
        }
)
public class BlogMedia extends BaseBlogEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @NotNull(message = "Media type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 40)
    private BlogMediaType mediaType;

    @NotBlank(message = "Media URL is required.")
    @Column(name = "media_url", nullable = false, length = 700)
    private String mediaUrl;

    @Column(name = "webp_url", length = 700)
    private String webpUrl;

    @Column(name = "responsive_srcset", length = 1000)
    private String responsiveSrcset;

    @Column(name = "alt_text", length = 220)
    private String altText;

    @Column(name = "caption", length = 500)
    private String caption;

    @Column(name = "lazy_load", nullable = false)
    private boolean lazyLoad = true;

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public BlogMediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(BlogMediaType mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getWebpUrl() {
        return webpUrl;
    }

    public void setWebpUrl(String webpUrl) {
        this.webpUrl = webpUrl;
    }

    public String getResponsiveSrcset() {
        return responsiveSrcset;
    }

    public void setResponsiveSrcset(String responsiveSrcset) {
        this.responsiveSrcset = responsiveSrcset;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public boolean isLazyLoad() {
        return lazyLoad;
    }

    public void setLazyLoad(boolean lazyLoad) {
        this.lazyLoad = lazyLoad;
    }
}
