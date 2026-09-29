-- This release upgrades an existing Universes Ecommerce schema. Force a clear
-- failure before mutation when the approved legacy schema baseline is absent.
SET @cod_fraud_required_table_count = (
    SELECT COUNT(DISTINCT TABLE_NAME)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME IN (
          'global_settings',
          'usermodule_users',
          'guest_checkout_otp_verification',
          'fraud_velocity_counters',
          'fraud_device_identities',
          'fraud_trusted_devices',
          'fraud_cod_risk_profiles',
          'fraud_idempotency_records',
          'fraud_outbox_events'
      )
);
SET @cod_fraud_preflight_sql = IF(
    @cod_fraud_required_table_count = 9,
    'SELECT 1',
    'SELECT * FROM cod_fraud_required_schema_missing__install_approved_legacy_baseline_first'
);
PREPARE cod_fraud_preflight_statement FROM @cod_fraud_preflight_sql;
EXECUTE cod_fraud_preflight_statement;
DEALLOCATE PREPARE cod_fraud_preflight_statement;

SET @mysql_major = CAST(SUBSTRING_INDEX(VERSION(), '.', 1) AS UNSIGNED);
SET @mysql_minor = CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(VERSION(), '.', 2), '.', -1) AS UNSIGNED);
SET @mysql_patch = CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(VERSION(), '.', 3), '.', -1) AS UNSIGNED);
SET @cod_fraud_supported_mysql = VERSION() NOT LIKE '%MariaDB%'
    AND (@mysql_major > 8
        OR (@mysql_major = 8 AND (@mysql_minor > 0 OR (@mysql_minor = 0 AND @mysql_patch >= 16))));
SET @cod_fraud_version_sql = IF(
    @cod_fraud_supported_mysql,
    'SELECT 1',
    'SELECT * FROM cod_fraud_requires_mysql_8_0_16_or_newer'
);
PREPARE cod_fraud_version_statement FROM @cod_fraud_version_sql;
EXECUTE cod_fraud_version_statement;
DEALLOCATE PREPARE cod_fraud_version_statement;

SET @cod_fraud_required_column_count = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND CONCAT(TABLE_NAME, '.', COLUMN_NAME) IN (
          'usermodule_users.id',
          'usermodule_users.mobile_verified',
          'guest_checkout_otp_verification.purpose',
          'guest_checkout_otp_verification.status',
          'guest_checkout_otp_verification.created_at',
          'fraud_velocity_counters.id',
          'fraud_velocity_counters.counter_scope',
          'fraud_velocity_counters.counter_value_hash',
          'fraud_velocity_counters.window_start_at',
          'fraud_velocity_counters.window_end_at',
          'fraud_velocity_counters.counter_count',
          'fraud_device_identities.device_identifier',
          'fraud_device_identities.device_fingerprint_hash',
          'fraud_trusted_devices.device_identifier',
          'fraud_cod_risk_profiles.device_identifier',
          'fraud_idempotency_records.id',
          'fraud_idempotency_records.status',
          'fraud_idempotency_records.expires_at',
          'fraud_outbox_events.id',
          'fraud_outbox_events.status',
          'fraud_outbox_events.published_at'
      )
);
SET @cod_fraud_column_preflight_sql = IF(
    @cod_fraud_required_column_count = 21,
    'SELECT 1',
    'SELECT * FROM cod_fraud_required_columns_missing__install_approved_legacy_baseline_first'
);
PREPARE cod_fraud_column_preflight_statement FROM @cod_fraud_column_preflight_sql;
EXECUTE cod_fraud_column_preflight_statement;
DEALLOCATE PREPARE cod_fraud_column_preflight_statement;

SET @cod_fraud_innodb_table_count = (
    SELECT COUNT(*)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME IN (
          'global_settings',
          'usermodule_users',
          'guest_checkout_otp_verification',
          'fraud_velocity_counters',
          'fraud_device_identities',
          'fraud_trusted_devices',
          'fraud_cod_risk_profiles',
          'fraud_idempotency_records',
          'fraud_outbox_events'
      )
      AND ENGINE = 'InnoDB'
);
SET @cod_fraud_innodb_preflight_sql = IF(
    @cod_fraud_innodb_table_count = 9,
    'SELECT 1',
    'SELECT * FROM cod_fraud_mutable_tables_must_use_innodb'
);
PREPARE cod_fraud_innodb_preflight_statement FROM @cod_fraud_innodb_preflight_sql;
EXECUTE cod_fraud_innodb_preflight_statement;
DEALLOCATE PREPARE cod_fraud_innodb_preflight_statement;

SET @cod_fraud_signed_bigint_user_id_count = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usermodule_users'
      AND COLUMN_NAME = 'id'
      AND DATA_TYPE = 'bigint'
      AND COLUMN_TYPE NOT LIKE '%unsigned%'
);
SET @cod_fraud_user_id_preflight_sql = IF(
    @cod_fraud_signed_bigint_user_id_count = 1,
    'SELECT 1',
    'SELECT * FROM cod_fraud_usermodule_user_id_must_be_signed_bigint'
);
PREPARE cod_fraud_user_id_preflight_statement FROM @cod_fraud_user_id_preflight_sql;
EXECUTE cod_fraud_user_id_preflight_statement;
DEALLOCATE PREPARE cod_fraud_user_id_preflight_statement;

SET @cod_fraud_profile_collision_count = (
    SELECT COUNT(*)
    FROM (
        SELECT CASE
                WHEN LOWER(TRIM(device_identifier)) REGEXP '^[0-9a-f]{64}$'
                    THEN LOWER(TRIM(device_identifier))
                ELSE LOWER(SHA2(LOWER(TRIM(device_identifier)), 256))
            END AS canonical_identifier
        FROM fraud_cod_risk_profiles
        WHERE device_identifier IS NOT NULL
          AND TRIM(device_identifier) <> ''
        GROUP BY canonical_identifier
        HAVING COUNT(*) > 1
    ) AS profile_collisions
);
SET @cod_fraud_profile_collision_sql = IF(
    @cod_fraud_profile_collision_count = 0,
    'SELECT 1',
    'SELECT * FROM cod_fraud_device_profile_collisions__reconcile_before_migration'
);
PREPARE cod_fraud_profile_collision_statement FROM @cod_fraud_profile_collision_sql;
EXECUTE cod_fraud_profile_collision_statement;
DEALLOCATE PREPARE cod_fraud_profile_collision_statement;
