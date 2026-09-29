# Whole-Application Authentication, Authorization, and Endpoint Security Workflow

## Purpose

This is the parent security workflow for the complete application. It covers anonymous/public traffic, customers, vendor owners and staff, platform administrators, APIs, callbacks, scheduled/background work, authentication, module permissions, resource ownership, repository scope, sessions, credentials, audit, and every Spring MVC endpoint.

The focused vendor plan remains in docs/vendor-staff-method-security-workflow.md and is executed as a child of this workflow.

## Implementation Boundary

Status: Phase 1 manual endpoint classification complete; security implementation and deployment verification remain incomplete.

- All 742 current source endpoint rows have a durable decision record.
- 671 policy decisions are approved for implementation, 67 security rows are source-implemented pending migration/runtime proof, and 4 rows have explicit release-blocking deferrals.
- The PostgreSQL permission seed has not been applied. The temporary legacy admin/ROLE_ADMIN bridge remains until exact-permission migration and smoke tests pass.
- Security is not considered complete from controller URL rules, menu visibility, source compilation, or an unauthenticated redirect alone.

## Audit Artifacts

- docs/application-security-endpoint-inventory.csv
  - Source-derived endpoint ledger with one row per expanded route and HTTP-method combination.
- docs/application-security-permission-catalogue.csv
  - Deduplicated source-derived permission candidates grouped from the endpoint ledger.
- docs/application-security-endpoint-decision-ledger.csv
  - Phase 1 durable per-endpoint decision record containing final zone, method decision, authentication, capability, scope, security controls, tests, evidence, and status.
- docs/application-security-permission-decision-ledger.csv
  - Phase 1 durable per-capability decision record containing assignment, step-up, lifecycle, evidence, and status.
- docs/security/generate-endpoint-security-inventory.ps1
  - Rebuilds the source-derived inventory and candidate catalogue.
- docs/security/sync-endpoint-security-decision-ledgers.ps1
  - Rebuilds and validates the decision ledgers; it rejects duplicate/orphaned decisions, blank mandatory controls, ALL final methods, invalid policy links, and assignable public/webhook/internal policies.
- docs/application-security-phase-one-classification.md
  - Phase 1 scope, result, evidence hashes, and documented release blockers.
- docs/vendor-staff-method-security-workflow.md
  - Detailed vendor membership, invitation, permission, ownership, revocation, and audit workflow.
- /admin/system/endpoints
  - Existing runtime mapping registry. It is authoritative for mappings registered by the running Spring context and must be reconciled with the source ledger before release.

## Status Legend

- Done: verified planning/audit output or implemented with evidence
- Partial: foundation exists but does not satisfy the complete control
- Pending: not implemented
- Blocked: waiting for a required decision or dependency
- Deferred: explicitly outside the current release

## Progress Summary

| Measure | Count |
| --- | ---: |
| Source endpoint rows with a Phase 1 decision | 742 |
| Endpoint policy decisions approved for implementation | 671 |
| Endpoint decisions source-implemented pending migration/runtime proof | 67 |
| Endpoint decisions deferred with an explicit blocker | 4 |
| Permission capability decisions | 132 |
| Permission decisions approved for implementation | 119 |
| Permission decisions source-implemented pending migration/runtime proof | 10 |
| Permission decisions deferred with an explicit blocker | 3 |
| Security implementation started | Yes |

## Verified Endpoint Baseline

### Source Coverage

| Measure | Current result |
| --- | ---: |
| Expanded route/HTTP-method rows | 742 |
| Mappings accepting all HTTP methods | 2 |
| GET routes that appear to delete/remove/revoke/suspend | 0 |
| Source rows with current method guards | 235 |

Existing method guards remain recorded per endpoint as migration evidence, but annotation counts are not permission-catalogue coverage and are not used as a release-completion metric.

### Current URL-Rule Classification

| Current Spring Security result | Endpoint rows |
| --- | ---: |
| permitAll | 89 |
| explicit authority set | 66 |
| fraud authority set | 24 |
| any authenticated user | 563 |

Any authenticated user is not the same as customer, vendor, administrator, module, tenant, or ownership authorization.

### Target-Zone Candidates

| Candidate zone | Endpoint rows | Current rows with a method guard | Principal requirement |
| --- | ---: | ---: | --- |
| platform admin | 303 | 179 | active platform account plus module permission |
| legacy admin route requiring prefix review | 115 | 0 | active platform account plus module permission |
| customer | 110 | 42 | active customer plus current-user/resource ownership |
| vendor | 117 | 9 | active vendor membership plus permission and vendor/resource scope |
| explicitly public candidate | 88 | 0 | explicit allowlist plus abuse and state controls |
| public candidate requiring reclassification | 0 | 0 | justify public or require authentication |
| API | 1 | 0 | client/user scope and resource scope |
| callback/webhook | 1 | 0 | signature, replay protection, and idempotency |
| shared or unclassified | 7 | 5 | explicit owner/module decision |

The target-zone and module columns are source-review candidates, not the final policy. The Phase 1 decision ledger records every current source row as APPROVED, IMPLEMENTED, or documented DEFERRED. A deferred decision is not release-eligible. The platform IAM, platform vendor-IAM, session-administration, vendor staff-IAM, destructive GET-route containment, explicit mutation-route containment, password-recovery route containment, disabled legacy password route containment, and vendor email verification action-GET containment rows remain source-implemented pending migration and deployment-like runtime proof. Public SEO, maintenance, district-selection, and registration ingress routes are now explicitly public and ledger-approved. Thirty-one sensitive GET rows have been source-reviewed as read-only page, redirect, form, policy, export, or download responses; no sensitive-operation GET deferrals remain.

## Critical Current Findings

### P0 - Immediate Containment Required Before Broad Rollout

1. Whole-application CSRF protection is incomplete.
   - Spring Security CSRF is now enabled for `/role/**`, `/privilege/**`, and `/module/**` unsafe methods as the first containment slice.
   - The fraud admin module retains its local unsafe-method token interceptor.
   - Other authenticated browser mutations still require the phased standard Spring Security CSRF rollout.

2. User identity administration routes remain available to any authenticated user.
   - `/users/**` still has commented controller guards and no protected application/service boundary.
   - The user controller can list all users and login history, edit users, assign roles/user types, delete users, and change a password for a supplied user ID.
   - `/role/**`, `/privilege/**`, and `/module/**` are now source-protected by exact platform-IAM permissions, controller and service `@PreAuthorize` checks, explicit HTTP methods, grant-ceiling validation, protected catalogue slugs, and scoped CSRF. Migration and deployment-like runtime proof remain required.

