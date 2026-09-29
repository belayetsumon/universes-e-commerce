# Vendor Staff and Method-Level Security Workflow

## Purpose

This document is the review and implementation tracker for vendor-owner, vendor-staff, method-authorization, vendor-scope, invitation, step-up authentication, revocation, and audit controls.

The workflow is based on the current source tree. It does not mark a security control complete merely because a menu item is hidden or a controller route requires login.

This is a child plan of `docs/security/application-security-authentication-authorization-workflow.md`; parent endpoint classification, authentication, CSRF, session, permission-catalogue, and release gates also apply.

## Implementation Boundary

Status: `Partial implementation in progress`

- The codebase audit and this workflow are complete.
- The existing `/vendor-users/**` staff and custom-role administration paths now have source-level containment evidence.
- Review the decisions in this document before implementation starts.
- Implement one phase at a time and keep the tracker current.

## Status Legend

- `Done`: verified in the current source or completed with evidence
- `Partial`: some foundation exists but the control is not complete
- `Pending`: not implemented
- `Blocked`: requires a decision or prerequisite
- `Deferred`: intentionally outside the current release

## Progress Summary

| Measure | Count |
| --- | ---: |
| Planning work packages complete | 1 |
| Implementation work packages complete | 1 |
| Implementation work packages remaining | 11 |
| Security implementation started | Yes |

## Current Codebase Audit

### Existing Foundation

| Area | Status | Current evidence |
| --- | --- | --- |
| Method authorization switch | `Done` | `SecurityConfig` already has `@EnableMethodSecurity`. |
| URL authentication | `Partial` | `SecurityConfig` requires authentication for unmatched routes, but authentication alone is not vendor authorization. |
| Vendor permissions | `Partial` | `VendorPrivilege`, `VendorRole`, and `UserVendorRole` exist. Authorities are emitted as `VENDOR_{vendorId}:{privilegeSlug}`. |
| Dedicated checks | `Partial` | `VendorAccessAuthorityChecker`, `VendorPrivilegeChecker`, and `VendorRoleChecker` exist, but they do not consistently evaluate membership state, vendor state, resource ownership, or grant ceilings. |
| Vendor-scoped repository examples | `Partial` | Some repositories query by vendor, such as payout lists, delivery-person lists, roles, shipments, and reports. Other mutation paths still load or delete by unscoped ID. |
| Login/session history | `Partial` | Login history records session IDs and a session-destroyed listener exists. There is no service that invalidates all live sessions for revoked staff. |
| JPA field auditing | `Partial` | Created/modified metadata exists on several entities. It is not an append-only staff-security event audit. |
| Security tests | `Partial` | Vendor staff IAM containment now has controller, service method-security, template, and CSRF coverage tests. Broader membership lifecycle, revocation, step-up, audit, and suspended-staff tests remain pending. |

### Verified Gaps

1. Method guards are not deployed across the vendor application boundary.
   - The 24 vendor and vendor-user controllers contain 133 request-mapping annotations but only 5 active `@PreAuthorize` annotations.
   - Those 5 annotations are limited to the two vendor-user controllers.
   - The 10 services under `vendor/services` and `vendor/user/services` contain no active `@PreAuthorize` annotations.
   - Many intended guards in product, order, stock, shipping, reports, and payout controllers are commented out.

2. The active-vendor context is not a sufficient security boundary.
   - `VendorUserContext` stores a mutable `Vendorprofile` entity in the HTTP session.
   - `VendorProfileController` selects the first vendor owned through `Vendorprofile.userId`.
   - It does not resolve staff vendors through active memberships.
   - Service authorization should not depend on a session-scoped entity.

3. Membership lifecycle is missing.
   - `UserVendorRole` contains only user, vendor, role, and basic created/modified metadata.
   - There is no `PENDING_VERIFICATION`, `ACTIVE`, `SUSPENDED`, or `REVOKED` membership state.
   - There are no activation, suspension, reactivation, or revocation timestamps and reasons.
   - `UsersDetails` loads authorities from every assignment without checking membership state or active vendor state.

4. Staff invitation is currently a direct assignment.
   - `/vendor-users/save` finds an already-active platform user by email and immediately saves `UserVendorRole`.
   - There is no invitation record, expiring token, token hash, single-use consumption, or verification gate.
   - New users cannot complete a vendor-staff invitation workflow.

