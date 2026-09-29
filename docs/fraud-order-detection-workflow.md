# Fraud Order Detection And Prevention Module Workflow

This file tracks the phased design and implementation of an enterprise-grade fraud order detection and prevention module for the single-vendor and multi-vendor ecommerce platform.

The workflow follows `docs/AGENTS.md`: clean layered Spring design, service-layer transactions, database-backed configuration, auditability, security separation, idempotent payment/order operations, outbox-ready events, pagination for large admin surfaces, and Bootstrap 5 Thymeleaf UI.

## Current Goal

Create one centralized fraud module that can:

1. Evaluate fraud risk before order creation, immediately after order creation, and during post-order operations.
2. Assign a configurable risk score from `0` to `100` to every assessed order.
3. Apply hard-block rules before scoring when confirmed fraud is detected.
4. Decide whether to approve, verify, manually review, hold, reject, cancel, block, disable COD, hold rewards, hold refunds, or hold vendor payouts.
5. Prevent payment capture, vendor acceptance, packing, shipment creation, fulfilment, refund release, reward release, and vendor payout while fraud verification is pending.
6. Support customer fraud, device/network fraud, promotion abuse, COD fraud, payment fraud, refund fraud, referral fraud, and vendor-side fraud.
7. Store complete assessment, rule execution, signal, review, evidence, event, and audit history.
8. Let administrators configure rules, score impacts, thresholds, decision matrices, COD limits, and blocklists without source-code changes.
9. Publish fraud events reliably using the outbox pattern and idempotency keys.
10. Provide REST APIs and admin Thymeleaf screens for dashboard, assessments, manual review, rules, blocklists, cases, and reports.

## Status Legend

- `Done`: already exists or completed in this workflow
- `In Progress`: currently being developed
- `Pending`: not started
- `Blocked`: waiting for decision, dependency, or migration approval
- `Deferred`: intentionally left for a later phase

## Current Status

Status: `In Progress`

Done:

- Read `docs/AGENTS.md`.
- Inspected existing order, payment, shipping, vendor payout, communication, referral, coupon, wallet, cashback, gift card, and admin template surfaces.
- Created this fraud workflow tracker.
- Completed Phase 1 foundation source files: fraud enums, DTOs, exceptions, service contracts, guard contracts, and signal evaluator contracts.
- Completed Phase 2 persistence source files: clean fraud JPA entities, repositories, PostgreSQL schema script, indexes script, seed rules, outbox persistence, and idempotency persistence.
- Completed Phase 3 signal collection source files: default fraud signal collector, shared evaluator helper, and independent customer, device, network, order velocity, payment, COD, address, promotion, referral, and vendor evaluators.
- Completed Phase 4 decision source files: configurable fraud rule matcher, hard-block engine, score adjustment, risk scoring, decision matrix, configuration service, and rule execution logger.
- Completed Phase 5 assessment/review source files: assessment orchestration, signal persistence, case creation, assignment and resolution, review history, event logs, evidence metadata handling, evidence file validation, and audit event recording.
- Completed Phase 6 checkout/order integration source files: pre-order fraud screening, post-order assessment creation, payment/refund guards, order status guards, and shipment creation guards.
- Completed Phase 7 Bangladesh COD controls source files: configurable COD eligibility, first-COD OTP enforcement, first-order COD limits, high-value COD controls, partial-prepayment requirements, customer/vendor/mobile/address/device/district COD profiles, RTO/refusal disablement, prepaid-success restoration, shipment COD outcome tracking, and COD disabled audit events.
- Completed Phase 8 post-order monitoring source files: idempotent post-order fraud event logging, shipment/delivery/RTO/refusal monitoring, cancellation/return/refund monitoring, cashback/wallet/gift-card/referral reward hold controls, vendor payout gates, chargeback event support, and high-risk case opening/updating.
- Completed Phase 9 multi-vendor fraud detection source files: vendor risk profile recalculation, self-purchase/collusion/shared-detail detection, tracking-number reuse attempt logging, fake-delivery suspicion events, abnormal vendor refund/cancellation/spike scoring, vendor payout hold escalation, and vendor fraud signal exposure for assessments.
- Completed Phase 11 admin dashboard and Thymeleaf UI source files: admin MVC controller, read-only/admin view service, dashboard metrics, assessment list/detail/manual review, case list/detail, rules list/form, blocklist list/form, configuration page, reports page, shared fraud admin CSS, and admin sidebar navigation.
- Completed Phase 12 events/outbox/notification/idempotency source files: durable fraud event publisher, scheduled outbox dispatcher, Spring application dispatch event, communication-module notification bridge, assessment/review/post-order idempotency records, and outbox wiring for assessments, reviews, cases, COD-disabled events, value holds, and vendor payout holds.
- Completed Phase 13 security/privacy source files: fraud authority constants, method-level RBAC, route-level fraud admin/API protection, fraud admin CSRF token interceptor, fraud form token wiring, webhook HMAC/timestamp/replay validation service, fraud rate limiter, centralized masking/redaction helpers, payment-token hashing service, masked configuration rows, and redacted audit metadata.
- Completed Phase 15 documentation source files: API guide, database design guide, admin configuration guide, security/privacy guide, and testing guide with current implementation boundaries, migration/rollback guidance, admin operations, security requirements, and Phase 10/14 pending-work notes.

Remaining:

- Create REST APIs and tests.
- Wire the module into REST APIs and test coverage.

## Workflow Completion Tracker

