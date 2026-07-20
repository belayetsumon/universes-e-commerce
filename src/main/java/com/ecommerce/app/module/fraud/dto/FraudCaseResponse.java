package com.ecommerce.app.module.fraud.dto;

import com.ecommerce.app.module.fraud.model.FraudCasePriority;
import com.ecommerce.app.module.fraud.model.FraudCaseStatus;
import java.time.LocalDateTime;

public class FraudCaseResponse {

    private Long id;
    private String caseNumber;
    private Long orderId;
    private Long customerId;
    private Long vendorId;
    private Long assessmentId;
    private FraudCaseStatus status;
    private FraudCasePriority priority;
    private String assignedInvestigator;
    private String caseReason;
    private String resolution;
    private LocalDateTime openedAt;
    private LocalDateTime resolvedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }
    public FraudCaseStatus getStatus() { return status; }
    public void setStatus(FraudCaseStatus status) { this.status = status; }
    public FraudCasePriority getPriority() { return priority; }
    public void setPriority(FraudCasePriority priority) { this.priority = priority; }
    public String getAssignedInvestigator() { return assignedInvestigator; }
    public void setAssignedInvestigator(String assignedInvestigator) { this.assignedInvestigator = assignedInvestigator; }
    public String getCaseReason() { return caseReason; }
    public void setCaseReason(String caseReason) { this.caseReason = caseReason; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public LocalDateTime getOpenedAt() { return openedAt; }
    public void setOpenedAt(LocalDateTime openedAt) { this.openedAt = openedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
