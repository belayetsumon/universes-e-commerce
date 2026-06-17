# Single Vendor Mode Guide

## Goal

Support customers who want to run this system as a single-vendor ecommerce store instead of a marketplace.

This project already has strong marketplace structure:

- products belong to a vendor
- cart and checkout group items by vendor
- orders store `vendorId`
- vendor finance, payout, and settlement flows already exist

Because of that, the safest approach is:

- keep the vendor model in the database
- run the store with only one active vendor
- hide or disable marketplace-only features when needed

Do not remove vendor support from the schema unless you are building a separate product branch.

## Recommendation

For this codebase, support **two operating modes**:

1. `MARKETPLACE`
   - multiple vendors
   - vendor registration allowed
   - vendor payout and settlement active

2. `SINGLE_VENDOR`
   - one store owner vendor only
   - no public vendor onboarding
   - no multi-vendor checkout behavior in the UI
   - vendor finance and payout can be hidden or disabled

## Best Practical Choice

For most customers who want a single-vendor store, use this strategy:

- keep one `Vendorprofile` as the store owner
- assign all products to that vendor
- disable vendor registration
- hide vendor-facing marketplace features
- keep admin, vendor, and customer panels shared

This gives you a single codebase for both customer types.

## Important Current Project Findings

### 1. Products are still vendor-based

`Product` has a `vendorprofile` relation.

That means even in single-vendor mode, products should still point to one owner vendor.

### 2. Cart logic expects a vendor on products

Cart and checkout group items by vendor ID.

So for this project, **products must not be left without a vendor**.

### 3. Orders are vendor-based

`SalesOrder` stores `vendorId`, and checkout creates orders grouped per vendor.

### 4. Vendor finance exists

Vendor payout, settlement, and vendor ledger are real application features, not just UI pages.

### 5. Vendor registration can already be disabled

`GlobalSettings` already has `vendorRegistrationEnabled`.

This is useful for the quick single-vendor setup.

## Two Ways To Support Single Vendor

## Option A: Quick Single-Vendor Setup

Use this when:

- a customer wants one store only
- you do not want major refactoring right now
- you still want to reuse current marketplace code

### Steps

1. Create one master vendor
   - create one `Vendorprofile` for the store owner
   - keep it active

2. Disable vendor registration
   - set `vendorRegistrationEnabled = false`

3. Assign all products to the master vendor
   - existing products
   - new products created by admin
   - imported products

4. Keep only one active vendor for storefront use
   - do not allow multiple selling vendors in production for that customer

5. Hide marketplace-only UI
   - vendor signup links
   - vendor listing pages
   - vendor finance pages if customer does not need them
   - public vendor labels if not wanted

6. Decide who manages products
   - admin panel only
   - or the owner uses the vendor panel

7. Review accounting fields
   - set `marketPlaceCommissionRate = 0` if commission is not needed
   - review `marketPlaceDiscount` usage

### Pros

- fastest setup
- low-risk change
- works with current cart, order, and product design

### Cons

- vendor concept still exists internally
- checkout still groups by vendor, even though there is only one
- finance tables and vendor payout logic still exist in the codebase

## Option B: Clean Productized Single-Vendor Mode

Use this when:

- you want one product that supports both marketplace and single-vendor customers cleanly
- you want clearer settings and less confusion in admin

### Recommended Settings Additions

Add these fields to global settings:

```text
storeMode
- MARKETPLACE
- SINGLE_VENDOR

primaryVendorId
- the owner vendor used in single-vendor mode
```

### Recommended Behavior

When `storeMode = SINGLE_VENDOR`:

- vendor registration is always disabled
- new admin-created products auto-attach to `primaryVendorId`
- public UI hides multi-vendor language
- vendor payout and settlement menus are hidden
- vendor access can be optional
- checkout can stay vendor-grouped internally for compatibility, but UI should behave like one store

### Pros

- better long-term structure
- easier for staff to understand
- easier to support multiple business models from one codebase

### Cons

- requires code changes
- needs testing in product creation, cart, checkout, and finance

## Recommended Implementation In This Project

For this codebase, I recommend:

### Phase 1

Implement safe single-vendor deployment support first:

- disable vendor registration
- create one owner vendor
- force all products to use that vendor
- hide extra vendor UI

### Phase 2

Add formal store mode support:

- `storeMode`
- `primaryVendorId`
- feature flags in UI and services

### Phase 3

Simplify single-vendor UX:

- remove vendor titles from public checkout
- remove vendor grouping visuals from cart and checkout
- auto-assign vendor in admin product creation