5. Privilege escalation controls are incomplete.
   - Controller authorization permits broad role-name bypasses such as `ADMIN`, `OWNER`, and `VENDOR_OWNER`.
   - Vendor role forms load all `VendorPrivilege` rows.
   - There is no policy comparing requested permissions and scopes with the inviter's effective access.
   - There is no explicit self-modification, self-promotion, final-owner, or platform-role guard.

6. Several mutation paths are vulnerable to cross-vendor ID use unless every controller check remains correct.
   - Staff assignment deletion calls `deleteById(id)` without adding vendor scope to the delete query.
   - Payout-method edit/delete uses unscoped `findById` and `deleteById`.
   - Vendor-profile save accepts an ID and loads `Vendorprofile` by that ID without owner or membership scope in the repository query.
   - Product and delivery code contains a mixture of manual ownership checks and unscoped repository loads.
   - Repository scope must be present even when a service authorization check has already passed.

7. Sensitive-action step-up authentication is missing.
   - Payout request guards are commented out.
   - Payout, refund, staff-role administration, and sensitive settings do not share a password re-authentication service.
   - Current password matching helpers are controller-local and are not a reusable, purpose-bound step-up control.

8. Revocation cannot take effect immediately in existing authenticated sessions.
   - Vendor authorities are captured when `UsersDetails` creates the authenticated principal.
   - `HttpSessionEventPublisher` observes session destruction but does not provide revocation by user or membership.
   - No permission-cache version or eviction mechanism was found.

9. Branch scope has no domain foundation.
   - No vendor branch aggregate or branch-membership model was found.
   - `Warehouse` is currently a small non-entity value class and must not be treated as a security branch.
   - Branch-scoped grants must remain disabled until a real vendor-owned branch aggregate and scoped repositories exist.

10. Security configuration has release-blocking baseline gaps.
    - Spring Security CSRF is enabled for selected browser mutation route families, including `/vendor-users/**`, but whole-application CSRF rollout and public-mutation review remain incomplete.
    - Some public order mutation routes require an explicit threat-model review before CSRF is re-enabled.
    - Database credentials are present in environment-specific property files and must be rotated and externalized without copying their values into logs or documentation.
    - No Flyway or Liquibase migration setup was found; schema changes currently rely on Hibernate update/manual SQL patterns.

## Decisions to Review Before Implementation

| ID | Recommended decision | Reason |
| --- | --- | --- |
| D01 | Evolve `UserVendorRole` into the canonical vendor-membership record instead of creating a second parallel assignment system. Keep the existing table during the first migration if that reduces deployment risk. | Preserves current assignments and avoids conflicting sources of truth. |
| D02 | Backfill every `Vendorprofile.userId` as an active protected owner membership. Use active owner memberships for authorization, while retaining `userId` temporarily for compatibility. | Supports final-owner enforcement and future multiple owners. |
| D03 | Store only an active vendor ID in the session. Resolve and revalidate the vendor, user, membership state, and vendor state on every vendor request. | Prevents stale entity state and makes staff vendor selection possible. |
| D04 | Use DB-backed authorization for mutations and sensitive reads. If a cache is added later, key it by user, vendor, and permission version and require explicit eviction. | Revocation and permission changes must take effect immediately. |
| D05 | Keep the first release vendor-wide only. Reject branch-scope input until an approved vendor-owned `Branch` aggregate exists. | The current code has no safe branch boundary. |
| D06 | Use a 48-hour invitation expiry by default. Store a SHA-256 or HMAC-SHA-256 token hash, display/send the raw token only once, and consume it transactionally. | Meets expiring, hashed, single-use requirements. The expiry remains configurable. |
| D07 | Require a password step-up grant that is bound to user, vendor, purpose, and session; expires after 5 minutes; and is consumed by one high-risk mutation. | Limits reuse and cross-purpose authorization. |
| D08 | Do not let a vendor role grant platform roles or authorities. Any platform support override must use a separate platform permission and an audited service path. | Separates marketplace administration from vendor delegation. |
| D09 | Add versioned migrations before membership schema changes. The project guidance names PostgreSQL as the target, while current profiles also contain MySQL configuration; confirm the deployment database before writing engine-specific SQL. | Prevents an unsafe or non-portable migration. |
| D10 | Replace delete operations with suspend/revoke state transitions. Preserve current routes only as temporary compatibility redirects to POST actions with CSRF. | Retains operational history and removes destructive GET behavior. |
| D11 | Re-enable CSRF globally and allow only narrowly documented callback exceptions with independent signature or token verification. | Authenticated state-changing browser requests require CSRF protection. |

