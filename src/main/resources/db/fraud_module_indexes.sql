-- Fraud Order Detection And Prevention Module - PostgreSQL indexes and constraints

CREATE INDEX IF NOT EXISTS idx_fraud_assessment_order ON fraud_assessments(order_id);
CREATE INDEX IF NOT EXISTS idx_fraud_assessment_customer ON fraud_assessments(customer_id);
CREATE INDEX IF NOT EXISTS idx_fraud_assessment_vendor ON fraud_assessments(vendor_id);
CREATE INDEX IF NOT EXISTS idx_fraud_assessment_status ON fraud_assessments(status);
CREATE INDEX IF NOT EXISTS idx_fraud_assessment_risk ON fraud_assessments(risk_level);
CREATE INDEX IF NOT EXISTS idx_fraud_assessment_decision ON fraud_assessments(decision);
CREATE INDEX IF NOT EXISTS idx_fraud_assessment_evaluated ON fraud_assessments(evaluated_at);
CREATE UNIQUE INDEX IF NOT EXISTS ux_fraud_assessment_idempotency ON fraud_assessments(idempotency_key) WHERE idempotency_key IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_fraud_signal_assessment ON fraud_signals(assessment_id);
CREATE INDEX IF NOT EXISTS idx_fraud_signal_code ON fraud_signals(signal_code);
CREATE INDEX IF NOT EXISTS idx_fraud_signal_category ON fraud_signals(signal_category);
CREATE INDEX IF NOT EXISTS idx_fraud_signal_reason ON fraud_signals(reason_code);

CREATE INDEX IF NOT EXISTS idx_fraud_rule_active_priority ON fraud_rules(active, priority, id);
CREATE INDEX IF NOT EXISTS idx_fraud_rule_type_active ON fraud_rules(rule_type, active);
CREATE INDEX IF NOT EXISTS idx_fraud_rule_scope ON fraud_rules(vendor_id, product_id, category_id, payment_method, country, district, channel);

CREATE INDEX IF NOT EXISTS idx_fraud_case_status ON fraud_cases(case_status);
CREATE INDEX IF NOT EXISTS idx_fraud_case_order ON fraud_cases(order_id);
CREATE INDEX IF NOT EXISTS idx_fraud_case_customer ON fraud_cases(customer_id);
CREATE INDEX IF NOT EXISTS idx_fraud_case_vendor ON fraud_cases(vendor_id);
CREATE INDEX IF NOT EXISTS idx_fraud_case_assigned ON fraud_cases(assigned_investigator);

CREATE INDEX IF NOT EXISTS idx_fraud_block_type_value ON fraud_blocklist(block_type, hashed_value);
CREATE UNIQUE INDEX IF NOT EXISTS ux_fraud_active_block_identity ON fraud_blocklist(block_type, hashed_value, scope) WHERE active = TRUE;
CREATE INDEX IF NOT EXISTS idx_fraud_block_expiry ON fraud_blocklist(expires_at);

CREATE INDEX IF NOT EXISTS idx_fraud_device_identifier ON fraud_device_identities(device_identifier);
CREATE INDEX IF NOT EXISTS idx_fraud_device_fingerprint ON fraud_device_identities(device_fingerprint_hash);
CREATE INDEX IF NOT EXISTS idx_fraud_device_customer ON fraud_device_identities(customer_id);
CREATE INDEX IF NOT EXISTS idx_fraud_device_ip ON fraud_device_identities(ip_address);
CREATE INDEX IF NOT EXISTS idx_fraud_trusted_customer_device ON fraud_trusted_devices(customer_id, device_identifier, active);

CREATE INDEX IF NOT EXISTS idx_fraud_customer_profile_risk ON fraud_customer_risk_profiles(risk_level);
CREATE INDEX IF NOT EXISTS idx_fraud_customer_profile_cod ON fraud_customer_risk_profiles(cod_disabled);
CREATE INDEX IF NOT EXISTS idx_fraud_vendor_profile_risk ON fraud_vendor_risk_profiles(risk_level);
CREATE INDEX IF NOT EXISTS idx_fraud_vendor_profile_payout ON fraud_vendor_risk_profiles(payout_held);
CREATE INDEX IF NOT EXISTS idx_fraud_vendor_profile_review ON fraud_vendor_risk_profiles(under_review);
CREATE INDEX IF NOT EXISTS idx_fraud_vendor_profile_collusion ON fraud_vendor_risk_profiles(collusion_signal_count);
CREATE INDEX IF NOT EXISTS idx_fraud_vendor_profile_tracking ON fraud_vendor_risk_profiles(tracking_reuse_count);

CREATE INDEX IF NOT EXISTS idx_fraud_review_assessment ON fraud_review_history(assessment_id);
CREATE INDEX IF NOT EXISTS idx_fraud_review_reviewer ON fraud_review_history(reviewed_by);
CREATE INDEX IF NOT EXISTS idx_fraud_rule_exec_assessment ON fraud_rule_executions(assessment_id);
CREATE INDEX IF NOT EXISTS idx_fraud_rule_exec_rule_code ON fraud_rule_executions(rule_code, matched);
CREATE INDEX IF NOT EXISTS idx_fraud_event_aggregate ON fraud_event_logs(aggregate_type, aggregate_id);
CREATE INDEX IF NOT EXISTS idx_fraud_event_order ON fraud_event_logs(order_id);
CREATE INDEX IF NOT EXISTS idx_fraud_event_vendor ON fraud_event_logs(vendor_id);
CREATE INDEX IF NOT EXISTS idx_fraud_config_active_key ON fraud_configurations(active, config_key);
CREATE INDEX IF NOT EXISTS idx_fraud_velocity_lookup ON fraud_velocity_counters(counter_scope, counter_value_hash, window_end_at);
CREATE INDEX IF NOT EXISTS idx_fraud_payment_order ON fraud_payment_risk_results(order_id);
CREATE INDEX IF NOT EXISTS idx_fraud_payment_token ON fraud_payment_risk_results(payment_token_hash);
CREATE INDEX IF NOT EXISTS idx_fraud_cod_customer ON fraud_cod_risk_profiles(customer_id);
CREATE INDEX IF NOT EXISTS idx_fraud_cod_vendor ON fraud_cod_risk_profiles(vendor_id);
CREATE INDEX IF NOT EXISTS idx_fraud_cod_mobile ON fraud_cod_risk_profiles(mobile_hash);
CREATE INDEX IF NOT EXISTS idx_fraud_cod_address ON fraud_cod_risk_profiles(address_hash);
CREATE INDEX IF NOT EXISTS idx_fraud_cod_district ON fraud_cod_risk_profiles(district);
CREATE INDEX IF NOT EXISTS idx_fraud_evidence_case ON fraud_evidence(case_id);
CREATE INDEX IF NOT EXISTS idx_fraud_evidence_assessment ON fraud_evidence(assessment_id);
CREATE INDEX IF NOT EXISTS idx_fraud_outbox_status_next ON fraud_outbox_events(status, next_attempt_at, id);
CREATE INDEX IF NOT EXISTS idx_fraud_outbox_aggregate ON fraud_outbox_events(aggregate_type, aggregate_id);
CREATE INDEX IF NOT EXISTS idx_fraud_idem_scope_status ON fraud_idempotency_records(operation_scope, status);
CREATE INDEX IF NOT EXISTS idx_fraud_idem_expiry ON fraud_idempotency_records(expires_at);
