package com.ecommerce.app.module.communication.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "communication_message_providers",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_comm_provider_name_channel", columnNames = {"provider_name", "channel"})
        },
        indexes = {
            @Index(name = "idx_comm_provider_channel_status_priority", columnList = "channel,status,priority")
        }
)
public class MessageProvider extends BaseCommunicationEntity {

    @NotBlank(message = "Provider name is required.")
    @Size(max = 120)
    @Column(name = "provider_name", nullable = false, length = 120)
    private String providerName;

    @NotNull(message = "Channel is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 30)
    private MessageChannel channel;

    @NotNull(message = "Provider type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 40)
    private ProviderType providerType;

    @Size(max = 500)
    @Column(name = "api_key", length = 500)
    private String apiKey;

    @Size(max = 500)
    @Column(name = "api_secret", length = 500)
    private String apiSecret;

    @Size(max = 100)
    @Column(name = "sender_id", length = 100)
    private String senderId;

    @Size(max = 500)
    @Column(name = "base_url", length = 500)
    private String baseUrl;

    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;

    @NotNull(message = "Status is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MessageStatus status = MessageStatus.ACTIVE;

    @Min(value = 0, message = "Priority cannot be negative.")
    @Column(name = "priority", nullable = false)
    private int priority = 100;

    public MessageProvider(String providerName, MessageChannel channel, ProviderType providerType, String apiKey, String apiSecret, String senderId, String baseUrl, String configJson) {
        this.providerName = providerName;
        this.channel = channel;
        this.providerType = providerType;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.senderId = senderId;
        this.baseUrl = baseUrl;
        this.configJson = configJson;
    }

    public MessageProvider() {
    }

    public String getConfigJson() {
        return configJson;
    }

    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }

    public String getMaskedApiKey() {
        return maskSecret(apiKey);
    }

    public String getMaskedApiSecret() {
        return maskSecret(apiSecret);
    }

    private String maskSecret(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 4) {
            return "****";
        }
        return "****" + trimmed.substring(trimmed.length() - 4);
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public void setChannel(MessageChannel channel) {
        this.channel = channel;
    }

    public ProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(ProviderType providerType) {
        this.providerType = providerType;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
