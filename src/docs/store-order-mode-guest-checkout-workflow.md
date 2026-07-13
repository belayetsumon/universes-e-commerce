# Store Mode, Sales Order Mode, And Guest Checkout Workflow

## Goal

Support both single-vendor and marketplace operation from settings, while also supporting guest checkout safely.

The system should support these business choices:

1. Single-vendor store with one sales order per checkout.
2. Marketplace store with vendor-split sales orders.
3. Marketplace store with one customer-facing order number, while preserving vendor-level sales orders internally.
4. Guest checkout when enabled from settings.

## Core Rule

Do not remove vendor ownership from products, cart items, order items, shipments, commission, payout, or vendor reports.

Current code expects:

- products have a vendor
- cart groups by vendor
- `SalesOrder` stores one `vendorId`
- shipment and vendor finance use vendor-level orders

Because one `SalesOrder` can only safely represent one vendor, marketplace "single order" should be implemented as a parent customer order/group with child vendor `SalesOrder` rows.

## Recommended Settings

Add these settings to `GlobalSettings`.

```text
storeMode
- SINGLE_VENDOR
- MARKETPLACE

salesOrderMode
- SINGLE_ORDER
- SPLIT_BY_VENDOR

primaryVendorId
- required when storeMode = SINGLE_VENDOR

allowGuestCheckout
- already exists
```

## Valid Mode Matrix

| Store Mode | Sales Order Mode | Expected Behavior | Status |
| --- | --- | --- | --- |
| `SINGLE_VENDOR` | `SINGLE_ORDER` | One checkout creates one `SalesOrder` for `primaryVendorId`. | Pending |
| `SINGLE_VENDOR` | `SPLIT_BY_VENDOR` | Technically works if all products use one vendor, but UI should hide split behavior. | Deferred |
| `MARKETPLACE` | `SPLIT_BY_VENDOR` | One checkout creates one `SalesOrder` per vendor. Existing order save flow already does this. | Partial |
| `MARKETPLACE` | `SINGLE_ORDER` | Customer sees one order group; backend keeps child vendor `SalesOrder` rows. | Partial |

Invalid behavior:

- Do not put multiple vendors into one existing `SalesOrder` row.
- Do not make `SalesOrder.vendorId` nullable for normal marketplace orders.
- Do not bypass vendor split for shipment, commission, payout, or vendor order screens.

## Current Code Anchors

Settings:

- `main/java/com/ecommerce/app/module/settings/model/GlobalSettings.java`
- `main/java/com/ecommerce/app/module/settings/services/GlobalSettingsService.java`
- `main/java/com/ecommerce/app/module/settings/form/StoreSettingsForm.java`
- `main/resources/templates/admin/settings/global-settings.html`
- `main/resources/db/global_settings_schema_repair.sql`

Cart and checkout:

- `main/java/com/ecommerce/app/module/cart/controller/CartController.java`
- `main/java/com/ecommerce/app/module/cart/controller/CartsController.java`
- `main/java/com/ecommerce/app/module/cart/services/CartService.java`
- `main/resources/templates/cart/checkout.html`
- `main/resources/templates/order/order/create.html`

Order creation:

- `main/java/com/ecommerce/app/order/controller/SalesOrderController.java`
- `main/java/com/ecommerce/app/order/model/SalesOrder.java`
- `main/java/com/ecommerce/app/order/model/OrderItem.java`
- `main/java/com/ecommerce/app/order/repository/SalesOrderRepository.java`
- `main/java/com/ecommerce/app/order/services/SalesOrderService.java`

Vendor dependencies:

- `main/java/com/ecommerce/app/vendor/model/Vendorprofile.java`
- `main/java/com/ecommerce/app/vendor/controller/VendorSalesOrderController.java`
- `main/java/com/ecommerce/app/vendor/services/VendorFinanceService.java`

Guest dependencies:

- `main/java/com/ecommerce/app/module/user/services/LoggedUserService.java`
- security configuration for public checkout routes
- order payment, invoice, notification, and customer order lookup flows

## Phase 1: Settings Foundation

Status: `Done`

Add enum types:

- `StoreMode`
  - `SINGLE_VENDOR`
  - `MARKETPLACE`
- `SalesOrderMode`
  - `SINGLE_ORDER`
  - `SPLIT_BY_VENDOR`

Update `GlobalSettings`:

- add `storeMode`
- add `salesOrderMode`
- add `primaryVendorId`
- keep `allowGuestCheckout`

Update settings forms and service:

- expose new fields in the Store or Order section
- validate `primaryVendorId` when `storeMode = SINGLE_VENDOR`
- force `vendorRegistrationEnabled = false` when `storeMode = SINGLE_VENDOR`
- default mode should be safe for current behavior:
  - `storeMode = MARKETPLACE`
  - `salesOrderMode = SPLIT_BY_VENDOR`

Update DB repair/init SQL:

- add columns with safe defaults
- backfill existing settings row

## Phase 2: Central Mode Service

Status: `Done`

