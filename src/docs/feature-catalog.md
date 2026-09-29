# Feature Catalog

Generated from the current codebase on 2026-09-29.

## Source Basis

This catalog is derived from:

- Java package structure under `main/java/com/ecommerce/app`.
- Spring MVC controllers and representative routes.
- Thymeleaf template folders under `main/resources/templates`.
- Existing operational documents, especially `docs/functional-operations-guide.md`, `docs/module-status.md`, and module workflow guides.

It is a feature inventory, not runtime proof. Compile, startup, browser, and permission proof should be captured separately when a feature is changed or released.

## High-Level Module Map

| Module | Sub-Modules | Primary Actors | Representative Source |
|---|---|---|---|
| Public Storefront | Home, catalogue browsing, SEO discovery, blog, social share tracking | Guest, customer | `publics`, `module/blog`, `module/marketing` |
| Identity and Access | Users, roles, privileges, password recovery, sessions, permission catalogue | Admin, staff | `module/user`, `security`, `module/system` |
| Customer Account | Registration, profile, billing address, orders, payments, EMI, wallet/rewards | Customer | `module/customer`, `module/checkout/customer` |
| Product Catalog | Products, categories, manufacturers, attributes, variants, images, dimensions, warranty, stock | Admin, vendor, customer | `product`, `vendor/controller/VendorProductController.java` |
| Cart and Checkout | Cart, grouped vendor checkout, address capture, charge validation, availability, mobile OTP | Customer, guest | `module/cart`, `module/checkout` |
| Orders and Sales | Order placement, order operations, returns, refunds, PDFs, dashboards | Admin, vendor, customer | `module/order`, `admincustomer`, `vendor/controller/VendorSalesOrderController.java`, `module/sales` |
| Vendor Portal | Vendor profile, staff, roles, products, stock, orders, shipments, finance, verification | Vendor, vendor staff | `vendor`, `vendor/user` |
| Shipping and Fulfillment | Carriers, rates, zones, profiles, rules, pickup addresses, shipments, documents, COD | Admin, vendor | `module/shipping`, `vendor/controller/Vendor_ShipmentController.java` |
| Finance and Commission | Vendor ledger, settlement, payouts, marketplace commission settings | Admin, vendor | `adminvendor`, `vendor/services`, `commission` |
| Rewards and Promotions | Referrals, level commission, wallets, coupons, cashback, gift cards, cashout | Admin, customer | `module/ReferralRewards`, `module/customer/ReferralRewards` |
| Fraud Detection | Assessments, rules, scoring, cases, blocklist, COD controls, vendor risk, events | Admin, system | `module/fraud` |
| Communication | Templates, providers, routing, manual messages, notifications, unsubscribe | Admin, vendor, customer | `module/communication` |
| Content and Marketing | Ads, blog, pages/content, subscribers, share analytics | Admin, customer, public | `module/ads`, `module/blog`, root repositories/controllers |
| Settings and System Tools | Global settings, image settings, seed tools, endpoint/security inventory | Admin | `module/settings`, `module/system`, `docs/security` |
| Wishlist and Reviews | Wishlist, product reviews/comments | Customer | `module/wishlist`, `module/review`, product comments |

## Module Feature Catalog

### 1. Public Storefront

| Sub-Module | Features | Representative Routes / Sources |
|---|---|---|
| Home and public pages | Landing/home, maintenance page, about, member login, front registration, forgot password | `/index`, `/index.html`, `/maintenance`, `/public/about-us`, `/public/member-login`, `/public/front-registration` |
| Product browsing | Public product list, product by category, product details, search suggestions | `/public/product`, `/public/product-by-category/{prodcatid}`, `/public/search/suggestions` |
| SEO discovery | Robots file, sitemap, LLM discovery text, page metadata service | `/robots.txt`, `/sitemap.xml`, `/llms.txt`, `PublicSeoService` |
| Public blog | Blog search, category/tag pages, article detail, public comments, subscription | `/public/blog/search`, `/public/blog/category/{slug}`, `/public/blog/tag/{slug}`, `/public/blog/{slug}`, `/public/blog/{slug}/comments`, `/public/blog/subscribe` |
| Social share tracking | Public share-event tracking and admin reporting | `/public/share-track`, `/admin/marketing/share-analytics` |

### 2. Identity and Access

