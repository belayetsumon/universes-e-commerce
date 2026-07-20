package com.ecommerce.app.module.fraud.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FraudDashboardResponse {

    private long totalAssessedOrders;
    private long lowRiskOrders;
    private long mediumRiskOrders;
    private long highRiskOrders;
    private long criticalRiskOrders;
    private long manualReviewOrders;
    private long blockedOrders;
    private long rejectedOrders;
    private BigDecimal fraudLossPrevented = BigDecimal.ZERO;
    private BigDecimal financialExposure = BigDecimal.ZERO;
    private double chargebackRate;
    private double codReturnToOriginRate;
    private double deliveryRefusalRate;
    private double falsePositiveRate;
    private List<FraudMetricRow> topSuspiciousDevices = new ArrayList<>();
    private List<FraudMetricRow> topSuspiciousIpAddresses = new ArrayList<>();
    private List<FraudMetricRow> topSuspiciousMobileNumbers = new ArrayList<>();
    private List<FraudMetricRow> topSuspiciousAddresses = new ArrayList<>();
    private List<FraudMetricRow> topSuspiciousCustomers = new ArrayList<>();
    private List<FraudMetricRow> topSuspiciousVendors = new ArrayList<>();
    private List<FraudMetricRow> ruleTriggerFrequency = new ArrayList<>();

    public long getTotalAssessedOrders() { return totalAssessedOrders; }
    public void setTotalAssessedOrders(long totalAssessedOrders) { this.totalAssessedOrders = totalAssessedOrders; }
    public long getLowRiskOrders() { return lowRiskOrders; }
    public void setLowRiskOrders(long lowRiskOrders) { this.lowRiskOrders = lowRiskOrders; }
    public long getMediumRiskOrders() { return mediumRiskOrders; }
    public void setMediumRiskOrders(long mediumRiskOrders) { this.mediumRiskOrders = mediumRiskOrders; }
    public long getHighRiskOrders() { return highRiskOrders; }
    public void setHighRiskOrders(long highRiskOrders) { this.highRiskOrders = highRiskOrders; }
    public long getCriticalRiskOrders() { return criticalRiskOrders; }
    public void setCriticalRiskOrders(long criticalRiskOrders) { this.criticalRiskOrders = criticalRiskOrders; }
    public long getManualReviewOrders() { return manualReviewOrders; }
    public void setManualReviewOrders(long manualReviewOrders) { this.manualReviewOrders = manualReviewOrders; }
    public long getBlockedOrders() { return blockedOrders; }
    public void setBlockedOrders(long blockedOrders) { this.blockedOrders = blockedOrders; }
    public long getRejectedOrders() { return rejectedOrders; }
    public void setRejectedOrders(long rejectedOrders) { this.rejectedOrders = rejectedOrders; }
    public BigDecimal getFraudLossPrevented() { return fraudLossPrevented; }
    public void setFraudLossPrevented(BigDecimal fraudLossPrevented) { this.fraudLossPrevented = fraudLossPrevented == null ? BigDecimal.ZERO : fraudLossPrevented; }
    public BigDecimal getFinancialExposure() { return financialExposure; }
    public void setFinancialExposure(BigDecimal financialExposure) { this.financialExposure = financialExposure == null ? BigDecimal.ZERO : financialExposure; }
    public double getChargebackRate() { return chargebackRate; }
    public void setChargebackRate(double chargebackRate) { this.chargebackRate = chargebackRate; }
    public double getCodReturnToOriginRate() { return codReturnToOriginRate; }
    public void setCodReturnToOriginRate(double codReturnToOriginRate) { this.codReturnToOriginRate = codReturnToOriginRate; }
    public double getDeliveryRefusalRate() { return deliveryRefusalRate; }
    public void setDeliveryRefusalRate(double deliveryRefusalRate) { this.deliveryRefusalRate = deliveryRefusalRate; }
    public double getFalsePositiveRate() { return falsePositiveRate; }
    public void setFalsePositiveRate(double falsePositiveRate) { this.falsePositiveRate = falsePositiveRate; }
    public List<FraudMetricRow> getTopSuspiciousDevices() { return topSuspiciousDevices; }
    public void setTopSuspiciousDevices(List<FraudMetricRow> topSuspiciousDevices) { this.topSuspiciousDevices = topSuspiciousDevices == null ? new ArrayList<>() : topSuspiciousDevices; }
    public List<FraudMetricRow> getTopSuspiciousIpAddresses() { return topSuspiciousIpAddresses; }
    public void setTopSuspiciousIpAddresses(List<FraudMetricRow> topSuspiciousIpAddresses) { this.topSuspiciousIpAddresses = topSuspiciousIpAddresses == null ? new ArrayList<>() : topSuspiciousIpAddresses; }
    public List<FraudMetricRow> getTopSuspiciousMobileNumbers() { return topSuspiciousMobileNumbers; }
    public void setTopSuspiciousMobileNumbers(List<FraudMetricRow> topSuspiciousMobileNumbers) { this.topSuspiciousMobileNumbers = topSuspiciousMobileNumbers == null ? new ArrayList<>() : topSuspiciousMobileNumbers; }
    public List<FraudMetricRow> getTopSuspiciousAddresses() { return topSuspiciousAddresses; }
    public void setTopSuspiciousAddresses(List<FraudMetricRow> topSuspiciousAddresses) { this.topSuspiciousAddresses = topSuspiciousAddresses == null ? new ArrayList<>() : topSuspiciousAddresses; }
    public List<FraudMetricRow> getTopSuspiciousCustomers() { return topSuspiciousCustomers; }
    public void setTopSuspiciousCustomers(List<FraudMetricRow> topSuspiciousCustomers) { this.topSuspiciousCustomers = topSuspiciousCustomers == null ? new ArrayList<>() : topSuspiciousCustomers; }
    public List<FraudMetricRow> getTopSuspiciousVendors() { return topSuspiciousVendors; }
    public void setTopSuspiciousVendors(List<FraudMetricRow> topSuspiciousVendors) { this.topSuspiciousVendors = topSuspiciousVendors == null ? new ArrayList<>() : topSuspiciousVendors; }
    public List<FraudMetricRow> getRuleTriggerFrequency() { return ruleTriggerFrequency; }
    public void setRuleTriggerFrequency(List<FraudMetricRow> ruleTriggerFrequency) { this.ruleTriggerFrequency = ruleTriggerFrequency == null ? new ArrayList<>() : ruleTriggerFrequency; }
}
