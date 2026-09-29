# Fraud Admin Configuration Guide

This guide explains how administrators and fraud teams use the Fraud Control admin module.

Admin route root:

```text
/admin/fraud
```

Implemented pages:

| Page | Route |
|---|---|
| Dashboard | `/admin/fraud/dashboard` |
| Assessments | `/admin/fraud/assessments` |
| Assessment detail | `/admin/fraud/assessments/{id}` |
| Manual review | `/admin/fraud/assessments/{id}/review` |
| Cases | `/admin/fraud/cases` |
| Case detail | `/admin/fraud/cases/{id}` |
| Rules | `/admin/fraud/rules` |
| Rule form | `/admin/fraud/rules/new`, `/admin/fraud/rules/{id}/edit` |
| Blocklist | `/admin/fraud/blocklist` |
| Blocklist form | `/admin/fraud/blocklist/new` |
| Configuration | `/admin/fraud/configuration` |
| Reports | `/admin/fraud/reports` |

## Roles

| Role/Authority | Access |
|---|---|
| `admin`, `ROLE_ADMIN` | Full access |
| `fraud-admin`, `ROLE_FRAUD_ADMIN` | Full fraud admin access |
| `fraud-supervisor`, `ROLE_FRAUD_SUPERVISOR` | Read, review, assign, resolve |
| `fraud-analyst`, `ROLE_FRAUD_ANALYST` | Read, review, assign, resolve |
| `finance`, `ROLE_FINANCE` | Read/report access |
| `vendor`, `ROLE_VENDOR` | No internal fraud admin access |

## Dashboard

Use the dashboard for a daily operating view:

- Total assessed orders.
- Risk level counts.
- Manual review queue.
- Blocked and rejected orders.
- Open cases.
- Active rules.
- Active blocklist rows.
- Vendor payout holds.
- Top triggered signals and suspicious values.

Dashboard numbers are read from fraud repositories and should be treated as operational indicators, not final financial reporting until reporting reconciliation is added.

## Assessments

Assessment list supports filtering by:

- Date range.
- Vendor.
- Customer.
- Order.
- Risk level.
- Decision.
- Assessment status.
- Search text.

Assessment detail shows:

- Assessment summary.
- Score and risk level.
- Decision/status.
- Signals and score contribution.
- Rule execution records.
- Review history.
- Order event log.

## Manual Review Workflow

Manual review actions:

| Action | Effect |
|---|---|
| Approve | Sets decision to `APPROVE` and status to `APPROVED` |
| Request OTP | Sets decision to `REQUIRE_OTP` and status to `VERIFICATION_REQUIRED` |
| Hold | Sets decision to `HOLD` and status to `FRAUD_HOLD` |
| Reject | Sets decision to `REJECT` and status to `FRAUD_REJECTED` |

Every review action requires a reason and may include notes. Review actions create:

- `fraud_review_history` row.
- `fraud_event_logs` row.
- Outbox event.
- Audit event.

Do not place internal risk details in customer-facing notes.

## Cases

Cases are opened automatically for high-risk assessment decisions and selected post-order events.

Case statuses:

```text
OPEN
ASSIGNED
IN_REVIEW
ESCALATED
RESOLVED
CLOSED
```

Case priority:

```text
LOW
MEDIUM
HIGH
CRITICAL
```

Assigning a case:

1. Open case detail.
2. Enter investigator identifier.
3. Add optional assignment note.
4. Submit.

Resolving a case:

1. Enter resolution.
2. Enter resolution reason.
3. Submit.

Vendor payouts remain held while related fraud cases are open.

## Rules

Rules are database-driven. Administrators can create, edit, activate, and deactivate rules without code changes.

Important fields:

| Field | Meaning |
|---|---|
| Rule code | Unique stable identifier |
| Rule type | Scoring, hard-block, COD control, vendor control, decision matrix, etc. |
| Signal code | Signal evaluated by the rule |
| Operator | Comparison operator |
| Comparison value | Value used by rule matcher |
| Score impact | Positive or negative score impact |
| Priority | Lower priority number runs earlier |
| Action | Decision/action to apply when matched |
| Hard block | Bypass scoring and apply action |
| Scope fields | Vendor, product, category, payment method, country, district, channel |

Default decision thresholds:

