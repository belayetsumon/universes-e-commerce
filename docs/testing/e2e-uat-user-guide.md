# E2E UAT User Guide With Real Business Data

Generated from the current codebase on 2026-09-29.

## Purpose

This guide gives a complete User Acceptance Testing path for the Universes E-Commerce marketplace. It is written for business users, QA, product owners, and release approvers who need to execute the application like a real marketplace operation, not only check pages one by one.

Use it with:

- `docs/testing/enterprise-sqa-business-feature-matrix.md`
- `docs/overview/feature-catalog.md`
- `docs/operations/functional-operations-guide.md`
- `docs/security/application-security-endpoint-inventory.csv`
- `docs/security/application-security-permission-catalogue.csv`

This document is a UAT execution guide. It does not prove the system by existing in the repository. Fill the Actual Result, Evidence, Tester, and Sign-off columns during execution.

## UAT Data Set

Use these records consistently across every test. If the environment already has matching records, reuse them and record the database IDs or visible codes in the Evidence column.

### Login And Actor Data

| Actor | Business Role | Login / Email | Mobile | Password For UAT | Expected Access |
|---|---|---|---|---|---|
| UAT-A01 | Marketplace Admin | admin.qa@universes.test | +8801710000001 | ChangeMe@12345 | Admin, system setup, users, products, shipping, finance, fraud |
| UAT-A02 | Operations Staff | ops.qa@universes.test | +8801710000002 | ChangeMe@12345 | Order, shipment, customer support |
| UAT-V01 | Vendor Owner | vendor.owner@dhakagadget.test | +8801810000001 | ChangeMe@12345 | Vendor profile, product, stock, shipment, payout |
| UAT-V02 | Vendor Staff | packer@dhakagadget.test | +8801810000002 | ChangeMe@12345 | Vendor packing/shipment role only |
| UAT-V03 | Second Vendor Owner | vendor.owner@ctgfashion.test | +8801910000001 | ChangeMe@12345 | Second vendor for split-order and IDOR checks |
| UAT-C01 | Registered Customer | tanvir.customer@testmail.test | +8801711000001 | ChangeMe@12345 | Customer profile, cart, checkout, orders, wallet |
| UAT-C02 | Referred Customer | rima.referred@testmail.test | +8801711000002 | ChangeMe@12345 | Referral and reward proof |
| UAT-G01 | Guest Customer | guest mobile only | +8801711000099 | OTP based | Guest checkout |

### Business Master Data

| Area | Value |
|---|---|
| Country | Bangladesh |
| Country code | BD |
| Division | Dhaka Division |
| District | Dhaka |
| Thana | Dhanmondi |
| Vendor 1 | Dhaka Gadget Hub |
| Vendor 2 | Chattogram Fashion House |
| Carrier | REDX Dhaka Metro |
| Carrier code | REDX-DHK |
| Shipping zone | DHAKA-METRO |
| Standard packaging | Standard Box |
| Fragile packaging | Premium Fragile Box |
| Coupon | EID10 |
| Gift card | GC-UAT-1000 |
| Payout reference | BNK-UAT-001 |

### Product Data

| Product | Vendor | Category | Type | SKU | Sales Price | Purchase Price | Stock | Weight | UAT Purpose |
|---|---|---|---|---:|---:|---:|---:|---:|---|
| Galaxy A55 5G 8/256 | Dhaka Gadget Hub | Electronics > Mobile | Physical | 1001001 | 52500.00 | 48000.00 | 25 | 0.45 kg | High-value physical item |
| Type-C Fast Charger 25W | Dhaka Gadget Hub | Electronics > Accessories | Physical | 1001002 | 1450.00 | 950.00 | 100 | 0.20 kg | Low-value accessory |
| Cotton Panjabi Blue XL | Chattogram Fashion House | Fashion > Men | Physical | 2002001 | 2200.00 | 1400.00 | 40 | 0.35 kg | Second-vendor split order |
| Jamdani Sharee Premium | Chattogram Fashion House | Fashion > Women | Physical | 2002002 | 9500.00 | 7200.00 | 12 | 0.85 kg | Low-stock and return |
| E-Gift Voucher 1000 | Platform / Owner Vendor | Digital Goods | Virtual | 3003001 | 1000.00 | 800.00 | 999 | 0.00 kg | Virtual order and no-shipment proof |

## Execution Rules

| Rule | UAT Instruction |
|---|---|
| Browser sessions | Use separate browser profiles for Admin, Vendor 1, Vendor 2, Customer, and Guest. |
| Screenshots | Capture before save, success message, list/detail page after save, and cross-actor denial screens. |
| IDs | Record generated IDs, UUIDs, order codes, shipment IDs, payout IDs, and wallet transaction references. |
| Currency | Use BDT amounts exactly as listed unless the environment has currency settings that display another symbol. |
| CSRF | Do not bypass browser forms. If using API tools, separately verify browser POST without CSRF is rejected. |
| Proof boundary | Mark a case Partial when source exists but browser, database, provider, or permission proof is missing. |

## UAT-00: Environment Smoke

