package com.ecommerce.app.module.communication.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "communication_message_templates",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_comm_template_event_channel_language", columnNames = {"event_type", "channel", "language"})
        },
        indexes = {
            @Index(name = "idx_comm_template_event_channel_status", columnList = "event_type,channel,status"),
            @Index(name = "idx_comm_template_language", columnList = "language")
        }
)
public class MessageTemplate extends BaseCommunicationEntity {

    @NotNull(message = "Event type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 80)
    private MessageEventType eventType;

    @NotNull(message = "Channel is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 30)
    private MessageChannel channel;

    @Size(max = 250, message = "Subject cannot exceed 250 characters.")
    @Column(name = "subject", length = 250)
    private String subject;

    @NotBlank(message = "Message body is required.")
    @Lob
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @NotBlank(message = "Language is required.")
    @Size(max = 10, message = "Language cannot exceed 10 characters.")
    @Column(name = "language", nullable = false, length = 10)
    private String language = "en";

    @NotNull(message = "Status is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MessageStatus status = MessageStatus.ACTIVE;

    @Lob
    @Column(name = "variables", columnDefinition = "TEXT")
    private String variables;

    public MessageEventType getEventType() {
        return eventType;
    }

    public void setEventType(MessageEventType eventType) {
        this.eventType = eventType;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public void setChannel(MessageChannel channel) {
        this.channel = channel;
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

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }
}
