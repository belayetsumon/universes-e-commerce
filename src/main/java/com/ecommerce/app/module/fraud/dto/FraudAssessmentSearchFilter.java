package com.ecommerce.app.module.fraud.dto;

import com.ecommerce.app.module.fraud.model.FraudAssessmentStatus;
import com.ecommerce.app.module.fraud.model.FraudDecision;
import com.ecommerce.app.module.fraud.model.FraudReasonCode;
import com.ecommerce.app.module.fraud.model.FraudRiskLevel;
import java.time.LocalDate;

public class FraudAssessmentSearchFilter {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Long orderId;
    private Long customerId;
    private Long vendorId;
    private Long productId;
    private Long categoryId;
    private String paymentMethod;
    private FraudRiskLevel riskLevel;
    private FraudDecision decision;
    private FraudAssessmentStatus status;
    private String district;
    private FraudReasonCode reasonCode;
    private String reviewer;

    public LocalDate getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; }
    public LocalDate getDateTo() { return dateTo; }
    public void setDateTo(LocalDate dateTo) { this.dateTo = dateTo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public FraudRiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(FraudRiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public FraudDecision getDecision() { return decision; }
    public void setDecision(FraudDecision decision) { this.decision = decision; }
    public FraudAssessmentStatus getStatus() { return status; }
    public void setStatus(FraudAssessmentStatus status) { this.status = status; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public FraudReasonCode getReasonCode() { return reasonCode; }
    public void setReasonCode(FraudReasonCode reasonCode) { this.reasonCode = reasonCode; }
    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }
}