3. Method authorization coverage is still incomplete outside the first IAM slice.
    - `IamAdministrationService` now has 10 service-layer `@PreAuthorize` guards, so direct invocation of the implemented role/permission/module use cases is protected.
    - `VendorStaffAdministrationService` now protects the existing vendor staff and custom-role administration paths with service `@PreAuthorize`, vendor-scoped repository lookups, grant-ceiling checks, self-removal/final-owner guards, and CSRF-protected POST mutations.
    - Other service entry points, scheduled work, consumers, callbacks, and future API reuse can still bypass controller-only checks until their work packages are implemented.

4. Public access is broader than explicit endpoint intent.
   - `PUBLIC_URLS` includes wildcard families such as `/public/**`, `/cart/**`, and `/carts/**` without HTTP-method restrictions.
   - Of 88 `permitAll` ledger rows, 1 still accepts all HTTP methods, 30 are POST, and 57 are explicit GET.
   - New handlers added under a public wildcard may become anonymous automatically.

5. Legacy password handling is inconsistent.
   - Password matching helpers accept plaintext stored-password fallback.
   - Password length requirements vary between forms.
   - The legacy `ChangePasswordController` creates a new transient `Users` instance instead of loading and authorizing the current account.
   - The self-service, admin, and recovery password paths now advance a credential epoch and expire registered sessions; broader role/membership revocation and sensitive-action grant invalidation remain pending.

6. Password recovery now has a secure lifecycle foundation, but release gates remain.
   - Requests are neutral for blank, unknown, and known addresses; explicit GET/POST reset routes are CSRF-covered.
   - Reset links use random tokens whose SHA-256 digests are persisted with a 30-minute expiry and atomic single-use consumption.
   - SMTP configuration, migration execution, and deployment-like runtime/browser proof remain outstanding; session-version invalidation is implemented in source and still needs deployment verification.

7. Vendor verification secrets require containment.
   - Vendor email verification tokens are now generated with 32 random bytes and stored as SHA-256 digests in the legacy `token` column.
   - Vendor mobile OTP values are now stored as BCrypt hashes in the legacy `otp` column.
   - Vendor verification logs no longer write raw email verification links, raw tokens, raw OTP values, or communication recipients.
   - Successful email/mobile verification clears the stored token or OTP hash.
   - Migration `V202608310002__vendor_verification_secret_hash_transition.sql` clears old pending plaintext vendor verification secrets; migration execution remains a release gate.
   - The more recent guest-checkout OTP flow provides a safer precedent by hashing OTPs, limiting attempts/resends, binding state to a session, and recording expiry/use state.

8. Credentials exist in environment-specific property files.
   - Rotate affected credentials and move all secrets to deployment configuration.
   - Never copy their values into tickets, audit events, logs, or this workflow.

### P1 - Authorization and Endpoint Design Gaps

1. Broad authorities replace module permissions.
   - Most protected admin pages use only `hasAuthority('admin')`.
   - Most protected customer pages use only `hasAuthority('customer')`.
   - Vendor guards cover only vendor staff/role screens; intended guards elsewhere are commented out.

2. Customer ownership is not consistently enforced at the repository boundary.
   - Customer endpoints include user IDs, recipient IDs, order IDs, wallet/reward records, PDFs, and messages.
   - Each lookup must include the authenticated customer ID or a trusted parent relationship.

3. Vendor ownership and membership are incomplete.
   - Follow the child vendor workflow for active membership, invitation, permission ceiling, repository scope, step-up authentication, suspension, revocation, and final-owner rules.

4. Admin routes use mixed prefixes.
   - 156 endpoints look administrative from their controllers/modules but are not under `/admin/**`.
   - Examples include catalog, product, shipping, promotion/reward, identity, role, and privilege administration.
   - URL appearance cannot be the security boundary, but consistent prefixes reduce configuration and operational mistakes.

5. State-changing unrestricted `@RequestMapping` remains common.
   - 0 GET routes now appear destructive in the regenerated source inventory.
   - 2 rows still accept every HTTP method: the framework-style `/error` and `/access-denied` handlers, where forwarded requests can originate from non-GET flows. The role/privilege/module rows, destructive deletes, password recovery, selected high-confidence mutations, and 128 additional admin, catalog, public, vendor, customer, order, cart, reward, promotion, and image-fragment page rows now use explicit methods. Thirty-one sensitive GET rows are additionally reviewed as read-only page, redirect, form, policy, export, or download responses.

6. API and callback trust models are undefined.
   - The commission API currently falls through to form-login authentication.
   - The EMI provider callback also falls through to form-login authentication instead of a documented provider-signature policy.

7. Public SEO and utility route intent is now aligned with configuration.
   - `/robots.txt`, `/sitemap.xml`, `/llms.txt`, `/maintenance`, `/district/select`, `/register`, `/customerregister/register`, and `/users/login` are explicitly permit-all and classified as public ingress.
   - These rows remain subject to public ingress input policy, CSRF for browser mutations, rate limiting, and runtime/browser proof.

8. Static `/files/**` is public as a complete family.
   - Public product media and private identity, invoice, export, or vendor documents must not share an undifferentiated public download boundary.

9. Authentication failures are logged but not used for a complete lockout/throttling policy.
   - Add account/IP throttling, bounded failure counters, safe responses, and support recovery.

10. Security headers, session-cookie policy, HTTPS enforcement, concurrency limits, and a general CORS policy were not explicitly configured in the inspected security configuration.

## Endpoint Ledger Review Contract

Every row in `application-security-endpoint-inventory.csv` must receive a manual final decision before its module is released.

Required final fields during implementation:

- final zone: `PUBLIC`, `CUSTOMER`, `VENDOR`, `PLATFORM_ADMIN`, `API`, `WEBHOOK`, or `INTERNAL_ONLY`
- exact HTTP method; `ALL` is not allowed for mutations
- authentication mechanism
- exact named permission or policy capability; blank, `N/A`, role-only, and route-inferred values are forbidden
- ownership/tenant/resource scope rule
- repository scope method
- CSRF/signature/replay rule
- rate-limit/idempotency rule where relevant
- audit event requirement
- test identifiers
- status: `APPROVED`, `IMPLEMENTED`, `VERIFIED`, or documented `DEFERRED`

Reconciliation rules:

1. Regenerate the source ledger after every controller change.
2. Export the running mappings from `/admin/system/endpoints` in a deployment-like environment.
3. Compare source and runtime rows.
4. Investigate framework, error, actuator, or third-party endpoints that exist only at runtime.
5. Fail the release when a non-framework endpoint has no approved classification.
6. Fail the release when a public route appears without an explicit public decision.
7. Fail the release when an implemented permission or ownership rule differs from the approved ledger.
8. Fail the release when any endpoint or linked service use case lacks an active catalogue capability.
9. Public capabilities must be explicit, non-assignable policy records rather than authorities granted to anonymous users.

