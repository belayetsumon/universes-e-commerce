package com.ecommerce.app.module.communication.dto;

import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageProvider;
import com.ecommerce.app.module.communication.model.MessageStatus;
import com.ecommerce.app.module.communication.model.ProviderType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Web-safe provider form that never exposes persistence or audit fields. */
public class MessageProviderForm {

    private Long id;

    @NotBlank(message = "Provider name is required.")
    @Size(max = 120)
    private String providerName;

    @NotNull(message = "Channel is required.")
    private MessageChannel channel;

    @NotNull(message = "Provider type is required.")
    private ProviderType providerType;

    @Size(max = 500)
    private String apiKey;

    @Size(max = 500)
    private String apiSecret;

    @Size(max = 100)
    private String senderId;

    @Size(max = 500)
    private String baseUrl;

    @Size(max = 10000)
    private String configJson;

    @NotNull(message = "Status is required.")
    private MessageStatus status = MessageStatus.ACTIVE;

    @Min(value = 0, message = "Priority cannot be negative.")
    private int priority = 100;

    private boolean apiKeyConfigured;
    private boolean apiSecretConfigured;
    private boolean configJsonConfigured;
    private boolean clearApiKey;
    private boolean clearApiSecret;
    private boolean clearConfigJson;

    public static MessageProviderForm fromProvider(MessageProvider provider) {
        MessageProviderForm form = new MessageProviderForm();
        if (provider == null) {
            return form;
        }
        form.setId(provider.getId());
        form.setProviderName(provider.getProviderName());
        form.setChannel(provider.getChannel());
        form.setProviderType(provider.getProviderType());
        form.setSenderId(provider.getSenderId());
        form.setBaseUrl(provider.getBaseUrl());
        form.setStatus(provider.getStatus());
        form.setPriority(provider.getPriority());
        form.setApiKeyConfigured(hasText(provider.getApiKey()));
        form.setApiSecretConfigured(hasText(provider.getApiSecret()));
        form.setConfigJsonConfigured(hasText(provider.getConfigJson()));
        // Existing credentials and provider configuration may contain secrets;
        // they intentionally remain absent from the browser form.
        form.setApiKey(null);
        form.setApiSecret(null);
        form.setConfigJson(null);
        return form;
    }

    public void applyTo(MessageProvider provider) {
        provider.setProviderName(clean(providerName));
        provider.setChannel(channel);
        provider.setProviderType(providerType);
        provider.setSenderId(clean(senderId));
        provider.setBaseUrl(clean(baseUrl));
        provider.setStatus(status);
        provider.setPriority(priority);
        if (clearApiKey) {
            provider.setApiKey(null);
        } else if (hasText(apiKey)) {
            provider.setApiKey(apiKey.trim());
        }
        if (clearApiSecret) {
            provider.setApiSecret(null);
        } else if (hasText(apiSecret)) {
            provider.setApiSecret(apiSecret.trim());
        }
        if (clearConfigJson) {
            provider.setConfigJson(null);
        } else if (hasText(configJson)) {
            provider.setConfigJson(configJson.trim());
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String clean(String value) {
        return hasText(value) ? value.trim() : null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public MessageChannel getChannel() { return channel; }
    public void setChannel(MessageChannel channel) { this.channel = channel; }
    public ProviderType getProviderType() { return providerType; }
    public void setProviderType(ProviderType providerType) { this.providerType = providerType; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getApiSecret() { return apiSecret; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public boolean isApiKeyConfigured() { return apiKeyConfigured; }
    public void setApiKeyConfigured(boolean apiKeyConfigured) { this.apiKeyConfigured = apiKeyConfigured; }
    public boolean isApiSecretConfigured() { return apiSecretConfigured; }
    public void setApiSecretConfigured(boolean apiSecretConfigured) { this.apiSecretConfigured = apiSecretConfigured; }
    public boolean isConfigJsonConfigured() { return configJsonConfigured; }
    public void setConfigJsonConfigured(boolean configJsonConfigured) { this.configJsonConfigured = configJsonConfigured; }
    public boolean isClearApiKey() { return clearApiKey; }
    public void setClearApiKey(boolean clearApiKey) { this.clearApiKey = clearApiKey; }
    public boolean isClearApiSecret() { return clearApiSecret; }
    public void setClearApiSecret(boolean clearApiSecret) { this.clearApiSecret = clearApiSecret; }
    public boolean isClearConfigJson() { return clearConfigJson; }
    public void setClearConfigJson(boolean clearConfigJson) { this.clearConfigJson = clearConfigJson; }
}
