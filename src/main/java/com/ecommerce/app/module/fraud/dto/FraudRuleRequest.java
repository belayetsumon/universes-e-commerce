package com.ecommerce.app.module.fraud.dto;

import com.ecommerce.app.module.fraud.model.FraudAction;
import com.ecommerce.app.module.fraud.model.FraudRuleOperator;
import com.ecommerce.app.module.fraud.model.FraudRuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class FraudRuleRequest {

    @NotBlank(message = "Rule code is required.")
    @Size(max = 100, message = "Rule code must be 100 characters or less.")
    private String ruleCode;

    @NotBlank(message = "Rule name is required.")
    @Size(max = 160, message = "Rule name must be 160 characters or less.")
    private String ruleName;

    @Size(max = 1000, message = "Description must be 1000 characters or less.")
    private String description;

    @NotNull(message = "Rule type is required.")
    private FraudRuleType ruleType;

    @Size(max = 100, message = "Signal code must be 100 characters or less.")
    private String signalCode;

    private FraudRuleOperator operator;
    private String comparisonValue;
    private int scoreImpact;
    private int priority = 100;
    private FraudAction action;
    private boolean hardBlock;
    private boolean active = true;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime effectiveStartAt;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime effectiveEndAt;
    private Long vendorId;
    private Long productId;
    private Long categoryId;
    private String paymentMethod;
    private String country;
    private String district;
    private String channel;
    private String ruleConfigurationJson;

    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public FraudRuleType getRuleType() { return ruleType; }
    public void setRuleType(FraudRuleType ruleType) { this.ruleType = ruleType; }
    public String getSignalCode() { return signalCode; }
    public void setSignalCode(String signalCode) { this.signalCode = signalCode; }
    public FraudRuleOperator getOperator() { return operator; }
    public void setOperator(FraudRuleOperator operator) { this.operator = operator; }
    public String getComparisonValue() { return comparisonValue; }
    public void setComparisonValue(String comparisonValue) { this.comparisonValue = comparisonValue; }
    public int getScoreImpact() { return scoreImpact; }
    public void setScoreImpact(int scoreImpact) { this.scoreImpact = scoreImpact; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public FraudAction getAction() { return action; }
    public void setAction(FraudAction action) { this.action = action; }
    public boolean isHardBlock() { return hardBlock; }
    public void setHardBlock(boolean hardBlock) { this.hardBlock = hardBlock; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getEffectiveStartAt() { return effectiveStartAt; }
    public void setEffectiveStartAt(LocalDateTime effectiveStartAt) { this.effectiveStartAt = effectiveStartAt; }
    public LocalDateTime getEffectiveEndAt() { return effectiveEndAt; }
    public void setEffectiveEndAt(LocalDateTime effectiveEndAt) { this.effectiveEndAt = effectiveEndAt; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getRuleConfigurationJson() { return ruleConfigurationJson; }
    public void setRuleConfigurationJson(String ruleConfigurationJson) { this.ruleConfigurationJson = ruleConfigurationJson; }
}
