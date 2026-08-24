-- Platform IAM permission foundation (PostgreSQL target).
-- Apply in a controlled deployment before removing the temporary legacy
-- `admin` / `ROLE_ADMIN` IAM access bridge.
--
-- This migration intentionally does not assign platform.iam.protected.manage.
-- Grant it separately to a reviewed break-glass/system-administrator role.

BEGIN;

CREATE UNIQUE INDEX IF NOT EXISTS uq_usermodule_application_module_slug_ci
    ON usermodule_application_module (lower(slug));

CREATE UNIQUE INDEX IF NOT EXISTS uq_usermodule_application_privilege_slug_ci
    ON usermodule_application_privilege (lower(slug));

CREATE UNIQUE INDEX IF NOT EXISTS uq_usermodule_role_slug_ci
    ON usermodule_role (lower(slug));

INSERT INTO usermodule_application_module (name, slug)
SELECT 'Identity and Access Management', 'identity-access'
WHERE NOT EXISTS (
    SELECT 1
    FROM usermodule_application_module
    WHERE lower(slug) = 'identity-access'
);

INSERT INTO usermodule_application_privilege (module_id, name, slug)
SELECT module.id, permission.name, permission.slug
FROM usermodule_application_module module
CROSS JOIN (
    VALUES
        ('View IAM catalogue', 'platform.iam.read'),
        ('Manage roles and grants', 'platform.iam.manage'),
        ('Manage protected IAM catalogue', 'platform.iam.protected.manage')
) AS permission(name, slug)
WHERE lower(module.slug) = 'identity-access'
  AND NOT EXISTS (
      SELECT 1
      FROM usermodule_application_privilege existing
      WHERE lower(existing.slug) = permission.slug
  );

COMMIT;

-- Deployment follow-up:
-- 1. Assign platform.iam.read/manage only within the administrator's current
--    effective grant ceiling.
-- 2. Assign platform.iam.protected.manage only to the reviewed break-glass or
--    system-administrator role.
-- 3. Re-authenticate affected administrators so authorities are reloaded.
-- 4. Remove the temporary legacy bridge after exact-permission smoke tests pass.
