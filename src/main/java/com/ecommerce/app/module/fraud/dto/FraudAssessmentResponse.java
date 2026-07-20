package com.ecommerce.app.module.fraud.dto;

import com.ecommerce.app.module.fraud.model.FraudAssessmentStatus;
import com.ecommerce.app.module.fraud.model.FraudDecision;
import com.ecommerce.app.module.fraud.model.FraudRiskLevel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FraudAssessmentResponse {

    private Long id;
    private String uuid;
    private Long orderId;
    private String orderUuid;
    private Long customerId;
    private Long vendorId;
    private Integer riskScore;
    private FraudRiskLevel riskLevel;
    private FraudDecision decision;
    private FraudAssessmentStatus status;
    private String decisionReason;
    private boolean automaticDecision;
    private boolean manualReviewRequired;
    private LocalDateTime evaluatedAt;
    private List<FraudSignalResult> signals = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderUuid() { return orderUuid; }
    public void setOrderUuid(String orderUuid) { this.orderUuid = orderUuid; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    public FraudRiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(FraudRiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public FraudDecision getDecision() { return decision; }
    public void setDecision(FraudDecision decision) { this.decision = decision; }
    public FraudAssessmentStatus getStatus() { return status; }
    public void setStatus(FraudAssessmentStatus status) { this.status = status; }
    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }
    public boolean isAutomaticDecision() { return automaticDecision; }
    public void setAutomaticDecision(boolean automaticDecision) { this.automaticDecision = automaticDecision; }
    public boolean isManualReviewRequired() { return manualReviewRequired; }
    public void setManualReviewRequired(boolean manualReviewRequired) { this.manualReviewRequired = manualReviewRequired; }
    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
    public List<FraudSignalResult> getSignals() { return signals; }
    public void setSignals(List<FraudSignalResult> signals) { this.signals = signals == null ? new ArrayList<>() : signals; }
}
