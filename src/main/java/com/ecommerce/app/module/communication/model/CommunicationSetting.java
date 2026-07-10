package com.ecommerce.app.module.communication.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "communication_settings")
@Check(constraints = "id = 1")
public class CommunicationSetting {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id = 1;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false, length = 36)
    private String uuid = UUID.randomUUID().toString();

    @NotBlank(message = "Default language is required.")
    @Size(max = 10)
    @Column(name = "default_language", nullable = false, length = 10)
    private String defaultLanguage = "en";

    @Min(value = 1, message = "Direct volume threshold must be at least 1.")
    @Column(name = "direct_volume_threshold", nullable = false)
    private int directVolumeThreshold = 100;

    @Min(value = 1, message = "Queue volume threshold must be at least 1.")
    @Column(name = "queue_volume_threshold", nullable = false)
    private int queueVolumeThreshold = 1000;

    @Min(value = 0, message = "Max retry count cannot be negative.")
    @Column(name = "max_retry_count", nullable = false)
    private int maxRetryCount = 3;

    @Min(value = 1, message = "Retry delay must be at least 1 minute.")
    @Column(name = "retry_delay_minutes", nullable = false)
    private int retryDelayMinutes = 5;

    @Column(name = "scheduler_enabled", nullable = false)
    private boolean schedulerEnabled = true;

    @Min(value = 1, message = "Recipient hourly limit must be at least 1.")
    @Column(name = "recipient_hourly_limit", nullable = false)
    private int recipientHourlyLimit = 20;

    @Min(value = 1, message = "Provider per-minute limit must be at least 1.")
    @Column(name = "provider_per_minute_limit", nullable = false)
    private int providerPerMinuteLimit = 60;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        id = 1;
        if (uuid == null || uuid.isBlank()) {
            uuid = UUID.randomUUID().toString();
        }
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    private void preUpdate() {
        id = 1;
        updatedAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = 1;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public int getDirectVolumeThreshold() {
        return directVolumeThreshold;
    }

    public void setDirectVolumeThreshold(int directVolumeThreshold) {
        this.directVolumeThreshold = directVolumeThreshold;
    }

    public int getQueueVolumeThreshold() {
        return queueVolumeThreshold;
    }

    public void setQueueVolumeThreshold(int queueVolumeThreshold) {
        this.queueVolumeThreshold = queueVolumeThreshold;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public int getRetryDelayMinutes() {
        return retryDelayMinutes;
    }

    public void setRetryDelayMinutes(int retryDelayMinutes) {
        this.retryDelayMinutes = retryDelayMinutes;
    }

    public boolean isSchedulerEnabled() {
        return schedulerEnabled;
    }

    public void setSchedulerEnabled(boolean schedulerEnabled) {
        this.schedulerEnabled = schedulerEnabled;
    }

    public int getRecipientHourlyLimit() {
        return recipientHourlyLimit;
    }

    public void setRecipientHourlyLimit(int recipientHourlyLimit) {
        this.recipientHourlyLimit = recipientHourlyLimit;
    }

    public int getProviderPerMinuteLimit() {
        return providerPerMinuteLimit;
    }

    public void setProviderPerMinuteLimit(int providerPerMinuteLimit) {
        this.providerPerMinuteLimit = providerPerMinuteLimit;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

