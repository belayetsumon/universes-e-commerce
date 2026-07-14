package com.ecommerce.app.module.blog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "blog_series",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_blog_series_slug", columnNames = {"slug"})
        },
        indexes = {
            @Index(name = "idx_blog_series_status", columnList = "record_status,active_flag,deleted_flag"),
            @Index(name = "idx_blog_series_sort", columnList = "sort_order")
        }
)
public class BlogSeries extends BaseBlogEntity {

    @NotBlank(message = "Series title is required.")
    @Size(max = 180, message = "Series title cannot exceed 180 characters.")
    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @NotBlank(message = "Series slug is required.")
    @Size(max = 180, message = "Slug cannot exceed 180 characters.")
    @Column(name = "slug", nullable = false, length = 180)
    private String slug;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