## What Needs Attention In Code

## 1. Product Creation

Admin product creation currently does not clearly enforce a vendor assignment for single-vendor mode.

In this project, you should auto-assign the primary vendor when:

- admin creates a product
- bulk import creates a product
- API or custom scripts create a product

### Rule

If `storeMode = SINGLE_VENDOR`, every product must receive `primaryVendorId`.

## 2. Cart And Checkout

Cart and checkout group by vendor.

That is acceptable for quick single-vendor setup because one vendor means one group.

For cleaner UX later:

- keep internal grouping if you want backward compatibility
- remove vendor-name sections from the public checkout UI

## 3. Order Creation

Orders are saved with `vendorId`.

Do not remove that field for single-vendor customers.

Reason:

- it keeps compatibility with existing order reporting
- it avoids breaking vendor-linked history
- it keeps the door open for marketplace customers later

## 4. Vendor Finance

Vendor finance is useful in marketplace mode, but in single-vendor mode it may become unnecessary.

You have two choices:

### Choice A

Hide vendor finance pages for single-vendor customers.

Use this when:

- owner and platform are the same business
- payout is meaningless

### Choice B

Keep vendor finance as internal accounting.

Use this when:

- you still want separate commercial reporting between platform admin and store operator

## 5. Vendor Users And Roles

If a customer wants one owner company but still needs separate operations users:

- keep vendor users/roles
- use only one vendor

This works well for:

- stock team
- delivery team
- finance team

If the customer wants simple admin-only operation:

- hide vendor user management
- let admin manage everything

## Single-Vendor Variants You Can Offer Customers

## Variant 1: Single Vendor With Vendor Panel

Use this when the customer wants:

- one store
- one vendor/company
- separate staff working in the vendor dashboard

Recommended setup:

- one active vendor
- vendor panel enabled
- vendor registration disabled
- payout pages hidden unless needed

## Variant 2: Single Vendor With Admin Only

Use this when the customer wants:

- one store
- no vendor dashboard
- all management from admin

Recommended setup:

- one active vendor in data only
- admin creates and manages products
- vendor pages hidden from menu and routes where possible

This is usually the cleanest business experience for a normal ecommerce brand.

## Migration Checklist For Existing Marketplace Data

If converting a marketplace customer into single-vendor mode:

1. Choose the primary owner vendor
2. Reassign all live products to that vendor
3. Disable all other vendors from selling
4. Review shipping and packaging settings for the primary vendor
5. Review carrier rates tied to vendor
6. Review settlements and payout screens
7. Set marketplace commission to zero if not needed
8. Hide vendor onboarding links
9. Test full checkout
10. Test order history and customer order pages

## Important Data Rule

Do not delete old vendor-linked orders just because the store is becoming single-vendor.

Keep historical records as-is.

Historical `vendorId` values should remain for reporting and traceability.

## Suggested Technical Design

Create a small service such as:

```text
StoreModeService
- isMarketplace()
- isSingleVendor()
- getPrimaryVendor()
- shouldShowVendorFinance()
- shouldAllowVendorRegistration()
```

This is better than spreading `if single vendor` checks everywhere.

## Suggested Admin Controls

In settings, add:

- Store Mode
- Primary Vendor
- Show Vendor Panel
- Show Vendor Finance
- Allow Vendor Registration

For single-vendor customers, the settings should automatically behave like:

- `Store Mode = SINGLE_VENDOR`
- `Allow Vendor Registration = false`

## Suggested UI Rules

When single-vendor mode is active:

- do not show vendor name on public product cards unless needed
- do not show multi-vendor wording in cart
- do not show vendor onboarding links
- do not show marketplace settlement labels to end users

## Suggested Business Rule

If a customer wants a normal branded ecommerce store, prefer:

- admin-only management
- one hidden owner vendor in data

If a customer wants departmental separation, prefer:

- one active vendor
- vendor panel enabled

## What Not To Do

- do not remove `vendorId` from orders
- do not allow products without vendor assignment
- do not fully delete vendor tables for quick single-vendor deployments
- do not maintain separate codebases for single-vendor and marketplace unless absolutely necessary

## Final Recommendation

For your project, the best approach is:

1. Keep vendor architecture in the backend
2. Add a `SINGLE_VENDOR` operating mode
3. Use one owner vendor as `primaryVendor`
4. Auto-assign products to that vendor
5. Disable vendor registration
6. Hide marketplace-only UI and finance where not needed

That will let you support:

- marketplace customers
- single-vendor ecommerce customers

from the same codebase with the lowest long-term maintenance cost.