| Step | Status | What Is Complete | Remaining Work |
|---|---|---|---|
| Phase 0: Workflow and discovery | Done | `docs/AGENTS.md` reviewed; existing order, payment, shipping, vendor payout, communication, referral, coupon, wallet, cashback, gift card, audit, and admin layout surfaces mapped; workflow tracker created | Keep this tracker updated after every implementation phase |
| Phase 1: Foundation, enums, DTOs, and contracts | Done | Created fraud package foundation with 80 Java source files covering enums, DTOs, exceptions, service contracts, guard contracts, and named signal evaluator contracts; source-level package scan passed | Maven compile not run because Maven/wrapper is unavailable in this environment |
| Phase 2: Database design, entities, repositories, and migrations | Done | Created 19 JPA persistence tables as entities, 19 repositories, PostgreSQL init/index/seed scripts, transactional outbox entity/table, and idempotency entity/table; source-level package scan passed | Maven compile not run because Maven/wrapper is unavailable in this environment |
| Phase 3: Signal collection and strategy evaluators | Done | Created `DefaultFraudSignalCollector`, shared evaluator helper, and 10 independent Spring-managed evaluators for customer, device, network, order velocity, payment, COD, address, promotion, referral, and vendor risk; source-level package scan passed | Maven compile not run because Maven/wrapper is unavailable in this environment |
| Phase 4: Rule engine, scoring, and decision matrix | Done | Created shared rule matcher, configurable hard-block engine, scoring-rule score adjustment, risk score calculator, decision matrix service, configuration service, and rule execution logger; source-level package scan passed | Maven compile not run because Maven/wrapper is unavailable in this environment |
| Phase 5: Assessment, cases, manual review, and audit | Done | Created assessment orchestration, idempotent assessment lookup, signal persistence, rule execution logging hookup, case opening/assignment/resolution, manual review history, review events, evidence metadata service with file safety validation, and audit event recording; source-level package scan passed | Maven compile not run because Maven/wrapper is unavailable in this environment |
| Phase 6: Checkout and order integration | Done | Created fraud integration guard, shared hashing support, pre-order checkout blocklist screening, post-order assessment creation in both active checkout save flows, payment/refund blocking in `PaymentService`, order status blocking in `SalesOrderService`, and shipment creation blocking in `ShipmentService`; source-level package scan passed | Maven compile not run because Maven/wrapper is unavailable in this environment |
| Phase 7: COD fraud prevention for Bangladesh ecommerce | Done | Created `CodEligibilityService`, `CodRiskProfileService`, `DefaultCodRiskService`, COD profile repository extensions, first-order COD limit checks, first-COD mobile OTP enforcement, high-value COD confirmation/partial-advance controls, customer/vendor COD limits, multi-level RTO/refusal profile updates, COD disablement after repeated RTO/refusal, prepaid-success COD restoration, shipment outcome tracking, and COD disabled audit events; source-level package scan passed | Maven compile not run because Maven/wrapper is unavailable in this environment |
| Phase 8: Post-order monitoring | Done | Created `FraudPostOrderEventType`, `FraudPostOrderEventRequest`, `FraudPostOrderMonitoringService`, `DefaultFraudPostOrderMonitoringService`, idempotent `FraudEventLog` recording, open-case update/open handling for high-risk post-order events, payout guard implementation, shipment event hooks, order cancellation/return hooks, refund monitoring, cashback/wallet/gift-card/referral release holds, vendor payout gates, and chargeback recording support; Maven compile passed from the parent project | Runtime/browser flow testing not run |
| Phase 9: Multi-vendor fraud detection | Done | Added vendor risk profile columns, repository queries, `DefaultVendorRiskProfileService`, order/shipment/payout hooks, vendor fraud post-order events, case escalation, vendor evaluator signals, default vendor-control rules, indexes, seed thresholds, and an additive Phase 9 PostgreSQL migration; Maven compile passed from the parent project | Runtime/browser flow testing not run; suspicious product price changes, artificial review activity, rolling reserves, and vendor promotion suspension remain for later domain-specific expansion |
| Phase 10: REST APIs | Pending | Not started | Add `/api/fraud/**` endpoints with DTOs, validation, pagination, filtering, sorting, idempotency keys, and correlation IDs |
| Phase 11: Admin dashboard and Thymeleaf UI | Done | Added `FraudAdminController`, `FraudAdminViewService`, specification-backed paginated admin lists, dashboard/report metrics, assessment detail/manual review forms, case assign/resolve forms, rule CRUD/status forms, hashed blocklist UI, configuration save UI, shared Bootstrap admin styling, and Fraud Control sidebar navigation; Maven compile passed from the parent project | Runtime/browser flow testing not run; advanced charting and deeper product/category/district/payment analytics can be expanded after more reporting DTOs/API work |
| Phase 12: Events, outbox, notifications, and idempotency | Done | Added `DefaultFraudEventPublisher`, `DefaultFraudOutboxDispatcherService`, `FraudOutboxDispatchEvent`, `DefaultFraudIdempotencyService`, `DefaultFraudNotificationService`, communication fraud message event types, retryable outbox dispatch, event deduplication, idempotency records for assessment/review/post-order operations, and communication-module notification bridge; Maven compile passed from the parent project | Runtime dispatch/browser notification testing not run; external SMS/email/WhatsApp delivery still depends on configured communication providers |
| Phase 13: Security and privacy | Done | Added `FraudPermissions`, method-level fraud RBAC, `/admin/fraud/**` and `/api/fraud/**` route authorization, module-scoped admin CSRF token service/interceptor/advice, hidden fraud form tokens, `FraudWebhookSecurityService` with HMAC/timestamp/replay validation, `FraudRateLimitService`, `FraudPrivacySupport`, `DefaultPaymentRiskService`, masked fraud configuration listing, payment-token hashing, redacted webhook/payment/audit metadata, and parent-project Maven compile verification | Runtime role/session/browser authorization testing not run; full REST endpoint method annotations will be completed when Phase 10 APIs exist |
| Phase 14: Testing | Pending | Not started | Add evaluator, scoring, rule-engine, hard-block, COD, vendor, repository, controller, security, integration, concurrency, idempotency, webhook, and performance tests |
| Phase 15: Documentation and admin guide | Done | Added `docs/fraud-api.md`, `docs/fraud-database-design.md`, `docs/fraud-admin-configuration-guide.md`, `docs/fraud-security-and-privacy-guide.md`, and `docs/fraud-testing-guide.md`; guides cover target API contract, DTOs, validation/error shape, idempotency/correlation IDs, schema/migration/index/rollback notes, admin rules/config/blocklist/COD/vendor operations, security/privacy controls, and test plan | REST controllers remain pending in Phase 10 and automated tests remain pending in Phase 14 |

Immediate next step:

- Start Phase 10 by adding `/api/fraud/**` REST endpoints with request/response DTOs, validation, pagination, filtering, sorting, idempotency keys, correlation IDs, and consistent error responses.

## Existing Code Found During Initial Inspection

Status: `Done`

Relevant existing surfaces to reuse or gate:

- `main/java/com/ecommerce/app/module/order/model/SalesOrder.java`
  - Existing order aggregate includes `id`, `uuid`, `customer`, `vendorId`, `grandTotal`, order status, payment plan/state, mobile verification fields, shipping/billing addresses, payments, and audit fields.
