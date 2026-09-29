-- Forward repair and postflight invariants. Canonicalization is deliberately
-- repeated here so databases that received an earlier V002 still converge.
SET @cod_fraud_postflight_collision_count = (
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
SET @cod_fraud_postflight_collision_sql = IF(
    @cod_fraud_postflight_collision_count = 0,
    'SELECT 1',
    'SELECT * FROM cod_fraud_duplicate_canonical_device_profiles__reconcile_before_startup'
);
PREPARE cod_fraud_postflight_collision_statement FROM @cod_fraud_postflight_collision_sql;
EXECUTE cod_fraud_postflight_collision_statement;
DEALLOCATE PREPARE cod_fraud_postflight_collision_statement;

SET @cod_fraud_identity_collision_count = (
    SELECT COUNT(*)
    FROM (
        SELECT
            CASE
                WHEN LOWER(TRIM(device_identifier)) REGEXP '^[0-9a-f]{64}$'
                    THEN LOWER(TRIM(device_identifier))
                ELSE LOWER(SHA2(LOWER(TRIM(device_identifier)), 256))
            END AS canonical_identifier,
            customer_id
        FROM fraud_device_identities
        WHERE customer_id IS NOT NULL
        GROUP BY canonical_identifier, customer_id
        HAVING COUNT(*) > 1
    ) AS identity_collisions
);
SET @cod_fraud_identity_collision_sql = IF(
    @cod_fraud_identity_collision_count = 0,
    'SELECT 1',
    'SELECT * FROM cod_fraud_duplicate_customer_device_identities__reconcile_before_startup'
);
PREPARE cod_fraud_identity_collision_statement FROM @cod_fraud_identity_collision_sql;
EXECUTE cod_fraud_identity_collision_statement;
DEALLOCATE PREPARE cod_fraud_identity_collision_statement;

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

UPDATE fraud_device_identities
SET identity_key = LOWER(SHA2(LOWER(CONCAT(
        device_identifier,
        '|CUSTOMER:',
        CAST(customer_id AS CHAR)
    )), 256))
WHERE identity_key IS NULL
  AND customer_id IS NOT NULL;

SET @cod_fraud_device_profile_key_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fraud_cod_risk_profiles'
      AND INDEX_NAME = 'uk_fraud_cod_device_identifier'
);
SET @cod_fraud_device_profile_key_sql = IF(
    @cod_fraud_device_profile_key_exists = 0,
    'CREATE UNIQUE INDEX uk_fraud_cod_device_identifier ON fraud_cod_risk_profiles (device_identifier)',
    'SELECT 1'
);
PREPARE cod_fraud_device_profile_key_statement FROM @cod_fraud_device_profile_key_sql;
EXECUTE cod_fraud_device_profile_key_statement;
DEALLOCATE PREPARE cod_fraud_device_profile_key_statement;

SET @cod_fraud_device_profile_key_valid = (
    SELECT COUNT(*)
    FROM (
        SELECT INDEX_NAME
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'fraud_cod_risk_profiles'
          AND INDEX_NAME = 'uk_fraud_cod_device_identifier'
        GROUP BY INDEX_NAME
        HAVING MIN(NON_UNIQUE) = 0
           AND COUNT(*) = 1
           AND MAX(CASE WHEN SEQ_IN_INDEX = 1 AND COLUMN_NAME = 'device_identifier' THEN 1 ELSE 0 END) = 1
    ) AS valid_index
);
SET @cod_fraud_device_profile_key_guard_sql = IF(
    @cod_fraud_device_profile_key_valid = 1,
    'SELECT 1',
    'SELECT * FROM cod_fraud_device_profile_unique_index_has_invalid_definition'
);
PREPARE cod_fraud_device_profile_key_guard_statement FROM @cod_fraud_device_profile_key_guard_sql;
EXECUTE cod_fraud_device_profile_key_guard_statement;
DEALLOCATE PREPARE cod_fraud_device_profile_key_guard_statement;

SET @cod_fraud_identity_key_valid = (
    SELECT COUNT(*)
    FROM (
        SELECT INDEX_NAME
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'fraud_device_identities'
          AND INDEX_NAME = 'uk_fraud_device_identity_key'
        GROUP BY INDEX_NAME
        HAVING MIN(NON_UNIQUE) = 0
           AND COUNT(*) = 1
           AND MAX(CASE WHEN SEQ_IN_INDEX = 1 AND COLUMN_NAME = 'identity_key' THEN 1 ELSE 0 END) = 1
    ) AS valid_index
);
SET @cod_fraud_identity_key_guard_sql = IF(
    @cod_fraud_identity_key_valid = 1,
    'SELECT 1',
    'SELECT * FROM cod_fraud_identity_key_unique_index_missing_or_invalid'
);
PREPARE cod_fraud_identity_key_guard_statement FROM @cod_fraud_identity_key_guard_sql;
EXECUTE cod_fraud_identity_key_guard_statement;
DEALLOCATE PREPARE cod_fraud_identity_key_guard_statement;
