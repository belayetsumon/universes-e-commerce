-- Registered-customer COD verification is bound to both the user and the
-- normalized mobile number that was verified. Existing flags cannot prove
-- that binding, so they are deliberately invalidated during this upgrade.

ALTER TABLE global_settings
    ADD COLUMN IF NOT EXISTS registered_customer_cod_mobile_verification_enabled BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE usermodule_users
    ADD COLUMN IF NOT EXISTS mobile_verified_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS mobile_verified_number VARCHAR(20) NULL;

UPDATE usermodule_users
SET mobile_verified = FALSE,
    mobile_verified_at = NULL,
    mobile_verified_number = NULL
WHERE mobile_verified = TRUE
  AND (mobile_verified_at IS NULL OR mobile_verified_number IS NULL);

ALTER TABLE guest_checkout_otp_verification
    ADD COLUMN IF NOT EXISTS user_id BIGINT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_guest_checkout_otp_user'
          AND conrelid = 'guest_checkout_otp_verification'::regclass
    ) THEN
        ALTER TABLE guest_checkout_otp_verification
            ADD CONSTRAINT fk_guest_checkout_otp_user
                FOREIGN KEY (user_id) REFERENCES usermodule_users(id)
                ON DELETE CASCADE;
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_guest_otp_user_purpose_status
    ON guest_checkout_otp_verification (user_id, purpose, status);

CREATE INDEX IF NOT EXISTS idx_guest_otp_user_purpose_created
    ON guest_checkout_otp_verification (user_id, purpose, created_at);

CREATE INDEX IF NOT EXISTS idx_users_mobile_verification_snapshot
    ON usermodule_users (mobile_verified, mobile_verified_number);