- `main/java/com/ecommerce/app/module/order/controller/SalesOrderController.java`
  - Checkout and order creation flow already touches cart, guest/mobile OTP, shipping location, wallet, rewards, cashback, coupon, gift card, EMI, payment plan, stock ledger, and incentive usage.
- `main/java/com/ecommerce/app/module/order/services/PaymentService.java`
  - Payment summary and payment recording surface to gate for capture/cancel/idempotency.
- `main/java/com/ecommerce/app/module/shipping/services/ShipmentService.java`
  - Shipment creation already enforces duplicate shipment prevention, UUID idempotency for repeated creates, COD sync, unique tracking number, payment collection, and shipment status events.
- `main/java/com/ecommerce/app/module/shipping/model/Shipment.java`
  - Shipment/COD fields are the natural location for post-order COD RTO/refusal monitoring.
- `main/java/com/ecommerce/app/vendor/services/VendorPayoutService.java`
  - Vendor payout release must be blocked while related fraud cases are open.
- `main/java/com/ecommerce/app/module/ReferralRewards/services/PromotionFraudService.java`
  - Existing promotion fraud flagging should be bridged into centralized fraud signals instead of duplicated.
- `main/java/com/ecommerce/app/module/ReferralRewards/services/CheckoutIncentiveService.java`
  - Coupon, cashback, reward, wallet, and gift-card usage should feed pre-order and post-order fraud signals.
- `main/java/com/ecommerce/app/module/communication`
  - Existing communication module should be reused for fraud notifications where possible.
- `main/java/com/ecommerce/app/audit`
  - Existing audit support should be reused for entity audit fields and security-aware audit metadata.
- `main/resources/templates/admin-layout.html`
  - Admin fraud pages should use the existing shared admin layout.
- `main/resources/templates/admin-nav-left.html`
  - Add fraud navigation only after MVC pages exist.

Implementation note:

- Do not create a parallel checkout, payment, shipping, promotion, or vendor payout system.
- Fraud must be a central module with narrow service interfaces that existing flows call before performing risky actions.
- Entities must stay clean and free from business logic.

## Target Module Directory Structure

Status: `Pending`

Create:

```text
main/java/com/ecommerce/app/module/fraud/
  api/
  config/
  controller/
  dto/
  events/
  exception/
  model/
  repository/
  security/
  services/
    evaluator/
    impl/
  support/
  validation/

main/resources/templates/admin/fraud/
  dashboard.html
  assessments.html
  assessment-detail.html
  cases.html
  case-detail.html
  manual-review.html
  rules.html
  rule-form.html
  blocklist.html
  blocklist-form.html
  configuration.html
  reports.html

main/resources/db/
  fraud_module_init.sql
  fraud_module_indexes.sql
  fraud_module_seed_rules.sql

test/java/com/ecommerce/app/module/fraud/
  evaluator/
  services/
  controller/
  repository/
  integration/
```

Package rules:

- REST API controllers live under `module.fraud.api`.
- MVC controllers live under `module.fraud.controller`.
- JPA entities and enums live under `module.fraud.model`.
- Service interfaces live under `module.fraud.services`.
- Strategy evaluators live under `module.fraud.services.evaluator`.
- No business logic in entities or templates.

## Fraud Evaluation Workflow

Status: `Pending`

Required order gate:

```text
ORDER_CREATED
  -> FRAUD_EVALUATION_PENDING
  -> APPROVED
     or VERIFICATION_REQUIRED
     or MANUAL_REVIEW
     or FRAUD_HOLD
     or FRAUD_REJECTED
  -> PAYMENT_CAPTURED
  -> FULFILMENT_READY
```

Blocking statuses:

```text
FRAUD_EVALUATION_PENDING
VERIFICATION_REQUIRED
MANUAL_REVIEW
FRAUD_HOLD
FRAUD_REJECTED
```

These statuses must prevent:

- Payment capture
- Vendor acceptance
- Packing
- Shipment creation
- Fulfilment
- Refund release
- Reward release
- Cashback release
- Gift-card redemption completion where configured
- Vendor payout release

Core synchronous flow:

```java
@Transactional
public FraudDecisionResult evaluateOrder(Order order, FraudContext context) {
    List<FraudSignalResult> signals = fraudSignalCollector.collect(order, context);
    Optional<FraudDecisionResult> hardDecision = fraudRuleEngine.evaluateHardRules(signals, context);

    if (hardDecision.isPresent()) {
        return fraudAssessmentService.persistAndApply(order, signals, hardDecision.get(), context);
    }

    int score = fraudRiskScoringService.calculate(signals, context);
    FraudDecisionResult decision = fraudDecisionService.decide(order, score, signals, context);
    return fraudAssessmentService.persistAndApply(order, signals, decision, context);
}
```

Transaction rule:

- Critical local checks run inside the fraud evaluation transaction.
- External payment-provider, IP reputation, device intelligence, SMS, email, WhatsApp, and push calls must not run inside a long database transaction.
- External results are stored separately and joined into the final assessment when available.

## Phase 1: Foundation, Enums, DTOs, And Contracts

Status: `Done`

Verification:

- Source files created under `main/java/com/ecommerce/app/module/fraud`.
- Source-level package declaration scan passed for 80 fraud Java files.
- Maven compile was not run because Maven and a Maven wrapper are not available in this environment.

Create enums:

- `FraudAction`
- `FraudAssessmentStatus`
- `FraudBlockScope`
- `FraudBlockType`
- `FraudCasePriority`
- `FraudCaseStatus`
- `FraudDecision`
- `FraudEvaluationSource`
- `FraudEventType`
- `FraudReasonCode`
- `FraudRiskLevel`
- `FraudRuleOperator`
- `FraudRuleType`
- `FraudSignalCategory`
- `FraudSignalSeverity`
- `PaymentRiskProviderStatus`
- `VelocityCounterScope`

Required `FraudAction` values:

```text
ALLOW
APPROVE
VERIFY
REQUIRE_OTP
REQUIRE_PREPAID
REQUIRE_PARTIAL_PREPAYMENT
MANUAL_REVIEW
HOLD
BLOCK
REJECT
CANCEL
DISABLE_COD
HOLD_REFUND
HOLD_REWARD
HOLD_VENDOR_PAYOUT
TEMPORARILY_BLOCK_ACCOUNT
PERMANENTLY_BLOCK_ACCOUNT
```

