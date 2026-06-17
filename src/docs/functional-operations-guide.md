# Functional Operations Guide

## Purpose

This guide explains how to operate the main business flows in this project:

- user creation
- customer registration
- vendor creation
- vendor staff assignment
- category and attribute setup
- product creation
- customer purchase flow
- payment flow
- shipment and carrier flow
- barcode and order PDF use
- COD collection
- settlement and payout
- return and refund

This system is marketplace-first. That means products, carts, orders, shipment, and finance are all vendor-aware.

For category and attribute setup details, also read:

- [Category Attribute Admin Guide](./category-attribute-admin-guide.md)

For single-vendor deployment strategy, also read:

- [Single Vendor Mode Guide](./single-vendor-mode.md)

## Main Actors

- `Marketplace Admin`: manages users, vendors, carriers, shipping setup, orders, finance, and payouts.
- `Customer`: registers, buys products, pays, tracks orders, and requests returns.
- `Vendor`: manages products, vendor staff, orders, shipment, COD collection, payout methods, and payout requests.
- `Carrier / Delivery Team`: delivers orders physically according to shipment and carrier setup.

## Recommended Setup Order

Use this sequence when setting up a fresh deployment:

1. Create admin and staff users.
2. Create vendor profile or vendor profiles.
3. Create vendor roles and vendor staff if needed.
4. Create carriers.
5. Create carrier rates.
6. Create shipping profiles.
7. Create delivery persons if using vendor riders.
8. Create categories and catalog attributes.
9. Add products and variants.
10. Test customer order, payment, shipment, COD, and return flow.
11. Review finance dashboard, settlement ledger, and payout workflow.

## 1. User Creation

### 1.1 Admin or Back Office User Creation

1. Open `/users/registrations`.
2. Fill user information, role, status, and user type.
3. Enter password for new users.
4. Save the record.
5. To update an existing user, open `/users/edit/{id}`.

Notes:

- This is the main back-office user creation flow.
- Passwords are encrypted when newly entered.

### 1.2 Public User Registration

This project has more than one registration entry point.

1. Basic public registration exists at `/users/uregistrations`.
2. That flow saves the user with `Pending` status.
3. A storefront customer registration flow also exists.

### 1.3 Customer Registration

1. Open `/customer_registration/registration`.
2. Fill customer account details.
3. Submit the form to `/customer_registration/customer_registration_save`.
4. The system creates the user with:
   - role `customer`
   - user type `customer`
   - status `Active`
5. The system also creates referral data for the customer.

There is also a front registration save route at `/users/frontRegistrationSave` that creates an active customer and referral profile.

## 2. Vendor Creation and Vendor Staff

### 2.1 Vendor Profile Creation

1. Login as the user who will own the vendor account.
2. Open `/vendorprofile/create`.
3. Fill vendor company and contact information.
4. Save the form.
5. The system generates a vendor code automatically.
6. Review the saved vendor at `/vendorprofile/details`.
7. Use `/vendorprofile/edit` later for updates.

### 2.2 Vendor User or Staff Assignment

Use this when a vendor needs staff accounts such as manager, packing staff, or order team.

1. Create the staff user first in `/users/registrations`.
2. Make sure the user is `Active`.
3. Open `/vendor-users/add_vendor_user`.
4. Enter the active user email.
5. Choose a vendor role.
6. Save.

Notes:

- The user must already exist.
- The same user cannot be assigned twice to the same vendor.

### 2.3 Vendor Payout Method Setup

Before vendors request payout, they should save their payout destination.

1. Open `/vendor-payout-methods/create`.
2. Choose payout method type.
3. Fill account details.
4. Save.
5. Review all methods at `/vendor-payout-methods/list`.

## 3. Carrier, Shipping, and Delivery Setup

### 3.1 Carrier Creation

1. Open `/admin/carriers/new`.
2. Fill `code` and `name`.
3. Set `active`, `trackable`, `supportsCod`, and `requiresApi` as needed.
4. Choose `Carrier Mode`:
   - `THIRD_PARTY`
   - `VENDOR_SELF`
   - `VENDOR_RIDER`
   - `STORE_PICKUP`
