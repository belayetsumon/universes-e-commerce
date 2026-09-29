# Fraud Security And Privacy Guide

This guide documents the fraud module security controls implemented through Phase 13.

## Access Control

Global route protection exists in `SecurityConfig`:

```text
/admin/fraud/**
/api/fraud/**
```

Method-level access exists in `FraudAdminController` through `@PreAuthorize` and `FraudPermissions`.

Permission groups:

| Group | Authorities |
|---|---|
| Read | `admin`, `fraud-admin`, `fraud-supervisor`, `fraud-analyst`, `finance`, and matching `ROLE_*` values |
| Review | `admin`, `fraud-admin`, `fraud-supervisor`, `fraud-analyst`, and matching `ROLE_*` values |
| Admin | `admin`, `fraud-admin`, `ROLE_ADMIN`, `ROLE_FRAUD_ADMIN` |

Rule/configuration/blocklist changes require admin-level fraud permission.

## CSRF Protection

The legacy global Spring CSRF setting is currently disabled. To avoid a broad platform change, the fraud module has module-scoped CSRF protection:

- `FraudCsrfTokenService`
- `FraudAdminCsrfInterceptor`
- `FraudSecurityWebConfig`
- `FraudAdminSecurityAdvice`

All fraud admin unsafe methods require token validation:

```text
POST
PUT
PATCH
DELETE
```

Templates include:

```html
<input type="hidden" th:name="${fraudCsrfParameterName}" th:value="${fraudCsrfToken}">
```

Header alternative:

```text
X-FRAUD-CSRF-TOKEN
```

## Webhook Security

Implemented service:

```text
FraudWebhookSecurityService
```

Controls:

- Provider secret lookup from fraud configuration.
- HMAC-SHA256 signature verification.
- Timestamp tolerance window.
- Replay prevention with `fraud_idempotency_records`.
- Provider-level rate limiting.
- Payload redaction before idempotency response storage.

Required provider config:

```text
fraud.webhook.<provider>.secret
```

Optional config:

```text
fraud.webhook.timestamp_tolerance_seconds
fraud.webhook.rate_limit.max_requests
fraud.webhook.rate_limit.window_seconds
```

Signature payload:

```text
<timestamp>.<payloadJson>
```

## Privacy And Sensitive Data

Implemented helper:

```text
FraudPrivacySupport
```

Protections:

- Hash identifiers with SHA-256.
- Mask email, mobile, numeric IDs, and generic identifiers.
- Mask payment tokens as `token_****1234`.
- Redact JSON fields named `cardNumber`, `cvv`, `password`, and `paymentToken`.

Do not store raw card data in fraud tables.

## Payment Token Handling

Implemented service:

```text
DefaultPaymentRiskService
```

Payment provider risk results store:

- Provider name.
- Provider risk status.
- Provider risk score.
- Hashed payment token.
- Provider reference.
- Redacted metadata JSON.

Payment token block checks compare hashed token values against `fraud_blocklist`.

## Blocklist Privacy

Blocklist records store:

| Column | Meaning |
|---|---|
| `hashed_value` | SHA-256 hash of raw identifier |
| `masked_value` | Safe display value |
| `reason` | Admin/investigation reason |
| `scope` | Global/vendor/customer/etc. scope |
| `expires_at` | Optional temporary block expiry |

Never expose `hashed_value` or raw submitted values in UI or API responses.

## Configuration Secrets

Fraud configuration list view masks values when config keys contain:

```text
secret
token
password
key
```

Administrators may still edit configuration values through the edit flow if authorized.

## Audit Logging

Audit records are written as fraud event logs for:

- Assessments.
- Review decisions.
- Rule changes.
- Configuration changes.
- Blocklist changes.
- COD disable events.
- Cases.

Audit metadata for blocklist/configuration changes is redacted and avoids raw sensitive values.

## Vendor Privacy Boundary

Vendor users must not access internal fraud pages or APIs.

Vendor-safe fraud features should expose only:

- Payout hold status.
- Evidence request status.
- Shipping confirmation requirement.
- Case-safe support messaging.

Vendor-facing features must not expose:

- Internal fraud score.
- Rule execution details.
- Signal values.
- Related-account intelligence.
- Customer device/IP/payment intelligence.

## Operational Security Checklist

Before production deployment:

- Confirm fraud authorities exist in role/privilege seed data.
- Confirm `/admin/fraud/**` denies ordinary customer/vendor users.
- Confirm all fraud admin POST forms include `_fraudCsrfToken`.
- Configure webhook provider secrets.
- Confirm sensitive configuration list view masks secrets.
- Confirm payment tokens are stored hashed only.
- Confirm no raw card, CVV, or password values are logged or persisted by fraud code.
- Confirm outbox failures do not expose sensitive payloads in logs.

## Known Boundaries

- Full REST controller method annotations will be completed in Phase 10 when controllers exist.
- Automated security tests are pending Phase 14.
- Browser/session authorization checks were not run during source implementation.
