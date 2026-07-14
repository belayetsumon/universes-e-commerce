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
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
        name = "blog_revisions",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_blog_revision_number", columnNames = {"blog_id", "revision_number"})
        },
        indexes = {
            @Index(name = "idx_blog_revision_blog", columnList = "blog_id,created_at"),
            @Index(name = "idx_blog_revision_author", columnList = "created_by")
        }
)
public class BlogRevision extends BaseBlogEntity {

    @NotNull(message = "Blog is required.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @Column(name = "revision_number", nullable = false)
    private Integer revisionNumber;

    @Column(name = "title", nullable = false, length = 220)
    private String title;

    @Column(name = "slug", nullable = false, length = 240)
    private String slug;

    @Lob
    @Column(name = "content_html", nullable = false, columnDefinition = "TEXT")
    private String contentHtml;

    @Lob
    @Column(name = "change_summary", columnDefinition = "TEXT")
    private String changeSummary;

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public Integer getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(Integer revisionNumber) {
        this.revisionNumber = revisionNumber;
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

    public String getContentHtml() {
        return contentHtml;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }
}