| Step | User | Route | Action | Expected Result | Actual Result | Evidence |
|---|---|---|---|---|---|---|
| 00.1 | Guest | `/index` | Open home page. | Storefront renders without login. |  |  |
| 00.2 | Guest | `/users/login` | Open login page. | Login form has `username`, `password`, remember-me checkbox. |  |  |
| 00.3 | Admin | `/admin/index` | Login as UAT-A01. | Admin dashboard opens. |  |  |
| 00.4 | Vendor | `/vendor/home` | Login as UAT-V01 after user creation. | Vendor dashboard opens. |  |  |
| 00.5 | Customer | `/customer` | Login as UAT-C01 after registration. | Customer dashboard opens. |  |  |

## UAT-01: Admin User, Role, Permission, And System Setup

### UAT-01A: Create Back-Office Or Vendor Users

Route: `/users/registrations`

Form action: `/users/save`

| Field | UAT-A02 Value | UAT-V01 Value | UAT-V02 Value | Notes |
|---|---|---|---|---|
| `firstName` | Arif | Rahim | Farzana | Required visible field. |
| `lastName` | Chowdhury | Hossain | Islam | Required visible field. |
| `mobile` | +8801710000002 | +8801810000001 | +8801810000002 | Use unique mobile values. |
| `email` | ops.qa@universes.test | vendor.owner@dhakagadget.test | packer@dhakagadget.test | Used as login username. |
| `role` | Operations role or admin role | Vendor owner role | Vendor staff compatible role | Role list is environment data. |
| `status` | Active | Active | Active | User must be active before assignment. |
| `userType` | admin or staff | vendor | vendor | Match available enum/options. |
| `remarks` | UAT operations user for order support. | UAT vendor owner for Dhaka Gadget Hub. | UAT vendor packing staff. | Keep audit-friendly text. |
| `password` | ChangeMe@12345 | ChangeMe@12345 | ChangeMe@12345 | Change after test if environment is shared. |

Expected result:

- User saves successfully.
- User appears in `/users`.
- Login works for the created actor.
- Password is not displayed in list/detail pages.

### UAT-01B: Install Permission Catalogue

Route: `/admin/system`

Action: submit permission-catalogue seed form to `/admin/system/seed/permission-catalogue`.

Expected result:

- System reports missing permissions inserted or existing permissions skipped.
- Re-running the action does not create duplicates.
- Permission catalogue remains aligned with `docs/security/application-security-permission-catalogue.csv`.

### UAT-01C: Endpoint Inventory Review

Route: `/admin/system/endpoints`

Expected result:

- Admin can open endpoint dashboard.
- Non-admin users cannot open it.
- Counts and route families align with the current security inventory.

## UAT-02: Vendor Profile And Vendor Staff Setup

### UAT-02A: Create Vendor Profile

Login as UAT-V01.

Route: `/vendorprofile/create`

Form action: `/vendorprofile/save`

| Field | Dhaka Gadget Hub Value |
|---|---|
| `companyName` | Dhaka Gadget Hub |
| `designation` | Managing Director |
| `firstName` | Rahim |
| `lastName` | Hossain |
| `phone` | +8801810000001 |
| `email` | vendor.owner@dhakagadget.test |
| `address` | House 18, Road 7, Dhanmondi, Dhaka 1209, Bangladesh |
| `description` | Authorized electronics seller for smartphones, chargers, accessories, and warranty-backed devices in Dhaka metro. |

Expected result:

- Vendor profile saves.
- Vendor code is generated or retained.
- `/vendorprofile/details` shows the saved company, contact, address, and status.

Repeat for UAT-V03:

| Field | Chattogram Fashion House Value |
|---|---|
| `companyName` | Chattogram Fashion House |
| `designation` | Owner |
| `firstName` | Sadia |
| `lastName` | Karim |
| `phone` | +8801910000001 |
| `email` | vendor.owner@ctgfashion.test |
| `address` | Shop 22, Agrabad Commercial Area, Chattogram 4100, Bangladesh |
| `description` | Apparel vendor selling panjabi, sharee, and seasonal fashion products across Bangladesh. |

### UAT-02B: Assign Vendor Staff

Login as UAT-V01.

Routes:

- `/vendor-users/roles`
- `/vendor-users/roles/add`
- `/vendor-users/add_vendor_user`
- `/vendor-users/save`

Use these values:

| Form | Field | Value |
|---|---|---|
| Vendor role | Role name | Packing And Shipment Operator |
| Vendor role | Privileges | Shipment read, shipment manage, order read, stock read only |
| Vendor user assignment | Staff email | packer@dhakagadget.test |
| Vendor user assignment | Vendor role | Packing And Shipment Operator |

Expected result:

- UAT-V02 can open permitted vendor shipment/order pages.
- UAT-V02 cannot manage payout method, payout request, or vendor roles unless explicitly granted.
- UAT-V02 cannot see UAT-V03 vendor data.

## UAT-03: Shipping, Location, Carrier, Rate, Packaging, And Profile Setup

### UAT-03A: Create International Location Hierarchy

Route: `/admin/shipping-locations/create`

Form action: `/admin/shipping-locations/save`

Create records in this order:

