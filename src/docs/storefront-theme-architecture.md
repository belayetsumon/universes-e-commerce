# Storefront Theme Architecture

## Goal

Use one codebase for many companies.

- Public storefront: different theme per company
- Admin panel: same for all companies
- Vendor panel: same for all companies
- Customer panel: same for all companies

## Current Findings

- Public pages are returned directly from `frontview/*` in `PublicController`
- Public header and footer still contain hardcoded brand content such as `Musapir`, phone number, and static menu text
- `GlobalSettings` is a singleton (`id = 1`), so only one site configuration can exist now
- Admin already has its own separate layout
- Customer and vendor pages still reuse the public header/footer, so public theme changes will also affect those panels

## Recommended Architecture

Do not duplicate the whole project for each company.

Keep one project and split the UI into two areas:

1. Fixed dashboard area
   - `admin`
   - `vendor`
   - `customer`

2. Themeable storefront area
   - home
   - product list
   - category page
   - product details
   - public static pages
   - public header/footer

## Recommended Folder Structure

```text
templates/
  admin/
  customer/
  vendor/
  storefront/
    themes/
      default/
        fragments/
          header.html
          footer.html
        layouts/
          home.html
          inner.html
        pages/
          home.html
          product.html
          product-by-category.html
          single-product.html
      fashion/
      electronics/

static/
  storefront/
    themes/
      default/
        css/
          theme.css
        js/
          theme.js
        img/
      fashion/
      electronics/
```

## Data Model Recommendation

Keep `GlobalSettings` only for global system-wide settings if needed.

Create a new company storefront configuration model such as:

```text
StorefrontSite
- id
- code
- companyName
- domain
- themeCode
- logo
- favicon
- primaryColor
- secondaryColor
- supportPhone
- supportEmail
- address
- active
```

Important:

- Do not use `Vendorprofile` for this
- Vendor is marketplace/vendor logic
- Company storefront theme is tenant/site branding logic

## Rendering Strategy

Instead of hardcoding `frontview/product`, resolve public views through a theme service.

Example idea:

```text
themeViewResolver.resolve("product")
-> storefront/themes/default/pages/product
```

That lets all public routes keep the same controller logic while the selected company decides which template is rendered.

## First Refactor Steps

1. Separate customer and vendor layouts from public theme
   - Create dedicated `customer-layout.html`
   - Create dedicated `vendor-layout.html`
   - Remove dependency on public `front-header` and `front-footer`

2. Move hardcoded public branding into company storefront settings
   - welcome text
   - phone
   - footer links
   - support details
   - colors
   - logos

3. Convert public layouts into theme layouts
   - current `front-layout-home.html`
   - current `front-layout-inner-page.html`
   - current `front-header.html`
   - current `front-footer.html`

4. Add company/domain to theme resolution
   - domain based if each company has separate domain
   - request parameter or path based for local testing

5. Keep admin layout unchanged
   - admin already has a separate shell and can stay shared

## Best Practical Path For This Project

For this codebase, the safest approach is:

- keep `admin-layout.html` as shared dashboard layout
- create new shared `customer-layout.html`
- create new shared `vendor-layout.html`
- turn current public layouts into `storefront/themes/default/*`
- add `themeCode` on company storefront settings
- resolve only public pages by theme

## What Not To Do

- Do not copy the full project for each company
- Do not make admin/vendor/customer pages theme-dependent
- Do not keep multi-company storefront settings inside a singleton settings row
- Do not mix vendor identity with storefront company identity

## Suggested Phase Plan

### Phase 1

- isolate vendor/customer layouts from public header/footer
- replace hardcoded public brand text with dynamic settings

### Phase 2

- create `default` storefront theme package
- move public templates into theme folder structure

### Phase 3

- add `themeCode` and `domain` based company storefront selection

### Phase 4

- add second and third storefront themes

## Recommendation

If you want different UI for each company, use theme packages for public pages only.

If you only want different colors and logos, CSS variables inside one public theme are enough.

For your project, the better long-term choice is:

- same admin/vendor/customer panels
- separate theme package per public storefront
- company/domain based theme selection
