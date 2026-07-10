package com.ecommerce.app.module.communication.model;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "communication_message_logs",
        indexes = {
            @Index(name = "idx_comm_log_event_channel_status", columnList = "event_type,channel,status"),
            @Index(name = "idx_comm_log_sent_at", columnList = "sent_at"),
            @Index(name = "idx_comm_log_recipient", columnList = "recipient"),
            @Index(name = "idx_comm_log_type_sent", columnList = "message_type,sent_at"),
            @Index(name = "idx_comm_log_idempotency", columnList = "idempotency_key")
        }
)
public class MessageLog extends BaseCommunicationEntity {

    @NotNull(message = "Event type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 80)
    private MessageEventType eventType;

    @NotNull(message = "Channel is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 30)
    private MessageChannel channel;

    @NotNull(message = "Message type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 30)
    private MessageType messageType = MessageType.TRANSACTIONAL;

    @NotBlank(message = "Recipient is required.")
    @Size(max = 255)
    @Column(name = "recipient", nullable = false, length = 255)
    private String recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private MessageProvider provider;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "template_version")
    private Long templateVersion;

    @Size(max = 160)
    @Column(name = "idempotency_key", length = 160)
    private String idempotencyKey;

    @NotNull(message = "Status is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MessageStatus status;

    @Size(max = 80)
    @Column(name = "response_code", length = 80)
    private String responseCode;

    @Lob
    @Column(name = "response_message", columnDefinition = "TEXT")
    private String responseMessage;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    public MessageEventType getEventType() { return eventType; }
    public void setEventType(MessageEventType eventType) { this.eventType = eventType; }
    public MessageChannel getChannel() { return channel; }
    public void setChannel(MessageChannel channel) { this.channel = channel; }
    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType == null ? MessageType.TRANSACTIONAL : messageType; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public MessageProvider getProvider() { return provider; }
    public void setProvider(MessageProvider provider) { this.provider = provider; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public Long getTemplateVersion() { return templateVersion; }
    public void setTemplateVersion(Long templateVersion) { this.templateVersion = templateVersion; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }
    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }
    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
