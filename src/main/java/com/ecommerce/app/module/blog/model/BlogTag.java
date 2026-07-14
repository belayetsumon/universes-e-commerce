package com.ecommerce.app.module.blog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "blog_tags",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_blog_tag_slug", columnNames = {"slug"})
        },
        indexes = {
            @Index(name = "idx_blog_tag_status", columnList = "record_status,active_flag,deleted_flag"),
            @Index(name = "idx_blog_tag_sort", columnList = "sort_order")
        }
)
public class BlogTag extends BaseBlogEntity {

    @NotBlank(message = "Tag name is required.")
    @Size(max = 120, message = "Tag name cannot exceed 120 characters.")
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @NotBlank(message = "Tag slug is required.")
    @Size(max = 150, message = "Slug cannot exceed 150 characters.")
    @Column(name = "slug", nullable = false, length = 150)
    private String slug;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
