package com.ecommerce.app.module.fraud.model;

public enum FraudAssessmentStatus {
    REQUESTED,
    FRAUD_EVALUATION_PENDING,
    APPROVED,
    VERIFICATION_REQUIRED,
    MANUAL_REVIEW,
    FRAUD_HOLD,
    FRAUD_REJECTED,
    CANCELLED,
    EXPIRED
}
