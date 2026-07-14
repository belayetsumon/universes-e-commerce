package com.ecommerce.app.module.blog.model;

import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "blog_comment_reactions",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_blog_comment_reaction_actor", columnNames = {"comment_id", "user_id", "anonymous_key", "reaction_type"})
        },
        indexes = {
            @Index(name = "idx_blog_comment_reaction_comment", columnList = "comment_id,reaction_type")
        }
)
public class BlogCommentReaction extends BaseBlogEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private BlogComment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "anonymous_key", length = 120)
    private String anonymousKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false, length = 30)
    private BlogReactionType reactionType;

    public BlogComment getComment() {
        return comment;
    }

    public void setComment(BlogComment comment) {
        this.comment = comment;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getAnonymousKey() {
        return anonymousKey;
    }

    public void setAnonymousKey(String anonymousKey) {
        this.anonymousKey = anonymousKey;
    }

    public BlogReactionType getReactionType() {
        return reactionType;
    }

    public void setReactionType(BlogReactionType reactionType) {
        this.reactionType = reactionType;
    }
}
