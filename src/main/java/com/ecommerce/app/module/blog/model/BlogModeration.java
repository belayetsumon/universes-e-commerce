package com.ecommerce.app.module.blog.model;

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
import java.time.LocalDateTime;

@Entity
@Table(
        name = "blog_moderation",
        indexes = {
            @Index(name = "idx_blog_moderation_blog", columnList = "blog_id,created_at"),
            @Index(name = "idx_blog_moderation_status", columnList = "moderation_status")
        }
)
public class BlogModeration extends BaseBlogEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id")
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private BlogComment comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false, length = 30)
    private BlogModerationStatus moderationStatus = BlogModerationStatus.PENDING;

    @Column(name = "moderator", length = 120)
    private String moderator;

    @Lob
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public BlogComment getComment() {
        return comment;
    }

    public void setComment(BlogComment comment) {
        this.comment = comment;
    }

    public BlogModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(BlogModerationStatus moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public String getModerator() {
        return moderator;
    }

    public void setModerator(String moderator) {
        this.moderator = moderator;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getModeratedAt() {
        return moderatedAt;
    }

    public void setModeratedAt(LocalDateTime moderatedAt) {
        this.moderatedAt = moderatedAt;
    }
}