Create a small service, for example:

- `StoreOperationModeService`

Responsibilities:

- return active `storeMode`
- return active `salesOrderMode`
- resolve `primaryVendorId`
- answer `isSingleVendorMode()`
- answer `isMarketplaceMode()`
- answer `shouldSplitOrdersByVendor()`
- answer `isGuestCheckoutAllowed()`

All checkout, product, UI, and vendor registration logic should call this service instead of reading raw settings in many places.

## Phase 3: Product Vendor Enforcement

Status: `Partial`

When `storeMode = SINGLE_VENDOR`:

- admin-created products must auto-assign `primaryVendorId` - `Done`
- product imports must auto-assign `primaryVendorId`
- API/custom product creation must auto-assign `primaryVendorId`
- product vendor select can be hidden or locked in admin UI - `Done`

When `storeMode = MARKETPLACE`:

- product vendor selection remains required
- vendor-created products use the active vendor context

Validation:

- no sellable product should enter cart without a valid active vendor
- single-vendor mode should not allow products from other active vendors unless admin explicitly migrates them

## Phase 4: Checkout Order Group Foundation

Status: `Done`

Create a parent customer-facing order model for marketplace single-order display.

Suggested entity:

```text
CustomerOrderGroup
- id
- uuid
- orderGroupCode
- customerId nullable
- guestName
- guestEmail
- guestPhone
- guestSessionId
- subtotal
- shippingTotal
- packingTotal
- discountTotal
- grandTotal
- paymentState
- paymentMethod
- statusSummary
- created
- modified
```

Relationship:

```text
CustomerOrderGroup 1 -> many SalesOrder
SalesOrder many -> 1 CustomerOrderGroup
```

Update `SalesOrder`:

- add `orderGroup`
- keep `vendorId`
- keep vendor totals and shipment behavior unchanged

Why this is needed:

- customer can see one order number
- payment can happen once for the checkout
- vendors still receive separate operational orders
- shipment, commission, payout, and vendor reports remain safe

## Phase 5: Order Creation Logic

Status: `Partial`

Refactor current duplicated order creation paths:

- `/order/savebyvendor`
- `/order/savebyvendorupdate`

Create a reusable order placement service, for example:

- `CheckoutOrderPlacementService`

Responsibilities:

- validate cart
- validate billing and shipping address
- detect customer or guest
- calculate incentives
- calculate payment plan
- create `CustomerOrderGroup` when needed
- create one or more `SalesOrder` records
- create order items
- apply shipping and packing per vendor
- reserve stock
- apply referral/commission only when eligible
- create payment records
- clear checkout session

Behavior by mode:

### `SINGLE_VENDOR + SINGLE_ORDER`

- require `primaryVendorId`
- ensure all cart items belong to `primaryVendorId`
- create one `SalesOrder`
- set `SalesOrder.vendorId = primaryVendorId`
- no customer-facing vendor split UI

### `MARKETPLACE + SPLIT_BY_VENDOR`

- group cart by vendor
- create one `SalesOrder` per vendor
- optionally create `CustomerOrderGroup` for payment and customer summary
- preserve current vendor order behavior

### `MARKETPLACE + SINGLE_ORDER`

- create one `CustomerOrderGroup`
- group cart by vendor internally
- create child `SalesOrder` rows per vendor
- customer sees `orderGroupCode`
- vendor/admin fulfillment uses child `SalesOrder.orderCode`

## Phase 6: Guest Checkout

Status: `Partial - mobile OTP gate added`

Current issue:

- `allowGuestCheckout` exists in settings
- checkout previously called `LoggedUserService.activeUserid()` from guest-sensitive paths
- guest-safe checks now exist for `/order/create`, `/cart/checkout`, `/order/savebyvendor`, and `/order/savebyvendorupdate`

Required changes:

1. Add a safe current-customer resolver.
   - return logged-in `Users` when authenticated
   - return guest checkout identity when anonymous
   - never call `LoggedUserService.activeUserid()` unless user is authenticated

2. Allow guest routes in security. - `Done`
   - cart
   - checkout page
   - address save
   - order submit
   - payment callback/return routes as needed

3. Capture guest identity. - `Mobile OTP path added`
   - verified mobile only
   - no guest email collection
   - server-side guest checkout session
   - billing address copied from shipping details
   - shipping address snapshot

4. Decide customer persistence strategy. - `Updated`
   - mobile-only guest checkout now finds or creates an existing `Users` entity row after OTP verification
   - `SalesOrder.customer` is set to the resolved user while the browser remains in guest checkout mode
   - no readable default password is assigned

5. Disable account-only features for guests.
   - wallet - `Done`
   - reward point redemption - `Done`
   - coupon codes in the current coupon service - `Done`
   - EMI if it requires customer account - `Done`
   - customer dashboard order lookup

6. Add guest order lookup.
   - order code or group code
   - phone/email verification
   - signed token in order confirmation link

7. Update notifications.
   - send order confirmation to guest email/phone
   - do not depend on customer account id

