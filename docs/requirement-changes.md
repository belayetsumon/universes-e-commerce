# Requirement Changes

Use this file whenever a requirement changes after the original specification.

## Change Template

### Change Entry
- Date:
- Module:
- Old Requirement:
- New Requirement:
- Reason:
- Impacted Areas:
- Files or Pages Likely Affected:
- Priority:
- Action Needed:

## Change History

### 2026-05-15
- Module: Product Catalog
- Old Requirement:
  - Product catalog supported direct product fields plus legacy `size` and `color` variant tables only.
- New Requirement:
  - Scaffold a reusable dynamic attribute architecture for phase 1, use Java UUID strings for new catalog UUIDs, and prefer UUID-based single-record lookups for security-sensitive flows.
- Reason:
  - Enterprise catalog requirements were documented in `attribute_specifications.docx` and approved for implementation planning.
- Impacted Areas:
  - Product attribute entities, category schema mapping, catalog variant model, repository/service layer, legacy variant migration utilities, admin/vendor variant actions.
- Files or Pages Likely Affected:
  - Product model classes
  - Product repositories and services
  - Product variant modal flows in admin and vendor product details
  - `docs/module-status.md`
- Priority:
  - High
- Action Needed:
  - Phase-1 scaffold added. Bootstrap legacy size/color attributes before wiring dynamic forms and storefront facets.

### 2026-04-29
- Module: Wishlist
- Old Requirement:
  - Wishlist feature not implemented.
- New Requirement:
  - Add wishlist with `Principal` based user identification and customer wishlist page.
- Reason:
  - Feature requested during development.
- Impacted Areas:
  - Backend entity, repository, service, controller, shared model attributes, product pages, customer area, header.
- Files or Pages Likely Affected:
  - Wishlist backend classes
  - `front-header.html`
  - `single-product.html`
  - `product.html`
  - `product-by-category.html`
  - `customer/wishlist/index.html`
- Priority:
  - Medium
- Action Needed:
  - Completed implementation, keep manual browser verification pending.

### 2026-04-29
- Module: Settings
- Old Requirement:
  - Global settings page had incomplete CRUD wiring and single logo handling.
- New Requirement:
  - Complete settings CRUD, secure admin access, and support resized logo variants.
- Reason:
  - Feature correction and UI needs.
- Impacted Areas:
  - Settings repository, service, controller, admin template, header, maintenance page.
- Files or Pages Likely Affected:
  - Global settings backend classes
  - `global-settings.html`
  - `front-header.html`
  - `maintenance.html`
- Priority:
  - High
- Action Needed:
  - Completed implementation, keep full UI verification pending.
