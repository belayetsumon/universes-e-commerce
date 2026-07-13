package com.ecommerce.app.module.communication.dto;

import com.ecommerce.app.module.communication.model.ManualAudience;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ManualMessageRequest {

    @NotNull(message = "Audience is required.")
    private ManualAudience audience;

    private Long recipientId;

    private List<Long> recipientIds = new ArrayList<>();

    @NotEmpty(message = "Select at least one delivery channel.")
    private Set<MessageChannel> channels = new LinkedHashSet<>();

    private MessageType messageType = MessageType.CUSTOM;

    @Size(max = 250, message = "Subject must be 250 characters or fewer.")
    private String subject;

    @NotBlank(message = "Message body is required.")
    @Size(max = 5000, message = "Message body must be 5000 characters or fewer.")
    private String body;

    @Size(max = 10)
    private String language;

    @Size(max = 64, message = "Invalid message submission token.")
    @Pattern(regexp = "[A-Za-z0-9-]{16,64}", message = "Invalid message submission token.")
    private String batchId;

    public ManualAudience getAudience() {
        return audience;
    }

    public void setAudience(ManualAudience audience) {
        this.audience = audience;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public List<Long> getRecipientIds() {
        return recipientIds;
    }

    public void setRecipientIds(List<Long> recipientIds) {
        this.recipientIds = recipientIds == null ? new ArrayList<>() : recipientIds;
    }

    public Set<MessageChannel> getChannels() {
        return channels;
    }

    public void setChannels(Set<MessageChannel> channels) {
        this.channels = channels == null ? new LinkedHashSet<>() : channels;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType == null ? MessageType.CUSTOM : messageType;
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }
}
