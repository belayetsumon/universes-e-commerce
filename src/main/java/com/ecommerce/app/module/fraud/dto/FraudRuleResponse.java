package com.ecommerce.app.module.fraud.dto;

import com.ecommerce.app.module.fraud.model.FraudAction;
import com.ecommerce.app.module.fraud.model.FraudRuleOperator;
import com.ecommerce.app.module.fraud.model.FraudRuleType;
import java.time.LocalDateTime;

public class FraudRuleResponse {

    private Long id;
    private String uuid;
    private String ruleCode;
    private String ruleName;
    private FraudRuleType ruleType;
    private String signalCode;
    private FraudRuleOperator operator;
    private String comparisonValue;
    private int scoreImpact;
    private int priority;
    private FraudAction action;
    private boolean hardBlock;
    private boolean active;
    private LocalDateTime effectiveStartAt;
    private LocalDateTime effectiveEndAt;
    private Long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
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
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
