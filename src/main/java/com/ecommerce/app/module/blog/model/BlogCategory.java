package com.ecommerce.app.module.blog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "blog_categories",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_blog_category_slug", columnNames = {"slug"})
        },
        indexes = {
            @Index(name = "idx_blog_category_status", columnList = "record_status,active_flag,deleted_flag"),
            @Index(name = "idx_blog_category_parent", columnList = "parent_id"),
            @Index(name = "idx_blog_category_sort", columnList = "sort_order")
        }
)
public class BlogCategory extends BaseBlogEntity {

    @NotBlank(message = "Category name is required.")
    @Size(max = 150, message = "Category name cannot exceed 150 characters.")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @NotBlank(message = "Category slug is required.")
    @Size(max = 180, message = "Slug cannot exceed 180 characters.")
    @Column(name = "slug", nullable = false, length = 180)
    private String slug;

    @Size(max = 500, message = "Description cannot exceed 500 characters.")
    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private BlogCategory parent;

    @Column(name = "seo_title", length = 180)
    private String seoTitle;

    @Column(name = "meta_description", length = 320)
    private String metaDescription;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BlogCategory getParent() {
        return parent;
    }

    public void setParent(BlogCategory parent) {
        this.parent = parent;
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
