package com.ecommerce.app.module.blog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "blog_seo",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_blog_seo_blog", columnNames = {"blog_id"})
        },
        indexes = {
            @Index(name = "idx_blog_seo_canonical", columnList = "canonical_url"),
            @Index(name = "idx_blog_seo_robots", columnList = "robots_meta")
        }
)
public class BlogSeo extends BaseBlogEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @Size(max = 180, message = "SEO title cannot exceed 180 characters.")
    @Column(name = "seo_title", length = 180)
    private String seoTitle;

    @Size(max = 320, message = "Meta description cannot exceed 320 characters.")
    @Column(name = "meta_description", length = 320)
    private String metaDescription;

    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;

    @Column(name = "canonical_url", length = 500)
    private String canonicalUrl;

    @Column(name = "robots_meta", length = 80)
    private String robotsMeta = "index,follow";

    @Column(name = "open_graph_title", length = 180)
    private String openGraphTitle;

    @Column(name = "open_graph_description", length = 320)
    private String openGraphDescription;

    @Column(name = "open_graph_image", length = 500)
    private String openGraphImage;

    @Column(name = "twitter_card", length = 80)
    private String twitterCard = "summary_large_image";

    @Lob
    @Column(name = "json_ld", columnDefinition = "TEXT")
    private String jsonLd;

    @Lob
    @Column(name = "breadcrumb_schema", columnDefinition = "TEXT")
    private String breadcrumbSchema;

    @Lob
    @Column(name = "faq_schema", columnDefinition = "TEXT")
    private String faqSchema;

    @Lob
    @Column(name = "article_schema", columnDefinition = "TEXT")
    private String articleSchema;

    @Column(name = "redirect_from", length = 500)
    private String redirectFrom;

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
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

    public String getBreadcrumbSchema() {
        return breadcrumbSchema;
    }

    public void setBreadcrumbSchema(String breadcrumbSchema) {
        this.breadcrumbSchema = breadcrumbSchema;
    }

    public String getFaqSchema() {
        return faqSchema;
    }

    public void setFaqSchema(String faqSchema) {
        this.faqSchema = faqSchema;
    }

    public String getArticleSchema() {
        return articleSchema;
    }

    public void setArticleSchema(String articleSchema) {
        this.articleSchema = articleSchema;
    }

    public String getRedirectFrom() {
        return redirectFrom;
    }

    public void setRedirectFrom(String redirectFrom) {
        this.redirectFrom = redirectFrom;
    }
}
