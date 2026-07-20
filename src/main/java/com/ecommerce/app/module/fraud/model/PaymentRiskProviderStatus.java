package com.ecommerce.app.module.fraud.model;

public enum PaymentRiskProviderStatus {
    NOT_REQUESTED,
    PENDING,
    APPROVED,
    REVIEW,
    HIGH_RISK,
    REJECTED,
    ERROR,
    TIMEOUT
}