Required reason codes:

```text
NEW_ACCOUNT_HIGH_VALUE
MOBILE_NOT_VERIFIED
EMAIL_NOT_VERIFIED
MULTIPLE_ACCOUNTS_SAME_DEVICE
MULTIPLE_ACCOUNTS_SAME_ADDRESS
MULTIPLE_ACCOUNTS_SAME_PAYMENT_METHOD
EXCESSIVE_ORDER_VELOCITY
EXCESSIVE_PAYMENT_ATTEMPTS
COD_RTO_HISTORY
DELIVERY_REFUSAL_HISTORY
PAYMENT_PROVIDER_HIGH_RISK
PAYMENT_COUNTRY_MISMATCH
ADDRESS_MISMATCH
REFERRAL_ABUSE
SELF_REFERRAL
CIRCULAR_REFERRAL
COUPON_ABUSE
CASHBACK_ABUSE
GIFT_CARD_ABUSE
WALLET_ABUSE
PROMOTION_STACKING
DEVICE_BLACKLISTED
MOBILE_BLACKLISTED
ADDRESS_BLACKLISTED
PAYMENT_TOKEN_BLACKLISTED
SUSPICIOUS_ADDRESS_CHANGE
SUSPICIOUS_ACCOUNT_CHANGE
CARD_TESTING
VENDOR_CUSTOMER_COLLUSION
TRACKING_NUMBER_REUSE
FAKE_DELIVERY
REFUND_COLLUSION
PAYOUT_RISK
```

Create DTOs:

- `FraudAssessmentCreateRequest`
- `FraudAssessmentResponse`
- `FraudAssessmentSearchFilter`
- `FraudAssessmentReviewRequest`
- `FraudBlocklistRequest`
- `FraudBlocklistResponse`
- `FraudCaseAssignRequest`
- `FraudCaseResolveRequest`
- `FraudCaseResponse`
- `FraudConfigurationRequest`
- `FraudContext`
- `FraudDashboardFilter`
- `FraudDashboardResponse`
- `FraudDecisionResult`
- `FraudEvidenceUploadRequest`
- `FraudRuleRequest`
- `FraudRuleResponse`
- `FraudSignalResult`
- `FraudWebhookRequest`

Acceptance criteria:

- DTOs use Bean Validation.
- Response DTOs mask sensitive fields.
- Request DTOs support correlation ID and idempotency key.
- No raw card data fields exist.

## Phase 2: Database Design, Entities, Repositories, And Migrations

Status: `Done`

Verification:

- Created 19 fraud persistence entities plus 2 persistence support enums under `main/java/com/ecommerce/app/module/fraud/model`.
- Created 19 Spring Data repositories under `main/java/com/ecommerce/app/module/fraud/repository`.
- Created PostgreSQL scripts:
  - `main/resources/db/fraud_module_init.sql`
  - `main/resources/db/fraud_module_indexes.sql`
  - `main/resources/db/fraud_module_seed_rules.sql`
- Schema script defines 19 fraud tables, including transactional outbox and idempotency records.
- Seed script defines 7 default configurations and 20 default fraud rules.
- Source-level package declaration scan passed for 121 fraud Java files.
- Maven compile was not run because Maven and a Maven wrapper are not available in this environment.

Create clean JPA entities:

- `FraudAssessment`
- `FraudSignal`
- `FraudRule`
- `FraudCase`
- `FraudBlocklist`
- `DeviceIdentity`
- `TrustedDevice`
- `CustomerRiskProfile`
- `VendorRiskProfile`
- `FraudReviewHistory`
- `FraudRuleExecution`
- `FraudEventLog`
- `FraudConfiguration`
- `VelocityCounter`
- `PaymentRiskResult`
- `CodRiskProfile`
- `FraudEvidence`
- `FraudOutboxEvent`
- `FraudIdempotencyRecord`

Entity requirements:

- Use explicit table and column names.
- Use `BigDecimal` for money.
- Use `LocalDateTime` or `Instant` for timestamps.
- Use `@Enumerated(EnumType.STRING)` for enums.
- Add `@Version` to admin-managed and decision-critical entities.
- Add created/modified audit fields.
- Keep sensitive values hashed, tokenized, encrypted, or masked where appropriate.
- Store JSON metadata as `TEXT` first unless the project already standardizes JSONB mapping.

Repository requirements:

- `JpaRepository` plus focused query methods only.
- Use `Pageable` for list screens and APIs.
- Add indexed filters for order ID, customer ID, vendor ID, risk level, decision, case status, block type, hashed value, active status, timestamps, district, payment method, and rule code.

Migration files:

- `main/resources/db/fraud_module_init.sql`
- `main/resources/db/fraud_module_indexes.sql`
- `main/resources/db/fraud_module_seed_rules.sql`

Acceptance criteria:

- PostgreSQL-compatible SQL.
- No reserved SQL keywords.
- Unique constraints for UUIDs, rule codes, case numbers, idempotency keys, and active blocklist identity where applicable.
- Indexes support dashboard and review queues.

## Phase 3: Signal Collection And Strategy Evaluators

Status: `Done`

Verification:

- Created `DefaultFraudSignalCollector` to run all Spring-managed `FraudSignalEvaluator` implementations.
- Created shared evaluator helper for consistent signal result creation and safe hashing.
- Created 10 evaluator implementations:
  - `DefaultCustomerHistorySignalEvaluator`
  - `DefaultDeviceRiskSignalEvaluator`
  - `DefaultNetworkRiskSignalEvaluator`
  - `DefaultOrderVelocitySignalEvaluator`
  - `DefaultPaymentRiskSignalEvaluator`
  - `DefaultCodRiskSignalEvaluator`
  - `DefaultAddressRiskSignalEvaluator`
  - `DefaultPromotionAbuseSignalEvaluator`
  - `DefaultReferralAbuseSignalEvaluator`
  - `DefaultVendorRiskSignalEvaluator`
- Source-level package declaration scan passed for 133 fraud Java files.
- Maven compile was not run because Maven and a Maven wrapper are not available in this environment.

Create service:

- `FraudSignalCollector`

Create independent evaluators:

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

Signal categories:

- Customer
- Device
- Network
- Order
- Payment
- COD
- Address
- Promotion
- Referral
- Vendor
- Fulfilment
- Refund
- Payout

