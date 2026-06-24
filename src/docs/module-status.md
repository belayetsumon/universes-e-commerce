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

## Daily Progress Log

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
- Next module: Product Catalog
- Scope for today: Dynamic attribute phase-2 wiring for admin forms and category-driven product specs
- Risks or blockers: Legacy size/color data still needs controlled bootstrap and UI adoption
