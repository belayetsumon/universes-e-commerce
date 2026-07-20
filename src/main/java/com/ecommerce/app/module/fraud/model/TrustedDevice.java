package com.ecommerce.app.module.fraud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_trusted_devices", indexes = {
    @Index(name = "idx_fraud_trusted_customer_device", columnList = "customer_id,device_identifier"),
    @Index(name = "idx_fraud_trusted_active", columnList = "active")
})
public class TrustedDevice extends BaseFraudEntity {

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "device_identifier", nullable = false, length = 160)
    private String deviceIdentifier;

    @Column(name = "trusted_at", nullable = false)
    private LocalDateTime trustedAt = LocalDateTime.now();

    @Column(name = "trusted_by", length = 120)
    private String trustedBy;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getDeviceIdentifier() { return deviceIdentifier; }
    public void setDeviceIdentifier(String deviceIdentifier) { this.deviceIdentifier = deviceIdentifier; }
    public LocalDateTime getTrustedAt() { return trustedAt; }
    public void setTrustedAt(LocalDateTime trustedAt) { this.trustedAt = trustedAt; }
    public String getTrustedBy() { return trustedBy; }
    public void setTrustedBy(String trustedBy) { this.trustedBy = trustedBy; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
