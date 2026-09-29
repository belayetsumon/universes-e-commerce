-- Fail closed when the legacy blocklist cannot satisfy the runtime contract.
SET @fraud_block_table_valid = (
    SELECT CASE
        WHEN COUNT(*) = 1 AND MAX(ENGINE) = 'InnoDB' THEN 1
        ELSE 0
    END
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fraud_blocklist'
);
SET @fraud_block_table_guard_sql = IF(
    @fraud_block_table_valid = 1,
    'SELECT 1',
    'SELECT * FROM fraud_blocklist_table_missing_or_not_innodb__repair_before_deployment'
);
PREPARE fraud_block_table_guard FROM @fraud_block_table_guard_sql;
EXECUTE fraud_block_table_guard;
DEALLOCATE PREPARE fraud_block_table_guard;

SET @fraud_block_required_columns = (
    SELECT COUNT(DISTINCT COLUMN_NAME)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fraud_blocklist'
      AND COLUMN_NAME IN ('block_type', 'hashed_value', 'scope', 'active', 'temporary', 'expires_at')
);
SET @fraud_block_column_guard_sql = IF(
    @fraud_block_required_columns = 6,
    'SELECT 1',
    'SELECT * FROM fraud_blocklist_required_columns_missing__repair_before_deployment'
);
PREPARE fraud_block_column_guard FROM @fraud_block_column_guard_sql;
EXECUTE fraud_block_column_guard;
DEALLOCATE PREPARE fraud_block_column_guard;

SET @fraud_block_invalid_expiry_rows = (
    SELECT COUNT(*)
    FROM fraud_blocklist
    WHERE (temporary = 1 AND expires_at IS NULL)
       OR (temporary = 0 AND expires_at IS NOT NULL)
);
SET @fraud_block_expiry_guard_sql = IF(
    @fraud_block_invalid_expiry_rows = 0,
    'SELECT 1',
    'SELECT * FROM fraud_blocklist_ambiguous_expiry_rows__reconcile_before_deployment'
);
PREPARE fraud_block_expiry_guard FROM @fraud_block_expiry_guard_sql;
EXECUTE fraud_block_expiry_guard;
DEALLOCATE PREPARE fraud_block_expiry_guard;

SET @fraud_block_expiry_constraint_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fraud_blocklist'
      AND CONSTRAINT_NAME = 'ck_fraud_blocklist_expiry_contract'
      AND CONSTRAINT_TYPE = 'CHECK'
);
SET @fraud_block_expiry_constraint_sql = IF(
    @fraud_block_expiry_constraint_exists = 0,
    'ALTER TABLE fraud_blocklist ADD CONSTRAINT ck_fraud_blocklist_expiry_contract CHECK ((temporary = 1 AND expires_at IS NOT NULL) OR (temporary = 0 AND expires_at IS NULL))',
    'SELECT 1'
);
PREPARE fraud_block_expiry_constraint_statement FROM @fraud_block_expiry_constraint_sql;
EXECUTE fraud_block_expiry_constraint_statement;
DEALLOCATE PREPARE fraud_block_expiry_constraint_statement;

SET @fraud_block_effective_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'fraud_blocklist'
      AND INDEX_NAME = 'idx_fraud_block_effective_lookup'
);
SET @fraud_block_effective_index_sql = IF(
    @fraud_block_effective_index_exists = 0,
    'CREATE INDEX idx_fraud_block_effective_lookup ON fraud_blocklist (block_type, hashed_value, scope, active, temporary, expires_at)',
    'SELECT 1'
);
PREPARE fraud_block_effective_index_statement FROM @fraud_block_effective_index_sql;
EXECUTE fraud_block_effective_index_statement;
DEALLOCATE PREPARE fraud_block_effective_index_statement;