| Score | Risk Level | Default Action |
|---:|---|---|
| 0-29 | LOW | Approve |
| 30-59 | MEDIUM | Verification |
| 60-79 | HIGH | Manual review/hold |
| 80-100 | CRITICAL | Reject/block/prepaid depending on rules |

Rule safety guidance:

- Test new hard-block rules as scoring rules first when possible.
- Avoid blocking only because IP and shipping location differ.
- Prefer verification or manual review for ambiguous signals.
- Use negative score rules for trusted customers/devices carefully.
- Keep priorities consistent so hard-blocks are easy to reason about.

## Blocklist

Supported block types:

```text
CUSTOMER
ACCOUNT
MOBILE_NUMBER
EMAIL
DEVICE
IP_ADDRESS
ADDRESS
PAYMENT_TOKEN
BANK_ACCOUNT
VENDOR
PRODUCT
REFERRAL_CODE
```

Blocklist storage:

- Raw value is hashed before storage.
- Masked value is shown in admin pages.
- Active duplicate block entries are prevented by unique index.

When adding a block:

1. Choose block type.
2. Choose scope.
3. Enter raw value.
4. Set temporary expiry if needed.
5. Enter reason.
6. Save.

When deactivating a block:

1. Use blocklist status action.
2. Provide reason where supported.
3. Confirm audit event exists.

## COD Configuration For Bangladesh

Common keys:

| Key | Purpose |
|---|---|
| `fraud.cod.first_order_limit` | Maximum first COD order amount |
| `fraud.cod.rto_disable_threshold` | COD RTO count that disables COD |
| `fraud.cod.delivery_refusal_disable_threshold` | Delivery refusal count that disables COD |
| `fraud.cod.restore_after_prepaid_success_count` | Successful prepaid orders needed to restore COD |
| `fraud.cod.high_value_confirmation_threshold` | High-value COD verification threshold |
| `fraud.cod.high_risk_partial_prepayment_rto_count` | RTO count requiring partial advance |
| `fraud.cod.high_risk_partial_prepayment_refusal_count` | Refusal count requiring partial advance |

Operational COD rules:

- First COD order requires mobile OTP.
- High-value COD orders require confirmation or partial advance.
- Repeated RTO/refusal disables COD.
- Successful prepaid history can restore COD.
- Address change after confirmation requires re-verification.

## Vendor Fraud Configuration

Common keys:

| Key | Purpose |
|---|---|
| `fraud.vendor.sales_spike.window_hours` | Recent sales spike window |
| `fraud.vendor.sales_spike.order_count` | Spike threshold |
| `fraud.vendor.abnormal_refund_rate` | Refund risk threshold |
| `fraud.vendor.abnormal_cancel_rate` | Cancellation risk threshold |

Vendor risk actions:

- Hold payout.
- Open/assign case.
- Require shipment evidence.
- Require carrier confirmation.
- Place vendor under manual review.

Vendor-facing pages must not expose internal fraud rules, score details, or signal history.

## Webhook Configuration

Webhook secrets are stored as fraud configuration values and masked in list view.

```text
fraud.webhook.<provider>.secret
fraud.webhook.timestamp_tolerance_seconds
fraud.webhook.rate_limit.max_requests
fraud.webhook.rate_limit.window_seconds
```

Rotate secrets by:

1. Create the new provider secret.
2. Coordinate with provider.
3. Verify signed callback.
4. Deactivate old secret only after provider switch is confirmed.

## Rollback Steps

To safely roll back a risky rule/configuration change:

1. Deactivate the changed fraud rule.
2. Restore previous configuration value.
3. Review new open cases created since the change.
4. Reopen or approve false-positive assessments manually.
5. If COD was disabled incorrectly, restore customer profile or require successful prepaid restoration.
6. Keep audit and event logs intact.

For database rollback:

- Prefer deactivation over table drops.
- Preserve assessments, cases, review history, evidence, outbox, and event logs for audit.
- Stop scheduled outbox dispatch before any rollback that should prevent downstream notifications.

## Operational Checklist

Daily:

- Review manual review queue.
- Review open critical cases.
- Review vendor payout holds.
- Check outbox failures.
- Check top triggered rules.

Weekly:

- Audit hard-block rules.
- Review false positives.
- Tune COD thresholds.
- Review vendor-risk profiles.
- Rotate or validate webhook secret handling where needed.

Monthly:

- Archive old outbox rows according to retention policy.
- Review blocklist expiry.
- Review fraud loss prevented and financial exposure.