5. Choose `Settlement Mode`:
   - `MARKETPLACE_MANAGED`
   - `VENDOR_MANAGED`
6. Choose `Shipping Charge Owner`:
   - `MARKETPLACE`
   - `VENDOR`
   - `CUSTOMER`
7. Choose `COD Collection Mode`:
   - `MARKETPLACE_COLLECTS`
   - `CARRIER_COLLECTS_FOR_MARKETPLACE`
   - `VENDOR_COLLECTS`
8. Save.

Why this matters:

- Carrier setup controls more than delivery.
- It also affects shipment settlement and COD responsibility.

### 3.2 Carrier Rate Setup

1. Open `/admin/carrier-rates/create`.
2. Choose carrier.
3. Choose district.
4. Choose delivery speed and delivery type.
5. Enter the rate fields shown in the form.
6. Save.

Use `/admin/carrier-rates/list` to review or edit rates.

### 3.3 Shipping Profile Setup

1. Open `/admin/shipping-profiles/create`.
2. Choose vendor if the profile is vendor-specific.
3. Choose profile type.
4. Select allowed carriers.
5. Select districts or delivery coverage rules exposed by the form.
6. Save.

Use `/admin/shipping-profiles/list` for ongoing management.

### 3.4 Delivery Person Setup

This project also supports delivery persons for vendor rider or internal delivery.

1. Open the delivery person management screen from the shipping module or menu.
2. Create the rider or delivery person.
3. Assign the related vendor if needed.
4. Save.
5. Use the saved delivery person during shipment creation.

Important note:

- The delivery person controller is a simple legacy module.
- In some deployments the menu path may be clearer than the raw route, so verify the final screen from the UI.

## 4. Category, Attribute, and Product Setup

### 4.1 Category and Attribute Setup

1. Create categories first.
2. Create reusable catalog attributes.
3. Add attribute options for select-type fields.
4. Map attributes to categories.
5. Mark variant-driving attributes correctly at category mapping level.

Read the full setup details here:

- [Category Attribute Admin Guide](./category-attribute-admin-guide.md)

### 4.2 Product Creation by Admin

1. Open `/product/create`.
2. Select the category first.
3. Fill product information:
   - name
   - price
   - discount
   - manufacturer
   - stock-related fields
   - image
   - physical or virtual product settings
4. Fill the dynamic attribute section that loads from the selected category.
5. If the product uses catalog variants, configure the variant-related data.
6. Save.
7. Review the product in `/product/details/{id}`.

### 4.3 Product Creation by Vendor

1. Open `/productvendor/create`.
2. Fill product information.
3. Select category.
4. Fill dynamic specifications.
5. Add shipping or delivery area data if the product is physical.
6. Save.
7. Review at `/productvendor/details/{id}`.

Important notes:

- In marketplace mode, every sellable product should belong to a vendor.
- In single-vendor mode, all products should still point to the owner vendor.
- If `manageProductVariants = true`, customers must choose a catalog variant before adding to cart.

## 5. Customer Buying Flow

### 5.1 Customer Prerequisites

1. Customer creates an account and logs in.
2. Customer selects delivery district before adding a physical product to cart.

Important note:

- Physical products are delivery-area validated against the selected district.
- Virtual-only products do not need shipment.

### 5.2 Add to Cart

1. Customer opens the single product page.
2. If the product has variants, the customer selects the required catalog variant.
3. Customer enters quantity.
4. Customer submits add to cart.

The system blocks add to cart when:

- product does not exist
- physical product has no selected district
- delivery area does not match the selected district
- required variant is missing
- selected quantity is unavailable

### 5.3 Cart and Checkout

1. Customer opens `/cart/index`.
2. Cart items are grouped by vendor.
3. Customer selects shipping and packaging choices per vendor group when applicable.
4. Customer proceeds to `/cart/checkout`.
5. Checkout calculates:
   - vendor subtotal
   - vendor shipping cost
   - vendor packaging cost
   - grand total
6. Customer chooses payment plan and payment method.
7. Customer places the order.

Important system behavior:

- One checkout can create multiple `SalesOrder` records.
- The split happens by vendor.
- Stock is reserved automatically during order placement.

## 6. Payment Flow

