package com.ecommerce.app.module.blog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "blog_subscribers",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_blog_subscriber_email", columnNames = {"email"})
        },
        indexes = {
            @Index(name = "idx_blog_subscriber_status", columnList = "subscriber_status,active_flag")
        }
)
public class BlogSubscriber extends BaseBlogEntity {

    @NotBlank(message = "Email is required.")
    @Email(message = "Enter a valid email.")
    @Column(name = "email", nullable = false, length = 180)
    private String email;

    @Column(name = "name", length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscriber_status", nullable = false, length = 30)
    private BlogSubscriberStatus subscriberStatus = BlogSubscriberStatus.SUBSCRIBED;

    @Column(name = "source", length = 100)
    private String source;

    @Column(name = "consent_at")
    private LocalDateTime consentAt;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BlogSubscriberStatus getSubscriberStatus() {
        return subscriberStatus;
    }

    public void setSubscriberStatus(BlogSubscriberStatus subscriberStatus) {
        this.subscriberStatus = subscriberStatus;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getConsentAt() {
        return consentAt;
    }

    public void setConsentAt(LocalDateTime consentAt) {
        this.consentAt = consentAt;
    }
}