| Level | Field Data |
|---|---|
| Country | Name `Bangladesh`; code `BD`; type `COUNTRY`; parent blank; active true |
| Division | Name `Dhaka Division`; code `BD-DHA`; type `DIVISION`; parent `Bangladesh`; active true |
| District | Name `Dhaka`; code `BD-DHA-DHK`; type `DISTRICT`; parent `Dhaka Division`; legacy district `DHAKA`; active true |
| Thana | Name `Dhanmondi`; code `BD-DHA-DHK-DHAN`; type `THANA`; parent `Dhaka`; active true |

Expected result:

- Location list shows parent-child hierarchy.
- District remains compatible with legacy checkout district selection.

### UAT-03B: Create Shipping Zone

Route: `/admin/shipping-zones/create`

Form action: `/admin/shipping-zones/save`

| Field | Value |
|---|---|
| `name` | Dhaka Metro Same Day |
| `code` | DHAKA-METRO |
| `priority` | 10 |
| `active` | true |
| `coverageLocationIds` | Bangladesh / Dhaka Division / Dhaka / Dhanmondi, and Dhaka District if selectable |

Expected result:

- Zone saves.
- Zone appears in `/admin/shipping-zones/list`.
- Zone can be selected in carrier rate form.

### UAT-03C: Create Carrier

Route: `/admin/carriers/new`

Form action: `/admin/carriers/save`

| Field | Value |
|---|---|
| `name` | REDX Dhaka Metro |
| `code` | REDX-DHK |
| `apiKey` | sandbox-redx-key-uat |
| `mode` | THIRD_PARTY |
| `configJson` | `{"sandbox":true,"service":"dhaka-metro"}` |
| `requiresApi` | false for manual UAT, true only if sandbox API is connected |
| `trackable` | true |
| `supportsCod` | true |
| `settlementMode` | MARKETPLACE_MANAGED |
| `shippingChargeOwner` | MARKETPLACE |
| `codCollectionMode` | CARRIER_COLLECTS_FOR_MARKETPLACE |
| `active` | true |

Expected result:

- Carrier appears in carrier list.
- Carrier is available in carrier rate and shipment forms.

### UAT-03D: Create Carrier Rate

Route: `/admin/carrier-rates/create`

Form action: `/admin/carrier-rates/save`

| Field | Value |
|---|---|
| `carrier` | REDX Dhaka Metro |
| `speed` | Same day or fastest available enum |
| `deliveryType` | HOME_DELIVERY or closest available enum |
| `zone` | Dhaka Metro Same Day |
| `district` | Leave blank if zone selected; otherwise select Dhaka/Dhanmondi |
| `basePrice` | 80.00 |
| `baseWeight` | 1.00 |
| `additionalWeightUnit` | 1.00 |
| `perKg` | 20.00 |
| `codFee` | 15.00 |
| `codAvailable` | true |
| `estimatedMinDays` | 1 |
| `estimatedMaxDays` | 1 |

Expected result:

- Rate saves and appears in carrier rate list.
- Checkout quote can use this carrier for Dhaka delivery.

### UAT-03E: Create Packaging Rates

Route: `/packagingrates/new`

Form action: `/packagingrates/save`

| Field | Standard Box Value | Premium Fragile Box Value |
|---|---|---|
| Name / title field | Standard Box | Premium Fragile Box |
| Packaging type | STANDARD or closest option | FRAGILE or closest option |
| Amount / rate | 30.00 | 80.00 |
| Active | true | true |

Expected result:

- Packaging choices appear in cart where packaging is enabled.
- Changing packaging updates cart totals.

### UAT-03F: Shipping Profile

Route: `/admin/shipping-profiles/create`

Form action: `/admin/shipping-profiles/save`

Use:

| Field | Value |
|---|---|
| Vendor | Dhaka Gadget Hub |
| Profile type | Vendor physical product profile |
| Allowed carriers | REDX Dhaka Metro |
| Coverage | DHAKA-METRO or Dhaka location |
| Active | true |

Expected result:

- Dhaka Gadget Hub physical products receive valid Dhaka shipping options.
- Chattogram Fashion House must use its own profile or default profile depending on business configuration.

## UAT-04: Product Catalog And Inventory Setup

### UAT-04A: Product Category, Manufacturer, Unit

Create or verify:

| Master Data | Route | Field Data |
|---|---|---|
| Category | `/productcategory/create` | Electronics, Mobile, Accessories, Fashion, Men, Women, Digital Goods |
| Manufacturer | `/manufacturer/save` | Samsung, Generic Accessories, Local Fashion |
| UOM | `/uom/save` | Piece, Box |
| Catalog attributes | `/catalog-attributes` | RAM, Storage, Color, Size |

Expected result:

- Category and attributes are selectable in admin/vendor product forms.
- Dynamic specification fields load after category selection where configured.

### UAT-04B: Vendor Product Form

Login as UAT-V01.

Route: `/productvendor/create`

Form action: `/productvendor/save`

Use this table for Galaxy A55:

