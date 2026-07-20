package com.ecommerce.app.module.fraud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_blocklist", indexes = {
    @Index(name = "idx_fraud_block_type_value", columnList = "block_type,hashed_value"),
    @Index(name = "idx_fraud_block_scope", columnList = "scope"),
    @Index(name = "idx_fraud_block_active", columnList = "active"),
    @Index(name = "idx_fraud_block_expiry", columnList = "expires_at")
})
public class FraudBlocklist extends BaseFraudEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "block_type", nullable = false, length = 50)
    private FraudBlockType blockType;

    @Column(name = "hashed_value", nullable = false, length = 128)
    private String hashedValue;

    @Column(name = "masked_value", length = 160)
    private String maskedValue;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 40)
    private FraudBlockScope scope = FraudBlockScope.GLOBAL;

    @Column(name = "temporary", nullable = false)
    private boolean temporary;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_by_user", length = 120)
    private String createdByUser;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public FraudBlockType getBlockType() { return blockType; }
    public void setBlockType(FraudBlockType blockType) { this.blockType = blockType; }
    public String getHashedValue() { return hashedValue; }
    public void setHashedValue(String hashedValue) { this.hashedValue = hashedValue; }
    public String getMaskedValue() { return maskedValue; }
    public void setMaskedValue(String maskedValue) { this.maskedValue = maskedValue; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public FraudBlockScope getScope() { return scope; }
    public void setScope(FraudBlockScope scope) { this.scope = scope; }
    public boolean isTemporary() { return temporary; }
    public void setTemporary(boolean temporary) { this.temporary = temporary; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public String getCreatedByUser() { return createdByUser; }
    public void setCreatedByUser(String createdByUser) { this.createdByUser = createdByUser; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
