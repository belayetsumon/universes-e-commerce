package com.ecommerce.app.module.blog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "blog_analytics",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_blog_analytics_day", columnNames = {"blog_id", "metric_date"})
        },
        indexes = {
            @Index(name = "idx_blog_analytics_date", columnList = "metric_date"),
            @Index(name = "idx_blog_analytics_performance", columnList = "views,product_clicks,revenue_attribution")
        }
)
public class BlogAnalytics extends BaseBlogEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "views", nullable = false)
    private Long views = 0L;

    @Column(name = "unique_visitors", nullable = false)
    private Long uniqueVisitors = 0L;

    @Column(name = "average_reading_seconds", nullable = false)
    private Integer averageReadingSeconds = 0;

    @Column(name = "average_scroll_depth", nullable = false)
    private Integer averageScrollDepth = 0;

    @Column(name = "bounce_rate", precision = 8, scale = 4)
    private BigDecimal bounceRate = BigDecimal.ZERO;

    @Column(name = "ctr", precision = 8, scale = 4)
    private BigDecimal ctr = BigDecimal.ZERO;

    @Column(name = "product_clicks", nullable = false)
    private Long productClicks = 0L;

    @Column(name = "add_to_cart_count", nullable = false)
    private Long addToCartCount = 0L;

    @Column(name = "revenue_attribution", precision = 18, scale = 2)
    private BigDecimal revenueAttribution = BigDecimal.ZERO;

    @Column(name = "social_shares", nullable = false)
    private Long socialShares = 0L;

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public LocalDate getMetricDate() {
        return metricDate;
    }

    public void setMetricDate(LocalDate metricDate) {
        this.metricDate = metricDate;
    }

    public Long getViews() {
        return views;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public Long getUniqueVisitors() {
        return uniqueVisitors;
    }

    public void setUniqueVisitors(Long uniqueVisitors) {
        this.uniqueVisitors = uniqueVisitors;
    }

    public Integer getAverageReadingSeconds() {
        return averageReadingSeconds;
    }

    public void setAverageReadingSeconds(Integer averageReadingSeconds) {
        this.averageReadingSeconds = averageReadingSeconds;
    }

    public Integer getAverageScrollDepth() {
        return averageScrollDepth;
    }

    public void setAverageScrollDepth(Integer averageScrollDepth) {
        this.averageScrollDepth = averageScrollDepth;
    }

    public BigDecimal getBounceRate() {
        return bounceRate;
    }

    public void setBounceRate(BigDecimal bounceRate) {
        this.bounceRate = bounceRate;
    }

    public BigDecimal getCtr() {
        return ctr;
    }

    public void setCtr(BigDecimal ctr) {
        this.ctr = ctr;
    }

    public Long getProductClicks() {
        return productClicks;
    }

    public void setProductClicks(Long productClicks) {
        this.productClicks = productClicks;
    }

    public Long getAddToCartCount() {
        return addToCartCount;
    }

    public void setAddToCartCount(Long addToCartCount) {
        this.addToCartCount = addToCartCount;
    }

    public BigDecimal getRevenueAttribution() {
        return revenueAttribution;
    }

    public void setRevenueAttribution(BigDecimal revenueAttribution) {
        this.revenueAttribution = revenueAttribution;
    }

    public Long getSocialShares() {
        return socialShares;
    }

    public void setSocialShares(Long socialShares) {
        this.socialShares = socialShares;
    }
}
