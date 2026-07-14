package com.ecommerce.app.module.blog.model;

import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "blog_views",
        indexes = {
            @Index(name = "idx_blog_view_blog_time", columnList = "blog_id,viewed_at"),
            @Index(name = "idx_blog_view_unique", columnList = "visitor_key,blog_id"),
            @Index(name = "idx_blog_view_utm", columnList = "utm_campaign")
        }
)
public class BlogView extends BaseBlogEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "visitor_key", length = 120)
    private String visitorKey;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt = LocalDateTime.now();

    @Column(name = "reading_seconds")
    private Integer readingSeconds;

    @Column(name = "scroll_depth")
    private Integer scrollDepth;

    @Column(name = "utm_campaign", length = 160)
    private String utmCampaign;

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getVisitorKey() {
        return visitorKey;
    }

    public void setVisitorKey(String visitorKey) {
        this.visitorKey = visitorKey;
    }

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }

    public Integer getReadingSeconds() {
        return readingSeconds;
    }

    public void setReadingSeconds(Integer readingSeconds) {
        this.readingSeconds = readingSeconds;
    }

    public Integer getScrollDepth() {
        return scrollDepth;
    }

    public void setScrollDepth(Integer scrollDepth) {
        this.scrollDepth = scrollDepth;
    }

    public String getUtmCampaign() {
        return utmCampaign;
    }

    public void setUtmCampaign(String utmCampaign) {
        this.utmCampaign = utmCampaign;
    }
}