Important rule:

- IP location and shipping location mismatch must be a risk signal only. It must never reject an order by itself.

Acceptance criteria:

- Each evaluator is independently unit-testable.
- Evaluators return signal results and never persist directly.
- Expensive historical checks can be marked asynchronous.
- Each signal includes source, severity, score impact, triggered status, reason code, and metadata.

## Phase 4: Rule Engine, Scoring, And Decision Matrix

Status: `Done`

Verification:

- Created `FraudRuleMatcher` for shared rule matching, scope checks, numeric comparisons, and execution detail generation.
- Created `DefaultFraudRuleEngine` for hard-block checks and configurable scoring-rule score adjustment.
- Created `DefaultFraudRiskScoringService` to calculate and clamp risk score from `0` to `100`.
- Created `DefaultFraudDecisionService` with configurable low/medium/high/critical thresholds and COD-aware decisions.
- Created `DefaultFraudConfigurationService` for database-backed configuration lookup.
- Created `FraudRuleExecutionLogService` and `DefaultFraudRuleExecutionLogService` for explainability logging after an assessment is persisted.
- Extended `FraudContext` with vendor, product, category, order value, and promotion type fields for scoped rules.
- Source-level package declaration scan passed for 140 fraud Java files.
- Maven compile was not run because Maven and a Maven wrapper are not available in this environment.

Create services:

- `FraudRuleEngine`
- `FraudRiskScoringService`
- `FraudDecisionService`
- `FraudConfigurationService`

Default score rules to seed:

| Signal | Score |
|---|---:|
| Mobile not verified | 25 |
| Account created within 24 hours | 10 |
| New or unknown device | 10 |
| More than three orders within 15 minutes | 25 |
| Same device used by multiple accounts | 30 |
| High-value first order | 20 |
| Shipping and payment country mismatch | 15 |
| Two previous COD delivery refusals | 30 |
| Blacklisted address | 100 |
| Blacklisted device | 100 |
| Excessive coupon usage | 20 |
| Excessive referral usage | 20 |
| Five or more successful orders | -20 |
| Trusted device | -10 |
| Successful 3-D Secure verification | -15 |
| Established prepaid customer | -10 |

Default decision matrix:

| Score | Risk Level | Decision |
|---:|---|---|
| 0-29 | LOW | APPROVE |
| 30-59 | MEDIUM | REQUIRE_OTP or VERIFY |
| 60-79 | HIGH | MANUAL_REVIEW |
| 80-100 | CRITICAL | REJECT, BLOCK, or REQUIRE_PREPAID |

Decision dimensions:

- Payment method
- Vendor
- Product
- Product category
- Customer segment
- Order value
- Country
- District
- Sales channel
- Promotion type

Acceptance criteria:

- Hard-block rules bypass scoring.
- Score is clamped to `0` through `100`.
- Negative trust signals reduce score but never hide a hard block.
- `FraudRuleExecution` stores explainability for each matched rule.

## Phase 5: Assessment, Cases, Manual Review, And Audit

Status: `Done`

Verification:

- Created `DefaultFraudAssessmentService` for synchronous assessment orchestration, idempotency-key lookup, signal persistence, rule execution logging, decision persistence, case opening, review updates, and event/audit recording.
- Created `DefaultFraudCaseService` for case lookup, open-case checks, assignment, resolution, automatic case opening from risky assessments, and case event logging.
- Created `DefaultFraudReviewService` for approve, reject, request-verification, and hold review actions with normalized decision/action values.
- Created `DefaultFraudAuditService` and fraud audit event recording through `FraudEventLog`.
- Created `FraudEvidenceService` and `DefaultFraudEvidenceService` for evidence metadata persistence, case/assessment linkage, file name, extension, content type, and size validation, and evidence event logging.
- Extended `FraudEventType` with review, evidence, and audit events.
- Source-level scan passed for 146 fraud Java files, 24 service contracts, and 23 implementation/helper classes.
- No `TODO`, `System.out`, `RuntimeException`, or `printStackTrace` leftovers were found in fraud Java source files.
- Maven compile was not run because Maven and a Maven wrapper are not available in this environment.

Create services:

- `FraudAssessmentService`
- `FraudCaseService`
- `FraudReviewService`
- `FraudAuditService`
- `FraudEvidenceService`

Manual review actions:

- Approve order
- Reject order
- Request OTP
- Require prepaid payment
- Require partial prepayment
- Disable COD
- Block customer
- Block device
- Block mobile number
- Block address
- Hold refund
- Hold reward
- Hold vendor payout
- Escalate case
- Assign another investigator

Acceptance criteria:

- Every review action requires a reason.
- Every review action writes `FraudReviewHistory` and `FraudEventLog`.
- Evidence upload validates file size, extension, content type, and safe filename.
- Supervisor-only actions are separated from analyst actions.

## Phase 6: Checkout And Order Integration

Status: `Done`

Verification:

- Created `DefaultFraudIntegrationGuard`, implementing `FraudPreOrderGuard`, `FraudOrderAssessmentGuard`, `FraudPaymentCaptureGuard`, and `FraudFulfilmentGuard`.
- Created `FraudHashingSupport` for normalized SHA-256 checks used by checkout blocklist screening.
- Extended `FraudOrderAssessmentGuard` with a context-aware order assessment method.
- Wired `/order/savebyvendor` and `/order/savebyvendorupdate` to run pre-order fraud eligibility checks and create post-order assessments before customer payment or fulfilment steps.
- Wired `PaymentService.recordPayment` to block payment capture until fraud approval.
- Wired `PaymentService.refundPaidAmount` and `PaymentService.refundAmount` to block refund release while fraud verification or a fraud case is open.
- Wired `SalesOrderService` to block vendor/admin operational status changes into confirmed, processing, packed, shipped, delivered, completed, and virtual fulfilment paths until fraud approval.
- Wired `ShipmentService` to block shipment creation through both `save` and existing `getShipmentBlockReason` paths used by admin/vendor screens.
- Source-level scan passed for 148 fraud Java files, 24 fraud implementation/helper classes, and 1 fraud support helper.
- No `TODO`, `System.out`, `RuntimeException`, or `printStackTrace` leftovers were found in fraud Java source files.
- Maven compile was not run because Maven and a Maven wrapper are not available in this environment.

