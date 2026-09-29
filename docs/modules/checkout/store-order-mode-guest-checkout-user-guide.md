# Store Mode, Sales Order Mode, And Guest Checkout User Guide

## Purpose

Use this guide when configuring the system for:

- a single-vendor ecommerce store
- a multi-vendor marketplace
- guest checkout
- one customer-facing order number with vendor-level fulfillment orders

## Important Rule

The system must keep vendor ownership internally.

Even when the customer sees one order, the backend should still keep vendor-level `SalesOrder` records so vendor orders, shipment, commission, payout, and reports continue to work safely.

## Settings Location

Open:

```text
/admin/settings/index
```

Use these sections:

- Store tab
  - Store Mode
  - Primary Vendor
  - Allow Guest Checkout
- Order tab
  - Sales Order Mode

## Store Mode

### Marketplace

Use this when multiple vendors sell products.

Expected behavior:

- products can belong to different vendors
- cart can contain products from many vendors
- checkout creates child vendor orders
- vendor panel shows each vendor only their own orders
- payout, commission, shipment, and vendor reports remain active

Recommended Sales Order Mode:

```text
SPLIT_BY_VENDOR
```

### Single Vendor

Use this when one owner sells all products.

Before enabling:

1. Create or select one owner vendor.
2. Set that vendor as Primary Vendor.
3. Move or assign all sellable products to that vendor.
4. Confirm vendor registration should be disabled.

Expected behavior:

- admin-created products are assigned to the Primary Vendor
- checkout only accepts cart items from the Primary Vendor
- public vendor registration is forced off
- vendor-specific UI can be hidden later where needed

Recommended Sales Order Mode:

```text
SINGLE_ORDER
```

## Sales Order Mode

### Split By Vendor

Use this for normal marketplace operation.

If a cart has products from 3 vendors, checkout creates 3 vendor `SalesOrder` records.

Use when:

- vendors fulfill their own orders
- shipment is vendor-based
- commission and payout are vendor-based
- admin needs per-vendor settlement

### Single Order

Use this when the customer should see one order reference.

Important:

- the customer-facing order is saved as `CustomerOrderGroup`
- vendor-level `SalesOrder` records still exist underneath
- fulfillment, shipment, commission, and payout still use child vendor orders

Use when:

- customers should see one checkout/order reference
- admin still needs vendor-level operation
- marketplace checkout should feel simpler to customers

Do not use one mixed-vendor `SalesOrder` row. That breaks vendor operations.

## Guest Checkout

Setting:

```text
Allow Guest Checkout
```

When enabled:

- guests can open checkout
- guests can save billing/shipping address
- guests can place orders
- guests are redirected to the order received page

Guest order page:

```text
/order/placed?group={uuid}
```

Currently allowed for guests:

- COD checkout
- gift card style checkout where supported by current backend
- normal address capture
- order group confirmation

Currently blocked for guests:

- wallet payment
- reward point redemption
- coupon code usage
- EMI checkout
- customer dashboard order list

Reason:

These features require a real customer account in the current codebase.

## Recommended Configurations

### Single Vendor Store

```text
Store Mode: SINGLE_VENDOR
Primary Vendor: Owner vendor
Sales Order Mode: SINGLE_ORDER
Allow Guest Checkout: Optional
```

Use this for one business selling its own products.

### Marketplace

```text
Store Mode: MARKETPLACE
Sales Order Mode: SPLIT_BY_VENDOR
Allow Guest Checkout: Optional
```

Use this for multiple sellers with vendor operations, shipment, commission, and payout.

### Marketplace With One Customer-Facing Order

```text
Store Mode: MARKETPLACE
Sales Order Mode: SINGLE_ORDER
Allow Guest Checkout: Optional
```

Use this when customers should see one order reference, while the backend keeps child vendor orders.

## Admin Checklist Before Go Live

1. Save Store Mode and Sales Order Mode from settings.
2. If Single Vendor mode is selected, choose Primary Vendor.
3. Confirm all active products have the correct vendor.
4. Test adding products to cart.
5. Test logged-in checkout.
6. Test guest checkout if enabled.
7. Test vendor order page.
8. Test admin order list.
9. Test shipment creation from child vendor order.
10. Test commission, vendor amount, and payout reports.

## Guest Checkout Test Flow

1. Logout.
2. Add product to cart.
3. Open checkout.
4. Save billing address.
5. Save shipping address if physical products exist.
6. Select COD or allowed online method.
7. Place order.
8. Confirm the order received page shows an order reference.
9. Login as admin and verify child `SalesOrder` records exist.
10. Login as vendor and verify the vendor sees only their own child order.

## Single Vendor Test Flow

1. Set Store Mode to `SINGLE_VENDOR`.
2. Select Primary Vendor.
3. Save settings.
4. Create a product from admin.
5. Confirm product vendor is auto-assigned to Primary Vendor.
6. Add product to cart.
7. Checkout as logged-in customer.
8. Confirm one order group and one child sales order are created.

## Marketplace Split Test Flow

1. Set Store Mode to `MARKETPLACE`.
2. Set Sales Order Mode to `SPLIT_BY_VENDOR`.
3. Add products from two different vendors to cart.
4. Checkout.
5. Confirm one order group is created.
6. Confirm two child `SalesOrder` records are created.
7. Confirm each vendor sees only their own order.

## Current Limitations

- Product imports and custom API product creation still need primary-vendor enforcement.
- Order placement logic still has duplicated controller code and should be moved to a service.
- Guest order lookup by order code/email/phone token is not complete yet.
- Guest online payment callback flow still needs dedicated testing.
- Admin/customer order group list UI still needs enhancement.

## Production Notes

Run the database scripts before using the new settings and order group flow:

```text
main/resources/db/global_settings_schema_repair.sql
main/resources/db/order_group_guest_checkout_init.sql
```

The order group script also makes `sales_order.customer_id` nullable when the existing database still requires a logged-in customer. This is required for guest checkout.

Back up the database before running schema changes on production.
