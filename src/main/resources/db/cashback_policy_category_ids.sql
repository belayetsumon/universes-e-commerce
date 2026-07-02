-- Cashback policy category scope helper.
-- Hibernate ddl-auto=update can add this automatically in local profiles, but
-- production databases should review and run an explicit migration.

CREATE TABLE IF NOT EXISTS cashback_policy_categories (
    cashback_policy_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (cashback_policy_id, category_id)
);

-- Run this insert only when upgrading from an older schema that still has
-- promotions_cashback_policy.category_id.
-- INSERT IGNORE INTO cashback_policy_categories (cashback_policy_id, category_id)
-- SELECT id, category_id
-- FROM promotions_cashback_policy
-- WHERE category_id IS NOT NULL;
