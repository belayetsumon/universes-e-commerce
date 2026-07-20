# Fraud Testing Guide

Phase 14 automated tests are pending. This guide defines the test plan, scenarios, and manual smoke checks required to prove the fraud module safely handles order, payment, COD, post-order, vendor, security, idempotency, and outbox workflows.

## Test Layers

| Layer | Scope |
|---|---|
| Unit | Evaluators, scoring, rule matching, decision matrix, privacy helpers |
| Service | Assessment, review, case, COD, post-order, vendor profile, outbox, idempotency |
| Repository | Query methods, indexes, duplicate prevention, pagination/specifications |
| Controller MVC | Fraud admin pages, CSRF token, role access, form validation |
| REST API | Pending Phase 10 endpoints |
| Integration | Checkout/order/payment/shipment/refund/payout guards |
| Security | RBAC, CSRF, webhook HMAC, replay prevention, masking |
| Concurrency | Duplicate assessment/review/outbox/idempotency prevention |
| Performance | High-volume signal/rule/order assessment |

## Required Unit Tests

Signal evaluators:

- `CustomerHistorySignalEvaluator`
- `DeviceRiskSignalEvaluator`
- `NetworkRiskSignalEvaluator`
- `OrderVelocitySignalEvaluator`
- `PaymentRiskSignalEvaluator`
- `CodRiskSignalEvaluator`
- `AddressRiskSignalEvaluator`
- `PromotionAbuseSignalEvaluator`
- `ReferralAbuseSignalEvaluator`
- `VendorRiskSignalEvaluator`

Rule/scoring:

- Hard-block rules bypass score calculation.
- Scoring rules add/subtract configured score.
- Negative trust rules cannot override hard-block rules.
- Decision thresholds produce LOW/MEDIUM/HIGH/CRITICAL.
- Payment method, vendor, category, country, district, and channel scopes match correctly.

Security/privacy:

- `FraudPrivacySupport` masks emails, mobiles, generic IDs, and payment tokens.
- Redaction removes card number, CVV, password, and payment token fields.
- Webhook HMAC validation accepts valid signature.
- Webhook validation rejects invalid signature, missing timestamp, expired timestamp, and duplicate idempotency key.

## Required Service Tests

Assessment:

- Legitimate new customer approved or verification-only.
- High-value first order receives elevated score.
- Multiple accounts from one device triggers risk.
- Blacklisted address hard-blocks or holds.
- Payment-provider high-risk result hard-blocks.
- Trusted customer and trusted device reduce score.
- Idempotency key returns existing assessment.

Manual review:

- Approve changes assessment to `APPROVED`.
- Reject changes assessment to `FRAUD_REJECTED`.
- Request verification changes assessment to `VERIFICATION_REQUIRED`.
- Review history is saved.
- Review reason is required.
- Outbox event is created.

Cases:

- High-risk assessment opens case.
- Case assignment updates status and event log.
- Resolution requires resolution and reason.
- Vendor payout guard blocks while open case exists.

COD:

- First COD order requires mobile OTP.
- First COD order over limit is blocked.
- Repeated RTO disables COD.
- Repeated delivery refusal disables COD.
- High-risk COD requires partial prepayment.
- Successful prepaid order count restores COD.
- Address change after confirmation requires re-verification.

Post-order:

- Shipment created records event.
- Delivery refusal opens/updates case.
- Return-to-origin updates COD profile.
- Chargeback opens critical case.
- Refund/reward/cashback/wallet/vendor payout release is blocked while open case exists.

Vendor fraud:

- Vendor self-purchase increments profile risk.
- Vendor/customer shared mobile/address/bank account triggers risk.
- Tracking-number reuse records event and holds payout.
- Delivery confirmed without carrier verification records fake-delivery suspicion.
- Abnormal refund/cancel rates increase vendor profile risk.
- Vendor payout is held while related case is open.

Outbox/idempotency:

- Duplicate outbox idempotency key creates one row.
- Failed outbox event retries with backoff.
- Published event is marked `PUBLISHED`.
- Duplicate post-order event idempotency key is ignored.
- Concurrent evaluation with same idempotency key creates one completed record.

## Required MVC Security Tests

Use MockMvc once tests are added:

- Unauthenticated `/admin/fraud/dashboard` redirects to login.
- Customer/vendor authorities are denied.
- Fraud analyst can view assessments.
- Fraud analyst cannot create rules or blocklist entries.
- Fraud admin can create rules and blocklist entries.
- POST without `_fraudCsrfToken` returns 403.
- POST with valid fraud CSRF token succeeds.
- Configuration list masks secret values.

## Required REST Tests After Phase 10

Endpoints:

```text
POST /api/fraud/assessments
GET /api/fraud/assessments/{id}
POST /api/fraud/assessments/{id}/review
GET /api/fraud/cases
POST /api/fraud/cases/{id}/assign
GET /api/fraud/rules
POST /api/fraud/rules
GET /api/fraud/blocklist
POST /api/fraud/blocklist
GET /api/fraud/dashboard
GET /api/fraud/reports
```

Assertions:

- Validation errors return `FraudApiErrorResponse`.
- Correlation ID is returned.
- Idempotency key deduplicates mutating calls.
- Pagination metadata is correct.
- Sorting and filtering are applied.
- Unauthorized/forbidden responses are correct.

## Manual Smoke Test

Minimum manual validation after deployment:

1. Login as fraud admin.
2. Open `/admin/fraud/dashboard`.
3. Open assessments list.
4. Open an assessment detail.
5. Submit manual review with missing reason and confirm validation failure.
6. Submit manual review with reason.
7. Create a blocklist entry and confirm masked display.
8. Create a rule and deactivate/reactivate it.
9. Create/update configuration with a key containing `secret` and confirm list display is masked.
10. Assign and resolve a case.
11. Confirm outbox rows are created and scheduled dispatcher marks them published.
12. Try admin fraud POST without `_fraudCsrfToken` and confirm 403.

## Performance Tests

Recommended baselines:

- 10,000 assessments with dashboard metrics.
- 100,000 signals with assessment detail lookup.
- 100,000 outbox events with due-event query.
- 50,000 blocklist rows with active block lookup.
- 10,000 vendor profiles with report sorting.

Performance acceptance:

- Blocklist lookup must use `(block_type, hashed_value)`.
- Assessment list must page, not load all rows.
- Outbox dispatcher must batch and avoid unbounded loads.
- Dashboard/report queries must be reviewed before production-scale reporting.

## Test Data

Required scenarios:

- Legitimate new customer.
- High-value first order.
- Multiple accounts from one device.
- Repeated COD rejection.
- Coupon abuse.
- Self-referral.
- Payment-provider high-risk result.
- Trusted customer.
- Trusted device.
- Blacklisted address.
- Vendor/customer collusion.
- Duplicate tracking number.
- Manual review approval.
- Manual review rejection.
- Concurrent fraud evaluations.

## Current Proof Boundary

Completed proof:

- Parent-project Maven compile has passed after Phases 8, 9, 11, 12, and 13.

Pending proof:

- Automated tests.
- Runtime browser/admin role tests.
- REST controller tests after Phase 10.
- High-volume performance tests.
- Real provider webhook tests.
