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
@Table(name = "fraud_payment_risk_results", indexes = {
    @Index(name = "idx_fraud_payment_order", columnList = "order_id"),
    @Index(name = "idx_fraud_payment_provider", columnList = "provider_name"),
    @Index(name = "idx_fraud_payment_status", columnList = "provider_status"),
    @Index(name = "idx_fraud_payment_token", columnList = "payment_token_hash")
})
public class PaymentRiskResult extends BaseFraudEntity {

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "provider_name", length = 80)
    private String providerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_status", nullable = false, length = 40)
    private PaymentRiskProviderStatus providerStatus = PaymentRiskProviderStatus.NOT_REQUESTED;

    @Column(name = "provider_risk_score")
    private Integer providerRiskScore;

    @Column(name = "cvv_result", length = 40)
    private String cvvResult;

    @Column(name = "avs_result", length = 40)
    private String avsResult;

    @Column(name = "three_d_secure_result", length = 60)
    private String threeDSecureResult;

    @Column(name = "authorization_status", length = 60)
    private String authorizationStatus;

    @Column(name = "payment_country", length = 80)
    private String paymentCountry;

    @Column(name = "payment_token_hash", length = 128)
    private String paymentTokenHash;

    @Column(name = "provider_reference", length = 160)
    private String providerReference;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Lob
    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public PaymentRiskProviderStatus getProviderStatus() { return providerStatus; }
    public void setProviderStatus(PaymentRiskProviderStatus providerStatus) { this.providerStatus = providerStatus; }
    public Integer getProviderRiskScore() { return providerRiskScore; }
    public void setProviderRiskScore(Integer providerRiskScore) { this.providerRiskScore = providerRiskScore; }
    public String getCvvResult() { return cvvResult; }
    public void setCvvResult(String cvvResult) { this.cvvResult = cvvResult; }
    public String getAvsResult() { return avsResult; }
    public void setAvsResult(String avsResult) { this.avsResult = avsResult; }
    public String getThreeDSecureResult() { return threeDSecureResult; }
    public void setThreeDSecureResult(String threeDSecureResult) { this.threeDSecureResult = threeDSecureResult; }
    public String getAuthorizationStatus() { return authorizationStatus; }
    public void setAuthorizationStatus(String authorizationStatus) { this.authorizationStatus = authorizationStatus; }
    public String getPaymentCountry() { return paymentCountry; }
    public void setPaymentCountry(String paymentCountry) { this.paymentCountry = paymentCountry; }
    public String getPaymentTokenHash() { return paymentTokenHash; }
    public void setPaymentTokenHash(String paymentTokenHash) { this.paymentTokenHash = paymentTokenHash; }
    public String getProviderReference() { return providerReference; }
    public void setProviderReference(String providerReference) { this.providerReference = providerReference; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
}
