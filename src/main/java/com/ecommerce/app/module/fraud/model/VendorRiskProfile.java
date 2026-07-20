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
@Table(name = "fraud_vendor_risk_profiles", indexes = {
    @Index(name = "idx_fraud_vendor_profile_vendor", columnList = "vendor_id"),
    @Index(name = "idx_fraud_vendor_profile_risk", columnList = "risk_level"),
    @Index(name = "idx_fraud_vendor_profile_payout", columnList = "payout_held")
})
public class VendorRiskProfile extends BaseFraudEntity {

    @Column(name = "vendor_id", nullable = false, unique = true)
    private Long vendorId;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 30)
    private FraudRiskLevel riskLevel = FraudRiskLevel.LOW;

    @Column(name = "order_count", nullable = false)
    private long orderCount;

    @Column(name = "refund_count", nullable = false)
    private long refundCount;

    @Column(name = "cancel_count", nullable = false)
    private long cancelCount;

    @Column(name = "fake_delivery_count", nullable = false)
    private long fakeDeliveryCount;

    @Column(name = "tracking_reuse_count", nullable = false)
    private long trackingReuseCount;

    @Column(name = "self_purchase_count", nullable = false)
    private long selfPurchaseCount;

    @Column(name = "collusion_signal_count", nullable = false)
    private long collusionSignalCount;

    @Column(name = "shared_mobile_count", nullable = false)
    private long sharedMobileCount;

    @Column(name = "shared_address_count", nullable = false)
    private long sharedAddressCount;

    @Column(name = "shared_bank_account_count", nullable = false)
    private long sharedBankAccountCount;

    @Column(name = "unverified_delivery_count", nullable = false)
    private long unverifiedDeliveryCount;

    @Column(name = "sudden_sales_spike_count", nullable = false)
    private long suddenSalesSpikeCount;

    @Column(name = "abnormal_refund_rate", precision = 7, scale = 4)
    private BigDecimal abnormalRefundRate = BigDecimal.ZERO;

    @Column(name = "abnormal_cancellation_rate", precision = 7, scale = 4)
    private BigDecimal abnormalCancellationRate = BigDecimal.ZERO;

    @Column(name = "chargeback_exposure", precision = 19, scale = 4)
    private BigDecimal chargebackExposure = BigDecimal.ZERO;

    @Column(name = "last_risk_reason", length = 500)
    private String lastRiskReason;

    @Column(name = "under_review", nullable = false)
    private boolean underReview;

    @Column(name = "payout_held", nullable = false)
    private boolean payoutHeld;

    @Column(name = "last_assessed_at")
    private LocalDateTime lastAssessedAt;

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public FraudRiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(FraudRiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public long getOrderCount() { return orderCount; }
    public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
    public long getRefundCount() { return refundCount; }
    public void setRefundCount(long refundCount) { this.refundCount = refundCount; }
    public long getCancelCount() { return cancelCount; }
    public void setCancelCount(long cancelCount) { this.cancelCount = cancelCount; }
    public long getFakeDeliveryCount() { return fakeDeliveryCount; }
    public void setFakeDeliveryCount(long fakeDeliveryCount) { this.fakeDeliveryCount = fakeDeliveryCount; }
    public long getTrackingReuseCount() { return trackingReuseCount; }
    public void setTrackingReuseCount(long trackingReuseCount) { this.trackingReuseCount = trackingReuseCount; }
    public long getSelfPurchaseCount() { return selfPurchaseCount; }
    public void setSelfPurchaseCount(long selfPurchaseCount) { this.selfPurchaseCount = selfPurchaseCount; }
    public long getCollusionSignalCount() { return collusionSignalCount; }
    public void setCollusionSignalCount(long collusionSignalCount) { this.collusionSignalCount = collusionSignalCount; }
    public long getSharedMobileCount() { return sharedMobileCount; }
    public void setSharedMobileCount(long sharedMobileCount) { this.sharedMobileCount = sharedMobileCount; }
    public long getSharedAddressCount() { return sharedAddressCount; }
    public void setSharedAddressCount(long sharedAddressCount) { this.sharedAddressCount = sharedAddressCount; }
    public long getSharedBankAccountCount() { return sharedBankAccountCount; }
    public void setSharedBankAccountCount(long sharedBankAccountCount) { this.sharedBankAccountCount = sharedBankAccountCount; }
    public long getUnverifiedDeliveryCount() { return unverifiedDeliveryCount; }
    public void setUnverifiedDeliveryCount(long unverifiedDeliveryCount) { this.unverifiedDeliveryCount = unverifiedDeliveryCount; }
    public long getSuddenSalesSpikeCount() { return suddenSalesSpikeCount; }
    public void setSuddenSalesSpikeCount(long suddenSalesSpikeCount) { this.suddenSalesSpikeCount = suddenSalesSpikeCount; }
    public BigDecimal getAbnormalRefundRate() { return abnormalRefundRate; }
    public void setAbnormalRefundRate(BigDecimal abnormalRefundRate) { this.abnormalRefundRate = abnormalRefundRate; }
    public BigDecimal getAbnormalCancellationRate() { return abnormalCancellationRate; }
    public void setAbnormalCancellationRate(BigDecimal abnormalCancellationRate) { this.abnormalCancellationRate = abnormalCancellationRate; }
    public BigDecimal getChargebackExposure() { return chargebackExposure; }
    public void setChargebackExposure(BigDecimal chargebackExposure) { this.chargebackExposure = chargebackExposure; }
    public String getLastRiskReason() { return lastRiskReason; }
    public void setLastRiskReason(String lastRiskReason) { this.lastRiskReason = lastRiskReason; }
    public boolean isUnderReview() { return underReview; }
    public void setUnderReview(boolean underReview) { this.underReview = underReview; }
    public boolean isPayoutHeld() { return payoutHeld; }
    public void setPayoutHeld(boolean payoutHeld) { this.payoutHeld = payoutHeld; }
    public LocalDateTime getLastAssessedAt() { return lastAssessedAt; }
    public void setLastAssessedAt(LocalDateTime lastAssessedAt) { this.lastAssessedAt = lastAssessedAt; }
}
