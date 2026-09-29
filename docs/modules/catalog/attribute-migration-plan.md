# Attribute Migration Plan

## Date
- 2026-05-15

## Goal
- Introduce a reusable dynamic attribute engine without breaking the currently live `size` and `color` product variant flow.

## Phase 1 Delivered
- Standard Java `UUID.randomUUID().toString()` support for new catalog-facing UUIDs.
- Shared base entity with:
  - Numeric internal ID
  - Public UUID
  - Optimistic locking version
  - Created/updated audit fields
- New dynamic catalog entities:
  - `Attribute`
  - `AttributeOption`
  - `CategoryAttribute`
  - `ProductAttribute`
  - `ProductVariant`
  - `ProductVariantOption`
- Repository and service scaffolding for the new entities.
- `AttributeMigrationService` to bootstrap legacy `ProductSize` and `ProductColor` data into reusable attributes/options.
- UUID-based add/delete routing for the currently live `ProductVariants` modal flow.

## Why IDs Are Still Kept Internally
- Existing relational joins, legacy code paths, and stock logic already depend on numeric primary keys.
- UUIDs are now the preferred external single-record identifier for security-sensitive routes.
- This lets the app migrate safely without a risky full primary-key rewrite.

## Recommended Next Steps
1. Call `AttributeMigrationService.bootstrapLegacyVariantAttributes()` in a controlled admin-only flow or one-time bootstrap script.
2. Attach migrated attributes to relevant categories with `attachAttributeToCategory(...)`.
3. Add admin screens for attribute definitions and category mappings.
4. Render product specification inputs dynamically from `CategoryAttribute`.
5. Migrate storefront filters from hardcoded `size` and `color` lookups to generic attribute facets.
6. Migrate legacy `ProductVariants` rows into the new `ProductVariant` + `ProductVariantOption` tables after SKU rules are finalized.

## Notes
- This phase intentionally does not auto-convert legacy variant rows because SKU, barcode, and merchandising rules need business confirmation first.
- Existing product and category pages still use numeric IDs in several older routes. The variant modal flow was updated first as a safe incremental step.
