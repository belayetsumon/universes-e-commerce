package com.ecommerce.app.module.fraud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_idempotency_records", indexes = {
    @Index(name = "idx_fraud_idem_key", columnList = "idempotency_key"),
    @Index(name = "idx_fraud_idem_scope", columnList = "operation_scope"),
    @Index(name = "idx_fraud_idem_status", columnList = "status"),
    @Index(name = "idx_fraud_idem_expiry", columnList = "expires_at")
})
public class FraudIdempotencyRecord extends BaseFraudEntity {

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 160)
    private String idempotencyKey;

    @Column(name = "operation_scope", nullable = false, length = 100)
    private String operationScope;

    @Column(name = "request_hash", length = 128)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private FraudIdempotencyStatus status = FraudIdempotencyStatus.STARTED;

    @Lob
    @Column(name = "response_json", columnDefinition = "TEXT")
    private String responseJson;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getOperationScope() { return operationScope; }
    public void setOperationScope(String operationScope) { this.operationScope = operationScope; }
    public String getRequestHash() { return requestHash; }
    public void setRequestHash(String requestHash) { this.requestHash = requestHash; }
    public FraudIdempotencyStatus getStatus() { return status; }
    public void setStatus(FraudIdempotencyStatus status) { this.status = status; }
    public String getResponseJson() { return responseJson; }
    public void setResponseJson(String responseJson) { this.responseJson = responseJson; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