## Decisions to Review Before Implementation

| ID | Recommended decision | Reason |
| --- | --- | --- |
| A-D01 | Use namespaced immutable permission slugs such as `platform.catalog.read`, `customer.order.access`, and `vendor.product.write`. | Prevents collisions and separates role names from permissions. |
| A-D02 | Treat roles as permission bundles only. Authorization components separately enforce current account state, ownership, vendor membership, and tenant/resource scope. | A role alone cannot prove resource access. |
| A-D03 | Seed protected system modules/permissions with versioned migrations. Do not allow ordinary UI users to create arbitrary authority slugs. | Protects the authorization vocabulary from privilege injection. |
| A-D04 | Keep a separately controlled platform break-glass administrator with MFA, step-up authentication, a reason, and full audit. Do not make ordinary `admin` a universal bypass. | Supports recovery without weakening least privilege. |
| A-D05 | Require MFA for platform administrators and strongly recommend it for vendor owners. | Reduces takeover risk for high-impact accounts. |
| A-D06 | Use opaque, hashed, expiring, single-use password-reset tokens and neutral recovery responses. | Replaces enumeration and insecure recovery behavior. |
| A-D07 | Store active sessions in a revocable session registry/store and add credential/permission versions. | Password, role, status, membership, and revocation changes must invalidate existing access. |
| A-D08 | Make public access method-specific. Anonymous browser mutations use CSRF, rate limits, DTO allowlists, and idempotency where applicable. | A public route is not an unprotected route. |
| A-D09 | Require provider signature verification, timestamp tolerance, replay protection, and idempotency for callbacks. | Form login is not a webhook authentication mechanism. |
| A-D10 | Move legacy administrative routes under `/admin/**` with temporary compatibility redirects after permission checks. | Reduces routing ambiguity while preserving links during migration. |
| A-D11 | Split public media from protected documents and authorize protected downloads through controllers/services. | Prevents sensitive files from inheriting `/files/**` public access. |
| A-D12 | Confirm the live database engine and introduce versioned migrations before changing identity/permission tables. | The project guidance names PostgreSQL while current profiles also include MySQL settings. |
| A-D13 | Use the vendor workflow's vendor-wide first release; keep branch permissions disabled until a real vendor-owned branch aggregate exists. | No safe branch security aggregate currently exists. |
| A-D14 | End with explicit route rules and `.anyRequest().denyAll()` after all runtime mappings are classified. Use staged compatibility rules only during migration. | Makes new or forgotten endpoints fail closed. |

## Target Authentication Model

### Account Loading

- Normalize login identifiers consistently.
- Load only an active account.
- Return generic authentication failures regardless of account existence or status.
- Build authorities from protected role/permission records and active vendor memberships.
- Add credential and permission versions to detect stale sessions.
- Remove plaintext-password fallback after a controlled one-time migration.
- Record last password change, failed-attempt state, lock expiry, and security revocation time.

### Login Protection

- Use the configured adaptive password encoder and one consistent password policy.
- Rate-limit by normalized account, IP, and device/risk signal.
- Apply bounded temporary lockout without enabling permanent denial-of-service.
- Require MFA for platform administrators.
- Preserve Spring Security session-fixation protection and make the policy explicit in configuration/tests.
- Audit success, failure category, lockout, MFA challenge, and recovery without recording credentials.

### Password Change and Reset

- Current-user password change requires current password plus the authenticated user identity.
- Platform password reset for another user requires `platform.identity.password.reset`, step-up authentication, and audit.
- Self-service recovery always returns a neutral response.
- Reset tokens are random, hashed, expiring, single use, purpose bound, and consumed transactionally.
- Password change/reset increments credential version and invalidates all sessions, remember-me tokens, reset tokens, and step-up grants.

### Session and Cookie Policy

- Use `Secure`, `HttpOnly`, and an approved `SameSite` value in production.
- Configure inactivity and absolute session lifetimes.
- Register sessions by user so status, password, role, vendor membership, and revocation changes can invalidate them.
- Limit concurrent platform-admin sessions unless an approved operational case requires otherwise.
- Clear customer carts only according to business policy; security-context invalidation must not delete business data.

## Target Authorization Model

### Layered Enforcement

1. `SecurityFilterChain`
   - Defines public, customer, vendor, platform-admin, API, webhook, and internal ingress zones.
   - Enables CSRF for browser sessions.
   - Applies headers, HTTPS, session, exception, and CORS policies.
2. Controller
   - Uses explicit HTTP methods, DTOs, validation, and defense-in-depth method guards.
3. Application/service entry point
   - Uses `@PreAuthorize` for the actual use case.
   - Calls dedicated authorization components for ownership or scope checks.
4. Repository
   - Includes customer/vendor/tenant scope in every protected lookup and mutation.
5. Database
   - Uses foreign keys, unique constraints, optimistic locking, and lifecycle constraints.
6. UI
   - Hides unavailable actions for usability only.

### Preferred Method-Authorization Shapes

Platform module permission:

```java
@PreAuthorize("hasAuthority('platform.catalog.manage')")
```

Customer-owned operation:

```java
@PreAuthorize("@customerAuthorization.canAccess(authentication, #customerId)")
```

Prefer deriving the current customer inside the service instead of accepting an arbitrary customer ID when the use case is always self-service.

Vendor operation:

```java
@PreAuthorize("@vendorAuthorization.can(authentication, #vendorId, 'vendor.order.update')")
```

Complex checks belong in dedicated components, not long SpEL expressions.

### Ownership and Scope Rules

| Zone | Mandatory rule |
| --- | --- |
| public cart/checkout | anonymous session/cart ownership, signed identifiers where needed, bounded quantities, idempotency, and abuse limits |
| customer | authenticated active customer plus current-user ownership in repository queries |
| vendor | active membership plus vendor permission plus vendor/resource ownership; branch only after approved foundation |
| platform admin | explicit module permission; customer/vendor impersonation or override is separately authorized and audited |
| API | client or user scope plus resource ownership and bounded DTOs |
| webhook | signature, timestamp, replay protection, idempotency, and aggregate correlation |
| internal job | authenticated system actor or non-web internal boundary plus explicit tenant/aggregate scope and audit |

## Universal Feature Permission Rule

Every feature must have a catalogue decision before implementation. This applies to controllers, APIs, application/service use cases, Thymeleaf actions, reports, imports, exports, uploads, downloads, PDFs, scheduled jobs, background consumers, callbacks, webhooks, and anonymous pages or forms.

Each feature maps to one primary named capability and, where necessary, an additional high-risk capability. The catalogue record must define:

