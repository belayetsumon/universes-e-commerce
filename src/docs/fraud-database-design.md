# Fraud Database Design

This document describes the PostgreSQL schema for the Fraud Order Detection and Prevention module.

Schema files:

- `main/resources/db/fraud_module_init.sql`
- `main/resources/db/fraud_module_indexes.sql`
- `main/resources/db/fraud_module_seed_rules.sql`
- `main/resources/db/fraud_module_phase9_vendor_risk.sql`

## Design Goals

- Keep JPA entities clean and free from business logic.
- Preserve complete decision, signal, case, review, evidence, event, outbox, and idempotency history.
- Store raw sensitive identifiers only where the existing platform already requires them; fraud-specific block/payment/token values must be hashed or masked.
- Support high-volume search by order, customer, vendor, status, risk level, event type, and active rules.
- Keep operational actions idempotent and retry-safe.

## Core Tables

| Table | Purpose |
|---|---|
| `fraud_assessments` | One fraud assessment result per evaluation, linked to order/customer/vendor where available |
| `fraud_signals` | Captured fraud signals and score contribution for an assessment |
| `fraud_rules` | Database-configurable rules, scopes, score impact, action, priority, and hard-block flag |
| `fraud_rule_executions` | Explainability log showing which rules matched and why |
| `fraud_cases` | Manual investigation cases opened from assessments or post-order events |
| `fraud_review_history` | Manual review decision history |
| `fraud_event_logs` | Audit/event log for fraud domain actions |
| `fraud_evidence` | Evidence metadata for investigations |
| `fraud_configurations` | Runtime fraud settings, thresholds, webhook secrets, and toggles |

## Identity, Risk, And Velocity Tables

| Table | Purpose |
|---|---|
| `fraud_blocklist` | Hashed block entries for customer/account/mobile/email/device/IP/address/payment/vendor/product/referral |
| `fraud_device_identities` | Device identity/fingerprint/network posture capture |
| `fraud_trusted_devices` | Customer-trusted device records |
| `fraud_customer_risk_profiles` | Customer lifetime fraud, COD, chargeback, and trust profile |
| `fraud_vendor_risk_profiles` | Vendor fraud, collusion, payout, refund, cancellation, and tracking-risk profile |
| `fraud_velocity_counters` | Request/order/payment/account velocity counters by hashed key |
| `fraud_payment_risk_results` | Payment-provider fraud result using hashed payment token |
| `fraud_cod_risk_profiles` | Bangladesh COD RTO/refusal/limit profile by customer/vendor/mobile/address/device/district |

## Reliability Tables

| Table | Purpose |
|---|---|
| `fraud_outbox_events` | Durable fraud event outbox with retry status and idempotency key |
| `fraud_idempotency_records` | Retry-safe command and webhook idempotency |

Outbox statuses:

```text
PENDING
PROCESSING
PUBLISHED
FAILED
CANCELLED
```

Idempotency statuses:

```text
STARTED
COMPLETED
FAILED
EXPIRED
```

## Important Constraints

- `fraud_assessments.uuid` is unique.
- `fraud_assessments.idempotency_key` has a unique partial index where not null.
- `fraud_rules.rule_code` is unique.
- `fraud_cases.case_number` is unique.
- `fraud_blocklist(block_type, hashed_value, scope)` is unique for active rows.
- `fraud_outbox_events.idempotency_key` is unique.
- `fraud_idempotency_records.idempotency_key` is unique.

## High-Value Indexes

Assessment search:

```sql
idx_fraud_assessment_order
idx_fraud_assessment_customer
idx_fraud_assessment_vendor
idx_fraud_assessment_status
idx_fraud_assessment_risk
idx_fraud_assessment_decision
idx_fraud_assessment_evaluated
```

Rule matching:

```sql
idx_fraud_rule_active_priority
idx_fraud_rule_type_active
idx_fraud_rule_scope
```

Case queues:

```sql
idx_fraud_case_status
idx_fraud_case_order
idx_fraud_case_customer
idx_fraud_case_vendor
idx_fraud_case_assigned
```

Blocklist and privacy:

```sql
idx_fraud_block_type_value
ux_fraud_active_block_identity
idx_fraud_block_expiry
```

Outbox/idempotency:

