package com.ecommerce.app.module.fraud.dto;

import com.ecommerce.app.module.fraud.model.FraudAction;
import com.ecommerce.app.module.fraud.model.FraudAssessmentStatus;
import com.ecommerce.app.module.fraud.model.FraudDecision;
import com.ecommerce.app.module.fraud.model.FraudReasonCode;
import com.ecommerce.app.module.fraud.model.FraudRiskLevel;
import java.util.ArrayList;
import java.util.List;

public class FraudDecisionResult {

    private int riskScore;
    private FraudRiskLevel riskLevel;
    private FraudDecision decision;
    private FraudAction action;
    private FraudAssessmentStatus status;
    private FraudReasonCode primaryReasonCode;
    private String decisionReason;
    private boolean hardBlock;
    private boolean automaticDecision = true;
    private boolean manualReviewRequired;
    private List<FraudReasonCode> reasonCodes = new ArrayList<>();

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = Math.max(0, Math.min(100, riskScore)); }
    public FraudRiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(FraudRiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public FraudDecision getDecision() { return decision; }
    public void setDecision(FraudDecision decision) { this.decision = decision; }
    public FraudAction getAction() { return action; }
    public void setAction(FraudAction action) { this.action = action; }
    public FraudAssessmentStatus getStatus() { return status; }
    public void setStatus(FraudAssessmentStatus status) { this.status = status; }
    public FraudReasonCode getPrimaryReasonCode() { return primaryReasonCode; }
    public void setPrimaryReasonCode(FraudReasonCode primaryReasonCode) { this.primaryReasonCode = primaryReasonCode; }
    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }
    public boolean isHardBlock() { return hardBlock; }
    public void setHardBlock(boolean hardBlock) { this.hardBlock = hardBlock; }
    public boolean isAutomaticDecision() { return automaticDecision; }
    public void setAutomaticDecision(boolean automaticDecision) { this.automaticDecision = automaticDecision; }
    public boolean isManualReviewRequired() { return manualReviewRequired; }
    public void setManualReviewRequired(boolean manualReviewRequired) { this.manualReviewRequired = manualReviewRequired; }
    public List<FraudReasonCode> getReasonCodes() { return reasonCodes; }
    public void setReasonCodes(List<FraudReasonCode> reasonCodes) { this.reasonCodes = reasonCodes == null ? new ArrayList<>() : reasonCodes; }
}