- immutable namespaced slug, module, audience, and action
- `assignable` and `protected` flags
- anonymous/public allowance or required authentication mechanism
- sensitivity and step-up requirement
- ownership, membership, tenant, branch, and aggregate scope policy
- audit requirement, lifecycle status, and catalogue version

Capability namespaces are `platform.*`, `customer.*`, `vendor.*`, `api.*`, `webhook.*`, `internal.*`, and `public.*`. A `public.*` capability is a reviewed ingress policy and is never assigned to an anonymous user, account, or role. An `internal.*` capability is bound only to an approved system actor or non-web execution boundary and is not grantable through ordinary IAM screens.

No capability bypasses resource scope. Permission/policy, account state, ownership or membership, tenant/branch/aggregate scope, and independently scoped repository access must all pass where applicable. An unknown, inactive, blank, `N/A`, or arbitrary slug fails closed.

## Module Permission Catalogue

The following is the minimum family catalogue. The endpoint ledger supplies a candidate for every discovered route, and `application-security-permission-catalogue.csv` deduplicates those candidates for review. Implementation review replaces each candidate with the exact approved slug and links the corresponding service use case. Read and mutation permissions remain separate; high-risk operations receive dedicated permissions and step-up authentication. A dash means that action is not currently defined for the feature, never that access is allowed without a capability.

| Module | Read/access | Manage | High risk |
| --- | --- | --- | --- |
| platform dashboard | `platform.dashboard.read` | - | - |
| users | `platform.identity.user.read` | `platform.identity.user.manage` | `platform.identity.password.reset`, `platform.identity.session.revoke` |
| roles and permissions | `platform.iam.read` | `platform.iam.manage` | `platform.iam.protected.manage` |
| login/security audit | `platform.security.audit.read` | `platform.security.audit.manage` | `platform.security.audit.export` |
| customers | `platform.customer.read` | `platform.customer.manage` | `platform.customer.status.manage` |
| vendors | `platform.vendor.read` | `platform.vendor.manage` | `platform.vendor.approve`, `platform.vendor.owner.override` |
| catalog | `platform.catalog.read` | `platform.catalog.manage` | `platform.catalog.bulk.manage` |
| inventory | `platform.inventory.read` | `platform.inventory.manage` | `platform.inventory.adjust` |
| orders | `platform.order.read` | `platform.order.manage` | `platform.order.refund`, `platform.order.export` |
| payments and payouts | `platform.finance.read` | `platform.finance.manage` | `platform.finance.refund`, `platform.finance.payout.approve` |
| shipping | `platform.shipping.read` | `platform.shipping.manage` | `platform.shipping.provider.manage` |
| commission | `platform.commission.read` | `platform.commission.manage` | `platform.commission.override` |
| promotions/rewards | `platform.promotion.read` | `platform.promotion.manage` | `platform.promotion.balance.adjust` |
| communication | `platform.communication.read` | `platform.communication.send` | `platform.communication.provider.manage` |
| fraud | `platform.fraud.read` | `platform.fraud.review` | `platform.fraud.admin`, `platform.fraud.finance` |
| blog/content | `platform.content.read` | `platform.content.manage` | `platform.content.publish` |
| marketing/SEO/ads | `platform.marketing.read` | `platform.marketing.manage` | `platform.marketing.tracking.manage` |
| global settings | `platform.settings.read` | `platform.settings.manage` | `platform.settings.sensitive.manage` |
| system endpoints/seeds | `platform.system.read` | `platform.system.manage` | `platform.system.seed`, `platform.system.endpoint.export` |
| customer account | `customer.account.read` | `customer.account.manage` | `customer.account.password.change` |
| customer orders | `customer.order.read` | `customer.order.manage` | `customer.order.return`, `customer.order.cancel` |
| customer wallet/rewards | `customer.reward.read` | `customer.reward.redeem` | `customer.reward.cashout` |
| customer communication | `customer.communication.read` | `customer.communication.manage` | `customer.communication.export` |
| vendor modules | See vendor child workflow | See vendor child workflow | payout, refund, staff/role, owner, and settings step-up |
| public storefront/content | `public.catalog.read`, `public.content.read` | `public.review.create` | `public.contact.submit`, `public.subscription.manage` |
| public cart/checkout | `public.cart.read` | `public.cart.manage` | `public.checkout.create`, `public.checkout.otp.verify` |
| public identity/token actions | `public.identity.form.read` | `public.identity.register`, `public.identity.recover` | `public.identity.email.verify`, `public.identity.token.consume` |
| API modules | `api.<module>.read` | `api.<module>.manage` | exact high-risk `api.<module>.<action>` |
| provider callbacks | `webhook.<provider-or-module>.receive` | replay-safe aggregate update under the receive capability | provider-specific sensitive callback capability where required |
| scheduled/background work | `internal.<module>.read` | `internal.<job>.execute` | exact `internal.<job>.<high-risk-action>` |

Permission administration rules:

- Slugs are unique, immutable after release, and seeded through migrations.
- Protected permissions cannot be deleted or repurposed from the UI.
- A role editor cannot grant permissions outside their own grant authority.
- A user cannot change their own platform roles or use a role update to retain a stale session.
- Permission changes increment a permission version and invalidate affected sessions/caches.
- Every catalogue slug has at least one approved feature/use-case mapping or a documented migration reservation; orphaned active slugs fail review.
- `public.*` and `internal.*` policy capabilities are protected and non-assignable through ordinary role administration.

## Public Endpoint Policy

Public endpoints are divided into explicit categories:

1. Anonymous read
   - home, product discovery, public blog, policies, SEO files, and explicitly public media
   - GET/HEAD only
2. Anonymous browser mutation
   - registration, contact, subscription, blog comment, cart mutation, guest OTP, address, and checkout/order placement
   - POST only, CSRF, rate limit, DTO allowlist, validation, and idempotency where money/order state is involved
3. Token action
   - unsubscribe, email verification, password reset, and invitation acceptance
   - opaque hashed token, purpose, expiry, single use where appropriate, and neutral response
4. Provider callback/webhook
   - signature, timestamp, replay cache, idempotency key, provider/aggregate binding, and safe logging

Do not place a route in `permitAll` merely because the UI is public. The handler's input, state transition, ownership, replay, and abuse controls must all pass review.

## Phased Implementation Plan

### S01 - Decisions, Database, and Release Baseline

Status: `Pending`

Description: Establish the approved security decisions, database/migration baseline, backups, compatibility rules, and rollback plan before further rollout.

- Approve A-D01 through A-D14.
- Confirm the live database engine and migration mechanism.
- Back up role, privilege, user-role, session, and vendor-membership data.
- Define compatibility and rollback rules.
- Freeze new features and endpoints until they include an inventory classification and named catalogue capability.

