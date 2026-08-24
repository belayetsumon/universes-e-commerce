# COD Mobile Verification and Fraud Production Runbook

## Release Scope

This release controls Cash on Delivery at the server boundary. It covers:

- separate guest and registered-customer mobile OTP switches;
- verification bound to the authenticated user and current normalized mobile number;
- COD availability, aggregate checkout limits, per-vendor limits, and fraud holds;
- replay-safe order submission and concurrent velocity accounting;
- CSRF, admin authorization, secret-free runtime configuration, and audited database migrations.

The browser is never the authority for mobile-verification, COD-eligibility, order totals, customer identity, or fraud decisions.

## Required Runtime Configuration

Copy `.env.example` into the deployment secret manager. Do not deploy a plaintext `.env` file with the application artifact.

Required values:

- `SPRING_PROFILES_ACTIVE=live`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- production mail/SMS provider credentials used by the communication module
- `STORAGE_ROOT_PATH`

TLS must terminate at the application or a trusted reverse proxy. The proxy must replace, not append untrusted forwarded headers. Production cookies are Secure and HttpOnly.

## Database Upgrade

1. Take and verify a restorable database backup.
2. Restore the backup into staging and run the application migrations there first.
3. For the first Flyway adoption of an existing non-empty database only, set `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true`. Confirm the configured baseline version before starting exactly one application instance.
4. After the baseline is recorded, set `SPRING_FLYWAY_BASELINE_ON_MIGRATE=false` permanently.
5. Deploy with `spring.jpa.hibernate.ddl-auto=validate`; never use `update`, `create`, or `create-drop` in production.
6. Confirm Flyway validation succeeds before allowing traffic.

The registered-mobile migration intentionally invalidates legacy `mobile_verified` flags that have no verification timestamp and mobile snapshot. Affected customers must verify again. This is a security migration, not data loss.

## Recommended Settings

Start with:

- Cash on Delivery: enabled only after the SMS provider and fraud operations are ready;
- guest mobile required: enabled;
- guest mobile OTP verification: enabled;
- registered-customer COD mobile verification: enabled;
- OTP expiry: 5 minutes;
- maximum attempts: 5;
- resend cooldown: 60 seconds;
- daily send limit: 5 per mobile/account, with the stricter server-side IP, device, and session caps retained;
- automatic order confirmation: disabled for COD and fraud-held orders.

Turning either OTP switch off is an explicit business-risk decision. It changes only that customer type's verification gate; it must not mark a mobile as verified.

## Provider Readiness

Before enabling COD:

- prove that the SMS adapter reports only accepted `SENT` or `QUEUED` dispatches as success;
- prove provider errors return a generic customer message and leave no usable OTP;
- confirm OTP values never appear in application, proxy, APM, or provider-debug logs;
- configure provider timeouts, retry policy, delivery receipts, spend alerts, and credential rotation;
- alert on dispatch failure rate, delivery latency, and rate-limit spikes.

## Release Acceptance Matrix

Run every scenario against the production database engine in staging:

| Scenario | Expected result |
| --- | --- |
| Registered customer, current mobile unverified, COD selected | Order blocked; OTP UI available |
| Registered customer verifies OTP | Verification snapshot and timestamp saved; COD may proceed subject to fraud |
| Registered customer changes mobile | Prior verification immediately invalidated |
| Registered OTP switch disabled | COD does not require registered OTP; no verified flag is fabricated |
| Guest OTP switch enabled | Guest COD blocked until the server-side guest session is verified |
| Guest OTP switch disabled | Guest COD may proceed without OTP; session remains unverified |
| Global COD disabled | COD hidden in UI and rejected by the server |
| Aggregate checkout exceeds COD limit | Whole checkout rejected even if each vendor subtotal is below the limit |
| One vendor/order receives a fraud hold | No child order reserves stock, captures payment, or enters fulfilment |
| Same checkout submitted twice or concurrently | One checkout outcome; no duplicate order, stock reservation, incentive, or referral |
| Wrong/expired/replayed OTP | Generic rejection; attempt/expiry state enforced |
| SMS dispatch fails or throws | Generic service-unavailable response; OTP cannot be verified |
| Blocklisted mobile, IP, or device | Checkout rejected before normal processing |
| High/manual-review fraud decision | Audited held order; payment and fulfilment blocked |

Also verify an authenticated admin can change the setting, a non-admin receives 403, and every protected POST without a valid CSRF token receives 403.

## Operational Monitoring

Dashboard and alert on:

- OTP requested, accepted, failed, verified, expired, blocked, and rate-limited counts;
- verification conversion and resend rates by provider, without exposing mobile numbers;
- COD denials by safe reason category;
- fraud decision/assessment latency and counts by risk level;
- held or rejected order age and manual-review backlog;
- duplicate checkout/idempotency conflicts and stale processing claims;
- velocity-counter conflicts or update failures;
- fraud outbox pending age, retry count, and dead-letter/failure count;
- payment/fulfilment guard denials for non-approved assessments.

Retain fraud evidence and OTP metadata only for an approved business/legal period. Restrict database access and never export raw OTP hashes, device identifiers, or unmasked customer identifiers to analytics.

## Incident Controls

- SMS outage: disable both OTP-dependent COD paths or disable COD globally; do not bypass verification by editing users.
- Fraud service degradation: fail closed for COD/payment/fulfilment and route held orders to manual review.
- Suspected credential leak: disable affected integration, rotate the credential in the secret manager, revoke the old credential, and review dispatch/access logs.
- Duplicate-order signal: disable checkout mutation traffic, preserve idempotency/order evidence, and reconcile stock/payment before reopening.

## Rollback

The migrations are additive. Prefer rolling back the application artifact while retaining the new columns and indexes. Do not restore legacy `mobile_verified` flags and do not delete fraud/idempotency evidence during rollback. If business continuity requires it, disable COD globally until the corrected artifact has passed the acceptance matrix.
