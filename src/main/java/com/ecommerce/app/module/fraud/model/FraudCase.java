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
@Table(name = "fraud_cases", indexes = {
    @Index(name = "idx_fraud_case_number", columnList = "case_number"),
    @Index(name = "idx_fraud_case_order", columnList = "order_id"),
    @Index(name = "idx_fraud_case_customer", columnList = "customer_id"),
    @Index(name = "idx_fraud_case_vendor", columnList = "vendor_id"),
    @Index(name = "idx_fraud_case_status", columnList = "case_status"),
    @Index(name = "idx_fraud_case_assigned", columnList = "assigned_investigator")
})
public class FraudCase extends BaseFraudEntity {

    @Column(name = "case_number", nullable = false, unique = true, length = 60)
    private String caseNumber;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "vendor_id")
    private Long vendorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id")
    private FraudAssessment assessment;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_status", nullable = false, length = 40)
    private FraudCaseStatus caseStatus = FraudCaseStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 30)
    private FraudCasePriority priority = FraudCasePriority.MEDIUM;

    @Column(name = "assigned_investigator", length = 120)
    private String assignedInvestigator;

    @Column(name = "case_reason", length = 500)
    private String caseReason;

    @Lob
    @Column(name = "investigation_notes", columnDefinition = "TEXT")
    private String investigationNotes;

    @Lob
    @Column(name = "evidence", columnDefinition = "TEXT")
    private String evidence;

    @Column(name = "resolution", length = 120)
    private String resolution;

    @Column(name = "resolution_reason", length = 1000)
    private String resolutionReason;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public FraudAssessment getAssessment() { return assessment; }
    public void setAssessment(FraudAssessment assessment) { this.assessment = assessment; }
    public FraudCaseStatus getCaseStatus() { return caseStatus; }
    public void setCaseStatus(FraudCaseStatus caseStatus) { this.caseStatus = caseStatus; }
    public FraudCasePriority getPriority() { return priority; }
    public void setPriority(FraudCasePriority priority) { this.priority = priority; }
    public String getAssignedInvestigator() { return assignedInvestigator; }
    public void setAssignedInvestigator(String assignedInvestigator) { this.assignedInvestigator = assignedInvestigator; }
    public String getCaseReason() { return caseReason; }
    public void setCaseReason(String caseReason) { this.caseReason = caseReason; }
    public String getInvestigationNotes() { return investigationNotes; }
    public void setInvestigationNotes(String investigationNotes) { this.investigationNotes = investigationNotes; }
    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public String getResolutionReason() { return resolutionReason; }
    public void setResolutionReason(String resolutionReason) { this.resolutionReason = resolutionReason; }
    public LocalDateTime getOpenedAt() { return openedAt; }
    public void setOpenedAt(LocalDateTime openedAt) { this.openedAt = openedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
