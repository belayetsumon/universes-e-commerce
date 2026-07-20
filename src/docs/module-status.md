# Module Status

Use this file to track module progress day by day.

## Status Legend
- `Pending`: not started
- `In Progress`: currently being developed
- `Completed`: development finished
- `Tested`: verified with test flow
- `Blocked`: waiting for clarification or dependency

## Summary
| Module | Status | Tested | Last Updated | Notes |
|---|---|---|---|---|
| Authentication | Pending | No | 2026-04-29 | |
| Product Catalog | In Progress | No | 2026-05-15 | Dynamic attribute phase-1 scaffold, UUID base, and migration helpers added |
| Cart | Pending | No | 2026-04-29 | |
| Wishlist | Completed | Partial | 2026-04-29 | Add, remove, count, customer wishlist page implemented |
| Checkout | Pending | No | 2026-04-29 | |
| Orders | Pending | No | 2026-04-29 | |
| Vendor Panel | In Progress | No | 2026-06-24 | Added vendor shipment document flow: labels, manifests, and shipment invoice shortcuts scoped to active vendor. Manual browser/database testing pending |
| Shipping & Fulfillment | In Progress | No | 2026-06-24 | Enterprise shipping phases implemented: zones, slabs, tracking, pickup addresses, rules/quote service, labels, manifests, invoices, vendor document handover flow, vendor COD ownership guard, POST shipment deletes. Manual browser/database testing pending |
| Settings | Completed | Partial | 2026-04-29 | Global settings CRUD and logo variants updated |
| Fraud Detection & Prevention | In Progress | Compile passed | 2026-07-18 | Phases 1-9, 11-13, and 15 completed: foundation, persistence, signal collection, rule engine/scoring/decision matrix, assessment/case/review/audit services, checkout/order/payment/shipment integration, Bangladesh COD fraud controls, post-order monitoring, multi-vendor fraud detection, admin Thymeleaf UI, events/outbox/notifications/idempotency, security/privacy, and documentation/admin guides; Phase 10 REST APIs still pending |

## Daily Progress Log

### 2026-07-18
- Created `docs/fraud-order-detection-workflow.md` for the enterprise fraud order detection and prevention module.
- Mapped fraud work across checkout, order, payment, shipping, COD, referral, coupon, wallet, cashback, gift card, refund, vendor payout, communication, audit, REST API, admin UI, outbox, idempotency, and testing phases.
- Marked implementation as pending after workflow creation so entities, services, controllers, migrations, pages, and tests can be built in traceable phases.
- Added a workflow completion tracker: Phase 0 workflow/discovery is done, and Phases 1-15 remain pending until implementation and verification are completed.
- Completed Phase 1 fraud foundation under `main/java/com/ecommerce/app/module/fraud`: enums, DTOs, exceptions, service contracts, guard contracts, and named signal evaluator contracts. Source-level package scan passed for 80 fraud Java files; Maven compile was not available.
- Completed Phase 2 fraud persistence: 19 JPA entity tables, 19 repositories, PostgreSQL init/index/seed scripts, transactional outbox persistence, and idempotency persistence. Source-level package scan passed for 121 fraud Java files; Maven compile was not available.
- Completed Phase 3 fraud signal collection: default collector, shared evaluator helper, and 10 independent evaluators for customer, device, network, order velocity, payment, COD, address, promotion, referral, and vendor risk. Source-level package scan passed for 133 fraud Java files; Maven compile was not available.
- Completed Phase 4 fraud rule and decision layer: shared rule matcher, hard-block engine, scoring-rule score adjustment, risk score calculator, configurable decision matrix, configuration service, and rule execution logger. Source-level package scan passed for 140 fraud Java files; Maven compile was not available.
- Completed Phase 5 fraud assessment/review layer: assessment orchestration, idempotency-key lookup, signal persistence, rule execution logging hookup, case open/assign/resolve, review history, review/evidence/audit event logging, and evidence file safety validation. Source-level package scan passed for 146 fraud Java files; Maven compile was not available.
- Completed Phase 6 fraud checkout/order integration: fraud integration guard, shared hashing support, pre-order checkout screening, post-order assessment creation, payment/refund blocking, order operational-status blocking, and shipment creation blocking. Source-level package scan passed for 148 fraud Java files; Maven compile was not available.
- Completed Phase 7 Bangladesh COD controls: COD eligibility service, COD risk profile service, first-COD OTP enforcement, first-order COD limit, high-value COD confirmation/partial-advance controls, customer/vendor COD limits, mobile/address/device/district/customer/vendor RTO and refusal profiles, COD disablement after repeated RTO/refusal, prepaid-success COD restoration, shipment outcome tracking, and COD disabled audit events. Source-level package scan passed for 151 fraud Java files; Maven compile was not available.
- Completed Phase 8 post-order monitoring: post-order event DTO/type/service, idempotent fraud event logs, high-risk case open/update handling, shipment delivery/refusal/RTO monitoring, cancellation/return/refund monitoring, cashback/wallet/gift-card/referral reward hold controls, vendor payout gates, chargeback event support, and parent-project Maven compile verification.
- Completed Phase 9 multi-vendor fraud detection: vendor risk profile recalculation, self-purchase and shared mobile/address/bank-account detection, tracking-number reuse attempt logging, delivery-without-carrier fake-delivery suspicion, abnormal refund/cancel/sales-spike metrics, payout-hold escalation, vendor fraud signals, default vendor-control seed rules, additive PostgreSQL migration, and parent-project Maven compile verification.
- Completed Phase 11 admin dashboard and Thymeleaf UI: `/admin/fraud/**` MVC controller, admin view service, paginated/filterable assessments/cases/rules/blocklist/configuration/events, dashboard/report metrics, assessment detail/manual review forms, case assign/resolve forms, rule save/status forms, masked blocklist UI, shared fraud admin CSS, Fraud Control sidebar navigation, and parent-project Maven compile verification.
- Completed Phase 12 events, outbox, notifications, and idempotency: durable fraud outbox publisher, scheduled retry dispatcher, application dispatch event, communication-module notification bridge, assessment/review/post-order idempotency records, outbox publication from assessment/review/case/COD/post-order/value-hold/vendor-payout flows, fraud communication event types, and parent-project Maven compile verification.
- Completed Phase 13 security and privacy: fraud role/authority constants, route and method-level access control, module-scoped fraud admin CSRF tokens/interceptor/template fields, webhook HMAC/timestamp/replay/rate-limit validation service, centralized masking/redaction, payment-token hashing service, masked sensitive configuration listing, redacted audit metadata, and parent-project Maven compile verification.
- Completed Phase 15 documentation and admin guide: added fraud API guide, database design guide, admin configuration guide, security/privacy guide, and testing guide with implementation boundaries, migration/rollback notes, admin operating instructions, security controls, and pending Phase 10/14 proof notes.

