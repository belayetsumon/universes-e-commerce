package com.ecommerce.app.module.blog.model;

import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "blog_comments",
        indexes = {
            @Index(name = "idx_blog_comment_blog_status", columnList = "blog_id,moderation_status,created_at"),
            @Index(name = "idx_blog_comment_parent", columnList = "parent_id"),
            @Index(name = "idx_blog_comment_user", columnList = "user_id")
        }
)
public class BlogComment extends BaseBlogEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private BlogComment parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @NotBlank(message = "Name is required.")
    @Size(max = 120, message = "Name cannot exceed 120 characters.")
    @Column(name = "guest_name", nullable = false, length = 120)
    private String guestName;

    @Email(message = "Enter a valid email.")
    @Size(max = 180, message = "Email cannot exceed 180 characters.")
    @Column(name = "guest_email", length = 180)
    private String guestEmail;

    @NotBlank(message = "Comment is required.")
    @Lob
    @Column(name = "comment_text", nullable = false, columnDefinition = "TEXT")
    private String commentText;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false, length = 30)
    private BlogModerationStatus moderationStatus = BlogModerationStatus.PENDING;

    @Column(name = "ip_hash", length = 128)
    private String ipHash;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "report_count", nullable = false)
    private Integer reportCount = 0;

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public BlogComment getParent() {
        return parent;
    }

    public void setParent(BlogComment parent) {
        this.parent = parent;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public BlogModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(BlogModerationStatus moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public String getIpHash() {
        return ipHash;
    }

    public void setIpHash(String ipHash) {
        this.ipHash = ipHash;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Integer getReportCount() {
        return reportCount;
    }

    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }
}