## Target Security Model

### Canonical Membership

Extend the current assignment model with at least:

- `status`: `PENDING_VERIFICATION`, `ACTIVE`, `SUSPENDED`, `REVOKED`
- `membershipType`: `OWNER`, `STAFF`
- `activatedAt`, `activatedBy`
- `suspendedAt`, `suspendedBy`, `suspensionReason`
- `reactivatedAt`, `reactivatedBy`
- `revokedAt`, `revokedBy`, `revocationReason`
- `permissionVersion`
- optimistic-lock `@Version`
- unique constraint on `(vendor_id, user_id)`
- indexes on `(user_id, status)`, `(vendor_id, status)`, and owner lookup fields

Rules:

- Only `ACTIVE` membership in an active vendor grants vendor access.
- An owner is represented by an active owner membership, not by a mutable role-name string alone.
- Suspending or revoking a membership never deletes orders, payouts, products, stock events, audit events, or attribution metadata.
- Reactivation is an explicit audited transition.
- Concurrent owner changes must lock the relevant owner-membership rows or vendor security aggregate before enforcing the final-owner invariant.

### Roles and Permissions

`VendorRole` remains vendor-scoped. Global roles may be templates for copying but must not be assigned directly to a membership unless the authorization policy treats them as immutable vendor-only system roles.

Recommended metadata:

- `roleType`: `SYSTEM_OWNER`, `SYSTEM_STAFF`, `CUSTOM`
- `protectedRole`
- optimistic-lock `@Version`
- unique constraint on `(vendor_id, normalized_slug)`

`VendorPrivilege` is an application-managed vendor-permission catalogue. Vendors may select only privileges marked vendor-assignable. It must never contain platform authorities.

Every vendor feature, endpoint, service use case, UI action, report, export, upload, download, PDF, AJAX operation, and background operation must map to an exact `vendor.*` catalogue permission. The following table is the minimum family catalogue, not an exhaustive allowlist. A dash means no such action is currently defined, never permission-free access.

Recommended permission catalogue:

| Area | Read permission | Mutation permission | High-risk permission |
| --- | --- | --- | --- |
| Staff | `vendor.staff.read` | `vendor.staff.manage` | `vendor.staff.revoke` |
| Roles | `vendor.role.read` | `vendor.role.manage` | `vendor.owner.manage` |
| Products | `vendor.product.read` | `vendor.product.write` | - |
| Orders | `vendor.order.read` | `vendor.order.update` | `vendor.order.refund` |
| Payouts | `vendor.payout.read` | `vendor.payout.request` | `vendor.payout.method.manage` |
| Stock | `vendor.stock.read` | `vendor.stock.manage` | - |
| Shipping | `vendor.shipping.read` | `vendor.shipping.manage` | - |
| Reports | `vendor.report.read` | - | - |
| Vendor profile | `vendor.profile.read` | `vendor.profile.manage` | `vendor.settings.sensitive.manage` |

The existing permission slugs found in commented guards are the migration seed, not proof that the related route is protected.

### Invitation

Add a dedicated `VendorStaffInvitation` aggregate with:

- vendor ID
- normalized invited email
- proposed vendor-role ID
- proposed scope snapshot
- inviter user and membership IDs
- token hash only
- `expiresAt`, `acceptedAt`, `revokedAt`
- state: `PENDING`, `ACCEPTED`, `EXPIRED`, `REVOKED`
- created/modified metadata and `@Version`

Invitation rules:

- Generate raw tokens with a cryptographically secure random source.
- Never persist or log the raw token.
- Hash before lookup and use a unique index on the hash.
- At creation and again at acceptance, verify that the inviter may grant every requested permission and scope.
- Accept only once inside a transaction that locks the invitation row.
- Require the authenticated or newly registered account to own the invited email.
- Require the account and required email/identity verification before membership activation.
- Revoking or replacing an invitation makes the previous token unusable.
- Reuse the communication module by adding a dedicated staff-invitation event; do not reuse the vendor-profile verification token record as staff membership proof.

