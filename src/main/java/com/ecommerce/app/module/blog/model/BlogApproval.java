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
        name = "blog_approvals",
        indexes = {
            @Index(name = "idx_blog_approval_blog", columnList = "blog_id,created_at"),
            @Index(name = "idx_blog_approval_decision", columnList = "decision,decided_at")
        }
)
public class BlogApproval extends BaseBlogEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @Column(name = "reviewer", length = 120)
    private String reviewer;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 40)
    private BlogApprovalDecision decision = BlogApprovalDecision.PENDING;

    @Lob
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public BlogApprovalDecision getDecision() {
        return decision;
    }

    public void setDecision(BlogApprovalDecision decision) {
        this.decision = decision;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }
}