| Field | Value |
|---|---|
| `sku` | 1001001 |
| `productcategory` | Electronics > Mobile |
| `manufacturer` | Samsung |
| `title` | Galaxy A55 5G 8/256 |
| `productType` | Physical |
| `orderno` | 10 |
| `uom` | Piece |
| Sales price field | 52500.00 |
| Purchase price field | 48000.00 |
| Discount fields | Marketplace discount 0.00; vendor discount 0.00 unless promo is tested |
| Tax / VAT fields | 0.00 unless configured |
| Commission field | Auto-populated from applicable rate or set 8.00 for Electronics |
| Short description | Samsung Galaxy A55 5G smartphone with 8GB RAM and 256GB storage. |
| Long description | Official warranty-backed smartphone for Dhaka metro delivery. |
| Image upload `pic` or image field | galaxy-a55-uat.jpg |
| `status` | Active or published equivalent |
| `onlineShow` | true |
| `featuredProduct` | true |
| `newProduct` | true |
| `availabilityMode` | STOCK_MANAGED |
| `manageProductVariants` | false |
| `emiavailable` | true |
| `preorderAvailableFrom` | blank |
| Digital delivery fields | blank for physical product |

Expected result:

- Product saves.
- Details page opens.
- Product is owned by Dhaka Gadget Hub.
- Storefront can show product when active and online.

Repeat for:

| Field Group | Type-C Charger Value |
|---|---|
| `sku` | 1001002 |
| `productcategory` | Electronics > Accessories |
| `manufacturer` | Generic Accessories |
| `title` | Type-C Fast Charger 25W |
| `productType` | Physical |
| `uom` | Piece |
| Sales price | 1450.00 |
| Purchase price | 950.00 |
| `onlineShow` | true |
| `availabilityMode` | STOCK_MANAGED |
| `emiavailable` | false |

Login as UAT-V03 and create:

| Field Group | Cotton Panjabi Value |
|---|---|
| `sku` | 2002001 |
| `productcategory` | Fashion > Men |
| `manufacturer` | Local Fashion |
| `title` | Cotton Panjabi Blue XL |
| `productType` | Physical |
| Sales price | 2200.00 |
| Purchase price | 1400.00 |
| `manageProductVariants` | true |
| Attributes | Color Blue, Size XL |

### UAT-04C: Product Details Follow-Up Forms

After product save, open `/productvendor/details/{id}`.

| Form / Action | Field Data | Expected Result |
|---|---|---|
| Specifications | RAM `8GB`, Storage `256GB`, Color `Awesome Navy`, Warranty `12 months` | Specification table updates. |
| Product image upload | `product` hidden ID, `productimg` galaxy-a55-front.jpg | Additional image appears and invalid file types are rejected. |
| Catalog variant | Color Blue, Size XL, SKU `2002001-BLUE-XL`, price 2200.00, stock 40 | Variant appears and customer must select variant. |
| Dimension | Length 16.1 cm, width 7.7 cm, height 0.82 cm, weight 0.45 kg | Shipping weight and physical dimension appear in details. |
| Warranty | Warranty type manufacturer, duration 12 months, note official warranty card required | Warranty appears in product details. |

### UAT-04D: Stock Receive And Low Stock

Routes:

- Admin: `/admin/stock/receive`, `/admin/stock/adjust`, `/admin/stock/current`
- Vendor: `/vendor/stock/receive`, `/vendor/stock/adjust`, `/vendor/stock/current`, `/vendor/stock/low-stock-alerts`

| Product | Receive Qty | Adjustment | Expected Stock |
|---|---:|---:|---:|
| Galaxy A55 | 25 | 0 | 25 |
| Type-C Charger | 100 | -2 damaged | 98 |
| Cotton Panjabi | 40 | 0 | 40 |
| Jamdani Sharee | 12 | 0 | 12 |

Expected result:

- Stock transactions record receive and adjustment.
- Low-stock alert appears only when threshold is crossed.
- Vendor sees own products only.

## UAT-05: Customer Registration, Profile, Referral, And Mobile Verification

### UAT-05A: Customer Registration

Route: `/customer_registration/registration`

Submit to `/customer_registration/customer_registration_save`.

| Field | UAT-C01 Value |
|---|---|
| First name | Tanvir |
| Last name | Ahmed |
| Email | tanvir.customer@testmail.test |
| Mobile | +8801711000001 |
| Password | ChangeMe@12345 |
| Confirm password | ChangeMe@12345 |
| Address | House 12, Road 8, Dhanmondi, Dhaka |

Expected result:

- Active customer user is created.
- Customer can log in.
- Referral/customer reward profile is created or visible.

Repeat for UAT-C02 using referral from UAT-C01 where the UI supports referral code.

### UAT-05B: Customer Profile And Billing Address

Routes:

- `/customer-profile`
- `/customer-profile/update`
- `/customer-profile/update-billing`
- `/customer-billingaddress`

| Field | Value |
|---|---|
| Name | Tanvir Ahmed |
| Mobile | +8801711000001 |
| Billing recipient | Tanvir Ahmed |
| Address line 1 | House 12, Road 8 |
| Address line 2 | Dhanmondi |
| District | Dhaka |
| Post code | 1209 |

Expected result:

- Updated profile and billing address are visible in account and checkout.
- UAT-C02 cannot see or edit UAT-C01 data.

### UAT-05C: Customer COD Mobile Verification

Routes:

