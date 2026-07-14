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
        name = "blog_notifications",
        indexes = {
            @Index(name = "idx_blog_notification_blog", columnList = "blog_id"),
            @Index(name = "idx_blog_notification_status", columnList = "notification_status,scheduled_at"),
            @Index(name = "idx_blog_notification_channel", columnList = "channel")
        }
)
public class BlogNotification extends BaseBlogEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 40)
    private BlogNotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status", nullable = false, length = 30)
    private BlogNotificationStatus notificationStatus = BlogNotificationStatus.PENDING;

    @Column(name = "recipient", length = 180)
    private String recipient;

    @Column(name = "subject", length = 220)
    private String subject;

    @Lob
    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public BlogNotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(BlogNotificationChannel channel) {
        this.channel = channel;
    }

    public BlogNotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(BlogNotificationStatus notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
