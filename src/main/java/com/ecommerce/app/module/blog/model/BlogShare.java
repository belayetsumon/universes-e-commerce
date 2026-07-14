package com.ecommerce.app.module.blog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "blog_shares",
        indexes = {
            @Index(name = "idx_blog_share_blog_platform", columnList = "blog_id,platform"),
            @Index(name = "idx_blog_share_campaign", columnList = "utm_campaign")
        }
)
public class BlogShare extends BaseBlogEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @Column(name = "platform", nullable = false, length = 80)
    private String platform;

    @Column(name = "share_url", length = 700)
    private String shareUrl;

    @Column(name = "utm_campaign", length = 160)
    private String utmCampaign;

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getUtmCampaign() {
        return utmCampaign;
    }

    public void setUtmCampaign(String utmCampaign) {
        this.utmCampaign = utmCampaign;
    }
}
