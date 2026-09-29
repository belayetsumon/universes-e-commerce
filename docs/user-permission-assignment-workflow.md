# User Permission Assignment Workflow

This focused workflow covers permission assignment without replacing the existing authorization model.

## Scope

The application continues to use:

`User -> Role(s) -> Privilege(s) -> Endpoint/service authorization`

Direct user-to-permission database links are out of scope for this workflow.

## Phase 1 - Role and privilege selection

Estimated effort: **1-3 working days**

Status: **Implemented - source verified**

### Goal

Allow an administrator to select roles for a user and immediately see the privileges that user will receive.

### Work

1. Load all active, assignable roles and their privileges.
2. Display roles grouped with their privileges and provide search, module filtering, and select-all support.
3. Show an effective-permission preview before saving.
4. Validate the submitted role IDs on the server and reload roles from the database.
5. Save the user-role relationship through the existing transactional service.
6. Exclude non-assignable `public.*`, `internal.*`, `webhook.*`, and service-only capabilities from the assignment UI.

### Acceptance criteria

- The user form displays all assignable roles and their privileges.
- Saving a user persists only valid role IDs.
- The effective permission preview matches the authorities loaded at login.
- Tampered or unknown role IDs are rejected.
- Existing endpoint and service-method authorization remains unchanged.

## Phase 2 - Audit, session invalidation, and verification

Estimated effort: **7-12 working days**

Status: **Pending**

### Goal

Make permission changes traceable, immediately effective, and release-verified.

### Work

1. Record every permission-affecting change with actor, target user, previous roles, new roles, reason, timestamp, and request reference.
2. Advance the target user's credential or authorization version after a role change.
3. Invalidate affected active sessions and prevent stale authorities from continuing to work.
4. Add safe administrator confirmation and conflict handling for concurrent edits.
5. Add focused tests for assignment, removal, tampering, ownership, session invalidation, and audit records.
6. Run the full regression suite and deployment-like smoke tests.
7. Verify database migration, runtime endpoint mappings, login authority loading, and rollback behavior.

### Acceptance criteria

- Every role change produces an auditable record.
- Removed privileges stop working after session invalidation.
- New privileges are available after the next authenticated session.
- Audit records never contain passwords, tokens, or session IDs.
- Focused, full-suite, migration, and runtime verification pass.

## Release boundary

Phase 1 is a usable role-based permission assignment feature. Phase 2 is required before treating permission administration as fully production-verified. Granular direct user permissions, MFA/step-up, and broader security operations remain separate future work unless explicitly added to scope.