- `/checkout/customer/mobile/status`
- `/checkout/customer/mobile/send-otp`
- `/checkout/customer/mobile/verify-otp`

| Field | Value |
|---|---|
| Mobile | +8801711000001 |
| OTP | Use test OTP from configured provider/log/sandbox |

Expected result:

- OTP is sent and verified.
- COD eligibility recognizes the verified mobile.
- Changed mobile requires new verification.

## UAT-06: Storefront, Wishlist, Cart, Coupon, Shipping, And Checkout

### UAT-06A: Public Storefront Product Discovery

| Step | Route | Action | Expected Result |
|---|---|---|---|
| 06.1 | `/public/product` | Search `Galaxy`. | Galaxy A55 appears. |
| 06.2 | `/public/product-by-category/{prodcatid}` | Open Electronics > Mobile. | Mobile products appear. |
| 06.3 | Product details route from UI | Open Galaxy A55. | Price, image, description, warranty, delivery eligibility are visible. |
| 06.4 | `/robots.txt`, `/sitemap.xml`, `/llms.txt` | Open SEO discovery files. | Public discovery files render without protected data. |

### UAT-06B: Wishlist

Login as UAT-C01.

| Step | Route | Action | Expected Result |
|---|---|---|---|
| 06.5 | Product list/detail | Add Galaxy A55 to wishlist. | Wishlist count increases. |
| 06.6 | `/wishlist` | Open wishlist. | Galaxy A55 appears. |
| 06.7 | `/wishlist/remove` or UI remove action | Remove Galaxy A55. | Wishlist count decreases. |

### UAT-06C: Add To Cart

Route/action: `/cart/add`

| Field | Galaxy A55 | Type-C Charger | Cotton Panjabi |
|---|---:|---:|---:|
| `product_uuid` or `product_id` | Galaxy product ID/UUID | Charger product ID/UUID | Panjabi product ID/UUID |
| `catalogVariantUuid` | blank | blank | Blue XL variant UUID |
| `quantity` | 1 | 2 | 1 |

Expected result:

- `/cart/index` groups items by vendor.
- Dhaka Gadget Hub group contains Galaxy and charger.
- Chattogram Fashion House group contains Panjabi.
- Total quantity and amounts are correct.

### UAT-06D: Cart Quantity, Shipping, Packaging, Incentive Preview

Routes/actions:

- `/carts/updateQuantity`
- `/carts/updateShippingOption`
- `/carts/updatePackagingRate`
- `/cart/checkout/incentives/preview`

| Action | Field Data | Expected Result |
|---|---|---|
| Update Galaxy quantity | `productId` or `productUuid`, `quantity` 1 | Cart remains valid. |
| Invalid high quantity | Galaxy quantity 9999 | Rejected or capped based on stock rules. |
| Select shipping | `vendorId` or `vendorUuid`, `shippingOptionCode` REDX-DHK option | Shipping charge appears for vendor group. |
| Select packaging | `vendorId` or `vendorUuid`, packaging rate Standard Box | Packaging charge appears. |
| Coupon preview | `couponCode` EID10, `giftCardCode` blank, `giftCardAmount` blank, `rewardPointsToUse` blank | Discount preview respects cap and eligibility. |
| Gift card preview | `giftCardCode` GC-UAT-1000, `giftCardAmount` 500.00 | Gift card deduction appears if gift card exists. |

### UAT-06E: Checkout Address And Order Placement

Routes:

- `/cart/checkout`
- `/cart_address/add_billing_address`
- `/cart_address/add_shipping_address`
- `/cart_address/guest_delivery_address`
- order placement route visible from checkout submit

Billing/shipping form data:

| Field | Value |
|---|---|
| `recipientName` | Tanvir Ahmed |
| `addressLineOne` | House 12, Road 8 |
| `addressLinetwo` | Dhanmondi |
| `postCode` | 1209 |
| District / location field | Dhaka / Dhanmondi |
| `sameAddress` | true when billing and shipping are same |

Expected result:

- Checkout shows vendor subtotals, shipping, packaging, discount, and grand total.
- Payment method allows COD only after mobile verification and fraud eligibility.
- Order placement creates separate vendor-scoped order records for Dhaka Gadget Hub and Chattogram Fashion House.
- Record generated order codes.

## UAT-07: Order Operations

### UAT-07A: Customer Order Review

Route: `/customerorder`

| Step | Action | Expected Result |
|---|---|---|
| 07.1 | Open order list. | Only UAT-C01 orders appear. |
| 07.2 | Open details. | Items, vendor, totals, payment plan, shipping and status are visible. |
| 07.3 | Attempt early cancellation on a permitted status. | Cancellation succeeds only for eligible status. |
| 07.4 | Attempt return before delivery. | Return is blocked until business rules allow it. |

### UAT-07B: Vendor Order Management

Login as UAT-V01.

Route: `/vendor-order`

| Field / Action | Value |
|---|---|
| Open order details | Use Dhaka Gadget Hub order code |
| Status change | NEW_ORDER to CONFIRMED to PROCESSING to PACKED |
| Add charges if needed | Delivery/packing adjustment 0.00 or configured test amount |
| Generate PDF | `/vendor-order/orders/{id}/pdf` |

