package com.ecommerce.app.module.order.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Entity
@Table(name = "checkout_placement_attempt", uniqueConstraints = {
    @UniqueConstraint(
            name = "uk_checkout_attempt_actor_request",
            columnNames = {"actor_scope_hash", "request_key_hash"}
    )
}, indexes = {
    @Index(name = "idx_checkout_attempt_status_lock", columnList = "status,locked_until"),
    @Index(name = "idx_checkout_attempt_order_group", columnList = "order_group_uuid"),
    @Index(name = "idx_checkout_attempt_status_updated", columnList = "status,updated_at")
})
public class CheckoutPlacementAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "actor_scope_hash", nullable = false, length = 64)
    private String actorScopeHash;

    @Column(name = "request_key_hash", nullable = false, length = 64)
    private String requestKeyHash;

    @Column(name = "payload_hash", nullable = false, length = 64)
    private String payloadHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CheckoutPlacementAttemptStatus status = CheckoutPlacementAttemptStatus.PROCESSING;

    @Column(name = "order_group_uuid", length = 36)
    private String orderGroupUuid;

    @Column(name = "redirect_path", length = 500)
    private String redirectPath;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void beforeInsert() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public String getActorScopeHash() { return actorScopeHash; }
    public void setActorScopeHash(String actorScopeHash) { this.actorScopeHash = actorScopeHash; }
    public String getRequestKeyHash() { return requestKeyHash; }
    public void setRequestKeyHash(String requestKeyHash) { this.requestKeyHash = requestKeyHash; }
    public String getPayloadHash() { return payloadHash; }
    public void setPayloadHash(String payloadHash) { this.payloadHash = payloadHash; }
    public CheckoutPlacementAttemptStatus getStatus() { return status; }
    public void setStatus(CheckoutPlacementAttemptStatus status) { this.status = status; }
    public String getOrderGroupUuid() { return orderGroupUuid; }
    public void setOrderGroupUuid(String orderGroupUuid) { this.orderGroupUuid = orderGroupUuid; }
    public String getRedirectPath() { return redirectPath; }
    public void setRedirectPath(String redirectPath) { this.redirectPath = redirectPath; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
