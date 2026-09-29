-- Fraud Order Detection And Prevention Module - MySQL 8 indexes and constraints.
-- The helper makes this manual index script safe to run more than once.

DELIMITER //

DROP PROCEDURE IF EXISTS fraud_add_index_if_missing//
CREATE PROCEDURE fraud_add_index_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_index_columns VARCHAR(2000),
    IN p_unique TINYINT
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
            'CREATE ', IF(p_unique = 1, 'UNIQUE ', ''),
            'INDEX `', p_index_name, '` ON `', p_table_name, '` (',
            p_index_columns, ')'
        );
        PREPARE fraud_index_statement FROM @fraud_index_ddl;
        EXECUTE fraud_index_statement;
        DEALLOCATE PREPARE fraud_index_statement;
    END IF;
END//

DELIMITER ;

CALL fraud_add_index_if_missing('fraud_assessments', 'idx_fraud_assessment_order', '`order_id`', 0);
CALL fraud_add_index_if_missing('fraud_assessments', 'idx_fraud_assessment_customer', '`customer_id`', 0);
CALL fraud_add_index_if_missing('fraud_assessments', 'idx_fraud_assessment_vendor', '`vendor_id`', 0);
CALL fraud_add_index_if_missing('fraud_assessments', 'idx_fraud_assessment_status', '`status`', 0);
CALL fraud_add_index_if_missing('fraud_assessments', 'idx_fraud_assessment_risk', '`risk_level`', 0);
CALL fraud_add_index_if_missing('fraud_assessments', 'idx_fraud_assessment_decision', '`decision`', 0);
CALL fraud_add_index_if_missing('fraud_assessments', 'idx_fraud_assessment_evaluated', '`evaluated_at`', 0);
-- MySQL unique indexes already allow multiple NULL values, matching the former
-- filtered uniqueness rule for a nullable idempotency key.
CALL fraud_add_index_if_missing('fraud_assessments', 'ux_fraud_assessment_idempotency', '`idempotency_key`', 1);

CALL fraud_add_index_if_missing('fraud_signals', 'idx_fraud_signal_assessment', '`assessment_id`', 0);
CALL fraud_add_index_if_missing('fraud_signals', 'idx_fraud_signal_code', '`signal_code`', 0);
CALL fraud_add_index_if_missing('fraud_signals', 'idx_fraud_signal_category', '`signal_category`', 0);
CALL fraud_add_index_if_missing('fraud_signals', 'idx_fraud_signal_reason', '`reason_code`', 0);

CALL fraud_add_index_if_missing('fraud_rules', 'idx_fraud_rule_active_priority', '`active`, `priority`, `id`', 0);
CALL fraud_add_index_if_missing('fraud_rules', 'idx_fraud_rule_type_active', '`rule_type`, `active`', 0);
CALL fraud_add_index_if_missing('fraud_rules', 'idx_fraud_rule_scope', '`vendor_id`, `product_id`, `category_id`, `payment_method`, `country`, `district`, `channel`', 0);

CALL fraud_add_index_if_missing('fraud_cases', 'idx_fraud_case_status', '`case_status`', 0);
CALL fraud_add_index_if_missing('fraud_cases', 'idx_fraud_case_order', '`order_id`', 0);
CALL fraud_add_index_if_missing('fraud_cases', 'idx_fraud_case_customer', '`customer_id`', 0);
CALL fraud_add_index_if_missing('fraud_cases', 'idx_fraud_case_vendor', '`vendor_id`', 0);
CALL fraud_add_index_if_missing('fraud_cases', 'idx_fraud_case_assigned', '`assigned_investigator`', 0);

CALL fraud_add_index_if_missing('fraud_blocklist', 'idx_fraud_block_type_value', '`block_type`, `hashed_value`', 0);
-- Functional key parts reproduce uniqueness only for active blocklist rows.
-- Inactive rows evaluate to NULL and therefore remain repeatable.
CALL fraud_add_index_if_missing(
    'fraud_blocklist',
    'ux_fraud_active_block_identity',
    '(CASE WHEN `active` = 1 THEN `block_type` ELSE NULL END), (CASE WHEN `active` = 1 THEN `hashed_value` ELSE NULL END), (CASE WHEN `active` = 1 THEN `scope` ELSE NULL END)',
    1
);
CALL fraud_add_index_if_missing('fraud_blocklist', 'idx_fraud_block_expiry', '`expires_at`', 0);

