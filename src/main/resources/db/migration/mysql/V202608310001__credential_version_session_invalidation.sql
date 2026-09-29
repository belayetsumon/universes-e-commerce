-- Add a per-account credential/access epoch used to reject stale sessions.
-- The guard keeps this migration safe for databases where the user table is
-- created by an earlier baseline or is not present in a fresh schema yet.
SET @users_table_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usermodule_users'
);
SET @credential_version_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'usermodule_users'
      AND COLUMN_NAME = 'credential_version'
);
SET @credential_version_sql = CASE
    WHEN @users_table_exists = 0 THEN 'SELECT 1'
    WHEN @credential_version_exists = 0
        THEN 'ALTER TABLE usermodule_users ADD COLUMN credential_version BIGINT NOT NULL DEFAULT 1'
    ELSE 'SELECT 1'
END;
PREPARE credential_version_statement FROM @credential_version_sql;
EXECUTE credential_version_statement;
DEALLOCATE PREPARE credential_version_statement;
