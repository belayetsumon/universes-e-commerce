package com.ecommerce.app.module.fraud.dto;

import com.ecommerce.app.module.fraud.model.FraudAssessmentStatus;
import com.ecommerce.app.module.fraud.model.FraudBlockType;
import com.ecommerce.app.module.fraud.model.FraudCaseStatus;
import com.ecommerce.app.module.fraud.model.FraudDecision;
import com.ecommerce.app.module.fraud.model.FraudRiskLevel;
import com.ecommerce.app.module.fraud.model.FraudRuleType;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class FraudAdminSearchCriteria {

    private String q;
    private Long orderId;
    private Long customerId;
    private Long vendorId;
    private FraudRiskLevel riskLevel;
    private FraudDecision decision;
    private FraudAssessmentStatus assessmentStatus;
    private FraudCaseStatus caseStatus;
    private FraudRuleType ruleType;
    private FraudBlockType blockType;
    private Boolean active;
    private String reviewer;
    private String district;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public FraudRiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(FraudRiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public FraudDecision getDecision() { return decision; }
    public void setDecision(FraudDecision decision) { this.decision = decision; }
    public FraudAssessmentStatus getAssessmentStatus() { return assessmentStatus; }
    public void setAssessmentStatus(FraudAssessmentStatus assessmentStatus) { this.assessmentStatus = assessmentStatus; }
    public FraudCaseStatus getCaseStatus() { return caseStatus; }
    public void setCaseStatus(FraudCaseStatus caseStatus) { this.caseStatus = caseStatus; }
    public FraudRuleType getRuleType() { return ruleType; }
    public void setRuleType(FraudRuleType ruleType) { this.ruleType = ruleType; }
    public FraudBlockType getBlockType() { return blockType; }
    public void setBlockType(FraudBlockType blockType) { this.blockType = blockType; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
}
