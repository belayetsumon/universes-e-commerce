package com.ecommerce.app.module.fraud.model;

public enum FraudOutboxStatus {
    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED,
    CANCELLED
}