Integration points:

- Before order creation in checkout eligibility.
- Immediately after `SalesOrder` creation.
- Before payment capture.
- Before order status changes to packing or fulfilment-ready statuses.
- Before stock fulfilment side effects where applicable.

Required gates:

- `FraudPreOrderGuard`
- `FraudOrderAssessmentGuard`
- `FraudPaymentCaptureGuard`
- `FraudFulfilmentGuard`

Acceptance criteria:

- Order creation creates or requests a fraud assessment idempotently.
- Payment capture is delayed until fraud approval.
- Rejected card authorization is cancelled idempotently.
- Pending fraud statuses block packing, vendor acceptance, shipment creation, and fulfilment.
- Failed fraud evaluation fails closed for high-risk payment capture and fulfilment, but returns a user-safe message.

## Phase 7: COD Fraud Prevention For Bangladesh Ecommerce

Status: `Done`

Verification:

- Created `CodEligibilityService` for checkout-time COD decisions.
- Created `CodRiskProfileService` for COD profile updates from order and shipment outcomes.
- Created `DefaultCodRiskService`, implementing `CodRiskService`, `CodEligibilityService`, and `CodRiskProfileService`.
- Enforced first-COD mobile OTP verification using existing authenticated-customer and guest mobile verification state.
- Enforced configurable first-order COD amount limit with `fraud.cod.first_order_limit`.
- Enforced configurable high-value COD confirmation/partial-advance control with `fraud.cod.high_value_confirmation_threshold`.
- Enforced profile-based partial-prepayment requirement using RTO/refusal thresholds.
- Added customer-specific and vendor-specific COD limit checks from `CodRiskProfile`.
- Added mobile-, address-, device-, district-, customer-, and vendor-level COD profile updates.
- Added COD disablement after repeated delivery refusal or RTO.
- Added COD restoration after successful prepaid deliveries using `fraud.cod.restore_after_prepaid_success_count`.
- Added shipment delivery/failed/returned outcome tracking through `ShipmentService`.
- Added COD disabled audit events through `FraudEventLog`.
- Extended PostgreSQL init/seed scripts for successful prepaid count and COD configuration defaults.
- Source-level scan passed for 151 fraud Java files, 26 fraud service contracts, and 25 fraud implementation/helper classes.
- No `TODO`, `System.out`, `RuntimeException`, or `printStackTrace` leftovers were found in fraud Java source files.
- Maven compile was not run because Maven and a Maven wrapper are not available in this environment.

Create services:

- `CodRiskService`
- `CodEligibilityService`
- `CodRiskProfileService`

Rules:

- First COD order requires mobile OTP.
- High-value COD requires OTP or customer-service confirmation.
- First-order COD amount has a configurable maximum.
- High-risk customers require partial advance payment.
- Critical-risk customers require prepaid payment.
- Repeated delivery refusal disables COD.
- Repeated return-to-origin disables COD.
- Successful prepaid history can restore COD after configured count.
- Address change after confirmation requires re-verification.

Profiles:

- Mobile-level RTO statistics
- Address-level RTO statistics
- Device-level RTO statistics
- District-level RTO statistics
- Customer-level RTO statistics
- Vendor-level RTO statistics

Acceptance criteria:

- COD disablement is auditable and reversible by authorized staff.
- COD limits can be customer-specific and vendor-specific.
- Packing is delayed until COD fraud assessment is completed.

## Phase 8: Post-Order Monitoring

Status: `Done`

Verification:

- Created `FraudPostOrderEventType` and `FraudPostOrderEventRequest`.
- Created `FraudPostOrderMonitoringService` and `DefaultFraudPostOrderMonitoringService`.
- Added idempotent post-order `FraudEventLog` persistence using event idempotency keys.
- Added high-risk case opening/updating for delivery refusal, RTO, chargeback, held reward/wallet/cashback/payout releases, and high-risk cancellation/return events.
- Wired `ShipmentService` to record shipment creation, delivery confirmation, delivery refusal, and return-to-origin events.
- Wired `SalesOrderService` to record cancellation, return requested, partial return, and completed return events.
- Wired `PaymentService` to record released refunds after fraud guard approval.
- Wired `CashbackService` to hold cashback release while fraud review is open.
- Wired `WalletService` to create pending wallet transactions instead of crediting balances while fraud review is open.
- Wired `RewardAccountService` to hold referral and level-commission rewards while fraud review is open.
- Wired `GiftCardService` to block gift-card redemption if the target order has an open fraud case and record successful gift-card usage.
- Wired `VendorFinanceService` and `DefaultFraudIntegrationGuard` to block vendor payout request/approval/payment while a vendor fraud case is open and record payout events.
- Added chargeback recording support through `FraudPostOrderMonitoringService.recordChargeback(...)`.
- Source-level package declaration scan passed for 155 fraud Java files.
- Maven compile passed from the parent project with `D:\Maven_Home\bin\mvn.cmd -q -DskipTests compile`.

Monitor:

- Shipment creation
- Delivery confirmation
- Delivery refusal
- Return-to-origin
- Cancellation
- Refund
- Cashback
- Wallet credit
- Gift-card usage
- Referral reward
- Vendor payout
- Chargeback
- Return and replacement activity

Acceptance criteria:

- Post-order events create `FraudEventLog` rows.
- High-risk post-order events can open or update `FraudCase`.
- Rewards, refunds, cashback, wallet credits, and vendor payouts can be held while a fraud case is open.

## Phase 9: Multi-Vendor Fraud Detection

Status: `Done`

Verification:

- Added `DefaultVendorRiskProfileService` for recalculating vendor risk from orders, shipments, payout methods, profile relationships, configurable thresholds, and live post-order events.
- Expanded `VendorRiskProfile` and PostgreSQL scripts for self-purchase, shared mobile/address/bank account, collusion, sales spike, abnormal refund/cancel rate, unverified delivery, tracking reuse, and risk-reason fields.
- Extended `DefaultVendorRiskSignalEvaluator` so vendor fraud signals are saved with fraud assessments and visible to manual review once the UI/API phases are wired.
- Wired order placement, order return/status changes, shipment creation/status changes, duplicate tracking-number rejection, delivery confirmation, and vendor payout actions into vendor-risk refresh and event logging.
- Added vendor fraud post-order event types that can open/update cases and hold payout-related value release.
- Added default vendor-control fraud rules and configurable vendor thresholds in `fraud_module_seed_rules.sql`.
- Added `fraud_module_phase9_vendor_risk.sql` for additive migration on existing PostgreSQL databases.
- Maven compile passed from the parent project with `D:\Maven_Home\bin\mvn.cmd -q -DskipTests compile`.