| Sub-Module | Features | Representative Routes / Sources |
|---|---|---|
| User administration | User list, filter by status, registration, edit, delete, profile, login history | `/users`, `/users/userbystatus`, `/users/registrations`, `/users/edit/{id}`, `/users/login-history` |
| Password management | Admin/user password change, forgot password, reset flow | `/users/change-password`, `/changepassword/update`, `/forgotpassword/showemail`, `/forgotpassword/reset` |
| Role and privilege setup | Modules, roles, privileges CRUD | `/module`, `/role`, `/privilege` |
| Permission catalogue and system seeds | System endpoint page, Bangladesh location seed, permission catalogue seed | `/admin/system`, `/admin/system/endpoints`, `/admin/system/seed/bangladesh-locations`, `/admin/system/seed/permission-catalogue` |
| Security inventory | Endpoint inventory, permission catalogue, workflow ledgers | `docs/application-security-*.csv`, `docs/security/*` |

### 3. Customer Account

| Sub-Module | Features | Representative Routes / Sources |
|---|---|---|
| Customer registration | Customer signup, active customer creation, referral profile integration | `/customer_registration/registration`, `/customer_registration/customer_registration_save`, `/users/frontRegistrationSave` |
| Dashboard and profile | Customer dashboard, profile update, billing update, password change, profile image | `/customer`, `/customer-profile`, `/customer-profile/update`, `/customer-profile/update-billing`, `/customerprofileimage/save` |
| Billing and account data | Billing address dashboard and customer account forms | `/customer-billingaddress`, `CustomerBillingAddressForm`, `CustomerAccountForm` |
| Customer orders | Order list, order details, cancellation/status request, return request, order PDF | `/customerorder`, `/customerorder/details/{oid}`, `/customerorder/statuschange`, `/customerorder/request-item-return`, `/customerorder/orders/{id}/pdf` |
| Customer payments | Payment method page and follow-up order payments | `/customer_payment/payment_method`, `/customerorder/payment/{orderid}` |
| Customer EMI | EMI dashboard/detail, Meritten EMI routes | `/customeremi`, `/customer-meritten-emi`, `/customeremi/details/{planId}` |
| COD mobile verification | Customer checkout mobile OTP send, resend, verify, status | `/checkout/customer/mobile/status`, `/checkout/customer/mobile/send-otp`, `/checkout/customer/mobile/verify-otp` |

### 4. Product Catalog

| Sub-Module | Features | Representative Routes / Sources |
|---|---|---|
| Products | Admin product list/create/save/details/edit/delete, specifications update | `/product`, `/product/create`, `/product/save`, `/product/details/{id}`, `/product/details/{id}/specifications` |
| Vendor products | Vendor-owned product list/create/save/details/edit/delete, specifications update | `/productvendor`, `/productvendor/create`, `/productvendor/save`, `/productvendor/details/{id}` |
| Categories | Category CRUD and details | `/productcategory`, `/productcategory/create`, `/productcategory/details/{id}` |
| Manufacturers and UOM | Manufacturer CRUD, unit-of-measure CRUD | `/manufacturer/list`, `/manufacturer/save`, `/uom/list`, `/uom/save` |
| Dynamic catalogue attributes | Reusable attributes, category mappings, options, dynamic specification fields | `/catalog-attributes`, `/catalog-attributes/options/{attributeUuid}`, `CatalogAttributeAdminController` |
| Catalog variants | Variant add/edit/save/generate/delete, variant selection for storefront | `/catalog-variants/add/{productUuid}`, `/catalog-variants/generate/{productUuid}`, `/catalog-variants/delete/{uuid}` |
| Product media | Product and vendor image upload/list/delete | `/productimage/upload`, `/productimage/list/{id}`, `/vendor_productimage/upload` |
| Product dimensions and warranty | Dimension and warranty add/edit/delete for admin/vendor products | `/productdimension/add/{pid}`, `/warranty/add/{pid}`, `/vendor_productdimension/add/{pid}`, `/vendor_warranty/add/{pid}` |
| Stock inventory | Current stock, PDF export, stock transactions, receive and adjust actions | `/admin/stock/current`, `/admin/stock/current/pdf`, `/admin/stock/transactions`, `/admin/stock/receive`, `/admin/stock/adjust` |

### 5. Cart and Checkout

