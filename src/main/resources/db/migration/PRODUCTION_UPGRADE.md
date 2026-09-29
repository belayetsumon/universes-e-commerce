# COD, fraud, and image settings: production upgrade

This migration set upgrades an existing Universes Ecommerce database. It is not
a greenfield schema initializer. `V202608230001` deliberately fails before any
mutation when the approved legacy tables are absent.

## Supported rollout target

- MySQL 8.0.16 or newer using InnoDB.
- The fixed production migration location is
  `spring.flyway.locations=classpath:db/migration/mysql`.
- Flyway history must either already contain the approved application baseline,
  or be baselined once at version `202608230000` after an operator has verified
  the existing schema. Never enable `baseline-on-migrate` as an unattended
  production default.

## Before migration

1. Back up the database and prove a restore into an isolated environment.
2. Run the release against a recent production-size copy, including the device
   and velocity data-normalization steps in `V202608240002`.
3. Confirm there are no unresolved Flyway checksum or failed-migration entries.
4. Drain checkout writers and deploy a single application instance first.
   MySQL DDL can implicitly commit and should run in a maintenance window.
5. Configure the real SMS provider, database credentials, application secrets,
   HTTPS/proxy headers, and production profile through the environment. Do not
   use the development/logging OTP provider in production.
6. Use a drained blue/green rollout. This release writes canonical Bangladesh
   mobile hashes while retaining compatibility reads; a pre-compatibility node
   cannot see those new writes and must not remain in service. Roll back only to
   a release that retains the compatibility reads.

## Migration

Keep `SPRING_FLYWAY_BASELINE_ON_MIGRATE=false` for a previously managed
database, then start the canary with Flyway enabled. The expected ordered chain
is:

1. `V202608230001__cod_fraud_schema_preflight.sql`
2. `V202608240001__registered_cod_mobile_verification.sql`
3. `V202608240002__checkout_idempotency_and_fraud_context.sql`
4. `V202608240003__cod_fraud_postflight_guards.sql`
5. `V202608240004__transient_security_data_retention_indexes.sql`
6. `V202608260005__fraud_blocklist_invariants.sql`
7. `V202608260006__image_upload_settings.sql`
8. `V202608270001__rename_reserved_value_columns.sql`
9. `V202608270002__product_availability_mode_invariant.sql`

Do not edit an applied migration. Add a new forward migration for every later
change.

## Post-migration validation

Verify all nine versions succeeded in `flyway_schema_history`, then check:

```sql
SELECT COUNT(*) FROM checkout_placement_attempt;
SELECT COUNT(*) FROM fraud_device_identities
WHERE identity_key IS NULL AND customer_id IS NOT NULL;
SELECT COUNT(*) FROM usermodule_users
WHERE mobile_verified = TRUE
  AND (mobile_verified_at IS NULL OR mobile_verified_number IS NULL);
SELECT COUNT(*) FROM fraud_blocklist
WHERE (temporary = TRUE AND expires_at IS NULL)
   OR (temporary = FALSE AND expires_at IS NOT NULL);
SELECT scope, COUNT(*) FROM fraud_blocklist
WHERE active = TRUE AND scope <> 'GLOBAL'
GROUP BY scope;
SELECT vendor_logo_max_file_size_bytes,
       product_featured_image_min_file_size_bytes,
       product_featured_image_max_file_size_bytes,
       product_featured_image_output_max_width,
       product_featured_image_output_max_height
FROM global_settings
WHERE id = 1;
SELECT COUNT(*) FROM product
WHERE (manage_stock = TRUE AND allow_preorder = TRUE)
   OR (manage_stock = FALSE AND allow_preorder = FALSE)
   OR manage_stock IS NULL
   OR allow_preorder IS NULL;
```

Use `1`/`0` for boolean values if the MySQL client does not accept
`TRUE`/`FALSE`. The mobile-proof and ambiguous expiry queries must return zero. Active non-global blocks are deliberately inert
in this release because their target contract is not implemented; convert them
to reviewed GLOBAL entries or deactivate them before relying on enforcement. An
empty checkout-attempt table is valid before traffic.

