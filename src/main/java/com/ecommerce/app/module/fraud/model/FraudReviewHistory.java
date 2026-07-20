package com.ecommerce.app.module.fraud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_review_history", indexes = {
    @Index(name = "idx_fraud_review_assessment", columnList = "assessment_id"),
    @Index(name = "idx_fraud_review_reviewer", columnList = "reviewed_by"),
    @Index(name = "idx_fraud_review_action", columnList = "action")
})
public class FraudReviewHistory extends BaseFraudEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private FraudAssessment assessment;

    @Column(name = "case_id")
    private Long caseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_decision", length = 50)
    private FraudDecision previousDecision;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_decision", nullable = false, length = 50)
    private FraudDecision newDecision;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 60)
    private FraudAction action;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Lob
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "reviewed_by", nullable = false, length = 120)
    private String reviewedBy;

    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt = LocalDateTime.now();

    public FraudAssessment getAssessment() { return assessment; }
    public void setAssessment(FraudAssessment assessment) { this.assessment = assessment; }
    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }
    public FraudDecision getPreviousDecision() { return previousDecision; }
    public void setPreviousDecision(FraudDecision previousDecision) { this.previousDecision = previousDecision; }
    public FraudDecision getNewDecision() { return newDecision; }
    public void setNewDecision(FraudDecision newDecision) { this.newDecision = newDecision; }
    public FraudAction getAction() { return action; }
    public void setAction(FraudAction action) { this.action = action; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
