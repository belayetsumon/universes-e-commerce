package com.ecommerce.app.module.fraud.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fraud_assessments", indexes = {
    @Index(name = "idx_fraud_assessment_order", columnList = "order_id"),
    @Index(name = "idx_fraud_assessment_customer", columnList = "customer_id"),
    @Index(name = "idx_fraud_assessment_vendor", columnList = "vendor_id"),
    @Index(name = "idx_fraud_assessment_status", columnList = "status"),
    @Index(name = "idx_fraud_assessment_risk", columnList = "risk_level"),
    @Index(name = "idx_fraud_assessment_decision", columnList = "decision"),
    @Index(name = "idx_fraud_assessment_evaluated", columnList = "evaluated_at")
})
public class FraudAssessment extends BaseFraudEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_uuid", length = 80)
    private String orderUuid;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 30)
    private FraudRiskLevel riskLevel = FraudRiskLevel.LOW;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 50)
    private FraudDecision decision = FraudDecision.APPROVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private FraudAssessmentStatus status = FraudAssessmentStatus.REQUESTED;

    @Lob
    @Column(name = "decision_reason", columnDefinition = "TEXT")
    private String decisionReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_source", nullable = false, length = 60)
    private FraudEvaluationSource evaluationSource = FraudEvaluationSource.API;

    @Column(name = "evaluation_version", length = 40)
    private String evaluationVersion;

    @Column(name = "automatic_decision", nullable = false)
    private boolean automaticDecision = true;

    @Column(name = "manual_review_required", nullable = false)
    private boolean manualReviewRequired;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    @Column(name = "reviewed_by", length = 120)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Lob
    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(name = "correlation_id", length = 120)
    private String correlationId;

    @Column(name = "idempotency_key", length = 160)
    private String idempotencyKey;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FraudSignal> signals = new ArrayList<>();

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
    public FraudEvaluationSource getEvaluationSource() { return evaluationSource; }
    public void setEvaluationSource(FraudEvaluationSource evaluationSource) { this.evaluationSource = evaluationSource; }
    public String getEvaluationVersion() { return evaluationVersion; }
    public void setEvaluationVersion(String evaluationVersion) { this.evaluationVersion = evaluationVersion; }
    public boolean isAutomaticDecision() { return automaticDecision; }
    public void setAutomaticDecision(boolean automaticDecision) { this.automaticDecision = automaticDecision; }
    public boolean isManualReviewRequired() { return manualReviewRequired; }
    public void setManualReviewRequired(boolean manualReviewRequired) { this.manualReviewRequired = manualReviewRequired; }
    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public List<FraudSignal> getSignals() { return signals; }
    public void setSignals(List<FraudSignal> signals) { this.signals = signals == null ? new ArrayList<>() : signals; }
}
