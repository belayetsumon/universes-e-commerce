package com.ecommerce.app.module.blog.model;

import com.ecommerce.app.vendor.model.Vendorprofile;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "blog_related_vendors",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_blog_related_vendor", columnNames = {"blog_id", "vendor_id"})
        },
        indexes = {
            @Index(name = "idx_blog_related_vendor_blog", columnList = "blog_id,sort_order")
        }
)
public class BlogRelatedVendor extends BaseBlogEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendorprofile vendor;

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public Vendorprofile getVendor() {
        return vendor;
    }

    public void setVendor(Vendorprofile vendor) {
        this.vendor = vendor;
    }
}