### 6.1 Supported Payment Methods

The main payment methods in this codebase are:

- `COD`
- `WALLET`
- `EMI`
- `SSLCOMMERZ`
- `BKASH`

### 6.2 Supported Payment Plans

The main payment plans are:

- `FULL_COD`
- `FULL_PREPAID`
- `PARTIAL_ADVANCE_COD`
- `EMI`

### 6.3 How Each Payment Plan Works

#### FULL_COD

1. Customer places order without advance payment.
2. Order stores COD due.
3. COD is collected later from the shipment flow.

#### FULL_PREPAID

1. Customer pays full amount online or wallet during checkout or follow-up payment.
2. If fully paid:
   - physical orders move to `CONFIRMED`
   - virtual-only orders can move directly to `COMPLETED`

#### PARTIAL_ADVANCE_COD

1. Customer pays only the planned advance amount.
2. Remaining balance stays as COD due.
3. After successful advance, the order becomes `CONFIRMED`.

#### EMI

1. Customer selects EMI-eligible checkout.
2. The project creates a provider-managed EMI request.
3. The order waits on the EMI provider flow.

### 6.4 Customer Follow-up Payment After Order Placement

1. Customer opens `/customerorder/payment/{orderid}`.
2. The payment screen offers online payment methods `SSLCOMMERZ` and `BKASH`.
3. Customer pays the remaining allowed amount.
4. System records the payment.
5. System refreshes payment summary and order payment state.

Important payment rules:

- Cancelled or returned orders do not accept new payments.
- Fully paid orders do not accept more payments.
- COD collection cannot exceed remaining COD due.
- Customer online payments cannot exceed the current payable amount.

## 7. Order Management by Actor

### 7.1 Customer Order Operations

Customer order screen: `/customerorder/index`

Customer can:

- view order details
- make eligible follow-up payments
- cancel order in early stages
- request return for eligible physical items

Customer cancellation rule:

- customer cancellation is allowed only while the order is still in `NEW_ORDER`, `PENDING`, or `CONFIRMED`

Customer return rule:

- return request is available only for eligible physical items
- virtual items cannot be returned from this flow

### 7.2 Vendor Order Operations

Vendor order screen: `/vendor-order/index`

Vendor can:

- review their own orders
- change allowed order statuses
- update packing and delivery charges
- manage item return processing
- generate order PDF with barcode

Vendor cancellation rule:

- vendor can cancel in `NEW_ORDER`, `PENDING`, `CONFIRMED`, and `PROCESSING`
- vendor can also cancel `PACKED` orders only if shipment has not been created yet

### 7.3 Marketplace Admin Order Operations

Admin order screen: `/admin-customer/orderlist`

Marketplace can:

- review all orders
- change order status
- create shipment
- process item return
- generate order PDF with barcode

Marketplace cancellation rule:

- marketplace can cancel up to the `PACKED` stage

### 7.4 Typical Operational Status Sequence

The common working sequence is:

1. `NEW_ORDER`
2. `CONFIRMED`
3. `PROCESSING`
4. `PACKED`
5. `SHIPPED`
6. `IN_TRANSIT`
7. `OUT_FOR_DELIVERY`
8. `DELIVERED`
9. `COMPLETED`

Important note:

- Cancellation is no longer the normal path once shipment has already started.
- After shipped or delivered stages, use shipment handling or return handling instead.

## 8. Shipment, Delivery Delay, and Barcode

### 8.1 When Shipment Can Be Created

Shipment can be created only when the order:

- is in `CONFIRMED`, `PROCESSING`, or `PACKED`
- has a vendor
- is not a virtual-only order
- has shipping district data
- does not already have a shipment

### 8.2 Shipment Creation by Admin

1. Open `/admin/shipments/create`.
2. Optionally open it with an order preselected.
3. Choose the sales order.
4. Select carrier.
5. Optionally select delivery person.
6. Fill tracking number, speed, delivery type, label URL, or metadata if your process uses them.
7. Save.

### 8.3 Shipment Creation by Vendor

1. Open `/vendor/shipments/new?orderId={orderId}` or `/vendor/shipments`.
2. Review the prefilled order data.
3. Select carrier.
4. Optionally select delivery person.
5. Enter tracking number and shipment details.
6. Save.