Exit gate:

- Decisions, migration tooling, backup, and rollback are approved and tested outside production.

### S02 - P0 Containment

Status: `Partial`

Description: Remove urgent exposure from unsafe mutations, identity administration, logout/password paths, CSRF coverage, and vendor-IAM entry points.

Implemented in the platform IAM slice:

- role, privilege, and module handlers now use explicit GET/POST mappings
- their destructive GET routes were removed
- their unsafe methods require Spring Security CSRF

Implemented in the user-identity containment slice (`Partial - source verified`):

- user lists, status views, legacy user-details, and arbitrary identity details require named platform identity access
- `/users/view/{uid}` permits platform identity readers or the authenticated customer owning that exact user ID
- login-history views require the separate `platform.security.audit.*` capability family at controller and service boundaries, and the admin view no longer exposes or searches raw servlet session identifiers
- user mutations and another-user password reset use separate manage and password-reset capabilities with a temporary legacy-admin bridge
- broad read mappings were narrowed to explicit GET mappings, and the combined containment suite passes all 62 focused security/regression tests
- the broken legacy password mutation is disabled with exact GET/POST mappings and a fail-closed method guard
- logout is owned by Spring Security as a POST-only, CSRF-protected operation, and all eight template surfaces submit CSRF forms

Implemented in the platform vendor-IAM containment slice (`Partial - source verified`):

- all ten `/adminvendorusers/**` routes require exact approved `platform.vendor.management.*` capabilities at URL and controller boundaries
- transactional `AdminVendorIamService` methods repeat the permission boundary for direct invocation and keep controller writes out of shared vendor services
- role and permission saves reject duplicate or invalid slugs, including non-`vendor.*` permission entries, and assigned roles or permissions cannot be deleted
- both delete routes are POST-only, scoped CSRF applies, and all three affected template surfaces submit explicit CSRF forms
- the combined containment regression suite passes all 71 focused tests

Implemented in the destructive GET-route containment slice (`Partial - source verified`):

- all 9 Phase 1 destructive delete endpoints were converted from GET to POST
- affected admin, vendor, advertising, catalog attribute, manufacturer, and UOM templates now submit POST forms with CSRF tokens instead of delete links
- Spring Security CSRF coverage now includes the affected browser mutation route families
- source inventory now reports 0 destructive GET routes, and the focused route/CSRF/runtime-inventory suite passes all 87 tests; migration and deployment-like runtime proof remain outstanding

Implemented in the explicit mutation-route containment slice (`Partial - source verified`):

- nine high-confidence product, category, vendor product/profile, payout, customer profile, and public contact mutation handlers now use explicit POST mappings instead of unrestricted `@RequestMapping`
- affected forms now include CSRF tokens, and category detail delete links were replaced with POST forms
- source inventory now reports these mutation routes as POST and the focused route/CSRF/runtime-inventory suite passes all 106 tests; migration and deployment-like runtime proof remain outstanding

Implemented in the broader CSRF and page-method containment slice (`Partial - source verified`):

- Spring Security CSRF coverage now includes additional browser mutation families: admin-customer order administration, customer-order mutations, product image fragments, vendor product image fragments, vendor logo changes, customer profile images, vendor order operations, reward/coupon/gift-card/wallet flows, and promotion administration
- 128 admin, catalog, public, vendor, customer, order, cart, reward, promotion, and image-fragment page rows were converted from unrestricted `@RequestMapping` to explicit GET mappings so POST/DELETE traffic is reserved for intentional mutation handlers
- refreshed source inventory now reports 2 all-method rows, down from 130, the focused route/CSRF/source-contract suite passes all 176 tests, and the full Maven test suite passes all 407 tests; migration execution and deployment-like browser/runtime proof remain outstanding

Implemented in the password-recovery lifecycle slice (`Partial - source verified`):

- recovery display routes are explicit GET mappings and recovery submissions are explicit POST-only
- requests return a neutral response for blank, unknown, and known addresses to prevent account enumeration
- reset capabilities use 32-byte random tokens, SHA-256 digests in `password_reset_tokens`, a configurable 30-minute expiry, and an atomic single-use consume update before the BCrypt password change
- reset links are sent directly through the configured mail sender so raw tokens are not retained in communication jobs; production should set `app.security.password-reset.base-url` to the trusted public HTTPS origin; both legacy and public forms remain CSRF-covered
- focused route, lifecycle, CSRF, and runtime-inventory checks pass; production SMTP configuration, migration execution, and deployment-like runtime proof remain outstanding

Implemented in the session and credential versioning slice (`Partial - source verified`):

- `usermodule_users.credential_version` is a separate account epoch from the legacy audit `version` column and is advanced for password changes/resets and account-status changes
- successful logins bind the current epoch to the HTTP session, active sessions are registered with Spring Security, and password/access changes expire all matching in-process sessions
- `CredentialVersionFilter` rejects a marked session when the database epoch has changed, providing a cross-node database backstop; focused service/filter/contract tests pass
- vendor staff assignment removal and vendor role permission changes now advance affected users' credential epochs, forcing stale vendor-authority snapshots to be revalidated; full membership lifecycle, migration execution, and deployment-like runtime/browser proof remain outstanding

Implemented in the baseline headers and secret-log containment slice (`Partial - source verified`):

- Spring Security explicitly migrates the session ID after authentication, emits same-origin frame policy, HSTS with subdomains, and a strict-origin-when-cross-origin referrer policy
- remote/live profiles already set secure, HTTP-only, SameSite=Lax session cookies and framework forwarded-header handling
- vendor verification communication logs retain event/vendor context while removing raw verification links, tokens, OTP values, email addresses, mobile numbers, and recipients
- vendor email verification now stores a SHA-256 token digest, mobile verification stores a BCrypt OTP hash, verified secrets are cleared after successful use, and migration `V202608310002__vendor_verification_secret_hash_transition.sql` clears prior pending raw values
- global CSRF rollout, credential rotation, migration execution, and deployment-like runtime/browser proof remain outstanding

Other P0 containment items remain pending:

- Rotate exposed credentials and externalize all deployment secrets.
- Continue the standard CSRF rollout with documented temporary exceptions only where necessary.
- Convert the remaining mutation-capable and ambiguous `@RequestMapping` handlers to explicit methods.

Exit gate:

- An ordinary authenticated customer cannot access identity administration or mutate another account.

### S03 - Authentication and Account Recovery Foundation

Status: `Partial`

Description: Build consistent login, password, lockout, MFA, secure reset, session-version, cookie, and session-fixation controls.

