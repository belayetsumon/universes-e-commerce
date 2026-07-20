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
@Table(name = "fraud_rule_executions", indexes = {
    @Index(name = "idx_fraud_rule_exec_assessment", columnList = "assessment_id"),
    @Index(name = "idx_fraud_rule_exec_rule", columnList = "rule_id"),
    @Index(name = "idx_fraud_rule_exec_matched", columnList = "matched"),
    @Index(name = "idx_fraud_rule_exec_code", columnList = "rule_code")
})
public class FraudRuleExecution extends BaseFraudEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private FraudAssessment assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private FraudRule rule;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Column(name = "signal_code", length = 100)
    private String signalCode;

    @Column(name = "matched", nullable = false)
    private boolean matched;

    @Column(name = "score_impact", nullable = false)
    private int scoreImpact;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 60)
    private FraudAction action;

    @Column(name = "hard_block", nullable = false)
    private boolean hardBlock;

    @Lob
    @Column(name = "execution_detail_json", columnDefinition = "TEXT")
    private String executionDetailJson;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt = LocalDateTime.now();

    public FraudAssessment getAssessment() { return assessment; }
    public void setAssessment(FraudAssessment assessment) { this.assessment = assessment; }
    public FraudRule getRule() { return rule; }
    public void setRule(FraudRule rule) { this.rule = rule; }
    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
    public String getSignalCode() { return signalCode; }
    public void setSignalCode(String signalCode) { this.signalCode = signalCode; }
    public boolean isMatched() { return matched; }
    public void setMatched(boolean matched) { this.matched = matched; }
    public int getScoreImpact() { return scoreImpact; }
    public void setScoreImpact(int scoreImpact) { this.scoreImpact = scoreImpact; }
    public FraudAction getAction() { return action; }
    public void setAction(FraudAction action) { this.action = action; }
    public boolean isHardBlock() { return hardBlock; }
    public void setHardBlock(boolean hardBlock) { this.hardBlock = hardBlock; }
    public String getExecutionDetailJson() { return executionDetailJson; }
    public void setExecutionDetailJson(String executionDetailJson) { this.executionDetailJson = executionDetailJson; }
    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
}