Important note:

- In practical use, always select a carrier.
- Delivery person is additional assignment, not a replacement for shipment structure.

### 8.4 Shipment Status Handling

Shipment statuses are:

- `PENDING`
- `SHIPPED`
- `IN_TRANSIT`
- `DELIVERED`
- `CANCELLED`
- `RETURNED`

### 8.5 Delivery Delay Handling

This codebase does not currently have a dedicated `DELAYED` shipment status.

Recommended operational handling:

1. Keep the shipment in `PENDING` or `IN_TRANSIT`.
2. Update tracking number or metadata if your team uses them.
3. Keep customer and vendor informed outside the status code if needed.
4. If delivery fails before completion, decide whether to retry, cancel before final completion, or move into return handling.

### 8.6 Barcode and Order PDF

Admin and vendor can generate printable order PDF files.

Available routes:

- admin: `/admin-customer/orders/{id}/pdf`
- vendor: `/vendor-order/orders/{id}/pdf`

The PDF includes:

- order summary
- `CODE_128` barcode based on order code
- QR code

## 9. COD Collection

COD collection is connected to shipment.

### 9.1 How COD Is Prepared

When shipment is created or edited:

1. Shipment syncs payment summary from the sales order.
2. Shipment stores:
   - total order amount
   - COD planned amount
   - COD collected amount
   - COD pending amount

### 9.2 How COD Is Collected

1. Open vendor shipment screen.
2. Use `/vendor/shipments/{id}/collect-cod`.
3. Enter the amount actually collected.
4. Submit.
5. System records a `COD` payment against the order.
6. COD pending is reduced automatically.
7. If COD pending becomes zero, shipment is marked `DELIVERED`.

Important note:

- COD collection amount cannot be greater than the remaining COD balance.

## 10. Settlement, Vendor Ledger, and Payout

This project has three related but different finance ideas:

1. shipment settlement
2. vendor transaction ledger
3. vendor payout request

### 10.1 Shipment Settlement

Each shipment stores settlement-related snapshot fields such as:

- carrier mode
- settlement mode
- shipping charge owner
- COD collection mode

Each shipment also calculates:

- `vendorPayableAmount`
- `marketplacePayableAmount`

High-level meaning:

- vendor payable is what the marketplace owes the vendor after commission and related rules
- marketplace payable is what stays with the marketplace from commission, shipping, and COD fee rules

Admin settlement screens:

- `/admin/finance/dashboard`
- `/admin/finance/settlements`

Vendor finance screens:

- `/vendor/finance/dashboard`
- `/vendor/finance/ledger`

### 10.2 Vendor Earning Ledger

When an order reaches `DELIVERED` or `COMPLETED`:

1. A vendor earning transaction is created if missing.
2. That transaction starts as `PENDING`.
3. Refund or return creates reversing finance transactions when needed.

### 10.3 Vendor Payout Request

1. Vendor opens `/vendor-payout/request`.
2. Vendor sees available balance.
3. Vendor selects saved payout method.
4. Vendor enters or confirms request.
5. System creates a payout request record.

Important note:

- This module has two request paths.
- The alternate automated request flow explicitly sets payout status to `REQUESTED`.
- Verify your form template or business process to confirm which request path your deployment uses.

### 10.4 Admin Payout Processing

1. Admin opens `/admin/payouts/list`.
2. Filter by vendor code, status, or date if needed.
3. Process request using one of:
   - `PROCESSING`
   - `PAID`
   - `CANCELLED`
4. Add reference and note when needed.

What happens next:

- `PROCESSING` marks the payout as being handled.
- `PAID` marks payout complete and moves requested vendor balances into paid state.
- `CANCELLED` returns requested balance back to available state.

### 10.5 Important Finance Caveat

I found service logic for moving vendor earnings from `PENDING` to `AVAILABLE`, but I did not find a matching visible controller or admin screen that performs that release in the current codebase.

That means:

- shipment settlement and vendor earnings are implemented
- vendor payout request logic is implemented
- but promotion from `PENDING` earning to `AVAILABLE` balance may require a custom admin step, hidden screen, or developer/service call depending on your deployment