- Implement consistent account loading, password policy, login throttling, lockout, neutral errors, and password migration.
- Implemented the hashed, expiring, single-use password-reset capability and explicit reset form; SMTP configuration and deployment verification remain pending.
- Add MFA for platform administrators.
- Session/credential versioning, password/account-status session invalidation, and vendor staff assignment/role-change authority invalidation are implemented; full membership lifecycle and sensitive-action grant invalidation remain pending.
- Configure secure production cookie lifetimes, concurrency limits, and remaining deployment checks; secure cookie attributes are present in remote/live profiles and baseline session-fixation/security headers are source-configured.

Exit gate:

- Login, password change, reset, lockout, MFA, logout, and session revocation integration tests pass.

### S04 - Protected Module and Permission Catalogue

Status: `Partial`

Description: Define immutable namespaced capabilities, migrate legacy authorities safely, and prevent privilege escalation or protected-permission changes.

Implemented in the platform IAM slice:

- `platform.iam.read`, `platform.iam.manage`, and `platform.iam.protected.manage` are source-defined and included in a controlled PostgreSQL seed
- public/internal/webhook capabilities cannot be assigned to platform roles through the IAM service
- protected catalogue slugs cannot be created, renamed, or deleted through application controllers
- role grants cannot exceed the actor's effective authorities, including through the legacy admin bridge
- database migration, protected-role assignment, audit, versioning, session invalidation, and remaining module catalogue work remain pending

- Add immutable namespaced permission slugs and protected metadata.
- Migrate legacy authorities without removing working access before verification.
- Separate platform, customer, vendor, API, webhook, internal, and public policy namespaces.
- Finalize a named capability for every endpoint row and every linked non-web service use case.
- Protect role/permission administration against self-escalation and protected-permission changes.
- Add optimistic locking and audit.

Exit gate:

- Every feature is mapped to an active catalogue capability; no ordinary role editor can create arbitrary authority, assign public/internal policy capabilities, or expand their own access.

### S05 - Runtime Endpoint Security Registry

Status: `Pending`

Description: Reconcile source routes with mappings registered by the running application and fail closed on unclassified or unexpectedly public endpoints.

- Extend the existing runtime endpoint metadata with security zone, current URL rule, method guard, candidate and final capability, ownership rule, module, linked service use case, and review status.
- Reconcile it with the generated source CSV.
- Exclude framework error mappings only through documented rules.
- Add a build/release check for unclassified routes and unexpected public routes.

Exit gate:

- Source and running mappings reconcile, and every application endpoint has an approved policy row with a final named capability.

### S06 - Method-Level Authorization Foundation

Status: `Partial`

Description: Enforce capability, ownership, tenant/vendor scope, and account-state decisions at controller and service boundaries.

Implemented in the platform IAM slice:

- dedicated `PlatformIamAuthorization` component
- controller and transactional application-service `@PreAuthorize` boundaries for role, privilege, and module administration
- direct controller repository mutation removed from these three surfaces
- method-security and MVC permission/CSRF tests added
- remaining platform, customer, vendor, API, webhook, internal, and resource authorization boundaries remain pending

- Keep `@EnableMethodSecurity` enabled.
- Add dedicated platform, customer, vendor, API, and resource authorization components.
- Add `@PreAuthorize` to application/service entry points before controller rollout.
- Require a named catalogue capability at public, customer, vendor, platform, API, webhook, and internal use-case boundaries; do not treat anonymous or system execution as permission-free.
- Remove controller-direct repository mutation where a service transaction is required.
- Use simple annotations for direct permissions and components for ownership/scope.
- Deny when actor, account state, scope, ownership, or permission is missing.

Exit gate:

- Direct service tests prove that controller bypass cannot bypass authorization and no use-case entry point accepts an unknown or missing capability.

### S07 - Public, Registration, Cart, Guest Checkout, and SEO

Status: `Pending`

Description: Secure anonymous ingress, registration, cart, guest checkout, OTP, redirects, throttling, CSRF, and public SEO behavior.

- Manually confirm every public ledger row.
- Replace broad public wildcards with method-specific rules.
- Correct SEO, maintenance, district-selection, unsubscribe, registration, and recovery route intent.
- Add DTO allowlists, CSRF, throttling, neutral errors, idempotency, session ownership, and safe redirects.
- Preserve the guest-checkout OTP hashing/attempt-limit foundation and add tests around all anonymous state transitions.

Exit gate:

- Every anonymous mutation has an approved threat model and no authenticated/private endpoint is accidentally public.

### S08 - Customer Modules and Ownership

Status: `Pending`

Description: Protect customer-owned profiles, addresses, orders, payments, wallets, rewards, reviews, wishlist, and communication data.

Roll out in this order:

1. profile, address, image, password, and notification ownership
2. order, payment, invoice/PDF, return, and refund visibility
3. wallet, cash-out, rewards, referral, gift card, redemption, and cashback
4. wishlist, reviews, team, products, blog, and communication

For each endpoint:

- derive current customer from authentication where possible
- add service authorization
- add customer ID/owner predicate to repository queries
- deny cross-customer IDs with a non-enumerating response
- add CSRF and audit for mutations
- update the endpoint ledger to `VERIFIED`

Exit gate:

- Two-customer tests prove no read, write, PDF, notification, reward, or financial cross-account access.

### S09 - Platform Identity, System, Settings, and Audit Modules

Status: `Pending`

Description: Secure platform users, roles, permissions, sessions, recovery, settings, audit, location data, and break-glass administration.

- Secure users, login history, roles, permissions, modules, password reset, sessions, system endpoint registry, location seeding, global settings, and security audit.
- Require dedicated high-risk permissions and step-up authentication.
- Protect final platform administrator/break-glass access.
- Replace hard delete with status/lifecycle transitions where history matters.

Exit gate:

- Limited admins cannot administer IAM/settings or elevate themselves.

### S10 - Platform Commerce Modules

Status: `Pending`

Description: Apply least-privilege permissions and scoped service boundaries across catalog, vendors, inventory, commerce, shipping, finance, content, and fraud.

Roll out module permissions and scoped service boundaries for:

- customer administration
- vendor administration and approval
- catalog, category, attributes, variants, media, units, manufacturer, and warranty
- inventory and stock adjustment
- order, return, refund, payment, EMI, PDF, and export
- shipping, carriers, rates, zones, packaging, pickup, delivery, labels, manifests, and invoices
- commission, finance, payout, promotion/reward, communication, blog/content, marketing/SEO/ads, and fraud

Exit gate:

- Each admin role accesses only its approved modules and high-risk actions require their dedicated permission/step-up policy.

### S11 - Vendor Owner and Staff Security

Status: `Pending`