Expected result:

- Vendor sees only own order items.
- Vendor cannot open Chattogram Fashion House order.
- PDF includes order barcode/QR and correct vendor/customer details.

### UAT-07C: Admin Order Management

Login as UAT-A01.

Routes:

- `/admin-customer/orderlist`
- `/admin-customer/order-details/{oid}`
- `/admin-customer/statuschange`
- `/admin-customer/orders/{id}/pdf`

Expected result:

- Admin can see all orders.
- Status changes update customer and vendor views.
- Audit evidence is captured for status changes.

## UAT-08: Shipment, COD Collection, Documents, And Fulfillment

### UAT-08A: Vendor Shipment Form

Login as UAT-V01.

Route: `/vendor/shipments/new?orderId={orderId}`

Form action: `/vendor/shipments`

| Field | Value |
|---|---|
| `salesOrderId` | Dhaka Gadget Hub packed order |
| `vendorId` | Hidden active vendor |
| `district` | Auto-filled Dhaka |
| `carrier` | REDX Dhaka Metro |
| `deliveryPerson` | Hasan Courier if created; otherwise blank |
| `pickupAddress` | Dhaka Gadget Hub default pickup address |
| `trackingNumber` | REDX-UAT-0001 |
| `status` | PENDING first, then SHIPPED / IN_TRANSIT / DELIVERED |
| `deliveryType` | HOME_DELIVERY or available enum |
| `speed` | SAME_DAY or available enum |
| `labelUrl` | https://carrier-sandbox.test/label/REDX-UAT-0001 |
| `shippingCost` | Auto-filled from order, expected 80.00 plus slabs if any |
| `totalOrderAmount` | Auto-filled from order |
| `cod` | Auto-detected true for COD order |
| `codCollected` | 0.00 before collection |
| `codPending` | Order COD due |
| `settlementMode` | MARKETPLACE_MANAGED |
| `shippingChargeOwner` | MARKETPLACE |
| `codCollectionMode` | CARRIER_COLLECTS_FOR_MARKETPLACE |
| `productNetAmount` | Product subtotal for this vendor |
| `marketplaceCommissionAmount` | Product subtotal x configured commission |
| `codFeeAmount` | 15.00 |
| `shippingPaidToMarketplace` | true if customer paid shipping to marketplace |
| `vendorPayableAmount` | Auto-calculated |
| `marketplacePayableAmount` | Auto-calculated |
| `metadataJson` | `{"note":"Fragile phone package","uat":"UAT-08A"}` |

Expected result:

- Shipment saves.
- Shipment appears in vendor shipment list.
- Cross-vendor access by UAT-V03 is denied.

### UAT-08B: COD Collection

Route: `/vendor/shipments/{id}/collect-cod`

| Field | Value |
|---|---|
| Amount collected | Remaining COD pending amount |
| Reference / note if present | CASH-UAT-0001 |

Expected result:

- COD payment is recorded once.
- Overcollection is rejected.
- Shipment COD pending becomes 0.00 when fully collected.
- Delivery status can move to DELIVERED according to business rules.

### UAT-08C: Shipping Documents

Admin routes:

- `/admin/shipping-documents/labels`
- `/admin/shipping-documents/manifests`
- `/admin/shipping-documents/invoices`

Vendor routes:

- `/vendor/shipping-documents/labels`
- `/vendor/shipping-documents/manifests`
- `/vendor/shipping-documents/invoices`

| Document | Field Data | Expected Result |
|---|---|---|
| Label | Shipment REDX-UAT-0001, label number LBL-UAT-0001, label URL sandbox URL | Label record saves and can be opened/printed. |
| Manifest | Manifest number MAN-UAT-0001, carrier REDX-DHK, shipment IDs from vendor order | Manifest groups selected shipments. |
| Invoice | Shipment REDX-UAT-0001, shipping cost, COD fee, vendor payable, marketplace payable | Invoice snapshots settlement amounts. |

## UAT-09: Finance, Commission, Settlement, And Payout

### UAT-09A: Commission Settings

Route: `/admin/commission-settings/create`

Form action: `/admin/commission-settings/create`

| Field | Electronics Value | Fashion Value |
|---|---|---|
| Type | Category or available commission type | Category or available commission type |
| Category | Electronics | Fashion |
| Vendor | Optional blank for category default | Optional blank |
| Product | Optional blank | Optional blank |
| Commission rate | 8.00 | 12.00 |
| Active | true | true |

Expected result:

- Applicable-rate API returns the expected rate for products by category/vendor/product precedence.

### UAT-09B: Finance Dashboards

Routes:

- `/admin/finance/dashboard`
- `/admin/finance/settlements`
- `/vendor/finance/dashboard`
- `/vendor/finance/ledger`

Expected result:

- Delivered orders create ledger evidence.
- Vendor dashboard totals match vendor-owned orders only.
- Admin dashboard totals include all vendors.

### UAT-09C: Vendor Payout Method

Login as UAT-V01.

Route: `/vendor-payout-methods/create`

Form action: `/vendor-payout-methods/save`

