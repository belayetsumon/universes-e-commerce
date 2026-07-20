package com.ecommerce.app.module.fraud.security;

public final class FraudPermissions {

    private FraudPermissions() {
    }

    public static final String ADMIN_OR_FRAUD_ADMIN_AUTHORITIES = "'admin','fraud-admin','ROLE_ADMIN','ROLE_FRAUD_ADMIN'";
    public static final String REVIEW_AUTHORITIES = "'admin','fraud-admin','fraud-supervisor','fraud-analyst','ROLE_ADMIN','ROLE_FRAUD_ADMIN','ROLE_FRAUD_SUPERVISOR','ROLE_FRAUD_ANALYST'";
    public static final String READ_AUTHORITIES = "'admin','fraud-admin','fraud-supervisor','fraud-analyst','finance','ROLE_ADMIN','ROLE_FRAUD_ADMIN','ROLE_FRAUD_SUPERVISOR','ROLE_FRAUD_ANALYST','ROLE_FINANCE'";

    public static final String CAN_READ = "hasAnyAuthority(" + READ_AUTHORITIES + ")";
    public static final String CAN_REVIEW = "hasAnyAuthority(" + REVIEW_AUTHORITIES + ")";
    public static final String CAN_ADMIN = "hasAnyAuthority(" + ADMIN_OR_FRAUD_ADMIN_AUTHORITIES + ")";
    public static final String CAN_FINANCE_OR_READ = "hasAnyAuthority(" + READ_AUTHORITIES + ")";
}
