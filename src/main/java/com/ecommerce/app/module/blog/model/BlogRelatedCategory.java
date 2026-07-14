package com.ecommerce.app.module.blog.model;

import com.ecommerce.app.product.model.Productcategory;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "blog_related_categories",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_blog_related_category", columnNames = {"blog_id", "category_id"})
        },
        indexes = {
            @Index(name = "idx_blog_related_category_blog", columnList = "blog_id,sort_order")
        }
)
public class BlogRelatedCategory extends BaseBlogEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Productcategory category;

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public Productcategory getCategory() {
        return category;
    }

    public void setCategory(Productcategory category) {
        this.category = category;
    }
}
