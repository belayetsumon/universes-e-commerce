package com.ecommerce.app.module.fraud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_velocity_counters", indexes = {
    @Index(name = "idx_fraud_velocity_scope_value", columnList = "counter_scope,counter_value_hash"),
    @Index(name = "idx_fraud_velocity_window", columnList = "window_start_at,window_end_at")
})
public class VelocityCounter extends BaseFraudEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "counter_scope", nullable = false, length = 40)
    private VelocityCounterScope counterScope;

    @Column(name = "counter_value_hash", nullable = false, length = 128)
    private String counterValueHash;

    @Column(name = "masked_value", length = 160)
    private String maskedValue;

    @Column(name = "counter_count", nullable = false)
    private long counterCount;

    @Column(name = "window_start_at", nullable = false)
    private LocalDateTime windowStartAt;

    @Column(name = "window_end_at", nullable = false)
    private LocalDateTime windowEndAt;

    public VelocityCounterScope getCounterScope() { return counterScope; }
    public void setCounterScope(VelocityCounterScope counterScope) { this.counterScope = counterScope; }
    public String getCounterValueHash() { return counterValueHash; }
    public void setCounterValueHash(String counterValueHash) { this.counterValueHash = counterValueHash; }
    public String getMaskedValue() { return maskedValue; }
    public void setMaskedValue(String maskedValue) { this.maskedValue = maskedValue; }
    public long getCounterCount() { return counterCount; }
    public void setCounterCount(long counterCount) { this.counterCount = counterCount; }
    public LocalDateTime getWindowStartAt() { return windowStartAt; }
    public void setWindowStartAt(LocalDateTime windowStartAt) { this.windowStartAt = windowStartAt; }
    public LocalDateTime getWindowEndAt() { return windowEndAt; }
    public void setWindowEndAt(LocalDateTime windowEndAt) { this.windowEndAt = windowEndAt; }
}