| Sub-Module | Features | Representative Routes / Sources |
|---|---|---|
| Cart page | Cart index, checkout page, availability check, incentive preview | `/cart`, `/cart/index`, `/cart/checkout`, `/cart/checkout/availability`, `/cart/checkout/incentives/preview` |
| Cart mutation | Add item, quantity update, item remove | `/cart/add`, `/cart/quantityUpdate`, `/cart/remove/{id}` |
| Cart API | Quantity update, remove item, shipping option update, packaging rate update | `/carts/api`, `/carts/updateQuantity`, `/carts/removeitem`, `/carts/updateShippingOption`, `/carts/updatePackagingRate` |
| Checkout address | Billing address, shipping address, guest delivery address | `/cart_address/add_billing_address`, `/cart_address/add_shipping_address`, `/cart_address/guest_delivery_address` |
| Guest checkout mobile OTP | Guest mobile send/resend/verify/change for checkout | `/checkout/guest/mobile/send-otp`, `/checkout/guest/mobile/resend-otp`, `/checkout/guest/mobile/verify-otp`, `/checkout/guest/mobile/change-mobile` |
| Checkout availability and validation | Checkout availability gate, address validation, charge validation | `CheckoutAvailabilityService`, `CheckoutAddressValidationService`, `CheckoutChargeValidationService` |

### 6. Orders and Sales

| Sub-Module | Features | Representative Routes / Sources |
|---|---|---|
| Order placement | Order index/create, vendor-based save/update, order placed page | `/order`, `/order/create`, `/order/savebyvendor`, `/order/savebyvendorupdate`, `/order/placed` |
| Admin order operations | All customer orders, customer-specific orders, details, status change, item return, PDF | `/admin-customer/orderlist`, `/admin-customer/order-by-customer/{cid}`, `/admin-customer/order-details/{oid}`, `/admin-customer/statuschange`, `/admin-customer/item-return`, `/admin-customer/orders/{id}/pdf` |
| Vendor order operations | Vendor order list, details, status change, returns/refunds, charges, PDF | `/vendor-order`, `/vendor-order/details/{oid}`, `/vendor-order/statuschange`, `/vendor-order/item-return`, `/vendor-order/addcharges/{oid}`, `/vendor-order/orders/{id}/pdf` |
| Sales items | Admin and vendor sales item surfaces | `/admin/sales/items`, `/vendor-order/items` |
| Sales dashboards | Admin and vendor sales dashboard plus data endpoint | `/admin/sales/dashboard`, `/admin/sales/dashboard/data`, `/vendor/sales/dashboard`, `/vendor/sales/dashboard/data` |
| Returns and refunds | Admin, vendor, customer returns/refunds pages and return processing | `/admin/returns-refunds`, `/admin/finance/returns`, `/vendor-order/returns`, `/customerorder/returns` |

### 7. Vendor Portal

| Sub-Module | Features | Representative Routes / Sources |
|---|---|---|
| Vendor dashboard | Vendor home and dashboard entry points | `/vendor/home`, `/vendor/{id}` |
| Vendor profile | Vendor profile create/save/edit/details | `/vendorprofile/create`, `/vendorprofile/save`, `/vendorprofile/edit`, `/vendorprofile/details` |
| Vendor logo and address | Vendor logo save/delete, vendor address dashboard | `/vendorlogo/save`, `/vendorlogo/delete`, `/vendoraddress` |
| Vendor verification | Email verification, mobile OTP, verification status pages | `/vendorverifications`, `/vendorverifications/emailverification`, `/vendorverifications/verify-email`, `/vendorverifications/mobile-verification-otp-send`, `/vendorverifications/verify-mobile` |
| Vendor staff | Vendor user list, add staff, save assignment, delete assignment | `/vendor-users/userlist`, `/vendor-users/add_vendor_user`, `/vendor-users/save`, `/vendor-users/delete/{id}` |
| Vendor roles | Vendor role list/add/edit/save/delete | `/vendor-users/roles`, `/vendor-users/roles/add`, `/vendor-users/roles/edit/{id}`, `/vendor-users/roles/save` |
| Vendor stock | Current stock, low-stock alerts, transactions, receive, adjust | `/vendor/stock/current`, `/vendor/stock/low-stock-alerts`, `/vendor/stock/transactions`, `/vendor/stock/receive`, `/vendor/stock/adjust` |
| Vendor delivery persons | Vendor delivery-person create/edit/delete | `/vendor/delivery-persons/new`, `/vendor/delivery-persons/edit/{id}`, `/vendor/delivery-persons/delete/{id}` |

### 8. Shipping and Fulfillment

