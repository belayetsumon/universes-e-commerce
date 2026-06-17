# Section Asset Map

This folder contains section-based CSS and JS bundles so front, customer, vendor, and admin can evolve independently.

Reference docs:
- `assets/SECTION_ASSET_MAP.md`: section bundle map and usage.
- `assets/INLINE_TEMPLATE_MIGRATION_TODO.md`: legacy templates that still need inline JS or `onclick` migration.

## Folder plan
```text
assets/
  admin/
    css/
    js/
    pages/
  customer/
    css/
    js/
  front/
    css/
    js/
  vendor/
    css/
    js/
```

## Admin
- `assets/admin/css/admin-panel.css`: used by `templates/admin-layout.html` for admin shell styling.
- `assets/admin/js/admin-panel.js`: used by `templates/admin-layout.html` for sidebar, submenu scrolling, dropdown behavior, datepicker setup, and DataTables setup.
- `assets/admin/pages/global-settings/global-settings.css`: page-level CSS used only by `templates/admin/settings/global-settings.html` for settings image preview sizing.
- `assets/admin/pages/global-settings/global-settings.js`: page-level JS used only by `templates/admin/settings/global-settings.html` for Summernote initialization, active-tab restore, validation, and delete confirmations.

## Front
- `assets/front/css/front-site.css`: used by `templates/front-layout-home.html` and `templates/front-layout-inner-page.html` for storefront styling and shared front fragments.
- `assets/front/js/front-site.js`: used by `templates/front-layout-home.html` and `templates/front-layout-inner-page.html` for storefront tracking, tabs, filters, and cart behavior.

## Customer
- `assets/customer/css/customer-panel.css`: used by `templates/front-layout-inner-customar-page.html` for customer panel styling and shared storefront fragments.
- `assets/customer/js/customer-panel.js`: used by `templates/front-layout-inner-customar-page.html` for customer panel tracking and tab helpers.

## Vendor
- `assets/vendor/css/vendor-panel.css`: used by `templates/front-layout-inner-vendor-page.html` for vendor panel styling and shared storefront fragments.
- `assets/vendor/js/vendor-panel.js`: used by `templates/front-layout-inner-vendor-page.html` for vendor text editor setup, datepicker setup, and DataTables setup.

## Page-level pattern
Use layout fragment slots named `page_css` and `page_js` to load page-specific assets without affecting the whole section.
