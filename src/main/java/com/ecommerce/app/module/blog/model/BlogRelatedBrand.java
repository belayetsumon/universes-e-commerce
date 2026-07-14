package com.ecommerce.app.module.blog.model;

import com.ecommerce.app.product.model.Manufacturer;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "blog_related_brands",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_blog_related_brand", columnNames = {"blog_id", "brand_id"})
        },
        indexes = {
            @Index(name = "idx_blog_related_brand_blog", columnList = "blog_id,sort_order")
        }
)
public class BlogRelatedBrand extends BaseBlogEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Manufacturer brand;

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public Manufacturer getBrand() {
        return brand;
    }

    public void setBrand(Manufacturer brand) {
        this.brand = brand;
    }
}
