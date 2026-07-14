package com.ecommerce.app.module.blog.model;

import com.ecommerce.app.product.model.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "blog_related_products",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_blog_related_product", columnNames = {"blog_id", "product_id"})
        },
        indexes = {
            @Index(name = "idx_blog_related_product_blog", columnList = "blog_id,sort_order"),
            @Index(name = "idx_blog_related_product_product", columnList = "product_id")
        }
)
public class BlogRelatedProduct extends BaseBlogEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "relation_type", length = 80)
    private String relationType = "RELATED";

    @Column(name = "cta_label", length = 80)
    private String ctaLabel = "View Product";

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public String getCtaLabel() {
        return ctaLabel;
    }

    public void setCtaLabel(String ctaLabel) {
        this.ctaLabel = ctaLabel;
    }
}
