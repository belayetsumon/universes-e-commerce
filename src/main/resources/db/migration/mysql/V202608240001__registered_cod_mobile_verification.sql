-- Registered-customer COD verification is bound to both the user and the
-- normalized mobile number that was verified. Existing flags cannot prove
-- that binding, so they are deliberately invalidated during this upgrade.

ALTER TABLE global_settings
    ADD COLUMN IF NOT EXISTS registered_customer_cod_mobile_verification_enabled BIT NOT NULL DEFAULT 1;

ALTER TABLE usermodule_users
    ADD COLUMN IF NOT EXISTS mobile_verified_at DATETIME(6) NULL,
    ADD COLUMN IF NOT EXISTS mobile_verified_number VARCHAR(20) NULL;

UPDATE usermodule_users
SET mobile_verified = 0,
    mobile_verified_at = NULL,
    mobile_verified_number = NULL
WHERE mobile_verified = 1
  AND (mobile_verified_at IS NULL OR mobile_verified_number IS NULL);

ALTER TABLE guest_checkout_otp_verification
    ADD COLUMN IF NOT EXISTS user_id BIGINT NULL;

SET @guest_otp_user_fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'guest_checkout_otp_verification'
      AND CONSTRAINT_NAME = 'fk_guest_checkout_otp_user'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @guest_otp_user_fk_sql = IF(
    @guest_otp_user_fk_exists = 0,
    'ALTER TABLE guest_checkout_otp_verification ADD CONSTRAINT fk_guest_checkout_otp_user FOREIGN KEY (user_id) REFERENCES usermodule_users(id) ON DELETE CASCADE',
    'SELECT 1'
);
PREPARE guest_otp_user_fk_statement FROM @guest_otp_user_fk_sql;
EXECUTE guest_otp_user_fk_statement;
DEALLOCATE PREPARE guest_otp_user_fk_statement;

SET @guest_otp_user_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'guest_checkout_otp_verification'
      AND INDEX_NAME = 'idx_guest_otp_user_purpose_status'
);
SET @guest_otp_user_index_sql = IF(
    @guest_otp_user_index_exists = 0,
    'CREATE INDEX idx_guest_otp_user_purpose_status ON guest_checkout_otp_verification (user_id, purpose, status)',
    'SELECT 1'
);
PREPARE guest_otp_user_index_statement FROM @guest_otp_user_index_sql;
EXECUTE guest_otp_user_index_statement;
DEALLOCATE PREPARE guest_otp_user_index_statement;

SET @guest_otp_user_created_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'guest_checkout_otp_verification'
      AND INDEX_NAME = 'idx_guest_otp_user_purpose_created'
);
SET @guest_otp_user_created_index_sql = IF(
    @guest_otp_user_created_index_exists = 0,
    'CREATE INDEX idx_guest_otp_user_purpose_created ON guest_checkout_otp_verification (user_id, purpose, created_at)',
    'SELECT 1'
);
PREPARE guest_otp_user_created_index_statement FROM @guest_otp_user_created_index_sql;
EXECUTE guest_otp_user_created_index_statement;
DEALLOCATE PREPARE guest_otp_user_created_index_statement;

SET @user_mobile_snapshot_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usermodule_users'
      AND INDEX_NAME = 'idx_users_mobile_verification_snapshot'
);
SET @user_mobile_snapshot_index_sql = IF(
    @user_mobile_snapshot_index_exists = 0,
    'CREATE INDEX idx_users_mobile_verification_snapshot ON usermodule_users (mobile_verified, mobile_verified_number)',
    'SELECT 1'
);
PREPARE user_mobile_snapshot_index_statement FROM @user_mobile_snapshot_index_sql;
EXECUTE user_mobile_snapshot_index_statement;
DEALLOCATE PREPARE user_mobile_snapshot_index_statement;