### Authorization Components

Keep SpEL small by introducing dedicated components/services:

- `VendorSecurityContextResolver`
  - Resolves authenticated user, requested vendor ID, vendor state, and active membership.
- `VendorAuthorization`
  - Answers permission checks such as `can(authentication, vendorId, permission)`.
  - Provides resource checks only through scoped lookup or trusted resource metadata.
- `VendorEffectiveAccessService`
  - Computes effective vendor permissions and, later, branch scopes.
- `VendorGrantPolicy`
  - Rejects permissions or branch scopes broader than the actor's effective access.
- `VendorMembershipPolicy`
  - Enforces self-change, self-promotion, final-owner, suspension, reactivation, and revocation rules.
- `SensitiveActionAuthenticationService`
  - Verifies the current encoded password and issues/consumes short-lived purpose-bound step-up grants.
- `VendorSessionInvalidationService`
  - Invalidates sessions and vendor-context selections for suspended or revoked staff.
- `VendorSecurityAuditService`
  - Writes append-only audit events without secrets or raw tokens.

Preferred annotation shape:

```java
@PreAuthorize("@vendorAuthorization.can(authentication, #vendorId, 'vendor.product.write')")
```

For a resource ID, the application service must take an explicit vendor ID and use a vendor-scoped repository lookup. Do not place a long ownership query in SpEL.

### Repository Scope Contract

Every vendor-facing lookup or mutation must encode vendor ownership in the query signature.

Preferred patterns:

```java
Optional<Product> findByIdAndVendorprofile_Id(Long productId, Long vendorId);

Optional<VendorPayoutMethod> findByIdAndVendor_Id(Long methodId, Long vendorId);

long deleteByIdAndVendor_Id(Long id, Long vendorId);
```

Rules:

- Vendor application services must not call a generic `findById`, `getReferenceById`, `deleteById`, or `findAll` for vendor-owned resources.
- Lists, counts, exports, PDFs, and background jobs require the same vendor predicate as detail and mutation paths.
- Child resources must be scoped through their vendor-owned parent in the query.
- A missing row and a cross-vendor row should produce the same not-found response to avoid resource enumeration.
- Platform administration repositories remain separate and require platform permissions.

### Audit Event

Add an append-only `VendorSecurityAuditEvent` with:

- vendor ID
- actor user and membership IDs
- target user, membership, role, or invitation ID
- event type
- result: `SUCCESS` or `DENIED`
- safe before/after summaries
- reason
- correlation/request ID
- source IP and user agent where available
- occurred-at timestamp

Required event types:

- invitation created, resent, expired, revoked, accepted, or denied
- membership activated, suspended, reactivated, or revoked
- role created, changed, or retired
- permission set changed
- branch scope assigned or removed
- owner promotion or demotion attempted/completed
- final-owner removal denied
- self-escalation denied
- step-up succeeded, failed, expired, or consumed
- sessions and permission caches invalidated

Do not store raw passwords, raw invitation tokens, session cookies, API keys, or unnecessary personal data in audit JSON.

## Phased Implementation Plan

### W01 - Security Baseline and Migration Decision

Status: `Partial`

- Confirm the live database engine and add versioned migration support.
- Rotate and externalize credentials currently stored in property files.
- Re-enable CSRF and define narrow, documented exceptions.
- Review every current public mutation route before changing access.
- Add standard 401/403/not-found handling that does not leak cross-vendor resource existence.
- Capture a pre-change route and database backup/rollback plan.

Exit gate:

- Database choice, migration method, CSRF policy, and secret-management changes are approved and verified in a non-production environment.

### W02 - Membership and Owner Foundation

Status: `Pending`

- Add membership lifecycle, type, timestamps, reasons, version, constraints, and indexes.
- Backfill one protected active owner membership from every valid `Vendorprofile.userId`.
- Detect vendors with missing owners, duplicate assignments, invalid roles, or inactive owner users and produce a repair report before enforcing constraints.
- Make active owner membership the authorization source of truth.
- Preserve compatibility fields until all callers migrate.

Exit gate:

- Every vendor has at least one active owner membership, no duplicate vendor/user membership exists, and rollback SQL is tested.

### W03 - Trusted Vendor Context and Login Authority Handling

Status: `Pending`

