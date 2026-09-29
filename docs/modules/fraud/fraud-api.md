# Fraud API Guide

Status: Phase 15 documentation complete. Phase 10 REST controllers are still pending, so this guide documents the target API contract, DTOs already present in the source tree, security requirements, idempotency rules, and implementation boundaries.

## Current Implementation State

Implemented source support:

- DTOs under `main/java/com/ecommerce/app/module/fraud/dto`.
- Service layer for assessment, review, cases, rules, blocklist, dashboard data, post-order monitoring, outbox, idempotency, webhook security, and privacy helpers.
- Route-level protection for `/api/fraud/**` in `SecurityConfig`.
- API error DTO `FraudApiErrorResponse`.

Pending Phase 10 work:

- REST controllers under `main/java/com/ecommerce/app/module/fraud/api`.
- Pagination and sorting response wrappers.
- Controller-level validation, correlation ID handling, and exception advice.
- OpenAPI/Swagger annotations or generated API docs.

## Authentication And Authorization

All `/api/fraud/**` endpoints must be authenticated.

Allowed authorities for read access:

```text
admin
fraud-admin
fraud-supervisor
fraud-analyst
finance
ROLE_ADMIN
ROLE_FRAUD_ADMIN
ROLE_FRAUD_SUPERVISOR
ROLE_FRAUD_ANALYST
ROLE_FINANCE
```

Mutating endpoints must use narrower permissions:

| Operation | Minimum Access |
|---|---|
| Assessment review, approve, reject, verification request | Fraud analyst, supervisor, fraud admin, admin |
| Rule create/update/status | Fraud admin or admin |
| Blocklist create/delete/status | Fraud admin or admin |
| Case assignment/resolution | Fraud analyst, supervisor, fraud admin, admin |
| Reports/dashboard read | Fraud read or finance access |

Vendor users must not receive internal signal, rule, or score details through fraud APIs. Vendor-safe fraud status APIs should be implemented separately when vendor-facing surfaces are added.

## Headers

Recommended headers for all mutating API calls:

| Header | Required | Purpose |
|---|---|---|
| `Authorization` | Yes | Session, bearer token, or current platform API authentication |
| `X-Correlation-ID` | Strongly recommended | Links request, fraud event log, outbox event, and downstream logs |
| `Idempotency-Key` | Yes for mutating operations | Prevents duplicate reviews, evaluations, callbacks, payout holds, and value-release events |
| `Content-Type: application/json` | Yes | JSON request body |

The service layer already supports idempotency keys through `fraud_idempotency_records` and assessment/post-order idempotent lookups.

## Target Endpoints

### Assessments

```text
POST   /api/fraud/assessments
GET    /api/fraud/assessments/{id}
GET    /api/fraud/assessments/order/{orderId}
POST   /api/fraud/assessments/{id}/review
POST   /api/fraud/assessments/{id}/approve
POST   /api/fraud/assessments/{id}/reject
POST   /api/fraud/assessments/{id}/request-verification
```

Primary DTOs:

- Request: `FraudAssessmentCreateRequest`
- Review request: `FraudAssessmentReviewRequest`
- Response: `FraudAssessmentResponse`
- Search filter: `FraudAssessmentSearchFilter`

Example assessment request:

```json
{
  "orderId": 1001,
  "correlationId": "checkout-1001-20260718",
  "idempotencyKey": "ORDER-1001-FRAUD-ASSESSMENT",
  "context": {
    "paymentMethod": "COD",
    "shippingCountry": "Bangladesh",
    "district": "Dhaka",
    "channel": "WEB",
    "metadata": {
      "mobileNumber": "+8801XXXXXXXXX",
      "deviceIdentifier": "device-abc",
      "ipAddress": "203.0.113.10"
    }
  }
}
```

Expected response:

```json
{
  "id": 15,
  "uuid": "assessment-uuid",
  "orderId": 1001,
  "customerId": 2001,
  "vendorId": 301,
  "riskScore": 65,
  "riskLevel": "HIGH",
  "decision": "MANUAL_REVIEW",
  "status": "MANUAL_REVIEW",
  "decisionReason": "Fraud assessment completed.",
  "automaticDecision": true,
  "manualReviewRequired": true,
  "evaluatedAt": "2026-07-18T14:30:00",
  "signals": []
}
```

