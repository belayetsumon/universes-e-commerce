# Mobile-Only Guest Checkout OTP Workflow

## Goal

Allow anonymous customers to place orders after providing a Bangladesh mobile number. OTP verification is controlled from Global Settings. The flow must use the existing `Users` entity and `usermodule_users` table; no separate guest-customer table is introduced.

## Global Settings

The Store settings section controls:

- `allowGuestCheckout`
- `guestMobileRequired`
- `guestMobileOtpVerificationEnabled`
- `guestOtpExpiryMinutes`
- `guestOtpMaximumAttempts`
- `guestOtpResendCooldownSeconds`
- `guestOtpDailySendLimit`
- `guestAutoCreateCustomerAccount`

## Implemented Flow

```text
Cart
-> Continue as Guest
-> Enter Mobile Number
-> Send OTP
-> Verify OTP
-> Find or Create Users record
-> Enter Delivery Details
-> Select Payment Method
-> Place Order
-> Order Confirmation
```

## Active Code Anchors

- `main/java/com/ecommerce/app/module/checkout/guest/services/MobileNumberNormalizationService.java`
- `main/java/com/ecommerce/app/module/checkout/guest/services/GuestCheckoutOtpService.java`
- `main/java/com/ecommerce/app/module/checkout/guest/services/GuestCheckoutUserResolver.java`
- `main/java/com/ecommerce/app/module/checkout/guest/session/GuestCheckoutSession.java`
- `main/java/com/ecommerce/app/module/checkout/guest/controller/GuestCheckoutMobileController.java`
- `main/java/com/ecommerce/app/order/controller/SalesOrderController.java`
- `main/java/com/ecommerce/app/module/cart/controller/CartAddressController.java`
- `main/resources/templates/cart/checkout.html`
- `main/resources/templates/order/order/create.html`
- `main/resources/db/guest_checkout_mobile_otp_init.sql`

## Security Rules

- Guest checkout is blocked unless `allowGuestCheckout` is enabled.
- Email is not shown or accepted in the guest checkout form.
- Mobile numbers are normalized to `8801XXXXXXXXX`.
- When `guestMobileOtpVerificationEnabled = true`, OTP is six digits, hashed at rest, expires according to settings, and uses the configured verification-attempt limit.
- Resend cooldown and daily mobile/session send limits come from Global Settings.
- Repeated sends are rate-limited by mobile, IP hash, device-fingerprint hash, and checkout session id.
- The order controller resolves the customer and verification state from the server-side `GuestCheckoutSession`; the browser cannot submit or replace `userId` or verification state.
- If OTP is required, order placement is blocked unless `mobileVerificationStatus = VERIFIED`.
- If OTP is disabled, the session is stored with `mobileVerificationStatus = UNVERIFIED` and order placement is allowed.
- OTP is marked `USED` after successful OTP-backed order placement and the guest checkout session is cleared.

## User Resolution

After mobile submission:

1. Search `usermodule_users.mobile` using the normalized mobile number.
2. If found, reuse the existing account.
3. If OTP is enabled and verified, mark `mobileVerified = true` when needed.
4. If OTP is disabled, do not mark a newly created account as mobile verified.
5. If not found and auto-create is enabled, create a `Users` row with:
   - `mobile = 8801XXXXXXXXX`
   - `status = Active`
   - `userType = customer`
   - `registrationSource = GUEST_CHECKOUT`
   - `guestAccount = true`
   - `mobileVerified = true` only after OTP verification
   - `emailVerified = false`
   - `passwordConfigured = false`
   - an unusable generated password hash

## Address Rule

Guest checkout captures only delivery details:

- Recipient name
- Detailed street address
- House / road / block / floor
- Optional post code

District and thana come from the existing `shippingLocation` session and are displayed read-only. Billing is copied from shipping automatically.

## Remaining Hardening

- Add automated OTP expiration/replay/concurrency tests.
- Add admin filters for guest-created customers and OTP-blocked attempts.
- Extend `MobileNumberNormalizationService` into all registration/login/account lookup controllers.
- Add idempotency-key enforcement to final order submit if the existing order service is refactored.
- Add deeper configurable fraud scoring and COD restrictions.
