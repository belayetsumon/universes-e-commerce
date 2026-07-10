package com.ecommerce.app.module.communication.model;

import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "communication_preferences",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_comm_pref_recipient_channel", columnNames = {"recipient", "channel"}),
            @UniqueConstraint(name = "uk_comm_pref_unsubscribe_token", columnNames = {"unsubscribe_token"})
        },
        indexes = {
            @Index(name = "idx_comm_pref_user_channel", columnList = "user_id,channel"),
            @Index(name = "idx_comm_pref_recipient", columnList = "recipient")
        }
)
public class CommunicationPreference extends BaseCommunicationEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @NotBlank(message = "Recipient is required.")
    @Size(max = 255)
    @Column(name = "recipient", nullable = false, length = 255)
    private String recipient;

    @NotNull(message = "Channel is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 30)
    private MessageChannel channel;

    @Column(name = "transactional_enabled", nullable = false)
    private boolean transactionalEnabled = true;

    @Column(name = "marketing_enabled", nullable = false)
    private boolean marketingEnabled = true;

    @Column(name = "unsubscribed_at")
    private LocalDateTime unsubscribedAt;

    @Size(max = 80)
    @Column(name = "unsubscribe_token", nullable = false, length = 80)
    private String unsubscribeToken = UUID.randomUUID().toString();

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public MessageChannel getChannel() { return channel; }
    public void setChannel(MessageChannel channel) { this.channel = channel; }
    public boolean isTransactionalEnabled() { return transactionalEnabled; }
    public void setTransactionalEnabled(boolean transactionalEnabled) { this.transactionalEnabled = transactionalEnabled; }
    public boolean isMarketingEnabled() { return marketingEnabled; }
    public void setMarketingEnabled(boolean marketingEnabled) { this.marketingEnabled = marketingEnabled; }
    public LocalDateTime getUnsubscribedAt() { return unsubscribedAt; }
    public void setUnsubscribedAt(LocalDateTime unsubscribedAt) { this.unsubscribedAt = unsubscribedAt; }
    public String getUnsubscribeToken() { return unsubscribeToken; }
    public void setUnsubscribeToken(String unsubscribeToken) { this.unsubscribeToken = unsubscribeToken; }
}