| Field | Value |
|---|---|
| `preferredMethod` | BANK or available bank-transfer option |
| `accountType` | Company |
| `bankName` | Sonali Bank PLC |
| `accountTitle` | Dhaka Gadget Hub |
| `accountNumber` | 096543456789999 |

Expected result:

- Payout method saves.
- Sensitive account number is masked where displayed if masking is implemented.

### UAT-09D: Vendor Payout Request

Route: `/vendor-payout/request` or `/vendor-payout/request-payout`

Form action: `/vendor-payout/save`

| Field | Value |
|---|---|
| `amount` | 15000.00 |
| `payoutMethod` | Saved bank method |
| `status` | REQUESTED |

Expected result:

- Request saves only if available balance supports it.
- Request appears in `/vendor-payout/list`.

### UAT-09E: Admin Payout Processing

Route: `/admin/payouts/list`

Use:

| Field / Action | Value |
|---|---|
| Status | PROCESSING, then PAID |
| Reference | BNK-UAT-001 |
| Note | UAT payout release for Dhaka Gadget Hub |

Expected result:

- Vendor balance moves correctly.
- Ledger and payout status reconcile.
- Vendor can see final status.

## UAT-10: Return, Refund, Restock, And Ledger Reversal

### UAT-10A: Customer Return Request

Login as UAT-C01.

Route: `/customerorder/details/{oid}`

Action: request item return.

| Field | Value |
|---|---|
| Item | Galaxy A55 |
| Quantity | 1 |
| Reason | Device seal broken on delivery |
| Note | UAT return test |

Expected result:

- Return request is recorded for physical item.
- Virtual item return is rejected.

### UAT-10B: Vendor Or Admin Return Processing

Routes:

- `/vendor-order/item-return`
- `/admin-customer/item-return`

Expected result:

- Returned item status updates.
- Stock is restored according to business rule.
- Refund is created if paid amount exceeds recalculated total.
- Vendor earning reversal appears in finance ledger.

## UAT-11: Rewards, Coupon, Gift Card, Wallet, Cashback, And Cashout

### UAT-11A: Referral And Level Rate

Routes:

- `/referral/list`
- `/lavelratesettings/create`
- `/lavelratesettings/save`

| Field | Value |
|---|---|
| Level 1 rate | 10 |
| Level 2 rate | 5 |
| Level 3 rate | 4 |

Expected result:

- Duplicate level rate is rejected.
- Order by UAT-C02 creates level commission for UAT-C01 when referral link exists.

### UAT-11B: Coupon

Route: `/coupon/create`

| Field | Value |
|---|---|
| Code | EID10 |
| Discount type | Percent |
| Discount value | 10 |
| Max discount | 500.00 |
| Minimum order | 1000.00 |
| Active | true |
| Validity | Current month |

Expected result:

- Coupon applies once at checkout.
- Discount does not exceed 500.00.

### UAT-11C: Gift Card

Route: `/giftcard/create`

| Field | Value |
|---|---|
| Code | GC-UAT-1000 |
| Amount | 1000.00 |
| Active | true |
| Expiry | Future date |

Expected result:

- Gift card can be used in checkout preview/payment flow if the module is enabled.

### UAT-11D: Customer Wallet And Cashout

Customer routes:

- `/customerwallet/wallet`
- `/customerwallet/wallet/cashout`
- `/cashoutcustomerrequest/list`

Admin route:

- `/admin/cashouts`

| Field | Value |
|---|---|
| Cashout points | 5000 |
| Payout method | MOBILE or BANK |
| Mobile account | +8801711000001 |
| Bank account if selected | Customer UAT Bank Account |

Expected result:

- Customer balance is debited once.
- Reject restores balance.
- Approve and mark paid do not double-debit.

## UAT-12: Fraud Detection, COD Risk, Blocklist, And Manual Review

Routes:

- `/admin/fraud`
- `/admin/fraud/dashboard`
- `/admin/fraud/assessments`
- `/admin/fraud/cases`
- `/admin/fraud/rules`
- `/admin/fraud/blocklist`
- `/admin/fraud/configuration`

### UAT-12A: High-Value COD Risk

| Step | Data | Expected Result |
|---|---|---|
| Create cart | Two Galaxy A55 devices, COD selected | Fraud/COD rules evaluate high value. |
| Mobile not verified | Use unverified guest/mobile | COD is blocked or assessment has high-risk reason. |
| Repeated attempts | Place repeated guest attempts from same mobile/device | Velocity/device risk increases. |
| Admin review | Open assessment and add note `UAT high-value COD review` | Review history records actor, decision, note, timestamp. |
| Blocklist | Block mobile hash or configured identifier | Future checkout is blocked or flagged. |

Expected result:

- Sensitive mobile/email/device data is masked where full value is not needed.
- Fraud event/outbox behavior is idempotent.

## UAT-13: Communication, Notification, Provider, Template, And Unsubscribe

Routes:

- `/admin/communication/templates`
- `/admin/communication/providers`
- `/admin/communication/routing-rules`
- `/admin/communication/manual/compose`
- `/vendor/communication/manual/compose`
- `/customer/communication/manual/compose`
- `/customer/notifications`
- `/vendor/notifications`
- `/admin/notifications`
- `/public/communication/unsubscribe`