Description: Complete vendor membership, invitations, roles, branch scope, ownership, suspension, revocation, audit, and session invalidation controls.

- Execute `vendor-staff-method-security-workflow.md` in its documented dependency order.
- Reconcile its endpoint work with the parent endpoint ledger.
- Do not activate branch scope until the branch foundation exists.

Exit gate:

- All vendor child-plan release gates pass.

### S12 - APIs, Webhooks, Files, Exports, and Background Jobs

Status: `Pending`

Description: Secure non-browser interfaces with API authentication, callback signatures, replay protection, protected files, export controls, and scoped system actors.

- Define API authentication/scopes and JSON 401/403 behavior.
- Add provider signature, replay, and idempotency protection to callbacks.
- Separate public media from protected files and authorize protected downloads.
- Apply permission and ownership checks to exports/PDFs.
- Give scheduled jobs an explicit system actor and tenant/aggregate scope.
- Assign a protected `internal.*` capability to each scheduled/background use case and a protected `webhook.*` capability to each callback handler.
- Prevent internal methods from becoming unauthenticated web entry points.

Exit gate:

- API, callback, file, export, and background-job tests pass without browser-session assumptions.

### S13 - Audit, Revocation, Monitoring, and Security Operations

Status: `Pending`

Description: Add immutable security events, timely revocation, safe metrics and alerts, retention rules, and incident-response procedures.

- Add append-only security events for authentication, IAM, customer override, vendor security, high-risk financial action, access denial, and revocation.
- Invalidate sessions/tokens/caches on account, password, permission, role, and membership changes.
- Add safe security metrics and alerts without secrets or sensitive payloads.
- Define retention, access, export, and incident-response procedures.

Exit gate:

- Security events are immutable through application APIs and revocation takes effect in existing sessions.

### S14 - Full Verification, Deployment, and Fail-Closed Cutover

Status: `Pending`

Description: Complete integration, migration, concurrency, deployment-like, and runtime reconciliation checks before fail-closed production cutover.

- Run unit, repository, method-security, MVC, integration, migration, concurrency, and deployment-like tests.
- Reconcile the source inventory with runtime endpoints.
- Confirm all 742 current source rows plus any new/runtime rows are approved and verified.
- Confirm every current and new feature, including non-web use cases, resolves to an active final catalogue capability.
- Change the final unmatched URL rule to deny.
- Deploy migrations before code that requires them and retain a tested rollback that does not erase audit/history.

Exit gate:

- All release gates pass with deployment-like evidence and no unclassified or capability-unmapped application feature remains.

## Required Test Matrix

### Public and Anonymous

- explicit public GET succeeds anonymously
- non-public route redirects or returns 401 according to channel
- anonymous mutation without CSRF is rejected
- registration cannot submit roles, status, user type, balance, vendor, or platform privileges
- cart/checkout cannot access another session's cart/order
- OTP/recovery/invitation tokens expire, cannot be replayed, and are not stored/logged raw
- rate limits and neutral responses prevent account enumeration

### Customer

- own profile/order/address/wallet/reward/message/PDF: permitted according to lifecycle
- another customer's ID: denied without existence disclosure
- customer calls admin/vendor route: denied
- blocked/pending customer: denied
- password change requires current password and revokes existing sessions

### Vendor

- run every test in the vendor child workflow
- customer or unrelated vendor identity: denied
- cross-vendor ID/list/export/PDF: denied or empty by scoped repository
- suspended/revoked staff and escalation attempts: denied

### Platform Admin

- required module permission: permitted
- missing module permission: denied
- broad `admin` role without specific permission after migration: denied
- self-role/self-permission elevation: denied
- protected permission or final break-glass admin removal: denied
- refund, payout, IAM, and sensitive settings without step-up: denied

### API, Webhook, File, and Background

- browser session cannot substitute for required API/client scope where prohibited
- invalid/missing callback signature: rejected
- expired timestamp or replayed callback: rejected
- duplicate callback remains idempotent
- protected file/export requires permission and ownership
- background command always carries explicit system actor and tenant/aggregate scope

### Endpoint Drift

- generated source ledger has no unresolved handler method
- runtime application endpoints reconcile with source rows
- no application endpoint is unclassified
- no unexpected endpoint is public
- no mutation accepts every HTTP method
- no GET endpoint changes persistent state

### Permission Catalogue Completeness

- every source and runtime endpoint row has a nonblank candidate and approved final capability
- every application/service entry point, scheduled job, consumer, callback, report, export, import, file action, and UI mutation maps to an active catalogue record
- no active slug is orphaned, unknown, duplicated, mutable, or accepted only because a broad role is present
- no `public.*` or `internal.*` policy capability is assignable through ordinary IAM or vendor-role administration
- missing, inactive, or unrecognized capabilities deny access
- permission success without required ownership, membership, tenant, branch, aggregate, and repository scope is denied

## Release Gates

- [ ] Every application endpoint has a final approved zone, method, permission, scope, and test.
- [ ] Every web and non-web feature/use case has an active named catalogue capability; none uses blank, `N/A`, role-only, or inferred-by-route authorization.
- [ ] Public and internal policy capabilities are protected, non-assignable, and enforced at their approved ingress/use-case boundaries.
- [ ] Source and runtime endpoint inventories reconcile.
- [ ] `@EnableMethodSecurity` is enabled and application/service entry points are protected.
- [ ] No service-layer use case relies only on controller URL rules or hidden UI.
- [ ] Platform module permissions replace universal `admin` access where least privilege applies.
- [ ] Customer repository queries enforce current-user ownership.
- [ ] Vendor membership, permission, ownership, scope, and revocation gates pass.
- [ ] Public routes are explicit and method-specific.
- [ ] Public mutations have CSRF, validation, rate limiting, and idempotency where required.
- [ ] Password reset and verification secrets are hashed, expiring, and replay safe.
- [ ] Raw passwords, OTPs, tokens, credentials, and session cookies are absent from logs/audits.
- [ ] Destructive GET and mutation-capable all-method mappings are removed.
- [ ] APIs and callbacks use their approved non-browser authentication model.
- [ ] Public and protected files are separated and protected downloads are authorized.
- [ ] Role/permission and account changes invalidate affected sessions and caches.
- [ ] MFA and step-up authentication protect approved high-risk actions.
- [ ] CSRF, headers, HTTPS, cookie, session, CORS, and exception policies are verified.
- [ ] Secrets are externalized and exposed credentials rotated.
- [ ] Two-customer, two-vendor, limited-admin, suspended-user, and cross-scope tests pass.
- [ ] The unmatched endpoint rule fails closed.
- [ ] Deployment-like runtime evidence is attached; source-only checks do not count as release proof.

## Implementation Record

