# Fraud Module Mode — Implementation Contract

Status: DESIGN_ONLY — implementation and regression checks pending.

This feature branch is reserved for introducing a platform-controlled fraud mode.
The module is not yet switched by this document.

## Modes

- `ACTIVE` (default): evaluate new events and enforce new automated decisions.
- `MONITOR_ONLY`: evaluate and record new risk signals, without imposing *new* automated restrictions.
- `INACTIVE`: do not perform new automatic fraud evaluation.

## Non-negotiable safeguards

1. Existing assessment holds, rejected orders, open fraud cases, vendor payout holds, and manual blocklist restrictions must not be released by a mode change.
2. OTP identity verification, CSRF, payment authorization, order ownership, and other independent business/security controls remain enforced regardless of mode.
3. Only authorized platform/fraud administrators can change the mode. Protect POST with CSRF and record old/new mode, actor, time, and reason.
4. Use a centrally managed, strongly typed mode stored in MySQL, defaulting to `ACTIVE` when absent. Reject invalid mode values rather than silently disabling controls.
5. All synchronous entry points and scheduled/background consumers must follow the same semantics. Do not drop pending outbox or audit records.
6. A new fraud assessment in monitor mode must not overwrite or downgrade an existing enforce-mode hold. Resuming active mode must not blindly approve pending assessments.
7. Tests must cover transitions among all three modes, pre-existing restrictions, forged admin requests, missing/invalid settings, concurrent updates, and cross-vendor access.

## Initial integration inventory

- `DefaultFraudIntegrationGuard`: checkout, order, payment capture/refund, fulfillment, and payout.
- `DefaultFraudAssessmentService`: assessment creation and scoring.
- `DefaultFraudPostOrderMonitoringService`: post-order signal and case creation.
- `DefaultFraudOutboxDispatcherService`: preserve queued events and audit delivery.
- `DefaultCodRiskService`: separate independent COD identity verification from configurable fraud enforcement.
- `FraudAdminController` and `templates/admin/fraud/configuration.html`: restricted mode selector and confirmation.

This document is the first branch commit used to confirm GitHub write access. It does not imply that the three-mode feature is implemented.
