package com.ecommerce.app.module.fraud.model;

public enum FraudDecision {
    APPROVE,
    VERIFY,
    REQUIRE_OTP,
    REQUIRE_PREPAID,
    REQUIRE_PARTIAL_PREPAYMENT,
    MANUAL_REVIEW,
    HOLD,
    REJECT,
    BLOCK,
    CANCEL,
    DISABLE_COD,
    HOLD_REFUND,
    HOLD_REWARD,
    HOLD_VENDOR_PAYOUT
}
