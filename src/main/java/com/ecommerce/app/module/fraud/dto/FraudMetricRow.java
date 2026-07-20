package com.ecommerce.app.module.fraud.dto;

import java.math.BigDecimal;

public class FraudMetricRow {

    private String label;
    private String maskedValue;
    private Long referenceId;
    private long count;
    private BigDecimal amount = BigDecimal.ZERO;
    private double rate;

    public FraudMetricRow() {
    }

    public FraudMetricRow(String label, long count) {
        this.label = label;
        this.count = count;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getMaskedValue() { return maskedValue; }
    public void setMaskedValue(String maskedValue) { this.maskedValue = maskedValue; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount == null ? BigDecimal.ZERO : amount; }
    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }
}
