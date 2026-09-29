-- Registered-customer COD verification is bound to both the user and the
-- normalized mobile number that was verified. Existing flags cannot prove
-- that binding, so they are deliberately invalidated during this upgrade.

SET @registered_cod_setting_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'global_settings'
      AND COLUMN_NAME = 'registered_customer_cod_mobile_verification_enabled'
);
SET @registered_cod_setting_sql = IF(
    @registered_cod_setting_exists = 0,
    'ALTER TABLE global_settings ADD COLUMN registered_customer_cod_mobile_verification_enabled BIT NOT NULL DEFAULT 1',
    'SELECT 1'
);
PREPARE registered_cod_setting_statement FROM @registered_cod_setting_sql;
EXECUTE registered_cod_setting_statement;
DEALLOCATE PREPARE registered_cod_setting_statement;

SET @mobile_verified_at_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usermodule_users'
      AND COLUMN_NAME = 'mobile_verified_at'
);
SET @mobile_verified_at_sql = IF(
    @mobile_verified_at_exists = 0,
    'ALTER TABLE usermodule_users ADD COLUMN mobile_verified_at DATETIME(6) NULL',
    'SELECT 1'
);
PREPARE mobile_verified_at_statement FROM @mobile_verified_at_sql;
EXECUTE mobile_verified_at_statement;
DEALLOCATE PREPARE mobile_verified_at_statement;

SET @mobile_verified_number_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usermodule_users'
      AND COLUMN_NAME = 'mobile_verified_number'
);
SET @mobile_verified_number_sql = IF(
    @mobile_verified_number_exists = 0,
    'ALTER TABLE usermodule_users ADD COLUMN mobile_verified_number VARCHAR(20) NULL',
    'SELECT 1'
);
PREPARE mobile_verified_number_statement FROM @mobile_verified_number_sql;
EXECUTE mobile_verified_number_statement;
DEALLOCATE PREPARE mobile_verified_number_statement;

UPDATE usermodule_users
SET mobile_verified = 0,
    mobile_verified_at = NULL,
    mobile_verified_number = NULL
WHERE mobile_verified = 1
  AND (mobile_verified_at IS NULL OR mobile_verified_number IS NULL);

SET @guest_otp_user_column_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'guest_checkout_otp_verification'
      AND COLUMN_NAME = 'user_id'
);
SET @guest_otp_user_column_sql = IF(
    @guest_otp_user_column_exists = 0,
    'ALTER TABLE guest_checkout_otp_verification ADD COLUMN user_id BIGINT NULL',
    'SELECT 1'
);
PREPARE guest_otp_user_column_statement FROM @guest_otp_user_column_sql;
EXECUTE guest_otp_user_column_statement;
DEALLOCATE PREPARE guest_otp_user_column_statement;

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
    'CREATE INDEX idx_guest_otp_user_purpose_status ON guest_checkout_otp_verification (user_id, purpose, status, created_at DESC)',
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