- Replace session-stored `Vendorprofile` with a selected vendor ID.
- Resolve owned and staffed vendors from active memberships.
- Revalidate user status, vendor status, and membership status on every vendor request.
- Update login routing so a vendor owner or staff member can reach vendor selection/home without requiring a platform user type.
- Stop relying on login-time vendor authorities for sensitive mutation decisions.
- Clear the selected vendor when membership becomes invalid.

Exit gate:

- A user can select only an active vendor where they have an active membership, and a stale/tampered vendor ID is denied.

### W04 - Authorization Core and Service Entry Points

Status: `Pending`

- Implement the dedicated authorization, effective-access, grant-policy, and membership-policy components.
- Add `@PreAuthorize` to application/service entry points before enabling UI actions.
- Pass vendor ID explicitly into vendor application services.
- Keep controller annotations as defense in depth, not as the only guard.
- Replace role-name bypasses with explicit owner semantics and permissions.
- Deny by default when authentication, membership, vendor, permission, or ownership information is missing.
- Completed containment slice: `VendorStaffAdministrationService` now protects the existing vendor staff and custom-role administration paths with service `@PreAuthorize`, active-vendor checks, vendor-scoped role/staff lookups, vendor-only privilege assignment, grant-ceiling validation, self-removal prevention, and final-owner deletion prevention.

Exit gate:

- Direct service invocation is denied without effective permission even when no controller is involved.

### W05 - Repository Scope Conversion

Status: `Partial`

Convert vendor-facing repositories and services in this order:

1. staff, roles, invitations, and membership transitions
2. payout requests and payout methods
3. order status, return, and refund operations
4. vendor profile, logo, verification, and sensitive settings
5. products, images, dimensions, warranties, delivery charges, timelines, and delivery areas
6. stock and stock transactions
7. shipments, shipping documents, manifests, labels, invoices, and delivery persons
8. finance reports, sales reports, PDFs, exports, and dashboard queries

Completed containment slice: existing staff assignment deletion and custom-role edit/delete now use vendor-scoped service/repository lookups. Invitation and membership-transition repositories remain pending.

For every slice:

- Add vendor-scoped repository reads, writes, counts, and deletes.
- Remove controller-direct writes where an application service should own the transaction.
- Add service method authorization.
- Add cross-vendor repository tests before moving to the next slice.

Exit gate:

- No vendor application service uses an unscoped generic repository operation for a vendor-owned resource.

### W06 - Invitation and Verified Activation

Status: `Pending`

- Add invitation entity, repository, service, DTOs, email template/event, acceptance controller, and pages.
- Replace direct existing-user assignment with invitation creation.
- Support existing accounts and registration for new invitees.
- Verify token hash, expiry, state, email ownership, account state, and inviter grant ceiling.
- Activate membership only after required verification succeeds.
- Make accept/consume transactional and single use.

Exit gate:

- Raw tokens never reach persistence/logs, replay is denied, expired/revoked tokens are denied, and an unverified invitee never gains vendor authorities.

### W07 - Role Administration and Anti-Escalation

Status: `Pending`

- Add vendor-assignable metadata to the permission catalogue.
- On role create/update and invitation create/update, compare requested permissions with the actor's effective permissions.
- When branch support exists, compare requested branch IDs with the actor's effective branch set.
- Block self-permission changes, self-promotion, and platform-role assignment.
- Block removal/demotion/suspension/revocation of the final active owner.
- Protect system owner roles from deletion or unsafe edits.
- Use optimistic locking and transactional final-owner enforcement.

Exit gate:

- Every escalation test is denied in the service layer and recorded in the security audit.

### W08 - Password Step-Up Authentication

Status: `Pending`

- Add a reusable password re-authentication endpoint and service.
- Bind the grant to user, membership, vendor, action purpose, session, and expiry.
- Require and consume it for payout requests, refunds, staff-role administration, owner changes, payout-method changes, and sensitive vendor settings.
- Rate-limit failures and avoid revealing whether a password or resource check failed first.
- Invalidate step-up grants on logout, password change, suspension, or revocation.

Exit gate:

- A normal authenticated session without a valid purpose-bound step-up grant cannot perform a high-risk mutation.

### W09 - Suspension, Revocation, Sessions, Tokens, and Caches

Status: `Partial`

