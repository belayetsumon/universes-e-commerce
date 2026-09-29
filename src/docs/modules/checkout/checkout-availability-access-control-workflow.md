# Checkout Availability and Access Control

## Global Settings

Checkout access is controlled by two independent store settings:

- `secureCheckoutEnabled`: requires authentication before checkout when enabled.
- `allowGuestCheckout`: allows mobile-based guest checkout when enabled.

## Availability Matrix

| Secure Checkout | Guest Checkout | Result |
| --- | --- | --- |
| Enabled | Enabled | Cart button opens login / continue-as-guest modal. |
| Enabled | Disabled | Cart button opens login-only modal. |
| Disabled | Enabled | Cart button opens guest checkout directly. |
| Disabled | Disabled | Checkout is unavailable and order placement is blocked. |

## Server Guard

`CheckoutAvailabilityService` is the single source of truth for the storefront checkout matrix. It is used before:

- opening `/cart/checkout`
- opening `/order/create`
- order submit endpoints
- checkout incentive preview
- shipping and packaging selections
- guest mobile checkout session creation
- guest delivery address submission

## Customer Message

When checkout is fully disabled, customers see:

`Checkout is currently unavailable. Purchasing has been temporarily disabled by the store administrator. Please try again later or contact customer support for assistance.`

The cart is preserved; no incomplete order or checkout session should be created.

## Admin Warning

The Global Settings store form shows a confirmation dialog before saving both Secure Checkout and Guest Checkout as disabled.
