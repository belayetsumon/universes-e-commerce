-- Fraud Module Phase 9 - additive vendor-risk profile columns and indexes for MySQL 8.
-- This manual helper is rerunnable and works on MySQL releases before
-- ADD COLUMN IF NOT EXISTS became available.

DELIMITER //

DROP PROCEDURE IF EXISTS fraud_add_column_if_missing//
CREATE PROCEDURE fraud_add_column_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_definition VARCHAR(1000)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND column_name = p_column_name
    ) THEN
        SET @fraud_column_ddl = CONCAT(
            'ALTER TABLE `', p_table_name, '` ADD COLUMN `',
            p_column_name, '` ', p_column_definition
        );
        PREPARE fraud_column_statement FROM @fraud_column_ddl;
        EXECUTE fraud_column_statement;
        DEALLOCATE PREPARE fraud_column_statement;
    END IF;
END//

DROP PROCEDURE IF EXISTS fraud_add_index_if_missing//
CREATE PROCEDURE fraud_add_index_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_index_columns VARCHAR(1000)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND index_name = p_index_name
    ) THEN
        SET @fraud_index_ddl = CONCAT(
            'CREATE INDEX `', p_index_name, '` ON `',
            p_table_name, '` (', p_index_columns, ')'
        );
        PREPARE fraud_index_statement FROM @fraud_index_ddl;
        EXECUTE fraud_index_statement;
        DEALLOCATE PREPARE fraud_index_statement;
    END IF;
END//

DELIMITER ;

CALL fraud_add_column_if_missing('fraud_vendor_risk_profiles', 'self_purchase_count', 'BIGINT NOT NULL DEFAULT 0');
CALL fraud_add_column_if_missing('fraud_vendor_risk_profiles', 'collusion_signal_count', 'BIGINT NOT NULL DEFAULT 0');
CALL fraud_add_column_if_missing('fraud_vendor_risk_profiles', 'shared_mobile_count', 'BIGINT NOT NULL DEFAULT 0');
CALL fraud_add_column_if_missing('fraud_vendor_risk_profiles', 'shared_address_count', 'BIGINT NOT NULL DEFAULT 0');
CALL fraud_add_column_if_missing('fraud_vendor_risk_profiles', 'shared_bank_account_count', 'BIGINT NOT NULL DEFAULT 0');
CALL fraud_add_column_if_missing('fraud_vendor_risk_profiles', 'unverified_delivery_count', 'BIGINT NOT NULL DEFAULT 0');
CALL fraud_add_column_if_missing('fraud_vendor_risk_profiles', 'sudden_sales_spike_count', 'BIGINT NOT NULL DEFAULT 0');
CALL fraud_add_column_if_missing('fraud_vendor_risk_profiles', 'abnormal_refund_rate', 'DECIMAL(7,4) DEFAULT 0');
CALL fraud_add_column_if_missing('fraud_vendor_risk_profiles', 'abnormal_cancellation_rate', 'DECIMAL(7,4) DEFAULT 0');
CALL fraud_add_column_if_missing('fraud_vendor_risk_profiles', 'last_risk_reason', 'VARCHAR(500)');

CALL fraud_add_index_if_missing('fraud_vendor_risk_profiles', 'idx_fraud_vendor_profile_review', '`under_review`');
CALL fraud_add_index_if_missing('fraud_vendor_risk_profiles', 'idx_fraud_vendor_profile_collusion', '`collusion_signal_count`');
CALL fraud_add_index_if_missing('fraud_vendor_risk_profiles', 'idx_fraud_vendor_profile_tracking', '`tracking_reuse_count`');

DROP PROCEDURE fraud_add_index_if_missing;
DROP PROCEDURE fraud_add_column_if_missing;