Detect:

- Vendor purchasing from its own store
- Customer and vendor sharing device, mobile, address, or bank account
- Fake orders created to earn commission
- Fake delivered orders
- Refund, return, cashback, referral, and payout collusion
- Tracking-number reuse
- Delivery confirmation without carrier verification
- Sudden sales spikes from new customer accounts
- Multiple vendors sharing payout details
- Abnormal refund or cancellation rates
- Suspicious product price changes before promotions
- Artificial rating or review activity
- Repeated orders between related accounts

Vendor actions:

- Hold payout
- Delay payout
- Apply rolling reserve
- Require shipment evidence
- Require carrier confirmation
- Require delivery proof
- Disable promotions
- Suspend new-order acceptance
- Place vendor under manual review
- Temporarily suspend vendor
- Permanently block vendor

Acceptance criteria:

- Vendor payouts are not released while a related fraud case is open.
- Vendor fraud signals are visible in manual review.
- Vendor-facing pages do not expose internal fraud logic.

## Phase 10: REST APIs

Status: `Pending`

Create endpoints:

```text
POST   /api/fraud/assessments
GET    /api/fraud/assessments/{id}
GET    /api/fraud/assessments/order/{orderId}
POST   /api/fraud/assessments/{id}/review
POST   /api/fraud/assessments/{id}/approve
POST   /api/fraud/assessments/{id}/reject
POST   /api/fraud/assessments/{id}/request-verification

GET    /api/fraud/cases
GET    /api/fraud/cases/{id}
POST   /api/fraud/cases/{id}/assign
POST   /api/fraud/cases/{id}/resolve

GET    /api/fraud/rules
POST   /api/fraud/rules
PUT    /api/fraud/rules/{id}
PATCH  /api/fraud/rules/{id}/status

GET    /api/fraud/blocklist
POST   /api/fraud/blocklist
DELETE /api/fraud/blocklist/{id}

GET    /api/fraud/dashboard
GET    /api/fraud/reports
```

Acceptance criteria:

- Consistent request and response DTOs.
- Validation and pagination.
- Sorting and filtering.
- Correlation IDs.
- Idempotency keys for mutating operations.
- Consistent fraud API error response.
- API documentation in `docs/fraud-api.md`.

## Phase 11: Admin Dashboard And Thymeleaf UI

Status: `Done`

Verification:

- Added MVC routes under `/admin/fraud/**` through `FraudAdminController`.
- Added `FraudAdminViewService` and `DefaultFraudAdminViewService` so dashboard metrics, filtering, save actions, and report rows stay out of templates/controllers.
- Extended fraud repositories with `JpaSpecificationExecutor` where list pages need server-side filtering.
- Added Bootstrap 5 Thymeleaf pages under `main/resources/templates/admin/fraud/`: dashboard, assessments, assessment detail, manual review, cases, case detail, rules, rule form, blocklist, blocklist form, configuration, and reports.
- Added shared fraud admin styling in `main/resources/static/assets/admin/css/fraud-admin.css`.
- Added Fraud Control navigation links to `admin-nav-left.html`.
- Sensitive blocklist values are hashed in the service and only masked values are displayed.
- Maven compile passed from the parent project with `D:\Maven_Home\bin\mvn.cmd -q -DskipTests compile`.

Create screens:

- Dashboard
- Assessment list
- Assessment detail
- Manual review
- Case list
- Case detail
- Rules list
- Rule form
- Blocklist list
- Blocklist form
- Configuration
- Reports

Dashboard metrics:

- Total assessed orders
- Low, medium, high, and critical risk orders
- Manual review queue
- Blocked orders
- Rejected orders
- Fraud loss prevented
- Chargeback rate
- COD RTO rate
- Delivery refusal rate
- Fraud rate by vendor, product, category, district, and payment method
- Coupon, referral, cashback, wallet, and gift-card abuse
- Top suspicious devices, IPs, mobiles, addresses, customers, and vendors
- Reviewer performance
- False-positive rate
- Rule-trigger frequency
- Manual decision overrides
- Financial exposure

Filters:

- Date range
- Vendor
- Customer
- Product
- Category
- Payment method
- Risk level
- Decision
- Reviewer
- District
- Fraud reason
- Order status

Acceptance criteria:

- Uses `admin-layout.html`.
- Bootstrap 5 markup.
- Pagination for all large lists.
- Sensitive data masked.
- No business logic in templates.

## Phase 12: Events, Outbox, Notifications, And Idempotency

Status: `Done`

Verification:

- Added durable outbox persistence through `DefaultFraudEventPublisher`.
- Added scheduled retry dispatch through `DefaultFraudOutboxDispatcherService`.
- Added `FraudOutboxDispatchEvent` for application-level event publication.
- Added `DefaultFraudNotificationService` to reuse the existing communication module for safe customer, analyst, and finance notifications.
- Added `DefaultFraudIdempotencyService` and wired assessment, review, and post-order operations into `fraud_idempotency_records`.
- Wired outbox publication from fraud assessments, manual reviews, fraud cases, COD-disabled events, post-order events, value holds, and vendor payout holds.
- Added communication `MessageEventType` values for fraud verification, review, prepayment, COD-disabled, rejection, case assignment, vendor payout hold, account block, and critical-rule alerts.
- Maven compile passed from the parent project with `D:\Maven_Home\bin\mvn.cmd -q -DskipTests compile`.

Events:

```text
OrderFraudEvaluationRequested
OrderFraudAssessmentCompleted
OrderFraudApproved
OrderFraudVerificationRequired
OrderFraudHeld
OrderFraudRejected
CustomerBlocked
DeviceBlocked
CodDisabled
RefundHeld
VendorPayoutHeld
FraudCaseOpened
FraudCaseResolved
```

Outbox requirements:

- Store event payload, event type, aggregate type, aggregate ID, correlation ID, idempotency key, retry count, next attempt time, published time, and failure reason.
- Publish after transaction commit.
- Retry failed events safely.
- Deduplicate by idempotency key.

Notifications:

- OTP verification required
- Order under review
- Prepayment required
- COD disabled
- Order rejected
- Fraud analyst case assignment
- Supervisor approval required
- Vendor payout held
- Customer or vendor blocked
- Critical fraud rule triggered

Important notification rule:

- Customer and vendor notifications must not expose internal fraud scoring, rule details, or sensitive risk logic.

Acceptance criteria:

- Reuse communication module where possible.
- Notification failures do not break the business transaction.
- Fraud actions remain auditable even if notification delivery fails.

## Phase 13: Security And Privacy

Status: `Done`

Verification:

- Added fraud-specific authority expressions for read, review, admin, and finance-access paths.
- Added route-level security for `/admin/fraud/**` and `/api/fraud/**`.
- Added method-level `@PreAuthorize` controls on fraud admin pages and mutating actions.
- Added module-scoped CSRF protection for fraud admin POST/PUT/PATCH/DELETE requests even though global CSRF is disabled in the legacy app.
- Added fraud CSRF hidden fields to every fraud admin POST form.
- Added webhook HMAC-SHA256 signature validation, timestamp tolerance checks, replay detection through idempotency records, and provider-level rate limiting.
- Added centralized masking/redaction for identifiers, payment tokens, card/CVV/password/payment-token JSON fields, and sensitive configuration display.
- Added payment-risk service support for hashed payment tokens and redacted provider metadata.
- Existing blocklist storage continues to store hashed values only, with masked values for display.
- Maven compile passed from the parent project with `D:\Maven_Home\bin\mvn.cmd -q -DskipTests compile`.

Implement:

- Role-based access control.
- Separate permissions for fraud analysts, supervisors, administrators, finance users, and vendors.
- CSRF protection for MVC forms.
- Secure API authentication.
- Webhook signature validation.
- Replay-attack prevention.
- Rate limiting for public/device/payment risk inputs.
- Masking of card and personal information.
- Hashing/tokenization of payment identifiers.
- Secure device fingerprint handling.
- Audit logs for every rule/configuration/review decision change.

Roles and permissions:

- `ROLE_FRAUD_ANALYST`
- `ROLE_FRAUD_SUPERVISOR`
- `ROLE_FRAUD_ADMIN`
- `ROLE_FINANCE`
- `ROLE_VENDOR`

Acceptance criteria:

- No raw card data stored.
- No sensitive token or fingerprint logged.
- Admin fraud pages and APIs are access-controlled.
- Vendor users can only see vendor-safe hold statuses, not internal signal detail.

## Phase 14: Testing

Status: `Pending`

Create tests:

- Unit tests for every signal evaluator.
- Score calculation tests.
- Rule-engine tests.
- Hard-block tests.
- COD fraud tests.
- Vendor fraud tests.
- Repository tests.
- Controller tests.
- Security tests.
- Integration tests.
- Concurrency tests.
- Idempotency tests.
- Payment callback tests.
- Webhook replay tests.
- False-positive tests.
- High-volume performance tests.

Required scenarios:

- Legitimate new customer
- High-value first order
- Multiple accounts from one device
- Repeated COD rejection
- Coupon abuse
- Self-referral
- Payment-provider high-risk result
- Trusted customer
- Trusted device
- Blacklisted address
- Vendor/customer collusion
- Duplicate tracking number
- Manual review approval
- Manual review rejection
- Concurrent fraud evaluations

Acceptance criteria:

- Tests prove hard blocks override trust signals.
- Concurrent evaluations do not duplicate assessments, captures, cancellations, rewards, or outbox events.
- False-positive scenarios remain approved or verification-only where configured.

## Phase 15: Documentation And Admin Guide

Status: `Done`

Verification:

- Added `docs/fraud-api.md` documenting the target REST contract, current DTO/service support, pending Phase 10 controller boundary, authentication/authorization, validation errors, idempotency, correlation IDs, webhook security, pagination, and implementation checklist.
- Added `docs/fraud-database-design.md` documenting tables, indexes, constraints, seed configuration, migration order, rollback considerations, retention, and privacy rules.
- Added `docs/fraud-admin-configuration-guide.md` documenting admin pages, roles, assessments, manual review, cases, rules, blocklist, COD controls, vendor controls, webhook configuration, rollback steps, and operating checklist.
- Added `docs/fraud-security-and-privacy-guide.md` documenting RBAC, module CSRF, webhook HMAC/timestamp/replay/rate-limit controls, masking, token hashing, audit logging, and vendor privacy boundaries.
- Added `docs/fraud-testing-guide.md` documenting unit, service, repository, MVC, REST, integration, security, concurrency, idempotency, and performance test plans.

Create:

- `docs/fraud-api.md`
- `docs/fraud-database-design.md`
- `docs/fraud-admin-configuration-guide.md`
- `docs/fraud-security-and-privacy-guide.md`
- `docs/fraud-testing-guide.md`

Acceptance criteria:

- Admin guide explains rules, thresholds, blocklists, COD limits, review actions, and rollback steps.
- API guide documents endpoints, DTOs, validation errors, idempotency, and correlation IDs.
- Database guide documents migration impact, indexes, and rollback considerations.

## Implementation Order

Status: `Pending`

Recommended build order:

1. Foundation enums, DTOs, exceptions, and contracts.
2. Database entities, repositories, migrations, seed rules, and indexes.
3. Signal evaluator interfaces plus first customer, order, COD, promotion, and blocklist evaluators.
4. Rule engine, scoring, hard-block handling, and decision matrix.
5. Assessment persistence, case creation, audit history, and event log.
6. Checkout/order fraud assessment hook without fulfilment side effects.
7. Fulfilment, shipment, payment capture, reward, refund, and payout blocking gates.
8. Admin review pages and REST API review actions.
9. Rule and blocklist admin configuration.
10. Outbox event publisher and idempotency table.
11. Dashboard/reporting pages.
12. Full test coverage and documentation.

## Proof Required Before Marking Done

Status: `Pending`

For each implementation phase:

- Source compiles.
- Relevant unit tests pass.
- New SQL is PostgreSQL-compatible.
- Admin pages render with authenticated admin access or source-level Thymeleaf validation is documented if auth blocks runtime proof.
- REST endpoints return consistent validation and error responses.
- Fraud gates are tested against order creation, payment capture, shipment creation, reward release, refund release, and vendor payout release.
- Any incomplete external-provider integration is explicitly marked `Deferred`.
