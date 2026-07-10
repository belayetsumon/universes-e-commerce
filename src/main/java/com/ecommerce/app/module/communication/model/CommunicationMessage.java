package com.ecommerce.app.module.communication.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
        name = "communication_messages",
        indexes = {
            @Index(name = "idx_comm_message_channel_created", columnList = "channel,created_at"),
            @Index(name = "idx_comm_message_status_created", columnList = "status,created_at"),
            @Index(name = "idx_comm_message_type_created", columnList = "message_type,created_at")
        }
)
public class CommunicationMessage extends BaseCommunicationEntity {

    @NotBlank(message = "Subject is required.")
    @Column(name = "subject", nullable = false, length = 250)
    private String subject;

    @NotBlank(message = "Message body is required.")
    @Lob
    @Column(name = "message_body", nullable = false, columnDefinition = "TEXT")
    private String messageBody;

    @NotNull(message = "Channel is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 30)
    private MessageChannel channel = MessageChannel.IN_APP;

    @NotNull(message = "Message type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 30)
    private MessageType messageType = MessageType.CUSTOM;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @NotNull(message = "Status is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MessageStatus status = MessageStatus.SENT;

    @PrePersist
    protected void prePersistCommunicationMessage() {
        if (channel == null) {
            channel = MessageChannel.IN_APP;
        }
        if (messageType == null) {
            messageType = MessageType.CUSTOM;
        }
        if (status == null) {
            status = MessageStatus.SENT;
        }
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public void setChannel(MessageChannel channel) {
        this.channel = channel;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }
}