Run signed-in and guest acceptance checks through the HTTPS entry point:

- registered COD can be enabled/disabled by an authorized administrator;
- verified mobile proof is bound to the current normalized mobile number;
- OTP delivery is direct-only, never queued, and provider failure yields no
  usable verification token;
- resend, attempt, expiry, daily, device, session, IP, and mobile limits fail
  closed; the multi-dimensional send reservation is all-or-none;
- changing a mobile number invalidates the prior proof;
- client-supplied shipping/packaging prices cannot alter the order total;
- repeated placement with the same request ID creates one order group;
- the same request ID with a different payload is rejected;
- blocked customer, vendor, device, mobile, address, district, sibling order,
  and payment/fulfilment cases remain blocked;
- prepaid checkout remains available where policy permits it.

Run concurrency tests against the selected MySQL 8 production database:

- parallel OTP requests sharing a mobile, account, IP, device, or session must
  never exceed a limit (a single anonymous database mutex serializes the short
  reservation transaction, and one-second buckets are portable to MySQL);
- simultaneous placement requests with the same idempotency key must create one
  order group;
- load-test the OTP reservation mutex at expected peak traffic and alert on lock
  wait/timeout growth.

## Mobile hash compatibility and secrets

New mobile block/profile/velocity writes use normalized `8801...` values.
Reads cover the canonical and common historical `+880`, `00880`, `01`, and bare
`1` representations. Hash-only rows cannot be normalized with SQL and arbitrary
historical punctuation cannot be recovered. Rebuild or reconcile durable mobile
blocklist and COD-risk rows from authoritative user/order/OTP data or controlled
administrator re-entry, audit unmatched rows, and let legacy velocity buckets
age out beyond the maximum lookback plus cleanup lag before removing compatibility
reads.

Provide fraud webhook secrets through `fraud.webhook.<provider>.secret` or
`FRAUD_WEBHOOK_<PROVIDER>_SECRET`. The admin form treats sensitive configuration
as write-only, but production provider/carrier credentials still require an
approved external secret manager or encrypted-at-rest database control and a
rotation test. The trusted reverse proxy must replace forwarded headers, direct
application ingress must be blocked, and secure cookies/HTTPS must be verified
at the public entry point.

## Retention and recovery

`CheckoutPlacementAttemptMaintenanceService` recovers stale processing claims
and deletes old terminal attempts. Production defaults are configurable with:

- `CHECKOUT_IDEMPOTENCY_STALE_GRACE_MINUTES` (default 15, minimum 5)
- `CHECKOUT_IDEMPOTENCY_RETENTION_DAYS` (default 30, minimum 1)
- `CHECKOUT_IDEMPOTENCY_CLEANUP_CRON` (default daily at 03:15 server time)

Set retention to match the privacy policy, incident-response window, and order
retry/support requirements. Monitor stale recoveries, cleanup failures, OTP
provider failures, throttling, rejected idempotency replays, and fraud blocks.

Transient OTP/fraud cleanup is bounded by `FRAUD_RETENTION_BATCH_SIZE` and
`FRAUD_RETENTION_MAX_BATCHES` and rechecks each row at deletion time. Defaults
retain OTP rows for 30 days, velocity buckets for 7 days, and published outbox
events for 30 days. Pending/failed outbox events and legal/audit fraud records
are never deleted by this job.

Legacy COD OTP communication jobs are purged and OTP provider responses are
redacted. The velocity table contains one permanent anonymous mutex row; all
identifier-derived OTP buckets remain subject to the configured velocity
retention.

## Rollback

`V202608240002` canonicalizes device identifiers and deduplicates velocity
buckets; those data changes are not safely reversible. Canonical mobile writes
are also invisible to pre-compatibility code. Roll back application code only
to a schema-compatible dual-read release and leave the additive schema in
place. If an incompatible rollback is unavoidable, restore the pre-migration
backup and reconcile every order accepted after that backup before reopening
checkout.