This should be checked before going live with vendor cash-out operations.

## 11. Return and Refund

### 11.1 Customer Return Request

1. Customer opens order details.
2. Customer selects eligible order items.
3. Customer submits return request.
4. System marks:
   - selected items as `RETURN_REQUESTED`
   - order as `RETURN_REQUESTED`

Rules:

- only eligible physical items can be requested for return
- virtual items cannot use this flow
- items already returned cannot be requested again

### 11.2 Vendor or Marketplace Return Processing

1. Vendor or marketplace opens order details.
2. Staff selects the requested item.
3. Staff marks the item returned.
4. System then:
   - sets item return status to `RETURNED`
   - restocks reserved or sold stock when applicable
   - recalculates order totals
   - checks if refund is needed
   - records refund payment if total paid is now higher than the new order total
   - creates vendor finance reversal if needed
   - updates order status to `PARTIALLY_RETURNED` or `RETURNED`

### 11.3 Cancellation and Refund

When an order is cancelled:

1. System can refund paid amount.
2. System can cancel linked open shipments.
3. Payment tracking is refreshed.

When an order is fully returned:

1. System refunds paid amount.
2. Vendor earning can be reversed.
3. Stock can be returned to available state for physical items.

Important notes:

- cancelled and returned orders are treated as closed states
- no additional payments should be accepted after that

## 12. Marketplace vs Single Vendor Operation

### Marketplace Mode

Use this when:

- multiple vendors will sell
- vendor payouts are real
- settlement reporting matters per vendor

### Single Vendor Mode

Use this when:

- one store owner sells everything
- public vendor onboarding should be disabled
- vendor payout and multi-vendor UI can be hidden

Recommended single-vendor approach for this project:

1. Keep one owner vendor profile.
2. Assign all products to that vendor.
3. Disable vendor registration.
4. Hide marketplace-only vendor UI where needed.

Read the full note here:

- [Single Vendor Mode Guide](./single-vendor-mode.md)

## 13. Current Limitations and Operational Warnings

- One customer checkout can create multiple orders because the system splits by vendor.
- Shipment creation is effectively limited to one shipment record per order in the current logic.
- There is no dedicated `DELAYED` shipment status.
- Virtual-only orders should not enter normal shipment flow.
- Variant-managed products require customer variant selection before cart add.
- Delivery-person management looks like a basic legacy module, so verify its final menu path in your deployed UI.
- Vendor payout release from `PENDING` earning to `AVAILABLE` balance is not clearly exposed in the visible controller layer.

## 14. Suggested Daily Admin Routine

1. Review new orders.
2. Confirm paid or valid COD orders.
3. Move ready orders to processing and packed.
4. Create shipments for eligible orders.
5. Track COD collection.
6. Review delayed or in-transit shipments.
7. Process return requests.
8. Review finance dashboard and settlement ledger.
9. Process vendor payout requests if your balance-release workflow is active.

## 15. Reward Commission

This project has a separate referral reward and commission system for customers.

It is different from:

- vendor payout
- marketplace settlement
- product marketplace commission

### 15.1 What Reward Commission Means Here

In this codebase, reward commission means:

- a referral chain exists between users
- when a buyer places an order, a commission pool is created
- that pool is distributed upward through the referral chain
- credited amounts go into customer wallets

### 15.2 Source of the Commission Pool

The reward commission pool is not taken from the full order total directly.

Current formula:

1. The system calculates marketplace commission on order items.
2. It totals `totalMarketPlaceCommissionAmount` on the sales order.
3. It takes `10%` of that amount as the referral commission pool.
4. It distributes that pool through the referral tree.

Practical example:

- if marketplace commission on an order is `100 BDT`
- the referral commission pool becomes `10 BDT`
- then that `10 BDT` is split by level rates

### 15.3 Who Gets Paid

The buyer does not receive the level commission.

The system walks upward through the buyer's referral chain:

1. level 1 parent
2. level 2 parent
3. level 3 parent
4. up to level 8

For each level:

- the system checks whether a parent referrer exists
- the system loads the rate for that level
- the system credits the parent wallet

If the chain breaks at any level, distribution stops there.

