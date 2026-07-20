package com.ecommerce.app.module.fraud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_rules", indexes = {
    @Index(name = "idx_fraud_rule_code", columnList = "rule_code"),
    @Index(name = "idx_fraud_rule_type", columnList = "rule_type"),
    @Index(name = "idx_fraud_rule_active", columnList = "active"),
    @Index(name = "idx_fraud_rule_scope", columnList = "vendor_id,product_id,category_id,payment_method,country,district,channel")
})
public class FraudRule extends BaseFraudEntity {

    @Column(name = "rule_code", nullable = false, unique = true, length = 100)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 160)
    private String ruleName;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 50)
    private FraudRuleType ruleType;

    @Column(name = "signal_code", length = 100)
    private String signalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_operator", length = 40)
    private FraudRuleOperator operator;

    @Column(name = "comparison_value", length = 500)
    private String comparisonValue;

    @Column(name = "score_impact", nullable = false)
    private int scoreImpact;

    @Column(name = "priority", nullable = false)
    private int priority = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 60)
    private FraudAction action;

    @Column(name = "hard_block", nullable = false)
    private boolean hardBlock;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "effective_start_at")
    private LocalDateTime effectiveStartAt;

    @Column(name = "effective_end_at")
    private LocalDateTime effectiveEndAt;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "payment_method", length = 40)
    private String paymentMethod;

    @Column(name = "country", length = 80)
    private String country;

    @Column(name = "district", length = 120)
    private String district;

    @Column(name = "channel", length = 60)
    private String channel;

    @Lob
    @Column(name = "rule_configuration_json", columnDefinition = "TEXT")
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
