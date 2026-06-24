# Test Flow

Use this file to record manual test cases and results for each module.

## Test Result Legend
- `Pass`
- `Fail`
- `Partial`
- `Not Tested`

## Wishlist

### Test Case 1
- URL: `/public/product`
- Steps:
  1. Login as a customer.
  2. Open product list.
  3. Click `Add to Wishlist`.
  4. Open `/wishlist/index`.
- Expected Result:
  - Product appears in wishlist.
  - Header wishlist count increases.
- Actual Result:
  - Pending confirmation
- Status:
  - Partial

### Test Case 2
- URL: `/wishlist/index`
- Steps:
  1. Login as a customer.
  2. Open wishlist page.
  3. Click `Remove`.
- Expected Result:
  - Product is removed.
  - Header wishlist count decreases.
- Actual Result:
  - Pending confirmation
- Status:
  - Partial

### Test Case 3
- URL: `/public/single-product/{id}`
- Steps:
  1. Login as a customer.
  2. Open a product details page.
  3. Add to wishlist.
  4. Click again to remove from wishlist.
- Expected Result:
  - Button text toggles between add and remove.
  - Flash message appears.
- Actual Result:
  - Pending confirmation
- Status:
  - Partial

## Settings

### Test Case 1
- URL: `/admin/settings`
- Steps:
  1. Login as admin.
  2. Update text fields.
  3. Upload logo, favicon, and og image.
  4. Save settings.
- Expected Result:
  - Settings save successfully.
  - Images are resized and previewed.
- Actual Result:
  - Java compile verified, browser flow still needs manual confirmation
- Status:
  - Partial

## Shipping And Fulfillment

### Test Case 1: Shipping Zone CRUD
- URL: `/admin/shipping-zones/create`
- Steps:
  1. Login as admin.
  2. Open `/admin/shipping-zones/create`.
  3. Create a zone named `Inside Dhaka`.
  4. Select `DHAKA` district.
  5. Save and open `/admin/shipping-zones/list`.
- Expected Result:
  - Zone saves successfully.
  - Zone appears in the list with selected district.
- Actual Result:
  - Pending browser/database verification.
- Status:
  - Not Tested

### Test Case 2: Zone-Based Carrier Rate
- URL: `/admin/carrier-rates/create`
- Steps:
  1. Login as admin.
  2. Create or select an active carrier.
  3. Open carrier rate create page.
  4. Select the `Inside Dhaka` zone.
  5. Leave direct districts empty.
  6. Fill base price, base weight, extra unit, COD fee, speed, delivery type, and ETA.
  7. Save.
- Expected Result:
  - Carrier rate saves successfully using the zone.
  - Carrier rate list shows the selected zone.
- Actual Result:
  - Pending browser/database verification.
- Status:
  - Not Tested

### Test Case 3: Carrier Rate Slabs
- URL: `/admin/carrier-rate-slabs/list`
- Steps:
  1. Login as admin.
  2. Open carrier rate list.
  3. Click `Slabs` for a carrier rate.
  4. Add slabs for `0-1kg`, `1-2kg`, and `2-5kg`.
  5. Save each slab.
- Expected Result:
  - Slabs save successfully.
  - Slabs appear in priority and minimum weight order.
  - Checkout calculation uses slab price when an active matching slab exists.
- Actual Result:
  - Pending browser/database verification.
- Status:
  - Not Tested

### Test Case 4: Legacy District Rate Still Works
- URL: `/cart/index`
- Steps:
  1. Login as customer.
  2. Select a shipping district that has an old direct district carrier rate.
  3. Add a physical product to cart.
  4. Open cart.
- Expected Result:
  - Shipping option still appears from old district-based rate.
  - No zone is required for old rate.
- Actual Result:
  - Pending browser/database verification.
- Status:
  - Not Tested

### Test Case 5: Shipment Tracking Timeline
- URL: `/admin/shipments/list`
- Steps:
  1. Login as admin.
  2. Create a shipment for a packed physical order.
  3. Change status from `PENDING` to `SHIPPED`.
  4. Open shipment list and edit form.
- Expected Result:
  - Shipment status saves.
  - Tracking event is recorded for creation/status change.
  - Vendor shipment list only shows the active vendor shipment timeline.