### 15.4 Default Level Rates

If admin has not saved custom level rate settings, the system falls back to these defaults:

- level 1: `10%`
- level 2: `5%`
- level 3: `4%`
- level 4: `3%`
- level 5: `2%`
- level 6: `1%`
- level 7: `1%`
- level 8: `0.5%`

These percentages are applied to the referral commission pool, not to the order total.

### 15.5 How Admin Manages Reward Commission Rates

1. Open `/lavelratesettings/list`.
2. Review existing level settings.
3. Use `/lavelratesettings/create` to add a new level rule.
4. Choose the level.
5. Enter commission rate.
6. Save.
7. Use `/lavelratesettings/edit/{id}` for updates.

Important note:

- if the saved rate is above `1`, the service treats it like a percent and divides by `100`
- for example `10` becomes `10%`
- if the saved rate is `0.10`, it is already treated as `10%`

### 15.6 Customer Reward and Referral Screens

Customers can use these screens:

- wallet balance: `/customerwallet/wallet`
- referral list: `/customerreferral/list`
- reward history: `/customerrewards/rewards`

Wallet transactions store useful metadata such as:

- transaction type
- source type
- source reference
- level number

That makes it possible to trace level commission entries back to an order.

### 15.7 Admin Reward Monitoring

Admin can review:

- wallets: `/wallet/walletlist`
- wallet transactions: `/wallettransaction/list`
- reward records: `/referral-reward/referral-reward-list`
- rate settings: `/lavelratesettings/list`

### 15.8 Related Reward Types

This module has more than one reward concept:

- `LEVEL_COMMISSION`: order-driven multi-level commission
- `REFERRAL_REWARD`: one-time referral reward style credits
- wallet redemption and cashout features

Current behavior:

- checkout uses `LEVEL_COMMISSION`
- referral signup can also trigger a one-time signup reward when the new user is linked to a real referrer

### 15.9 Customer Cashout Flow

Customers can now request cashout from the reward module.

Customer screens:

- wallet: `/customerwallet/wallet`
- cashout form: `/customerwallet/wallet/cashout`
- cashout request list: `/cashoutcustomerrequest/list`

Step by step:

1. Customer opens the wallet page.
2. Customer chooses `Request Cash Out`.
3. Customer enters reward points to redeem.
4. Customer chooses payout method:
   - `MOBILE`
   - `BANK`
5. System redeems the selected points from wallet balance.
6. System creates a `PENDING` cashout request.
7. Customer can review request status in the request list screen.

Important note:

- current code uses a conversion rate of `0.01`
- that means `100` reward points become `1.00` payout amount in the cashout request record

### 15.10 Admin Cashout Processing

Admin screen:

- `/admin/cashouts`

Step by step:

1. Open the admin cashout request list.
2. Review customer, payout amount, payment method, and current status.
3. For `PENDING` requests:
   - `Approve`
   - `Reject`
4. For `APPROVED` requests:
   - `Mark Paid`

What each action does:

- `Approve`: confirms the request for payout handling
- `Reject`: rejects the request and restores the redeemed balance to the customer wallet
- `Mark Paid`: marks the already approved request as completed

### 15.11 Operational Notes After Fix

- Signup reward state is now preserved correctly and is not falsely pre-marked as already granted in the main customer registration flow.
- Signup reward and referral profile creation are now handled consistently across the active registration flows.
- Admin cashout processing no longer creates a second wallet debit during `Mark Paid`.
- Customer and admin cashout routes are now aligned with their templates and list screens.

### 15.12 Remaining Business Notes

- The live order flow does trigger level commission distribution.
- Reward cashout currently uses the module's built-in `0.01` conversion rule from points to payout amount.
- If your business wants `1 point = 1 BDT` or another rule, update that conversion logic before production launch.
- If you want payout method details beyond `MOBILE` and `BANK`, extend the customer payout info design and related screens.

### 15.13 Recommended Verification

Before enabling reward commission and cashout in production:

1. test referral registration
2. test order placement with referrer chain
3. confirm wallet credits are created per level
4. verify reward history screen shows those credits
5. test signup reward with and without a referrer
6. test cashout request, approval, and payout end to end