| Date | Work package | Artifact/evidence | Result | Remaining |
| --- | --- | --- | --- | --- |
| 2026-08-14 | S00 source audit and parent workflow | endpoint CSV, permission catalogue CSV, generator, this workflow | `Done` | S01-S14 implementation |
| 2026-08-14 | S02/S04/S06 platform IAM foundation | IAM authorization component/service/controllers, PostgreSQL seed, 13 focused tests, regenerated catalogues | `Partial - source verified` | database migration, runtime proof, audit/version/session invalidation, all other security surfaces |
| 2026-08-28 | S02 user identity containment | ownership-aware authorization component, named identity/audit capabilities, exact read mappings, scoped repository predicate, refreshed decision ledgers, 56 focused tests | `Partial - source verified` | permission seeding/migration, step-up and audit, adjacent vendor IAM, broader CSRF/headers/session/recovery, runtime/browser proof |
| 2026-08-29 | S02 legacy password and logout containment | disabled legacy password mutation, POST-only Spring Security logout, CSRF matcher, eight template form conversions, route/template tests, refreshed decision ledgers, 62 focused tests passing | `Partial - source verified` | adjacent vendor IAM, broader CSRF/headers/session/recovery, migration and runtime/browser proof |
| 2026-08-29 | S02 platform vendor-IAM containment | exact `platform.vendor.management.*` URL/controller/service guards, transactional application service, POST-only role/permission deletes, CSRF forms, assignment-safe deletes, refreshed decision ledgers, 71 focused tests passing | `Partial - source verified` | permission migration and step-up/audit, vendor-scoped staff IAM, broader CSRF/headers/session/recovery, runtime/browser proof |
| 2026-08-30 | S02 session-administration containment | `SessionAdministrationService` audit guard, login-history repository search no longer matches raw session IDs, admin template shows audit reference instead of session ID, refreshed decision ledgers, 74 focused containment tests passing | `Partial - source verified` | permission migration and runtime proof, S03 session revocation/versioning, vendor-scoped staff IAM, broader CSRF/headers/session/recovery |
| 2026-08-30 | S02 destructive GET-route containment | 9 delete endpoints converted to POST-only, affected templates converted to CSRF POST forms, CSRF matcher expanded, refreshed decision ledgers, 87 focused tests passing | `Partial - source verified` | migration and runtime/browser proof, mutation-capable all-method mappings, broader CSRF/headers/session/recovery |
| 2026-08-30 | S02 explicit mutation-route containment | 9 high-confidence product, vendor, customer, payout, and public contact mutations converted to explicit POST mappings, CSRF forms/matcher updated, refreshed decision ledgers, 106 focused tests passing | `Partial - source verified` | remaining mutation-capable all-method mappings, migration and runtime/browser proof, broader CSRF/headers/session/recovery |
| 2026-08-30 | S03 password-recovery lifecycle | explicit GET/POST recovery and reset routes, neutral responses, hashed 30-minute single-use tokens, direct mail delivery, reset form, MySQL migration, refreshed decision ledgers, 114 focused tests passing | `Partial - source verified` | SMTP configuration, migration execution, session-version invalidation, deployment/runtime/browser proof, remaining all-method mappings |
| 2026-08-31 | S03 session and credential versioning | credential epoch migration/entity, Spring session registry, login-session binding, stale-session filter, password/reset/status-change wiring, 6 focused revocation/filter/contract tests and full Maven suite (334 tests) passing | `Partial - source verified` | vendor membership/role revocation, permission-cache invalidation, migration execution, deployment/runtime/browser proof |
| 2026-08-31 | S02/S03 Phase 2 closeout containment | vendor staff assignment/role-change credential epoch invalidation, vendor verification secret-log redaction, explicit session-fixation migration, HSTS, same-origin frame policy, referrer policy, 3 new focused contract/method checks | `Partial - source verified` | full invitation lifecycle, broader CSRF rollout, credential rotation, migration execution, deployment/runtime/browser proof |
| 2026-08-31 | S02 vendor verification hashed-secret containment | vendor email tokens stored as SHA-256 digests, vendor mobile OTP stored as BCrypt hashes, successful verification clears stored secrets, MySQL cleanup migration added, 4 focused service/logging/route tests passing | `Partial - source verified` | migration execution, runtime email/SMS proof, credential rotation, broader invitation lifecycle |
| 2026-08-31 | S02 broader CSRF and page-method containment | expanded CSRF matcher to admin-customer, customerorder, product/vendor image, vendor logo, customer profile image, vendor-order, reward/coupon/gift-card/wallet, and promotion mutation families; 128 page/display rows converted to GET-only; refreshed inventory and decision ledgers; 176 focused tests and 407 full-suite tests passing | `Partial - source verified` | migration execution, deployment/runtime/browser proof, credential rotation, invitation lifecycle, action-style GET review |
| 2026-08-31 | S02 sensitive read-only GET review | 30 sensitive GET rows source-reviewed as read-only page, redirect, form, policy, export, or download responses; decision-ledger generator now approves only exact reviewed read-only exceptions; remaining action-style/unclassified GET blockers stayed deferred; refreshed decision ledgers; 3 focused ledger-contract tests and 410 full-suite tests passing | `Partial - source verified` | migration execution, deployment/runtime/browser proof, credential rotation, invitation lifecycle, action-style GET containment |
| 2026-08-31 | S02 action-style GET containment | vendor email verification now uses a read-only GET confirmation page plus CSRF-covered POST token consumption; `/vendorverifications/**` added to the Spring CSRF matcher; legacy referral verify routes are read-only redirects; disabled `/changepassword` rows map to a named internal disabled capability; refreshed inventory and decision ledgers; 192 focused tests and 422 full-suite tests passing | `Partial - source verified` | migration execution, deployment/runtime/browser proof, credential rotation, invitation lifecycle, remaining public/zone/API/webhook/method deferrals |
| 2026-08-31 | S02 public ingress classification cleanup | public SEO, maintenance, district-selection, and registration ingress routes explicitly permit-all and classified as public; refreshed inventory and decision ledgers; endpoint deferrals reduced from 19 to 10 | `Partial - source verified` | focused/full test proof, migration execution, deployment/runtime/browser proof, remaining owner-zone/API/webhook/error-method deferrals |
| 2026-08-31 | S02 remaining owner-zone classification | marketplace-wide `/order` list restricted to the named platform order-read capability; `/users/login` explicitly public; user profile/view routes classified to public, customer, and platform zones; refreshed inventory and decision ledgers; endpoint deferrals reduced from 10 to 4 | `Partial - source verified` | API authentication/resource scope, webhook signature/replay/idempotency, framework error-handler method review, migration and deployment/runtime proof |