CALL fraud_add_index_if_missing('fraud_device_identities', 'idx_fraud_device_identifier', '`device_identifier`', 0);
CALL fraud_add_index_if_missing('fraud_device_identities', 'idx_fraud_device_fingerprint', '`device_fingerprint_hash`', 0);
CALL fraud_add_index_if_missing('fraud_device_identities', 'idx_fraud_device_customer', '`customer_id`', 0);
CALL fraud_add_index_if_missing('fraud_device_identities', 'idx_fraud_device_ip', '`ip_address`', 0);
CALL fraud_add_index_if_missing('fraud_trusted_devices', 'idx_fraud_trusted_customer_device', '`customer_id`, `device_identifier`, `active`', 0);

CALL fraud_add_index_if_missing('fraud_customer_risk_profiles', 'idx_fraud_customer_profile_risk', '`risk_level`', 0);
CALL fraud_add_index_if_missing('fraud_customer_risk_profiles', 'idx_fraud_customer_profile_cod', '`cod_disabled`', 0);
CALL fraud_add_index_if_missing('fraud_vendor_risk_profiles', 'idx_fraud_vendor_profile_risk', '`risk_level`', 0);
CALL fraud_add_index_if_missing('fraud_vendor_risk_profiles', 'idx_fraud_vendor_profile_payout', '`payout_held`', 0);
CALL fraud_add_index_if_missing('fraud_vendor_risk_profiles', 'idx_fraud_vendor_profile_review', '`under_review`', 0);
CALL fraud_add_index_if_missing('fraud_vendor_risk_profiles', 'idx_fraud_vendor_profile_collusion', '`collusion_signal_count`', 0);
CALL fraud_add_index_if_missing('fraud_vendor_risk_profiles', 'idx_fraud_vendor_profile_tracking', '`tracking_reuse_count`', 0);

CALL fraud_add_index_if_missing('fraud_review_history', 'idx_fraud_review_assessment', '`assessment_id`', 0);
CALL fraud_add_index_if_missing('fraud_review_history', 'idx_fraud_review_reviewer', '`reviewed_by`', 0);
CALL fraud_add_index_if_missing('fraud_rule_executions', 'idx_fraud_rule_exec_assessment', '`assessment_id`', 0);
CALL fraud_add_index_if_missing('fraud_rule_executions', 'idx_fraud_rule_exec_rule_code', '`rule_code`, `matched`', 0);
CALL fraud_add_index_if_missing('fraud_event_logs', 'idx_fraud_event_aggregate', '`aggregate_type`, `aggregate_id`', 0);
CALL fraud_add_index_if_missing('fraud_event_logs', 'idx_fraud_event_order', '`order_id`', 0);
CALL fraud_add_index_if_missing('fraud_event_logs', 'idx_fraud_event_vendor', '`vendor_id`', 0);
CALL fraud_add_index_if_missing('fraud_configurations', 'idx_fraud_config_active_key', '`active`, `config_key`', 0);
CALL fraud_add_index_if_missing('fraud_velocity_counters', 'idx_fraud_velocity_lookup', '`counter_scope`, `counter_value_hash`, `window_end_at`', 0);
CALL fraud_add_index_if_missing('fraud_payment_risk_results', 'idx_fraud_payment_order', '`order_id`', 0);
CALL fraud_add_index_if_missing('fraud_payment_risk_results', 'idx_fraud_payment_token', '`payment_token_hash`', 0);
CALL fraud_add_index_if_missing('fraud_cod_risk_profiles', 'idx_fraud_cod_customer', '`customer_id`', 0);
CALL fraud_add_index_if_missing('fraud_cod_risk_profiles', 'idx_fraud_cod_vendor', '`vendor_id`', 0);
CALL fraud_add_index_if_missing('fraud_cod_risk_profiles', 'idx_fraud_cod_mobile', '`mobile_hash`', 0);
CALL fraud_add_index_if_missing('fraud_cod_risk_profiles', 'idx_fraud_cod_address', '`address_hash`', 0);
CALL fraud_add_index_if_missing('fraud_cod_risk_profiles', 'idx_fraud_cod_district', '`district`', 0);
CALL fraud_add_index_if_missing('fraud_evidence', 'idx_fraud_evidence_case', '`case_id`', 0);
CALL fraud_add_index_if_missing('fraud_evidence', 'idx_fraud_evidence_assessment', '`assessment_id`', 0);
CALL fraud_add_index_if_missing('fraud_outbox_events', 'idx_fraud_outbox_status_next', '`status`, `next_attempt_at`, `id`', 0);
CALL fraud_add_index_if_missing('fraud_outbox_events', 'idx_fraud_outbox_aggregate', '`aggregate_type`, `aggregate_id`', 0);
CALL fraud_add_index_if_missing('fraud_idempotency_records', 'idx_fraud_idem_scope_status', '`operation_scope`, `status`', 0);
CALL fraud_add_index_if_missing('fraud_idempotency_records', 'idx_fraud_idem_expiry', '`expires_at`', 0);

DROP PROCEDURE fraud_add_index_if_missing;
