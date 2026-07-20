-- Fraud Order Detection And Prevention Module - default rules and configuration

INSERT INTO fraud_configurations (uuid, config_key, config_value, description, active)
VALUES
('fraud-config-0001', 'fraud.score.low.max', '29', 'Maximum score for LOW risk.', TRUE),
('fraud-config-0002', 'fraud.score.medium.max', '59', 'Maximum score for MEDIUM risk.', TRUE),
('fraud-config-0003', 'fraud.score.high.max', '79', 'Maximum score for HIGH risk.', TRUE),
('fraud-config-0004', 'fraud.cod.first_order_limit', '5000.00', 'Default first-order COD limit.', TRUE),
('fraud-config-0005', 'fraud.cod.rto_disable_threshold', '2', 'Disable COD after this many COD RTO events.', TRUE),
('fraud-config-0006', 'fraud.payment_attempt.block_threshold', '10', 'Temporarily block payment attempts after this count in the configured window.', TRUE),
('fraud-config-0007', 'fraud.payment_attempt.window_minutes', '10', 'Payment attempt velocity window in minutes.', TRUE),
('fraud-config-0008', 'fraud.cod.delivery_refusal_disable_threshold', '2', 'Disable COD after this many delivery refusals.', TRUE),
('fraud-config-0009', 'fraud.cod.restore_after_prepaid_success_count', '3', 'Restore COD after this many successful prepaid orders.', TRUE),
('fraud-config-0010', 'fraud.cod.high_value_confirmation_threshold', '15000.00', 'COD amount requiring customer-service confirmation or advance payment.', TRUE),
('fraud-config-0011', 'fraud.cod.high_risk_partial_prepayment_rto_count', '1', 'Require partial prepayment for COD customers with this many COD RTO events.', TRUE),
('fraud-config-0012', 'fraud.cod.high_risk_partial_prepayment_refusal_count', '1', 'Require partial prepayment for COD customers with this many delivery refusals.', TRUE),
('fraud-config-0013', 'fraud.vendor.sales_spike.window_hours', '24', 'Vendor sales-spike monitoring window in hours.', TRUE),
('fraud-config-0014', 'fraud.vendor.sales_spike.order_count', '20', 'Recent vendor order count treated as a suspicious sales spike.', TRUE),
('fraud-config-0015', 'fraud.vendor.abnormal_refund_rate', '0.20', 'Vendor refund rate that contributes payout and collusion risk.', TRUE),
('fraud-config-0016', 'fraud.vendor.abnormal_cancel_rate', '0.25', 'Vendor cancellation rate that contributes payout risk.', TRUE)
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO fraud_rules (
    uuid, rule_code, rule_name, description, rule_type, signal_code, rule_operator,
    comparison_value, score_impact, priority, action, hard_block, active
)
VALUES
('fraud-rule-0001', 'MOBILE_NOT_VERIFIED_SCORE', 'Mobile not verified', 'Adds risk when customer mobile is not verified.', 'SCORING', 'MOBILE_NOT_VERIFIED', 'EQUALS', 'true', 25, 100, 'VERIFY', FALSE, TRUE),
('fraud-rule-0002', 'NEW_ACCOUNT_24H_SCORE', 'Account created within 24 hours', 'Adds risk for very new accounts.', 'SCORING', 'ACCOUNT_AGE_HOURS', 'LESS_THAN_OR_EQUAL', '24', 10, 110, 'VERIFY', FALSE, TRUE),
('fraud-rule-0003', 'UNKNOWN_DEVICE_SCORE', 'New or unknown device', 'Adds risk when device is new or unknown.', 'SCORING', 'UNKNOWN_DEVICE', 'EQUALS', 'true', 10, 120, 'VERIFY', FALSE, TRUE),
('fraud-rule-0004', 'ORDER_VELOCITY_SCORE', 'More than three orders within 15 minutes', 'Adds risk for high order velocity.', 'SCORING', 'ORDER_COUNT_15M', 'GREATER_THAN', '3', 25, 130, 'MANUAL_REVIEW', FALSE, TRUE),
('fraud-rule-0005', 'MULTI_ACCOUNT_DEVICE_SCORE', 'Same device used by multiple accounts', 'Adds risk for multi-account device sharing.', 'SCORING', 'ACCOUNTS_PER_DEVICE', 'GREATER_THAN', '1', 30, 140, 'MANUAL_REVIEW', FALSE, TRUE),
('fraud-rule-0006', 'HIGH_VALUE_FIRST_ORDER_SCORE', 'High-value first order', 'Adds risk for high-value first orders.', 'SCORING', 'HIGH_VALUE_FIRST_ORDER', 'EQUALS', 'true', 20, 150, 'VERIFY', FALSE, TRUE),
('fraud-rule-0007', 'PAYMENT_COUNTRY_MISMATCH_SCORE', 'Shipping and payment country mismatch', 'Adds risk for country mismatch; never blocks by itself.', 'SCORING', 'PAYMENT_COUNTRY_MISMATCH', 'EQUALS', 'true', 15, 160, 'VERIFY', FALSE, TRUE),
('fraud-rule-0008', 'COD_REFUSAL_HISTORY_SCORE', 'Two previous COD delivery refusals', 'Adds risk for repeated COD delivery refusal.', 'COD_CONTROL', 'DELIVERY_REFUSAL_COUNT', 'GREATER_THAN_OR_EQUAL', '2', 30, 170, 'DISABLE_COD', FALSE, TRUE),
('fraud-rule-0009', 'TRUSTED_CUSTOMER_SCORE', 'Five or more successful orders', 'Reduces risk for established customers.', 'SCORING', 'SUCCESSFUL_ORDER_COUNT', 'GREATER_THAN_OR_EQUAL', '5', -20, 900, 'ALLOW', FALSE, TRUE),
('fraud-rule-0010', 'TRUSTED_DEVICE_SCORE', 'Trusted device', 'Reduces risk for known trusted devices.', 'SCORING', 'TRUSTED_DEVICE', 'EQUALS', 'true', -10, 910, 'ALLOW', FALSE, TRUE),
('fraud-rule-0011', 'THREE_DS_SUCCESS_SCORE', 'Successful 3-D Secure verification', 'Reduces risk for successful 3-D Secure result.', 'SCORING', 'THREE_D_SECURE_SUCCESS', 'EQUALS', 'true', -15, 920, 'ALLOW', FALSE, TRUE),
('fraud-rule-0012', 'DEVICE_BLACKLISTED_BLOCK', 'Blacklisted device hard block', 'Blocks order when device is blacklisted.', 'HARD_BLOCK', 'DEVICE_BLACKLISTED', 'EQUALS', 'true', 100, 10, 'BLOCK', TRUE, TRUE),
('fraud-rule-0013', 'MOBILE_BLACKLISTED_BLOCK', 'Blacklisted mobile hard block', 'Blocks order when mobile number is blacklisted.', 'HARD_BLOCK', 'MOBILE_BLACKLISTED', 'EQUALS', 'true', 100, 20, 'BLOCK', TRUE, TRUE),
('fraud-rule-0014', 'ADDRESS_BLACKLISTED_BLOCK', 'Blacklisted address hard block', 'Holds or blocks order when address is blacklisted.', 'HARD_BLOCK', 'ADDRESS_BLACKLISTED', 'EQUALS', 'true', 100, 30, 'HOLD', TRUE, TRUE),
('fraud-rule-0015', 'PAYMENT_PROVIDER_FRAUD_BLOCK', 'Payment provider confirmed fraud', 'Blocks order when payment provider confirms fraud.', 'HARD_BLOCK', 'PAYMENT_PROVIDER_HIGH_RISK', 'EQUALS', 'true', 100, 40, 'BLOCK', TRUE, TRUE),
('fraud-rule-0016', 'PAYMENT_TOKEN_CHARGEBACK_BLOCK', 'Payment token linked to chargeback fraud', 'Blocks order when payment token is linked to confirmed chargeback fraud.', 'HARD_BLOCK', 'PAYMENT_TOKEN_BLACKLISTED', 'EQUALS', 'true', 100, 50, 'BLOCK', TRUE, TRUE),
('fraud-rule-0017', 'SELF_REFERRAL_HOLD', 'Self-referral detected', 'Holds rewards and opens fraud review for self-referral.', 'REFERRAL_CONTROL', 'SELF_REFERRAL', 'EQUALS', 'true', 40, 60, 'HOLD_REWARD', TRUE, TRUE),
('fraud-rule-0018', 'CIRCULAR_REFERRAL_HOLD', 'Circular referral detected', 'Holds rewards and opens fraud review for circular referral.', 'REFERRAL_CONTROL', 'CIRCULAR_REFERRAL', 'EQUALS', 'true', 50, 70, 'HOLD_REWARD', TRUE, TRUE),
('fraud-rule-0019', 'COD_HIGH_RISK_PARTIAL_PREPAYMENT', 'High-risk COD partial prepayment', 'Requires partial prepayment for high-risk COD orders.', 'COD_CONTROL', 'FRAUD_RISK_SCORE', 'GREATER_THAN_OR_EQUAL', '60', 0, 200, 'REQUIRE_PARTIAL_PREPAYMENT', FALSE, TRUE),
('fraud-rule-0020', 'CRITICAL_RISK_PREPAID', 'Critical risk requires prepaid', 'Requires prepaid or rejects critical-risk orders based on decision matrix.', 'DECISION_MATRIX', 'FRAUD_RISK_SCORE', 'GREATER_THAN_OR_EQUAL', '80', 0, 210, 'REQUIRE_PREPAID', FALSE, TRUE),
('fraud-rule-0021', 'VENDOR_COLLUSION_HOLD_PAYOUT', 'Vendor/customer collusion', 'Holds vendor payout when vendor/customer collusion is detected.', 'VENDOR_CONTROL', 'VENDOR_CUSTOMER_COLLUSION', 'EQUALS', 'true', 70, 55, 'HOLD_VENDOR_PAYOUT', TRUE, TRUE),
('fraud-rule-0022', 'VENDOR_SELF_PURCHASE_SCORE', 'Vendor self-purchase', 'Adds risk when a vendor appears to purchase from its own store.', 'VENDOR_CONTROL', 'VENDOR_SELF_PURCHASE', 'GREATER_THAN', '0', 40, 180, 'MANUAL_REVIEW', FALSE, TRUE),
('fraud-rule-0023', 'TRACKING_REUSE_HOLD_PAYOUT', 'Tracking-number reuse', 'Holds payout when tracking-number reuse is attempted or detected.', 'VENDOR_CONTROL', 'TRACKING_NUMBER_REUSE', 'GREATER_THAN', '0', 50, 65, 'HOLD_VENDOR_PAYOUT', TRUE, TRUE),
('fraud-rule-0024', 'FAKE_DELIVERY_HOLD_PAYOUT', 'Fake delivery suspected', 'Holds payout when delivery is confirmed without carrier verification.', 'VENDOR_CONTROL', 'DELIVERY_WITHOUT_CARRIER_VERIFICATION', 'GREATER_THAN', '0', 45, 75, 'HOLD_VENDOR_PAYOUT', TRUE, TRUE),
('fraud-rule-0025', 'VENDOR_SHARED_BANK_ACCOUNT_SCORE', 'Shared vendor payout account', 'Adds risk when multiple vendors share payout account details.', 'VENDOR_CONTROL', 'VENDOR_SHARED_BANK_ACCOUNT', 'GREATER_THAN', '0', 35, 185, 'MANUAL_REVIEW', FALSE, TRUE),
('fraud-rule-0026', 'ABNORMAL_VENDOR_REFUND_RATE_SCORE', 'Abnormal vendor refund rate', 'Adds risk for unusually high vendor refund or return activity.', 'VENDOR_CONTROL', 'ABNORMAL_VENDOR_REFUND_RATE', 'EXISTS', NULL, 25, 190, 'MANUAL_REVIEW', FALSE, TRUE),
('fraud-rule-0027', 'ABNORMAL_VENDOR_CANCEL_RATE_SCORE', 'Abnormal vendor cancellation rate', 'Adds risk for unusually high vendor cancellation activity.', 'VENDOR_CONTROL', 'ABNORMAL_VENDOR_CANCEL_RATE', 'EXISTS', NULL, 20, 195, 'MANUAL_REVIEW', FALSE, TRUE),
('fraud-rule-0028', 'VENDOR_RISK_SCORE_HOLD_PAYOUT', 'High vendor risk score', 'Holds vendor payout when vendor profile risk reaches high level.', 'VENDOR_CONTROL', 'VENDOR_RISK_SCORE', 'GREATER_THAN_OR_EQUAL', '60', 0, 80, 'HOLD_VENDOR_PAYOUT', TRUE, TRUE)
ON CONFLICT (rule_code) DO NOTHING;
