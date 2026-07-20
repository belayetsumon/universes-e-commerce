package com.ecommerce.app.module.fraud.model;

public enum FraudIdempotencyStatus {
    STARTED,
    COMPLETED,
    FAILED,
    EXPIRED
}