| Sub-Module | Features | Representative Routes / Sources |
|---|---|---|
| Shipments | Admin shipment list/create/edit/save/delete, label generation | `/admin/shipments/list`, `/admin/shipments/create`, `/admin/shipments/edit/{id}`, `/admin/shipments/save`, `/admin/shipments/{id}/generate-label` |
| Vendor shipments | Vendor shipment list/new/edit/delete, COD collection, label generation | `/vendor/shipments`, `/vendor/shipments/new`, `/vendor/shipments/{id}/collect-cod`, `/vendor/shipments/{id}/generate-label` |
| Carriers | Carrier CRUD and operational attributes | `/admin/carriers/list`, `/admin/carriers/new`, `/admin/carriers/save`, `/admin/carriers/edit/{id}` |
| Carrier rates and slabs | Carrier rate CRUD, carrier rate slab CRUD | `/admin/carrier-rates/list`, `/admin/carrier-rates/create`, `/admin/carrier-rate-slabs/list`, `/admin/carrier-rate-slabs/create` |
| Shipping locations | Country/division/district/thana hierarchy management | `/admin/shipping-locations/list`, `/admin/shipping-locations/create`, `/district/select-district`, `/district/thanas` |
| Shipping zones | Zone CRUD with reusable geographic coverage | `/admin/shipping-zones/list`, `/admin/shipping-zones/create`, `/admin/shipping-zones/edit/{id}` |
| Shipping profiles | Admin profile management and vendor shipping profile surface | `/admin/shipping-profiles/list`, `/admin/shipping-profiles/create`, `/vendor/shipping-profile` |
| Shipping rules and quotes | Carrier disable, COD disable, extra fee, priority rules, quote service | `/admin/shipping-rules/list`, `/admin/shipping-rules/create`, `ShippingQuoteService` |
| Pickup addresses | Vendor pickup address management | `/admin/pickup-addresses/list`, `/admin/pickup-addresses/create` |
| Shipping documents | Labels, manifests, shipment invoices for admin and vendor | `/admin/shipping-documents/labels`, `/admin/shipping-documents/manifests`, `/admin/shipping-documents/invoices`, `/vendor/shipping-documents/labels`, `/vendor/shipping-documents/manifests`, `/vendor/shipping-documents/invoices` |
| Packaging rates | Packaging rate CRUD and cart selection support | `/packagingrates/new`, `/packagingrates/list`, `/packagingrates/save` |

### 9. Finance and Commission

| Sub-Module | Features | Representative Routes / Sources |
|---|---|---|
| Admin finance dashboard | Finance dashboard, settlements, returns/refunds reports | `/admin/finance/dashboard`, `/admin/finance/settlements`, `/admin/finance/returns`, `/admin/finance/refunds` |
| Vendor finance | Vendor dashboard and ledger | `/vendor/finance/dashboard`, `/vendor/finance/ledger` |
| Vendor payout methods | Vendor payout method list/create/save/edit/delete | `/vendor-payout-methods/list`, `/vendor-payout-methods/create`, `/vendor-payout-methods/save` |
| Vendor payout requests | Vendor payout list/request/save/request-payout | `/vendor-payout/list`, `/vendor-payout/request`, `/vendor-payout/save`, `/vendor-payout/request-payout` |
| Admin payout processing | Admin payout list and status processing | `/admin/payouts/list`, `/admin/payouts/process` |
| Admin vendor payout methods | Marketplace administration of vendor payout methods | `/admin-vendor-payout-methods`, `/admin-vendor-payout-methods/create`, `/admin-vendor-payout-methods/save` |
| Commission settings | Marketplace commission CRUD and applicable-rate API | `/admin/commission-settings/list`, `/admin/commission-settings/create`, `/admin/commission-settings/edit/{id}`, `/api/commission/applicable-rate` |
| Vendor transactions | Vendor transaction list | `/vendor-transaction/list` |

### 10. Rewards and Promotions

