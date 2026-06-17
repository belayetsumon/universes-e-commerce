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

