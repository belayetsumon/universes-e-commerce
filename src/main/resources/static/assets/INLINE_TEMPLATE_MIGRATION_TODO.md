# Remaining Inline Template Migration

This file tracks templates that still contain inline JavaScript blocks or inline `onclick` handlers outside the shared layout refactor.

## Inline `<script>` blocks still present
- `templates/admin/index_new.html`
- `templates/welcome/welcome.html`
- `templates/district/select-district.html`
- `templates/admin/stock/form.html`
- `templates/ads/ads_form.html`
- `templates/admintheme/admin-layout.html`
- `templates/vendor/stock/form.html`
- `templates/vendor/shipments/form.html`
- `templates/admin/shipping/admin_shipments_form.html`
- `templates/vendor/product/deliveryoption/delivery_area.html`
- `templates/vendor/product/add.html`
- `templates/frontview/single-product.html`
- `templates/fronttheme/front-layout.html`
- `templates/user/uregistrations.html`
- `templates/product/fragments/messages.html`
- `templates/product/deliveryoption/delivery_area.html`
- `templates/product/add.html`

## Inline `onclick` handlers still present
- `templates/ads/ads_list.html`
- `templates/welcome/welcome.html`
- `templates/error/access-denied.html`
- `templates/vendor/users/vendor_users_list.html`
- `templates/admin/shipping/shipment_list.html`
- `templates/vendor/users/vendor_role_manage_list.html`
- `templates/admin/shipping/packagingrate/packegeratelist.html`
- `templates/admin/shipping/delivery_person/list.html`
- `templates/vendor/users/vendor_role_list.html`
- `templates/vendor/users/privilegelist.html`
- `templates/admin/shipping/carriers/carriers_rate_list.html`
- `templates/bottom-nav.html`
- `templates/mobile-top-nav.html`
- `templates/vendor/payoutmethod/list.html`
- `templates/admin/referral_rewards/lavel_rate_settings_list.html`
- `templates/frontview/contactUs.html`
- `templates/vendor/payout/payout_list.html`

## Migration order suggestion
1. Move the remaining shared fragments first: `bottom-nav.html`, `mobile-top-nav.html`, and the legacy `fronttheme` / `admintheme` layouts.
2. Then migrate page clusters by area: `admin/shipping/*`, `vendor/product/*`, `vendor/users/*`, and `frontview/*`.
3. Keep using section bundles plus page-level `page_css` and `page_js` assets so each migration stays isolated.
