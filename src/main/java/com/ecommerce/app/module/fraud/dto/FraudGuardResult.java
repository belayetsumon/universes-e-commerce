package com.ecommerce.app.module.fraud.dto;

import com.ecommerce.app.module.fraud.model.FraudAssessmentStatus;
import com.ecommerce.app.module.fraud.model.FraudDecision;

public class FraudGuardResult {

    private boolean allowed;
    private String reason;
    private Long assessmentId;
    private FraudAssessmentStatus assessmentStatus;
    private FraudDecision decision;

    public static FraudGuardResult allowed() {
        FraudGuardResult result = new FraudGuardResult();
        result.setAllowed(true);
        return result;
    }

    public static FraudGuardResult blocked(String reason) {
        FraudGuardResult result = new FraudGuardResult();
        result.setAllowed(false);
        result.setReason(reason);
        return result;
    }

    public boolean isAllowed() { return allowed; }
    public void setAllowed(boolean allowed) { this.allowed = allowed; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }
    public FraudAssessmentStatus getAssessmentStatus() { return assessmentStatus; }
    public void setAssessmentStatus(FraudAssessmentStatus assessmentStatus) { this.assessmentStatus = assessmentStatus; }
    public FraudDecision getDecision() { return decision; }
    public void setDecision(FraudDecision decision) { this.decision = decision; }
}