- Implement suspend, reactivate, and revoke transitions; remove hard deletion from staff workflows.
- Register or index active sessions so all sessions for a user/membership can be invalidated.
- Revoke pending invitations and active step-up grants when appropriate.
- Increment permission versions and evict any user/vendor effective-access caches after role, permission, scope, suspension, or revocation changes.
- Completed containment slice: existing staff assignment creation/removal and vendor role permission updates now advance affected users' account credential epoch through `SessionCredentialVersionService`, expiring registered sessions and forcing the stale vendor-authority snapshot through the existing database epoch filter.
- Deny the current request immediately after revocation commits.
- Retain attribution and audit history.

Exit gate:

- A revoked or suspended staff member loses access in every existing session without waiting for the next login.

### W10 - Append-Only Security Audit

Status: `Pending`

- Add event entity, repository, service, safe serialization, and retention policy.
- Record successful and denied security administration operations.
- Record actor, target, vendor, safe before/after state, reason, request ID, IP, and user agent.
- Add an authorized vendor-owner audit view and a separate platform audit view if required.
- Prevent vendor staff from altering or deleting audit records.

Exit gate:

- All required staff lifecycle and permission events are queryable and immutable through application APIs.

### W11 - Full Vendor-Surface Authorization Rollout

Status: `Pending`

- Apply the permission matrix to all vendor controllers and application services.
- Keep Thymeleaf menu/button visibility aligned with effective permissions for usability only.
- Verify reads, writes, downloads, exports, uploads, PDFs, AJAX endpoints, and background operations.
- Remove commented security annotations and stale role-name checks after equivalent verified controls exist.
- Add branch scope only after the branch aggregate and repository ownership model are approved.

Exit gate:

- Every vendor route is mapped to a permission and a scoped service/repository path; the default for an unmapped vendor operation is deny.

### W12 - Verification, Deployment, and Rollback

Status: `Pending`

- Run unit, repository, method-security, MVC, integration, migration, and concurrency tests.
- Perform two-vendor live smoke tests with owner, limited staff, suspended staff, revoked staff, and platform admin accounts.
- Verify CSRF, invitation email delivery, token expiry/replay, step-up expiry, session invalidation, and audit output.
- Deploy schema changes before code that requires the new fields.
- Keep a tested rollback path that does not delete membership or audit history.
- Monitor access-denied, invitation failure, step-up failure, and cross-vendor lookup metrics without logging secrets.

Exit gate:

- All release gates below pass in the deployment-like environment and evidence is attached to this workflow.

## Required Test Matrix

### Authorization Component Tests

- active owner with permission: allow
- active staff with permission: allow
- active staff without permission: deny
- no vendor membership: deny
- pending membership: deny
- suspended membership: deny
- revoked membership: deny
- blocked vendor: deny
- requested vendor differs from membership vendor: deny
- requested permission is absent or unknown: deny

### Grant and Owner Policy Tests

- inviter grants a strict subset of their permissions: allow
- inviter grants an equal set within their scopes: allow
- inviter grants a permission they do not have: deny
- inviter grants a broader branch scope: deny
- staff edits their own role or permissions: deny
- staff promotes themselves to owner: deny
- vendor role attempts to add a platform authority: deny
- demote, suspend, revoke, or remove the final active owner: deny
- concurrent final-owner changes: one safe result, never zero active owners

### Invitation Tests

- valid token, matching verified email, before expiry: activate once
- raw token is not stored: verify database value differs from raw token
- second use of the same token: deny
- expired token: deny
- revoked token: deny
- changed role exceeds inviter's current access at acceptance: deny
- authenticated email differs from invited email: deny
- unverified account: remain pending and deny vendor access

### Repository Scope Tests

For each protected aggregate:

- same-vendor read/update/delete: allow
- cross-vendor ID: return empty/not found and do not mutate
- list/count/export: contain only the requested vendor
- child resource whose parent belongs to another vendor: deny

Minimum aggregates:

- membership and role
- payout and payout method
- order, return, and refund
- product and product child records
- stock transaction
- shipment, label, manifest, invoice, and delivery person
- vendor profile and sensitive settings

### MVC and CSRF Tests

- authenticated but missing permission: 403
- cross-vendor resource: 404 or approved non-enumerating response
- state-changing request without CSRF: rejected
- hidden button absent for missing permission, while a direct request is still denied server-side
- destructive staff and role actions are POST/DELETE, never GET

