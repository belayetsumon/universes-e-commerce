# Phase 1 Manual Endpoint Classification

Status: Complete as a security-policy and decision-record phase. This is not deployment, database-migration, or full runtime verification.

## Authoritative inputs

- Source endpoint inventory: docs/application-security-endpoint-inventory.csv
  - 742 source rows
  - SHA-256: A9BFB08EAFE99D16BC5407C6A53987870A5756C3CE5746EFE25D1E782D5CB981
- Source permission catalogue: docs/application-security-permission-catalogue.csv
  - 132 candidate capabilities
  - SHA-256: 9331D230EB9B387DBA2200FC2ABC1C806B7A967C1C860FF871D1ED65EACE2D16

The authoritative snapshots were refreshed after the Phase 1 containment slices. The endpoint total is 742 because vendor email verification is now split into a read-only GET confirmation route and a CSRF-covered POST token-consumption route, and the permission-catalogue seed flow now has a read-only GET redirect plus a protected POST seed action. Destructive GET routes are now POST-only, nine additional high-confidence mutation handlers use explicit POST mappings, password-recovery display/request/reset methods are explicit and CSRF-covered, 128 additional admin, catalog, public, vendor, customer, order, cart, reward, promotion, and image-fragment page rows are now GET-only, 31 sensitive GET routes are source-reviewed as read-only page, redirect, form, policy, export, or download responses, and no sensitive-operation GET deferrals remain. Public SEO, maintenance, district-selection, and registration ingress routes are now explicitly permit-all and source-classified as public. Only the framework-style `/error` and `/access-denied` handlers remain as all-method source mappings.

## Delivered decision records

- docs/application-security-endpoint-decision-ledger.csv
  - One current decision per source endpoint.
  - Records final zone, exact or explicitly deferred HTTP-method decision, required authentication, named capability, ownership and repository scope, CSRF/signature/replay, rate-limit and idempotency policy, audit requirement, evidence, tests, and decision status.
- docs/application-security-permission-decision-ledger.csv
  - One decision per candidate capability, including immutable namespace, assignment policy, step-up, lifecycle, evidence, and status.
- docs/security/sync-endpoint-security-decision-ledgers.ps1
  - Rebuilds both decision ledgers from current source artifacts and validates one-to-one coverage, unique keys, non-blank controls, exact release-eligible methods, capability links, and non-assignable public/webhook/internal policies.

## Phase 1 decision result

| Decision status | Endpoint rows | Meaning |
| --- | ---: | --- |
| APPROVED | 671 | Policy classification is ready for implementation. Current source enforcement, migration, and runtime proof remain separate gates. |
| IMPLEMENTED | 67 | Source containment controls are evidenced; migration and deployment-like runtime proof remain outstanding. |
| DEFERRED | 4 | A documented security blocker prevents release approval. |

Permission decisions: 119 approved, 10 source-implemented pending runtime proof, and 3 deferred.

The 67 implemented rows include the password-recovery lifecycle: explicit request/reset methods, neutral responses, direct-mail reset links, and SHA-256-hashed 30-minute single-use capabilities backed by the MySQL migration `V202608300001__password_reset_tokens.sql`. Account credential/session versioning is now source-implemented with the MySQL migration `V202608310001__credential_version_session_invalidation.sql`; vendor staff assignment and role changes now advance the same credential epoch so stale login authority snapshots are invalidated. Vendor verification email tokens and mobile OTP values are now source-implemented as hashed secrets with cleanup migration `V202608310002__vendor_verification_secret_hash_transition.sql`; vendor email verification token consumption now runs through a CSRF-covered POST confirmation route instead of GET. The 31 approved sensitive GET rows are source-reviewed as read-only page, redirect, form, policy, export, or download responses and now carry `SensitiveReadOnlyGetDecisionContractTest` evidence. The disabled legacy `/changepassword` route family is source-implemented as a deny-all flow with a named internal disabled capability. Public SEO, maintenance, district-selection, and registration ingress rows now carry `PublicRouteDecisionContractTest` evidence. SMTP configuration, migration execution, invitation lifecycle, remaining API/webhook/error-method surfaces, and deployment-like runtime/browser proof remain release gates.

## Documented blockers

| Blocker | Endpoint rows |
| --- | ---: |
| No reliable principal zone | 0 |
| Source mapping accepts every HTTP method | 2 |
| API still falls through to form-login authentication | 1 |
| Webhook lacks signature, replay, and idempotency controls | 1 |

A deferred decision is intentionally non-release-eligible. It does not approve the existing source behavior.

## Rebuild and validation

Run the decision-ledger script after any endpoint inventory regeneration:

    & docs/security/sync-endpoint-security-decision-ledgers.ps1

The script rejects duplicate or orphaned decisions, blank mandatory controls, ALL as a final HTTP method, release-eligible routes without an exact method, and invalid capability assignment policies.

## Remaining proof boundary

This phase does not prove database permission seeding, deployed Spring mappings, live CSRF coverage, ownership repository predicates, rate limiting, idempotency, provider signatures, or browser behavior. Those controls remain in the implementation and verification phases.
