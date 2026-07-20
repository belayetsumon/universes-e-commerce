package com.ecommerce.app.module.fraud.dto;

import com.ecommerce.app.module.fraud.model.FraudBlockScope;
import com.ecommerce.app.module.fraud.model.FraudBlockType;
import java.time.LocalDateTime;

public class FraudBlocklistResponse {

    private Long id;
    private String uuid;
    private FraudBlockType blockType;
    private FraudBlockScope scope;
    private String maskedValue;
    private String reason;
    private boolean temporary;
    private boolean active;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public FraudBlockType getBlockType() { return blockType; }
    public void setBlockType(FraudBlockType blockType) { this.blockType = blockType; }
    public FraudBlockScope getScope() { return scope; }
    public void setScope(FraudBlockScope scope) { this.scope = scope; }
    public String getMaskedValue() { return maskedValue; }
    public void setMaskedValue(String maskedValue) { this.maskedValue = maskedValue; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public boolean isTemporary() { return temporary; }
    public void setTemporary(boolean temporary) { this.temporary = temporary; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