- Actual Result:
  - Pending browser/database verification.
- Status:
  - Not Tested

### Test Case 6: Pickup Address Default
- URL: `/admin/pickup-addresses/create`
- Steps:
  1. Login as admin.
  2. Create two pickup addresses for one vendor.
  3. Mark the second address as default.
  4. Create a vendor shipment.
- Expected Result:
  - Only one pickup address remains default for that vendor.
  - Shipment form can select the vendor pickup address.
  - Vendor shipment form preselects the default pickup address.
- Actual Result:
  - Pending browser/database verification.
- Status:
  - Not Tested

### Test Case 7: Shipping Rule Quote Engine
- URL: `/admin/shipping-rules/create`
- Steps:
  1. Login as admin.
  2. Create a rule that disables one carrier above `10kg`.
  3. Add a cart above `10kg`.
  4. Create a rule that adds an extra fee for a selected district.
  5. Open cart shipping options.
- Expected Result:
  - Disabled carrier does not appear.
  - Extra fee is added to matching shipping option.
  - Existing carrier-rate options still appear when no rule matches.
- Actual Result:
  - Pending browser/database verification.
- Status:
  - Not Tested

### Test Case 8: Shipping Documents
- URL: `/admin/shipping-documents/labels`, `/vendor/shipping-documents/labels`
- Steps:
  1. Login as admin.
  2. Create a label for a shipment.
  3. Create a manifest and attach two shipments.
  4. Create an invoice for a shipment.
  5. Login as vendor.
  6. Open `/vendor/shipping-documents/labels` and generate a label for one vendor shipment.
  7. Open `/vendor/shipping-documents/manifests/create`, select multiple vendor shipments, and save a manifest.
  8. Open `/vendor/shipping-documents/invoices` and generate an invoice for one vendor shipment.
- Expected Result:
  - Label saves with provided or generated label number.
  - Manifest links selected shipments.
  - Invoice snapshots shipment shipping cost, COD fee, vendor payable, and marketplace payable.
  - Vendor document pages show only active-vendor shipments and documents.
  - Vendor can open or print the generated label URL.
  - Vendor manifest records the selected carrier batch and handover status.
- Actual Result:
  - Pending browser/database verification.
- Status:
  - Not Tested

### Test Case 9: Shipment Security Hardening
- URL: `/vendor/shipments`
- Steps:
  1. Login as one vendor.
  2. Try to collect COD for a shipment that belongs to another vendor by changing the shipment ID in the form action.
  3. Delete a shipment using the visible delete button.
- Expected Result:
  - Cross-vendor COD collection is rejected.
  - Shipment delete uses POST form submission.
  - Delete still requires active vendor ownership.
- Actual Result:
  - Pending browser/database verification.
- Status:
  - Not Tested

### Test Case 10: International Location Hierarchy
- URL: `/admin/shipping-locations/create`
- Steps:
  1. Login as admin.
  2. Create country `Bangladesh` with code `BD`.
  3. Create division `Dhaka Division` with parent `Bangladesh`.
  4. Create district `Dhaka` with parent `Dhaka Division` and legacy district `DHAKA`.
  5. Create thana `Dhanmondi` with parent `Dhaka`.
- Expected Result:
  - All locations save successfully.
  - Parent hierarchy appears in location list and zone selection.
  - District row is mapped to the old checkout district for migration compatibility.
- Actual Result:
  - Pending browser/database verification.
- Status:
  - Not Tested

### Test Case 11: Zone Coverage By Location Hierarchy
- URL: `/admin/shipping-zones/create`
- Steps:
  1. Create a zone named `Dhaka Division Coverage`.
  2. Select coverage location `Dhaka Division`.
  3. Create a carrier rate using this zone.
  4. Select `Dhaka` as checkout shipping district.
- Expected Result:
  - Checkout shipping quote matches through the location hierarchy.
  - Legacy direct district setup is not required for new zone coverage.
- Actual Result:
  - Pending browser/database verification.
- Status:
  - Not Tested

## New Test Case Template

### Test Case X
- URL:
- Steps:
  1.
  2.
  3.
- Expected Result:
  -
- Actual Result:
  -
- Status:
  - Not Tested
