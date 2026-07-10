package com.ecommerce.app.module.communication.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
        name = "communication_message_routing_rules",
        indexes = {
            @Index(name = "idx_comm_route_event_channel_active", columnList = "event_type,channel,active"),
            @Index(name = "idx_comm_route_volume", columnList = "min_volume,max_volume")
        }
)
public class MessageRoutingRule extends BaseCommunicationEntity {

    @NotNull(message = "Event type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 80)
    private MessageEventType eventType;

    @NotNull(message = "Channel is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 30)
    private MessageChannel channel;

    @NotNull(message = "Delivery mode is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_mode", nullable = false, length = 40)
    private DeliveryMode deliveryMode = DeliveryMode.DIRECT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private MessageProvider provider;

    @Min(value = 0, message = "Minimum volume cannot be negative.")
    @Column(name = "min_volume", nullable = false)
    private int minVolume = 0;

    @Min(value = 0, message = "Maximum volume cannot be negative.")
    @Column(name = "max_volume")
    private Integer maxVolume;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public boolean matchesVolume(int volume) {
        return volume >= minVolume && (maxVolume == null || volume <= maxVolume);
    }

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

    public DeliveryMode getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(DeliveryMode deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    public MessageProvider getProvider() {
        return provider;
    }

    public void setProvider(MessageProvider provider) {
        this.provider = provider;
    }

    public int getMinVolume() {
        return minVolume;
    }

    public void setMinVolume(int minVolume) {
        this.minVolume = minVolume;
    }

    public Integer getMaxVolume() {
        return maxVolume;
    }

    public void setMaxVolume(Integer maxVolume) {
        this.maxVolume = maxVolume;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