### Cases

```text
GET    /api/fraud/cases
GET    /api/fraud/cases/{id}
POST   /api/fraud/cases/{id}/assign
POST   /api/fraud/cases/{id}/resolve
```

Primary DTOs:

- `FraudCaseAssignRequest`
- `FraudCaseResolveRequest`
- `FraudCaseResponse`

All case actions require a reason or note where applicable and must create audit/event records.

### Rules

```text
GET    /api/fraud/rules
POST   /api/fraud/rules
PUT    /api/fraud/rules/{id}
PATCH  /api/fraud/rules/{id}/status
```

Primary DTOs:

- `FraudRuleRequest`
- `FraudRuleResponse`

Rules are evaluated by active status and priority. Rule code must remain unique.

### Blocklist

```text
GET    /api/fraud/blocklist
POST   /api/fraud/blocklist
DELETE /api/fraud/blocklist/{id}
```

Primary DTOs:

- `FraudBlocklistRequest`
- `FraudBlocklistResponse`

Raw block values must never be returned. Store `hashed_value`, display `masked_value`.

### Dashboard And Reports

```text
GET    /api/fraud/dashboard
GET    /api/fraud/reports
```

Primary DTOs:

- `FraudDashboardFilter`
- `FraudDashboardResponse`
- `FraudMetricRow`
- `FraudAdminMetric`
- `FraudAdminReportRow`

## Webhooks

Webhook DTO:

- `FraudWebhookRequest`

Security service:

- `FraudWebhookSecurityService`

Required webhook protections:

- HMAC-SHA256 signature validation.
- Timestamp validation using configured tolerance.
- Replay prevention using idempotency records.
- Provider-level rate limiting.
- Sensitive payload redaction before persistence.

Recommended webhook headers:

```text
X-Fraud-Signature: sha256=<hex-hmac>
X-Fraud-Timestamp: <epoch-seconds>
Idempotency-Key: <provider-event-id>
X-Correlation-ID: <provider-correlation-id>
```

Signature payload:

```text
<timestamp>.<payloadJson>
```

Provider secret configuration key:

```text
fraud.webhook.<provider>.secret
```

## Error Response

Use `FraudApiErrorResponse` for consistent errors:

```json
{
  "timestamp": "2026-07-18T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Review reason is required.",
  "path": "/api/fraud/assessments/15/review",
  "correlationId": "checkout-1001-20260718"
}
```

Recommended status codes:

| Code | Use |
|---:|---|
| 200 | Successful read or idempotent duplicate returning existing result |
| 201 | Created resource |
| 202 | Accepted async webhook or outbox-backed operation |
| 400 | Validation error |
| 401 | Not authenticated |
| 403 | Authenticated but not allowed |
| 404 | Resource not found |
| 409 | Idempotency key reused with different payload or duplicate active block |
| 429 | Rate limit exceeded |
| 500 | Unexpected server error |

## Pagination And Filtering

List endpoints should support:

```text
page
size
sort
q
fromDate
toDate
vendorId
customerId
orderId
riskLevel
decision
status
caseStatus
blockType
ruleType
active
```

Default page size should be `20`, matching the admin MVC page size.

## Idempotency Rules

Mutating APIs must use `Idempotency-Key`.

Recommended operation scopes:

| Endpoint | Scope |
|---|---|
| `POST /api/fraud/assessments` | `FRAUD_ASSESSMENT_EVALUATE` |
| review/approve/reject/request-verification | `FRAUD_ASSESSMENT_REVIEW` |
| post-order event APIs | `FRAUD_POST_ORDER_EVENT` |
| webhooks | `FRAUD_WEBHOOK:<provider>` |

If the same key is reused with a different request hash, return `409 Conflict`.

## Implementation Checklist For Phase 10

- Add REST controllers under `module.fraud.api`.
- Add `@Validated` request DTO validation.
- Add `@PreAuthorize` annotations using `FraudPermissions`.
- Add exception handler for `FraudException`, validation errors, access denied, idempotency conflicts, and unknown errors.
- Add correlation ID propagation into DTOs, service context, event log, and response headers.
- Add endpoint tests after controllers exist.
