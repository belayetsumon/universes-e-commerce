package com.ecommerce.app.module.blog.model;

import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "blog_authors",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_blog_author_slug", columnNames = {"slug"})
        },
        indexes = {
            @Index(name = "idx_blog_author_status", columnList = "record_status,active_flag,deleted_flag"),
            @Index(name = "idx_blog_author_user", columnList = "user_id")
        }
)
public class BlogAuthor extends BaseBlogEntity {

    @NotBlank(message = "Author name is required.")
    @Size(max = 160, message = "Author name cannot exceed 160 characters.")
    @Column(name = "display_name", nullable = false, length = 160)
    private String displayName;

    @NotBlank(message = "Author slug is required.")
    @Size(max = 180, message = "Slug cannot exceed 180 characters.")
    @Column(name = "slug", nullable = false, length = 180)
    private String slug;

    @Email(message = "Enter a valid author email.")
    @Size(max = 180, message = "Email cannot exceed 180 characters.")
    @Column(name = "email", length = 180)
    private String email;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Lob
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}