## Phase 7: UI Changes

Status: `Pending`

Admin settings UI:

- add Store Mode selector
- add Sales Order Mode selector
- add Primary Vendor selector
- keep Guest Checkout toggle
- show validation/help text for invalid combinations

Storefront checkout UI:

- single-vendor mode:
  - hide vendor headings
  - show one store checkout summary
- marketplace split mode:
  - show vendor groups and per-vendor shipping/packing
- marketplace single-order display:
  - show one checkout total
  - optional vendor package breakdown below summary

Guest checkout UI:

- show email and phone as required fields
- show login prompt as optional
- hide wallet/reward/EMI controls when guest
- keep COD/online payment based on settings

Admin order UI:

- add filter by order group code
- show grouped order summary when present
- keep child vendor sales order rows for operations

Customer order UI:

- logged-in customer sees order groups
- group details show vendor packages/orders

Vendor order UI:

- unchanged operationally
- only show child `SalesOrder` rows for the active vendor

## Phase 8: Finance, Shipment, And Reporting

Status: `Pending`

Shipment:

- shipment creation stays tied to child `SalesOrder`
- one vendor order can create its own shipment
- parent order group can show overall shipping progress

Vendor finance:

- commission and vendor earnings stay tied to child `SalesOrder`
- single-vendor mode can hide payout pages if owner and platform are the same business

Payment:

- payment can be recorded once on `CustomerOrderGroup`
- allocate payment/incentives to child orders for audit
- existing payment service must be checked before final schema choice

Reports:

- admin reports can aggregate by order group or child sales order
- vendor reports must stay child-order scoped
- customer reports should prefer order group view

## Phase 9: Testing Checklist

Status: `Pending`

Settings tests:

- save `storeMode`
- save `salesOrderMode`
- save `primaryVendorId`
- reject single-vendor mode without primary vendor
- force vendor registration off in single-vendor mode

Single-vendor checkout:

- logged-in checkout creates one `SalesOrder`
- guest checkout creates one `SalesOrder`
- all products are assigned to primary vendor
- vendor labels are hidden in public checkout

Marketplace split checkout:

- logged-in checkout with two vendors creates two child `SalesOrder` rows
- guest checkout with two vendors creates two child `SalesOrder` rows
- shipping and packing are calculated per vendor
- vendor order pages show only their own orders

Marketplace single-order display:

- checkout creates one `CustomerOrderGroup`
- checkout creates child vendor `SalesOrder` rows
- customer sees one order group code
- vendor sees only child order code
- payment and incentive allocation can be audited

Guest checkout:

- anonymous user can open checkout only when `allowGuestCheckout = true`
- anonymous user is redirected to login or blocked when disabled
- guest cannot use wallet or reward points
- guest order confirmation works by email/phone/token
- order notification sends without customer id

Regression:

- stock reservation still works
- referral/commission does not duplicate
- shipment creation still works from admin and vendor order lists
- return/refund flow still finds the correct vendor order
- sales dashboard/order reports still aggregate totals correctly

## Implementation Order

1. Add settings fields, enums, SQL, and admin UI.
2. Add `StoreOperationModeService`.
3. Add product primary-vendor enforcement.
4. Add `CustomerOrderGroup` schema and relationship to `SalesOrder`.
5. Refactor order placement into `CheckoutOrderPlacementService`.
6. Add guest checkout identity handling.
7. Update checkout UI by mode.
8. Update admin/customer order display for order groups.
9. Validate vendor shipment, vendor finance, and reporting flows.

## Done And Remaining

| Area | Status | Notes |
| --- | --- | --- |
| Existing marketplace split order flow identified | Done | `/order/savebyvendor` and `/order/savebyvendorupdate` already group cart by vendor. |
| Existing guest checkout setting identified | Done | `allowGuestCheckout` exists in `GlobalSettings`. |
| Store mode fields | Done | Added `storeMode` and `primaryVendorId` to settings, forms, service, UI, and repair SQL. |
| Sales order mode field | Done | Added `salesOrderMode` to settings, form, service, UI, and repair SQL. |
| Central mode service | Done | Added `StoreOperationModeService`. |
| Product vendor enforcement | Partial | Admin product create/edit auto-assigns primary vendor in single-vendor mode. Imports/API still need follow-up. |
| Parent order group | Done | Added `CustomerOrderGroup`, repository, code generator, and `SalesOrder.orderGroup`. |
| Order placement refactor | Partial | Both existing checkout save paths now create order groups; deeper service extraction remains. |
| Guest checkout backend | Partial | Checkout page/save paths are guest-safe for COD/gift-card style flows; guest online/advance checkout is blocked until callback ownership is added; guest order lookup remains. |
| Guest checkout UI | Partial | Account-only wallet/reward/coupon/EMI controls hidden on both checkout screens; dedicated guest lookup remains. |
| Admin/customer grouped order UI | Pending | Needed if marketplace single-order display is enabled. |
| Vendor/shipment/finance regression test | Pending | Must prove child vendor orders still drive operations. |