### UAT-13A: Provider And Template

| Form | Field | Value |
|---|---|---|
| Provider | Name | Sandbox SMS Provider |
| Provider | Type | SMS |
| Provider | API key / secret | sandbox-secret-uat |
| Provider | Active | true |
| Template | Code | ORDER_CONFIRMED_SMS |
| Template | Channel | SMS |
| Template | Body | Your order {{orderCode}} is confirmed. Total {{grandTotal}} BDT. |
| Routing rule | Event | ORDER_CONFIRMED |
| Routing rule | Provider | Sandbox SMS Provider |

Expected result:

- Provider secret is not fully visible after save.
- Order status change triggers or queues correct message where integration is enabled.

### UAT-13B: Manual Messages

| Actor | Route | Recipient | Message |
|---|---|---|---|
| Admin | `/admin/communication/manual/compose` | UAT-C01 | Your UAT order is being reviewed by support. |
| Vendor | `/vendor/communication/manual/compose` | UAT-C01 | Your Dhaka Gadget Hub shipment is packed. |
| Customer | `/customer/communication/manual/compose` | Support/Admin | Please confirm delivery time for REDX-UAT-0001. |

Expected result:

- Inbox/sent history records message.
- Vendor cannot message outside its business scope if the form is order/vendor scoped.
- Customer unsubscribe blocks marketing communication but not transactional messages.

## UAT-14: Blog, Content, Ads, Marketing, Wishlist, And Reviews

### UAT-14A: Ads

Route: `/admin/ads/create`

| Field | Value |
|---|---|
| Title | Eid Electronics Deal |
| Placement | Home banner or available placement |
| Target URL | `/public/product` |
| Active | true |
| Schedule | Current date range |

Expected result:

- Ad appears in the configured public placement only while active.

### UAT-14B: Blog

Routes:

- `/admin/blog/new`
- `/admin/blog`
- `/public/blog/{slug}`
- `/customer/blog/write`

| Field | Value |
|---|---|
| Title | How To Choose A Warranty-Backed Smartphone |
| Slug | warranty-backed-smartphone-uat |
| Category | Buying Guide |
| Tags | mobile, warranty, dhaka |
| Status | Published after preview |
| Body | UAT article body with real buying-guide content. |

Expected result:

- Draft, preview, publish, comment, moderate, and public view work.
- Customer sees only own blog management screens.

### UAT-14C: Review

Route/action: `/customer-product-review/save`

| Field | Value |
|---|---|
| Product | Galaxy A55 |
| Rating | 5 |
| Comment | Product delivered with official warranty card and sealed box. |

Expected result:

- Review saves for eligible delivered purchase.
- Duplicate or unauthorized review is rejected according to business rule.

## UAT-15: Security And Negative Business Tests

| Test | Actor | Data | Expected Result |
|---|---|---|---|
| Admin route anonymous | Guest | Open `/admin/system` | Redirect to login or 401/403. |
| Vendor IDOR | UAT-V03 | Open UAT-V01 shipment/order IDs | Denied or not found. |
| Customer IDOR | UAT-C02 | Open UAT-C01 order detail ID | Denied or not found. |
| CSRF | Any authenticated actor | Submit protected POST without token using tool | 403. |
| Price validation | Vendor | Sales price 900, purchase price 950 | Save rejected. |
| Stock validation | Customer | Quantity 9999 | Cart/checkout rejected. |
| COD eligibility | Guest | Unverified mobile COD | COD blocked. |
| Payout overdraw | Vendor | Request amount greater than available | Request rejected. |
| File upload | Vendor/Admin | Upload `.exe` renamed as `.jpg` | Rejected. |
| Payment callback replay | Sandbox/tool | Repeat provider callback | No duplicate payment. |

## UAT Sign-Off Sheet

| Area | Business Owner | QA Owner | Status | Evidence Link | Sign-Off Date |
|---|---|---|---|---|---|
| Environment smoke |  |  | Not Tested |  |  |
| IAM and permissions |  |  | Not Tested |  |  |
| Vendor profile and staff |  |  | Not Tested |  |  |
| Shipping setup |  |  | Not Tested |  |  |
| Product and stock |  |  | Not Tested |  |  |
| Customer and checkout |  |  | Not Tested |  |  |
| Order operations |  |  | Not Tested |  |  |
| Shipment and COD |  |  | Not Tested |  |  |
| Finance and payout |  |  | Not Tested |  |  |
| Return and refund |  |  | Not Tested |  |  |
| Rewards and promotions |  |  | Not Tested |  |  |
| Fraud |  |  | Not Tested |  |  |
| Communication |  |  | Not Tested |  |  |
| Content and marketing |  |  | Not Tested |  |  |
| Security negative tests |  |  | Not Tested |  |  |

## Known Execution Boundaries

- Exact enum labels can differ by deployment data and localized UI labels; use the closest visible option and record it in evidence.
- Payment, SMS, email, and carrier provider behavior must use sandbox or mock providers unless production credentials are explicitly approved.
- Some generated IDs, UUIDs, order codes, shipment codes, and OTP values are environment-specific.
- This guide covers the full real E2E business path; the security inventory remains the authoritative source for all individual route rows.