### 2026-06-18
- Started shipping module upgrade from `docs/shipping-module-update-workflow.md`.
- Added `ShippingZone` and zone admin management.
- Added `CarrierRateSlab` and slab admin management.
- Updated carrier rates to support either direct districts or reusable shipping zones.
- Updated shipping option lookup to match rates by direct district or zone district coverage.
- Updated carrier-rate calculation to prefer active matching slabs, then fall back to the existing base-weight calculation.
- Added manual shipping test cases to `docs/test-flow.md`.

### 2026-06-23
- Marked shipment tracking implementation as completed in workflow documentation.
- Added vendor pickup address model, repository, service, controller, admin screens, shipment reference, and form wiring.
- Added shipping rules and `ShippingQuoteService` for carrier disabling, COD disabling, extra fees, and quote priority.
- Added shipping label, manifest, and shipment invoice records with admin screens.
- Hardened vendor COD collection with vendor ownership validation.
- Made carrier optional when a delivery person is assigned, and changed shipping-module delete actions from GET links to POST forms.
- Added database-driven international shipping locations: country, division/state, district/city, and thana/upazila. Shipping zones can now use location hierarchy coverage while legacy district enum coverage remains available during migration.
- Added manual test cases for pickup addresses, rules, documents, and shipment hardening.

### 2026-06-24
- Added vendor panel document flow for shipment labels, carrier manifests, and shipment invoices.
- Added active-vendor scoped document repository queries and vendor document controller routes.
- Added vendor menu shortcuts and shipment-list shortcuts for labels, manifests, and invoices.
- Updated shipping workflow, functional guide, and test flow with vendor document QA steps.

### 2026-05-15
- Added phase-1 dynamic catalog entities for reusable attributes, category mappings, product attribute values, and catalog variants.
- Added Java UUID generation support for new catalog entities and for live `ProductVariants` rows created after this change.
- Switched live product-variant add/delete modal routes to UUID-based lookups.
- Added migration helper service to bootstrap legacy size/color data into the new attribute tables.

### 2026-04-29
- Completed wishlist backend and frontend flow.
- Added `Principal` based user lookup for wishlist.
- Added customer wishlist page and header count.
- Updated global settings CRUD and logo handling earlier in this session.

## Current Focus
- Next module: Fraud Detection & Prevention
- Scope for today: Continue from `docs/fraud-order-detection-workflow.md` Phase 10 by adding REST APIs for fraud assessments, cases, rules, blocklist, dashboard, and reports
- Risks or blockers: Full implementation still requires careful migration review plus staged REST API, outbox publisher, notification, security, browser/UI verification, and test coverage phases