| Sub-Module | Features | Representative Routes / Sources |
|---|---|---|
| Referral administration | Referral list/delete, reward dashboard, reward history | `/referral/list`, `/referral-reward/reward-dashboard`, `/referral-reward/rewards-history` |
| Level rate settings | Multi-level commission rate list/create/save/edit/delete | `/lavelratesettings/list`, `/lavelratesettings/create`, `/lavelratesettings/save`, `/lavelratesettings/edit/{id}` |
| Customer referral | Customer referral list, reward dashboard, reward history | `/customerreferral/list`, `/referralrewards/dashbords`, `/customerrewards/rewards` |
| Wallets | Admin wallet list, customer wallet, top-up, wallet transactions | `/wallet/walletlist`, `/wallet/wallet`, `/wallet/top-up`, `/wallettransaction/list`, `/customerwallet/wallet` |
| Customer cashout | Customer cashout form/request list; admin approve/reject/mark-paid | `/customerwallet/wallet/cashout`, `/cashoutcustomerrequest/list`, `/admin/cashouts`, `/admin/cashouts/{id}/approve`, `/admin/cashouts/{id}/reject`, `/admin/cashouts/{id}/mark-paid` |
| Coupons | Coupon CRUD and redemption list | `/coupon/list`, `/coupon/create`, `/coupon/save`, `/coupon-redemption/list` |
| Cashback | Cashback policy CRUD and transaction list | `/cashback-policy/list`, `/cashback-policy/create`, `/cashback-policy/save`, `/cashback/list`, `/customer-cashback/list` |
| Gift cards | Gift card CRUD, transaction list, customer purchase/payment | `/giftcard/list`, `/giftcard/create`, `/giftcard/save`, `/giftcard-transaction/list`, `/customer-giftcard/buy`, `/customer-giftcard/payment/{uuid}` |
| Promotion admin | Order incentives, notifications, fraud flags, promotion reports | `/admin/promotions/order-incentives`, `/admin/promotions/notifications`, `/admin/promotions/fraud-flags`, `/admin/promotions/reports` |

### 11. Fraud Detection and Prevention

| Sub-Module | Features | Representative Routes / Sources |
|---|---|---|
| Admin fraud dashboard | Fraud dashboard and metrics | `/admin/fraud`, `/admin/fraud/dashboard` |
| Assessments | Assessment list, detail, review, blocklist action | `/admin/fraud/assessments`, `/admin/fraud/assessments/{id}`, `/admin/fraud/assessments/{id}/review`, `/admin/fraud/assessments/{id}/blocklist` |
| Cases | Case list and case workflow services | `/admin/fraud/cases`, `FraudCaseService`, `FraudReviewService` |
| Rules and scoring | Rule engine, rule execution log, risk scoring, decision service | `FraudRuleEngine`, `FraudRiskScoringService`, `FraudDecisionService` |
| Signal collection | Device, network, velocity, payment, address, customer history, COD, promotion, referral, vendor risk evaluators | `module/fraud/services/evaluator`, `DefaultFraudSignalCollector` |
| COD fraud controls | COD eligibility, COD risk profile, customer/vendor COD limits, RTO/refusal behavior | `CodEligibilityService`, `CodRiskService`, `CodRiskProfileService` |
| Vendor fraud controls | Vendor risk profile, vendor-risk signal evaluator, payout/fulfillment guards | `VendorRiskProfileService`, `FraudPayoutGuard`, `FraudFulfilmentGuard` |
| Events and outbox | Fraud event publishing, outbox dispatcher, idempotency | `FraudEventPublisher`, `FraudOutboxDispatcherService`, `FraudIdempotencyService` |
| Security and privacy | Fraud permissions, webhook security, CSRF, masking/redaction | `FraudPermissions`, `FraudWebhookSecurityService`, `FraudPrivacySupport`, `FraudAdminCsrfInterceptor` |

### 12. Communication

| Sub-Module | Features | Representative Routes / Sources |
|---|---|---|
| Admin communication center | Templates, providers, routing rules, jobs, logs, settings | `/admin/communication`, `/admin/communication/templates`, `/admin/communication/providers`, `/admin/communication/routing-rules`, `/admin/communication/jobs`, `/admin/communication/logs`, `/admin/communication/settings` |
| Message reports | Message report and recipient details | `/admin/communication/messages/{messageId}/report`, `/admin/communication/messages/{messageId}/recipients` |
| Manual admin messages | Compose, send, inbox, sent, logs | `/admin/communication/manual/compose`, `/admin/communication/manual/send`, `/admin/communication/manual/inbox`, `/admin/communication/manual/sent`, `/admin/communication/manual/logs` |
| Manual vendor messages | Compose, send, inbox, sent | `/vendor/communication/manual/compose`, `/vendor/communication/manual/send`, `/vendor/communication/manual/inbox`, `/vendor/communication/manual/sent` |
| Manual customer messages | Compose, send, inbox, sent | `/customer/communication/manual/compose`, `/customer/communication/manual/send`, `/customer/communication/manual/inbox`, `/customer/communication/manual/sent` |
| Notifications | Customer, vendor, and admin notification lists/details | `/customer/notifications`, `/vendor/notifications`, `/admin/notifications` |
| Preferences | Public unsubscribe flow | `/public/communication/unsubscribe` |

### 13. Content and Marketing