```sql
idx_fraud_outbox_status_next
idx_fraud_outbox_aggregate
idx_fraud_idem_scope_status
idx_fraud_idem_expiry
```

COD and vendor risk:

```sql
idx_fraud_cod_customer
idx_fraud_cod_vendor
idx_fraud_cod_mobile
idx_fraud_cod_address
idx_fraud_cod_district
idx_fraud_vendor_profile_risk
idx_fraud_vendor_profile_payout
idx_fraud_vendor_profile_collusion
idx_fraud_vendor_profile_tracking
```

## Seed Configuration

Default configuration keys from `fraud_module_seed_rules.sql`:

| Key | Default | Meaning |
|---|---:|---|
| `fraud.score.low.max` | `29` | Highest LOW score |
| `fraud.score.medium.max` | `59` | Highest MEDIUM score |
| `fraud.score.high.max` | `79` | Highest HIGH score |
| `fraud.cod.first_order_limit` | `5000.00` | First COD order limit |
| `fraud.cod.rto_disable_threshold` | `2` | Disable COD after RTO threshold |
| `fraud.payment_attempt.block_threshold` | `10` | Payment attempt block threshold |
| `fraud.payment_attempt.window_minutes` | `10` | Payment velocity window |
| `fraud.cod.delivery_refusal_disable_threshold` | `2` | Disable COD after refusal threshold |
| `fraud.cod.restore_after_prepaid_success_count` | `3` | COD restore after prepaid success |
| `fraud.cod.high_value_confirmation_threshold` | `15000.00` | High-value COD threshold |
| `fraud.vendor.sales_spike.window_hours` | `24` | Vendor spike window |
| `fraud.vendor.sales_spike.order_count` | `20` | Vendor spike count |
| `fraud.vendor.abnormal_refund_rate` | `0.20` | Vendor refund risk threshold |
| `fraud.vendor.abnormal_cancel_rate` | `0.25` | Vendor cancel risk threshold |

Phase 13 also supports:

```text
fraud.webhook.<provider>.secret
fraud.webhook.timestamp_tolerance_seconds
fraud.webhook.rate_limit.max_requests
fraud.webhook.rate_limit.window_seconds
```

## Migration Order

For a fresh PostgreSQL installation:

1. Apply the base ecommerce schema.
2. Apply `fraud_module_init.sql`.
3. Apply `fraud_module_indexes.sql`.
4. Apply `fraud_module_seed_rules.sql`.
5. Apply `fraud_module_phase9_vendor_risk.sql` only when upgrading an older fraud schema created before Phase 9.

For an existing database:

1. Back up the database.
2. Apply additive scripts in a maintenance window.
3. Confirm all `CREATE TABLE IF NOT EXISTS`, `CREATE INDEX IF NOT EXISTS`, and `ON CONFLICT DO NOTHING` operations are supported by the target PostgreSQL version.
4. Verify unique indexes do not fail because of pre-existing duplicate records.
5. Run application compile and smoke-test admin fraud pages.

## Rollback Considerations

Rollback is safest before production traffic writes fraud rows.

If rolling back after traffic:

- Do not drop tables until order/payment/shipment code no longer references fraud services.
- Stop scheduled outbox dispatch first if events should not continue.
- Preserve `fraud_assessments`, `fraud_cases`, `fraud_event_logs`, and `fraud_outbox_events` for audit retention.
- To disable behavior without dropping data, deactivate rules, remove blocklist rows, or set configuration values to permissive thresholds.

## Data Retention

Suggested retention policy:

- Assessments, cases, reviews, rule executions, and event logs: retain according to legal/audit policy.
- Idempotency records: expire operational records after a configured period, but retain webhook/payment records longer if needed for dispute evidence.
- Outbox events: archive `PUBLISHED` rows after downstream reporting has consumed them.
- Evidence files: retain with case records and apply secure storage lifecycle rules.

## Privacy Rules

- Do not store raw card data in fraud tables.
- Store payment tokens as SHA-256 hashes through `FraudPrivacySupport.hashIdentifier`.
- Store blocklist values as `hashed_value` and display only `masked_value`.
- Redact `cardNumber`, `cvv`, `password`, and `paymentToken` JSON fields before storing provider metadata.
- Sensitive configuration keys containing `secret`, `token`, `password`, or `key` are masked in admin list views.
