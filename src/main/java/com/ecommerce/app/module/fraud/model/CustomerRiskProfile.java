package com.ecommerce.app.module.fraud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_customer_risk_profiles", indexes = {
    @Index(name = "idx_fraud_customer_profile_customer", columnList = "customer_id"),
    @Index(name = "idx_fraud_customer_profile_risk", columnList = "risk_level"),
    @Index(name = "idx_fraud_customer_profile_cod", columnList = "cod_disabled")
})
public class CustomerRiskProfile extends BaseFraudEntity {

    @Column(name = "customer_id", nullable = false, unique = true)
    private Long customerId;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 30)
    private FraudRiskLevel riskLevel = FraudRiskLevel.LOW;

    @Column(name = "successful_order_count", nullable = false)
    private long successfulOrderCount;

    @Column(name = "cancelled_order_count", nullable = false)
    private long cancelledOrderCount;

    @Column(name = "returned_order_count", nullable = false)
    private long returnedOrderCount;

    @Column(name = "refunded_order_count", nullable = false)
    private long refundedOrderCount;

    @Column(name = "cod_rto_count", nullable = false)
    private long codRtoCount;

    @Column(name = "delivery_refusal_count", nullable = false)
    private long deliveryRefusalCount;

    @Column(name = "chargeback_count", nullable = false)
    private long chargebackCount;

    @Column(name = "lifetime_value", precision = 19, scale = 4)
    private BigDecimal lifetimeValue = BigDecimal.ZERO;

    @Column(name = "trusted_customer", nullable = false)
    private boolean trustedCustomer;

    @Column(name = "blacklisted", nullable = false)
    private boolean blacklisted;

    @Column(name = "cod_disabled", nullable = false)
    private boolean codDisabled;

    @Column(name = "last_assessed_at")
    private LocalDateTime lastAssessedAt;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public FraudRiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(FraudRiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public long getSuccessfulOrderCount() { return successfulOrderCount; }
    public void setSuccessfulOrderCount(long successfulOrderCount) { this.successfulOrderCount = successfulOrderCount; }
    public long getCancelledOrderCount() { return cancelledOrderCount; }
    public void setCancelledOrderCount(long cancelledOrderCount) { this.cancelledOrderCount = cancelledOrderCount; }
    public long getReturnedOrderCount() { return returnedOrderCount; }
    public void setReturnedOrderCount(long returnedOrderCount) { this.returnedOrderCount = returnedOrderCount; }
    public long getRefundedOrderCount() { return refundedOrderCount; }
    public void setRefundedOrderCount(long refundedOrderCount) { this.refundedOrderCount = refundedOrderCount; }
    public long getCodRtoCount() { return codRtoCount; }
    public void setCodRtoCount(long codRtoCount) { this.codRtoCount = codRtoCount; }
    public long getDeliveryRefusalCount() { return deliveryRefusalCount; }
    public void setDeliveryRefusalCount(long deliveryRefusalCount) { this.deliveryRefusalCount = deliveryRefusalCount; }
    public long getChargebackCount() { return chargebackCount; }
    public void setChargebackCount(long chargebackCount) { this.chargebackCount = chargebackCount; }
    public BigDecimal getLifetimeValue() { return lifetimeValue; }
    public void setLifetimeValue(BigDecimal lifetimeValue) { this.lifetimeValue = lifetimeValue; }
    public boolean isTrustedCustomer() { return trustedCustomer; }
    public void setTrustedCustomer(boolean trustedCustomer) { this.trustedCustomer = trustedCustomer; }
    public boolean isBlacklisted() { return blacklisted; }
    public void setBlacklisted(boolean blacklisted) { this.blacklisted = blacklisted; }
    public boolean isCodDisabled() { return codDisabled; }
    public void setCodDisabled(boolean codDisabled) { this.codDisabled = codDisabled; }
    public LocalDateTime getLastAssessedAt() { return lastAssessedAt; }
    public void setLastAssessedAt(LocalDateTime lastAssessedAt) { this.lastAssessedAt = lastAssessedAt; }
}