### Step-Up and Revocation Tests

- correct password and intended purpose: valid short-lived grant
- wrong password: deny and audit without logging the password
- expired, wrong-purpose, wrong-vendor, wrong-session, or reused grant: deny
- suspend/revoke: all active sessions invalidated
- role/permission change: old cached authority no longer authorizes
- logout/password change: step-up grant invalidated

## Release Gates

- [ ] `@EnableMethodSecurity` remains enabled.
- [ ] Application/service entry points use `@PreAuthorize` through dedicated authorization components.
- [ ] URL rules and hidden UI elements are defense in depth only.
- [ ] Permission, vendor, ownership, membership state, and vendor state are evaluated.
- [ ] Every vendor feature and service use case maps to an active `vendor.*` catalogue permission; blank, `N/A`, broad-role-only, and inferred permissions are denied.
- [ ] Vendor-owned repository operations independently include vendor scope.
- [~] Owners can invite staff and assign only vendor-scoped roles. Existing direct staff assignment is contained; invitation workflow remains pending.
- [~] Inviters cannot grant permissions or branch scopes broader than their effective access. Vendor privilege grant ceiling exists for current role/staff assignment paths; branch scopes remain disabled/pending.
- [~] Self-permission changes, self-promotion, platform-role grants, and final-owner removal are denied. Existing self-removal, final-owner deletion, and platform privilege assignment are denied; full lifecycle self-promotion controls remain pending.
- [ ] Invitations use expiring, single-use, hashed tokens.
- [ ] Verification is required before membership activation.
- [ ] Password step-up is enforced for payouts, refunds, staff-role administration, and sensitive settings.
- [ ] Suspension/revocation preserves operational history.
- [ ] Revocation invalidates sessions, tokens, step-up grants, vendor context, and permission caches.
- [ ] Invitations and all membership, role, permission, branch, suspension, reactivation, and revocation events are audited.
- [~] Permitted, missing-permission, cross-vendor, suspended-staff, and privilege-escalation tests pass. Current vendor staff IAM containment has permitted, missing-permission, scoped-delete, self-removal, and privilege-escalation tests; suspended-staff tests require lifecycle state.
- [~] CSRF is enabled for authenticated browser mutations. `/vendor-users/**` is covered; whole-application rollout remains pending.
- [ ] Secrets are externalized and any exposed credentials are rotated.
- [ ] Deployment-like runtime proof is recorded; source inspection or unauthenticated redirects alone do not count.

## Implementation Record Template

Update this section after each authorized phase:

| Date | Work package | Files/migrations | Verification | Result | Remaining |
| --- | --- | --- | --- | --- | --- |
| 2026-08-14 | Codebase audit and workflow | `docs/security/vendor-staff-method-security-workflow.md` | Source trace and workflow coverage review | `Done` | W01-W12 implementation |
| 2026-08-30 | Existing vendor staff IAM containment | `VendorStaffAdministrationService`, `VendorAccessControllController`, `VendorRoleManagementController`, `UserVendorRoleRepository`, `/vendor-users/**` templates, endpoint and permission ledgers | `D:\Maven_Home\bin\mvn.cmd -q -Dtest="VendorStaffAdministrationServiceMethodSecurityTest,VendorStaffIamControllerSecurityTest,VendorStaffIamTemplateSecurityContractTest,SecurityConfigCsrfCoverageTest" test`; regenerated ledgers: 737 endpoint decisions, 148 permission decisions, 39 implemented endpoint rows, 7 implemented permission rows | `Done` | Invitation lifecycle, membership state, session revocation, step-up, audit, branch scope, and deployment-like runtime proof |
| 2026-08-31 | Existing staff revocation/session-cache containment | `VendorStaffAdministrationService`, `UserVendorRoleRepository`, `SessionCredentialVersionService` integration | `D:\Maven_Home\bin\mvn.cmd -q "-Dtest=VendorStaffAdministrationServiceMethodSecurityTest,VendorStaffIamControllerSecurityTest,SessionCredentialVersionServiceTest,CredentialVersionFilterTest,SessionCredentialVersionContractTest,VendorVerificationTokenLoggingContractTest,SecurityConfigCsrfCoverageTest" test` | `Partial - source verified` | Full membership state, invitation lifecycle, step-up grants, append-only audit, and deployment-like runtime proof |
