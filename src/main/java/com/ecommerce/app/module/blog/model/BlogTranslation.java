package com.ecommerce.app.module.blog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(
        name = "blog_translations",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_blog_translation_language", columnNames = {"blog_id", "language_code"})
        },
        indexes = {
            @Index(name = "idx_blog_translation_language", columnList = "language_code,record_status")
        }
)
public class BlogTranslation extends BaseBlogEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @NotBlank(message = "Language is required.")
    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Column(name = "rtl_enabled", nullable = false)
    private boolean rtlEnabled = false;

    @NotBlank(message = "Translated title is required.")
    @Column(name = "title", nullable = false, length = 220)
    private String title;

    @Column(name = "slug", nullable = false, length = 240)
    private String slug;

    @Column(name = "excerpt", length = 500)
    private String excerpt;

    @Lob
    @Column(name = "content_html", nullable = false, columnDefinition = "TEXT")
    private String contentHtml;

    @Column(name = "seo_title", length = 180)
    private String seoTitle;

    @Column(name = "meta_description", length = 320)
    private String metaDescription;

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public boolean isRtlEnabled() {
        return rtlEnabled;
    }

    public void setRtlEnabled(boolean rtlEnabled) {
        this.rtlEnabled = rtlEnabled;
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
}
