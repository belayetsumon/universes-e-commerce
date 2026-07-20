-- Fraud Module Phase 9 - additive vendor-risk profile columns and indexes for existing PostgreSQL databases

ALTER TABLE fraud_vendor_risk_profiles
    ADD COLUMN IF NOT EXISTS self_purchase_count BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS collusion_signal_count BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS shared_mobile_count BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS shared_address_count BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS shared_bank_account_count BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS unverified_delivery_count BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS sudden_sales_spike_count BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS abnormal_refund_rate NUMERIC(7,4) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS abnormal_cancellation_rate NUMERIC(7,4) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_risk_reason VARCHAR(500);

CREATE INDEX IF NOT EXISTS idx_fraud_vendor_profile_review ON fraud_vendor_risk_profiles(under_review);
CREATE INDEX IF NOT EXISTS idx_fraud_vendor_profile_collusion ON fraud_vendor_risk_profiles(collusion_signal_count);
CREATE INDEX IF NOT EXISTS idx_fraud_vendor_profile_tracking ON fraud_vendor_risk_profiles(tracking_reuse_count);
