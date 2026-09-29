-- Durable checkout replay protection and concurrency-safe fraud context data.

CREATE TABLE IF NOT EXISTS checkout_placement_attempt (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NULL,
    actor_scope_hash VARCHAR(64) NOT NULL,
    request_key_hash VARCHAR(64) NOT NULL,
    payload_hash VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    order_group_uuid VARCHAR(36) NULL,
    redirect_path VARCHAR(500) NULL,
    locked_until DATETIME(6) NULL,
    failure_reason VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_checkout_attempt_actor_request
        UNIQUE (actor_scope_hash, request_key_hash),
    CONSTRAINT chk_checkout_attempt_status
        CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED')),
    INDEX idx_checkout_attempt_status_lock (status, locked_until),
    INDEX idx_checkout_attempt_order_group (order_group_uuid),
    INDEX idx_checkout_attempt_status_updated (status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Fold any legacy duplicate velocity buckets before applying the unique key.
DROP TEMPORARY TABLE IF EXISTS checkout_velocity_bucket_rollup;
START TRANSACTION;

CREATE TEMPORARY TABLE checkout_velocity_bucket_rollup AS
SELECT
    MIN(id) AS keeper_id,
    counter_scope,
    counter_value_hash,
    window_start_at,
    window_end_at,
    SUM(counter_count) AS merged_count
FROM fraud_velocity_counters
GROUP BY counter_scope, counter_value_hash, window_start_at, window_end_at
HAVING COUNT(*) > 1;

UPDATE fraud_velocity_counters AS counter
JOIN checkout_velocity_bucket_rollup AS rollup
  ON counter.id = rollup.keeper_id
SET counter.counter_count = rollup.merged_count;

DELETE counter
FROM fraud_velocity_counters AS counter
JOIN checkout_velocity_bucket_rollup AS rollup
  ON counter.counter_scope = rollup.counter_scope
 AND counter.counter_value_hash = rollup.counter_value_hash
 AND counter.window_start_at = rollup.window_start_at
 AND counter.window_end_at = rollup.window_end_at
 AND counter.id <> rollup.keeper_id;

COMMIT;
DROP TEMPORARY TABLE checkout_velocity_bucket_rollup;

SET @velocity_bucket_key_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fraud_velocity_counters'
      AND CONSTRAINT_NAME = 'uk_fraud_velocity_bucket'
      AND CONSTRAINT_TYPE = 'UNIQUE'
);
SET @velocity_bucket_key_sql = IF(
    @velocity_bucket_key_exists = 0,
    'ALTER TABLE fraud_velocity_counters ADD CONSTRAINT uk_fraud_velocity_bucket UNIQUE (counter_scope, counter_value_hash, window_start_at, window_end_at)',
    'SELECT 1'
);
PREPARE velocity_bucket_key_statement FROM @velocity_bucket_key_sql;
EXECUTE velocity_bucket_key_statement;
DEALLOCATE PREPARE velocity_bucket_key_statement;

SET @fraud_device_identity_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fraud_device_identities'
      AND COLUMN_NAME = 'identity_key'
);
SET @fraud_device_identity_column_sql = IF(
    @fraud_device_identity_column_exists = 0,
    'ALTER TABLE fraud_device_identities ADD COLUMN identity_key VARCHAR(64) NULL',
    'SELECT 1'
);
PREPARE fraud_device_identity_column_statement FROM @fraud_device_identity_column_sql;
EXECUTE fraud_device_identity_column_statement;
DEALLOCATE PREPARE fraud_device_identity_column_statement;

UPDATE fraud_device_identities
SET device_identifier = CASE
        WHEN LOWER(TRIM(device_identifier)) REGEXP '^[0-9a-f]{64}$'
            THEN LOWER(TRIM(device_identifier))
        ELSE LOWER(SHA2(LOWER(TRIM(device_identifier)), 256))
    END,
    device_fingerprint_hash = CASE
        WHEN device_fingerprint_hash IS NULL OR TRIM(device_fingerprint_hash) = '' THEN NULL
        WHEN LOWER(TRIM(device_fingerprint_hash)) REGEXP '^[0-9a-f]{64}$'
            THEN LOWER(TRIM(device_fingerprint_hash))
        ELSE LOWER(SHA2(LOWER(TRIM(device_fingerprint_hash)), 256))
    END;

UPDATE fraud_trusted_devices
SET device_identifier = CASE
        WHEN LOWER(TRIM(device_identifier)) REGEXP '^[0-9a-f]{64}$'
            THEN LOWER(TRIM(device_identifier))
        ELSE LOWER(SHA2(LOWER(TRIM(device_identifier)), 256))
    END;

UPDATE fraud_cod_risk_profiles
SET device_identifier = CASE
        WHEN device_identifier IS NULL OR TRIM(device_identifier) = '' THEN NULL
        WHEN LOWER(TRIM(device_identifier)) REGEXP '^[0-9a-f]{64}$'
            THEN LOWER(TRIM(device_identifier))
        ELSE LOWER(SHA2(LOWER(TRIM(device_identifier)), 256))
    END;

SET @fraud_device_identity_key_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fraud_device_identities'
      AND INDEX_NAME = 'uk_fraud_device_identity_key'
);
SET @fraud_device_identity_key_sql = IF(
    @fraud_device_identity_key_exists = 0,
    'CREATE UNIQUE INDEX uk_fraud_device_identity_key ON fraud_device_identities (identity_key)',
    'SELECT 1'
);
PREPARE fraud_device_identity_key_statement FROM @fraud_device_identity_key_sql;
EXECUTE fraud_device_identity_key_statement;
DEALLOCATE PREPARE fraud_device_identity_key_statement;
