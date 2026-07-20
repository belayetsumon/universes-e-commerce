package com.ecommerce.app.module.fraud.dto;

public class FraudGuardResult {

    private boolean allowed;
    private String reason;
    private Long assessmentId;

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
}
