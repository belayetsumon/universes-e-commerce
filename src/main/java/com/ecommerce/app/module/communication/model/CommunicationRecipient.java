package com.ecommerce.app.module.communication.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "communication_recipients",
        indexes = {
            @Index(name = "idx_comm_recipient_user_read", columnList = "receiver_user_id,read_status"),
            @Index(name = "idx_comm_recipient_message_read", columnList = "message_id,read_status"),
            @Index(name = "idx_comm_recipient_message_type_read", columnList = "message_id,receiver_type,read_status"),
            @Index(name = "idx_comm_recipient_vendor", columnList = "vendor_id")
        }
)
public class CommunicationRecipient extends BaseCommunicationEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private CommunicationMessage message;

    @Column(name = "message_id", insertable = false, updatable = false)
    private Long messageId;

    @NotNull(message = "Receiver type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "receiver_type", nullable = false, length = 30)
    private ReceiverType receiverType = ReceiverType.USER;

    @NotNull(message = "Receiver user id is required.")
    @Column(name = "receiver_user_id", nullable = false)
    private Long receiverUserId;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "receiver_name", length = 180)
    private String receiverName;

    @Column(name = "receiver_email", length = 180)
    private String receiverEmail;

    @Column(name = "receiver_mobile", length = 50)
    private String receiverMobile;

    @Column(name = "delivered", nullable = false)
    private boolean delivered = true;

    @Column(name = "read_status", nullable = false)
    private boolean readStatus = false;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    @NotNull(message = "Status is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MessageStatus status = MessageStatus.SENT;

    @PrePersist
    protected void prePersistCommunicationRecipient() {
        if (delivered && deliveredAt == null) {
            deliveredAt = LocalDateTime.now();
        }
        if (status == null) {
            status = MessageStatus.SENT;
        }
        if (receiverType == null) {
            receiverType = ReceiverType.USER;
        }
    }

    public CommunicationMessage getMessage() {
        return message;
    }

    public void setMessage(CommunicationMessage message) {
        this.message = message;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public ReceiverType getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(ReceiverType receiverType) {
        this.receiverType = receiverType;
    }

    public Long getReceiverUserId() {
        return receiverUserId;
    }

    public void setReceiverUserId(Long receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getReceiverMobile() {
        return receiverMobile;
    }

    public void setReceiverMobile(String receiverMobile) {
        this.receiverMobile = receiverMobile;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public boolean isReadStatus() {
        return readStatus;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public LocalDateTime getClickedAt() {
        return clickedAt;
    }

    public void setClickedAt(LocalDateTime clickedAt) {
        this.clickedAt = clickedAt;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }
}
