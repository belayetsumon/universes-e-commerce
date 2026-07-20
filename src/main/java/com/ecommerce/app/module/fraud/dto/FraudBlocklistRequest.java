package com.ecommerce.app.module.fraud.dto;

import com.ecommerce.app.module.fraud.model.FraudBlockScope;
import com.ecommerce.app.module.fraud.model.FraudBlockType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class FraudBlocklistRequest {

    @NotNull(message = "Block type is required.")
    private FraudBlockType blockType;

    @NotBlank(message = "Block value is required.")
    @Size(max = 500, message = "Block value must be 500 characters or less.")
    private String blockValue;

    private FraudBlockScope scope = FraudBlockScope.GLOBAL;

    @NotBlank(message = "Reason is required.")
    @Size(max = 500, message = "Reason must be 500 characters or less.")
    private String reason;

    private boolean temporary;

    @Future(message = "Expiry must be in the future.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime expiresAt;

    public FraudBlockType getBlockType() { return blockType; }
    public void setBlockType(FraudBlockType blockType) { this.blockType = blockType; }
    public String getBlockValue() { return blockValue; }
    public void setBlockValue(String blockValue) { this.blockValue = blockValue; }
    public FraudBlockScope getScope() { return scope; }
    public void setScope(FraudBlockScope scope) { this.scope = scope == null ? FraudBlockScope.GLOBAL : scope; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public boolean isTemporary() { return temporary; }
    public void setTemporary(boolean temporary) { this.temporary = temporary; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
