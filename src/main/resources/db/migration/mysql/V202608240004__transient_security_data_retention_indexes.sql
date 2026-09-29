SET @idx_guest_otp_status_created_exists = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'guest_checkout_otp_verification'
      AND INDEX_NAME = 'idx_guest_otp_status_created'
);
SET @idx_guest_otp_status_created_sql = IF(
    @idx_guest_otp_status_created_exists = 0,
    'CREATE INDEX idx_guest_otp_status_created ON guest_checkout_otp_verification (status, created_at)',
    'SELECT 1'
);
PREPARE idx_guest_otp_status_created_statement FROM @idx_guest_otp_status_created_sql;
EXECUTE idx_guest_otp_status_created_statement;
DEALLOCATE PREPARE idx_guest_otp_status_created_statement;

SET @idx_guest_otp_status_expiry_exists = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'guest_checkout_otp_verification'
      AND INDEX_NAME = 'idx_guest_otp_status_expiry'
);
SET @idx_guest_otp_status_expiry_sql = IF(
    @idx_guest_otp_status_expiry_exists = 0,
    'CREATE INDEX idx_guest_otp_status_expiry ON guest_checkout_otp_verification (status, expires_at)',
    'SELECT 1'
);
PREPARE idx_guest_otp_status_expiry_statement FROM @idx_guest_otp_status_expiry_sql;
EXECUTE idx_guest_otp_status_expiry_statement;
DEALLOCATE PREPARE idx_guest_otp_status_expiry_statement;

SET @idx_fraud_velocity_retention_exists = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fraud_velocity_counters'
      AND INDEX_NAME = 'idx_fraud_velocity_retention'
);
SET @idx_fraud_velocity_retention_sql = IF(
    @idx_fraud_velocity_retention_exists = 0,
    'CREATE INDEX idx_fraud_velocity_retention ON fraud_velocity_counters (window_end_at, id)',
    'SELECT 1'
);
PREPARE idx_fraud_velocity_retention_statement FROM @idx_fraud_velocity_retention_sql;
EXECUTE idx_fraud_velocity_retention_statement;
DEALLOCATE PREPARE idx_fraud_velocity_retention_statement;

SET @idx_fraud_idem_expiry_exists = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fraud_idempotency_records'
      AND INDEX_NAME = 'idx_fraud_idem_expiry'
);
SET @idx_fraud_idem_expiry_sql = IF(
    @idx_fraud_idem_expiry_exists = 0,
    'CREATE INDEX idx_fraud_idem_expiry ON fraud_idempotency_records (expires_at)',
    'SELECT 1'
);
PREPARE idx_fraud_idem_expiry_statement FROM @idx_fraud_idem_expiry_sql;
EXECUTE idx_fraud_idem_expiry_statement;
DEALLOCATE PREPARE idx_fraud_idem_expiry_statement;

SET @idx_fraud_outbox_published_exists = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'fraud_outbox_events'
      AND INDEX_NAME = 'idx_fraud_outbox_published'
);
SET @idx_fraud_outbox_published_sql = IF(
    @idx_fraud_outbox_published_exists = 0,
    'CREATE INDEX idx_fraud_outbox_published ON fraud_outbox_events (status, published_at, id)',
    'SELECT 1'
);
PREPARE idx_fraud_outbox_published_statement FROM @idx_fraud_outbox_published_sql;
EXECUTE idx_fraud_outbox_published_statement;
DEALLOCATE PREPARE idx_fraud_outbox_published_statement;