| Sub-Module | Features | Representative Routes / Sources |
|---|---|---|
| Ads | Admin ad list/create/save/edit/delete | `/admin/ads/list`, `/admin/ads/create`, `/admin/ads/save`, `/admin/ads/edit/{id}`, `/admin/ads/delete/{id}` |
| Admin blog | Posts, preview, status, duplicate, delete, comments, subscribers, import/export | `/admin/blog`, `/admin/blog/new`, `/admin/blog/{id}/preview`, `/admin/blog/{id}/status`, `/admin/blog/comments/{id}/moderate`, `/admin/blog/subscribers`, `/admin/blog/import`, `/admin/blog/export` |
| Blog taxonomy | Categories and series management | `/admin/blog/categories`, `/admin/blog/categories/new`, `/admin/blog/series`, `/admin/blog/series/new` |
| Customer blog | Customer write, posts, saved posts, bookmark | `/customer/blog`, `/customer/blog/write`, `/customer/blog/my-posts`, `/customer/blog/saved`, `/customer/blog/{slug}/bookmark` |
| Legacy content repositories | Contact, FAQ, gallery, image gallery, job/category, news, services, clients, testimonials, subscribers | `ripository/*Repository.java`, templates under `contact`, `faq`, `gallery`, `news`, `services`, `clients`, `testimonial`, `subscriber` |

### 14. Settings and System Tools

| Sub-Module | Features | Representative Routes / Sources |
|---|---|---|
| Global settings | Basic, SEO, store, payment, delivery, order, image, social, policy, maintenance settings | `/admin/settings/index`, `/admin/settings/basic`, `/admin/settings/seo`, `/admin/settings/store`, `/admin/settings/payment`, `/admin/settings/delivery`, `/admin/settings/order`, `/admin/settings/image`, `/admin/settings/social`, `/admin/settings/policy`, `/admin/settings/maintenance` |
| Image settings | Logo variants, image upload settings, image settings forms | `ImageUploadSettingsService`, `GlobalSettingsServiceImageSettingsTest`, `ImageSettingsForm` |
| System endpoint tools | Endpoint dashboard, seed tools, permission catalogue install | `/admin/system`, `/admin/system/endpoints`, `/admin/system/seed/permission-catalogue` |
| Maintenance mode | Public maintenance page and settings integration | `/maintenance`, `templates/maintenance`, `GlobalSettingsController` |

### 15. Wishlist and Reviews

| Sub-Module | Features | Representative Routes / Sources |
|---|---|---|
| Wishlist | Wishlist page, add, remove, customer wishlist template/header count | `/wishlist`, `/wishlist/add`, `/wishlist/remove`, `module/wishlist` |
| Product reviews | Customer product review save | `/customer-product-review/save`, `module/review` |
| Product comments/ratings | Legacy product comment and rate controllers | `/examcomment`, `/rate` |

## Cross-Module Business Capabilities

| Capability | Modules Involved | Notes |
|---|---|---|
| Marketplace order split by vendor | Cart, checkout, order, vendor, finance | One checkout can create multiple vendor-scoped `SalesOrder` records. |
| Physical-product delivery eligibility | Public store, cart, shipping, product | Physical products require delivery location compatibility before cart/checkout. |
| COD lifecycle | Checkout, fraud, shipments, payments, finance | COD eligibility, OTP/risk controls, shipment collection, order payment summary, vendor settlement. |
| Returns and refunds | Customer orders, vendor orders, admin orders, finance, stock | Return processing can restock items, recalculate totals, refund overpayment, and reverse vendor earnings. |
| Vendor payout lifecycle | Vendor finance, admin finance, commission, settlement | Vendor earning, pending/available balance, payout request, admin processing. |
| Reward commission lifecycle | Customer registration, referrals, orders, wallets | Level commission is based on marketplace commission pool, then credited through referral chain. |
| Security and permissions | User, vendor IAM, system seed, fraud security, endpoint inventory | Includes admin roles/privileges, vendor privileges, permission catalogue seed, endpoint decision ledgers. |
| Communication delivery | Orders, fraud, marketing, admin/vendor/customer messaging | Communication module supports providers, templates, routing, manual messages, and notifications. |

## Known Proof Boundaries

- This document was created by static code and documentation inspection.
- It does not prove every route renders, every permission is seeded, or every workflow is fully production-ready.
- Existing docs note pending or partial verification for some areas, including vendor privilege startup proof, some browser/database flows, and fraud REST API Phase 10.
- Before production rollout of any module, capture focused compile/test, startup, permission, and browser evidence for that module.
