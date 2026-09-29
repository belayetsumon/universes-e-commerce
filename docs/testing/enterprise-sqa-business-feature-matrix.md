# Enterprise SQA Business Feature Matrix

Generated from the current codebase on 2026-09-29.

## Purpose

This document is the enterprise SQA baseline for the Universes E-Commerce codebase. It converts the current source-backed feature catalog into business-grade quality scenarios with realistic marketplace data, international quality expectations, security controls, and release evidence requirements.

Use this document together with:

- `docs/overview/feature-catalog.md` for the canonical module and route inventory.
- `docs/operations/functional-operations-guide.md` for operating flows.
- `docs/security/application-security-endpoint-inventory.csv` for route-level coverage.
- `docs/security/application-security-permission-catalogue.csv` for permission-vocabulary coverage.
- `docs/testing/test-flow.md` for existing manual test notes.

This is an SQA plan and traceability matrix. It is not runtime proof by itself. Each release still needs current compile, test, startup, permission, browser, and database evidence.

## Source Basis

| Evidence Source | Current Baseline |
|---|---:|
| Spring MVC controllers | 136 |
| Java test classes | 84 |
| Security endpoint inventory rows | 742 |
| Permission catalogue rows | 132 |
| Code-derived feature modules | 15 |

## Quality Standards

| Standard / Practice | Applied Expectation |
|---|---|
| ISO/IEC/IEEE 29119 | Test cases have objective preconditions, steps, expected results, actual result, status, and evidence. |
| ISO/IEC 25010 | Functional suitability, reliability, usability, performance efficiency, maintainability, portability, compatibility, and security are assessed per module. |
| OWASP ASVS / OWASP Top 10 | Authentication, authorization, CSRF, input validation, session handling, file upload, IDOR, and sensitive-data handling are checked for every protected feature. |
| PCI DSS scope awareness | Card data must not be stored by the application; payment integrations should use provider tokens, callbacks, and auditable transaction references. |
| GDPR / international privacy principles | Personal data is minimized, masked where possible, retained only as needed, and exported or deleted through controlled processes when required. |
| WCAG 2.2 AA target | Public, customer, vendor, and admin screens should support keyboard access, readable contrast, labels, validation messages, and responsive layout. |
| Enterprise release governance | Every production release must include traceability from business requirement to source, test, security control, and evidence. |

## Realistic Business Test Data

All names and values below are synthetic but business-realistic. Use them consistently so cross-module tests can prove marketplace flows end to end.

### Actors

| Actor ID | Role | Name | Email / Login | Business Use |
|---|---|---|---|---|
| SQA-A01 | Marketplace Admin | Nusrat Rahman | admin.qa@universes.test | Back-office setup, permission seed, finance approval, fraud review |
| SQA-A02 | Operations Staff | Arif Chowdhury | ops.qa@universes.test | Order, shipment, return, customer support |
| SQA-V01 | Vendor Owner | Dhaka Gadget Hub | vendor.owner@dhakagadget.test | Electronics vendor, stock, order fulfillment, payout |
| SQA-V02 | Vendor Staff | Farzana Islam | packer@dhakagadget.test | Vendor staff with limited packing and shipment privilege |
| SQA-V03 | Second Vendor | Chattogram Fashion House | vendor.owner@ctgfashion.test | Apparel vendor for multi-vendor split order proof |
| SQA-C01 | Registered Customer | Tanvir Ahmed | tanvir.customer@testmail.test | Normal checkout, wallet, COD, return |
| SQA-C02 | Referred Customer | Rima Akter | rima.referred@testmail.test | Referral-chain and reward proof |
| SQA-G01 | Guest Customer | Guest Mobile User | +8801711000001 | Guest checkout with mobile OTP |
| SQA-D01 | Delivery Person | Hasan Courier | rider.hasan@universes.test | Vendor rider or internal delivery |

### Products and Catalog Data

| Product ID | Vendor | Category | Product | Business Data |
|---|---|---|---|---|
| SQA-P01 | Dhaka Gadget Hub | Electronics > Mobile | Galaxy A55 5G 8/256 | Sales 52,500 BDT; purchase 48,000 BDT; physical; 0.45 kg; stock 25; warranty 12 months |
| SQA-P02 | Dhaka Gadget Hub | Electronics > Accessories | Type-C Fast Charger 25W | Sales 1,450 BDT; purchase 950 BDT; physical; 0.20 kg; stock 100 |
| SQA-P03 | Chattogram Fashion House | Fashion > Men | Cotton Panjabi Blue XL | Sales 2,200 BDT; purchase 1,400 BDT; physical; variants size/color; stock 40 |
| SQA-P04 | Chattogram Fashion House | Fashion > Women | Jamdani Sharee Premium | Sales 9,500 BDT; purchase 7,200 BDT; physical; 0.85 kg; stock 12 |
| SQA-P05 | Platform | Digital Goods | E-Gift Voucher 1000 | Sales 1,000 BDT; virtual; no shipping; no physical return |

### Shipping, Finance, and Promotions Data

| Data Area | Test Data |
|---|---|
| Country hierarchy | Bangladesh `BD`, Dhaka Division, Dhaka District, Dhanmondi Thana |
| Shipping zone | `DHAKA-METRO`, same-day Dhaka coverage |
| Carrier | `REDX-DHK`, supports COD, third-party, marketplace-managed settlement |
| Carrier rate | Dhaka metro base 80 BDT for first 1 kg, extra 20 BDT per kg |
| Packaging | Standard box 30 BDT, premium fragile packaging 80 BDT |
| COD rule | COD allowed below 100,000 BDT when mobile is verified and fraud risk is not high |
| Commission | Electronics 8%, fashion 12%, default fallback 10% |
| Coupon | `EID10`, 10% discount, max 500 BDT, valid for registered customers |
| Cashback | 2% cashback on prepaid orders, capped at 300 BDT |
| Gift card | `GC-SQA-1000`, 1,000 BDT balance |
| Referral | SQA-C01 refers SQA-C02; level commission uses configured level rates |
| Payout | Vendor bank transfer request 15,000 BDT with reference `BNK-SQA-001` |

## Release Entry And Exit Criteria

### Entry Criteria

- Feature is listed in `docs/overview/feature-catalog.md`.
- Feature owner, actor, route, service, template, and data model are known.
- Required seed data and permissions exist or have a documented setup step.
- Test environment has a clean database state or a known migration baseline.
- External providers such as payment, SMS, email, and carrier APIs are mocked, sandboxed, or explicitly out of scope.

### Exit Criteria

- `mvn test` or focused equivalent passes for the changed scope.
- `mvn package` passes with the agreed test policy.
- Application starts with the selected profile.
- Target routes render without Whitelabel error.
- Mutating browser forms enforce CSRF.
- Protected routes enforce expected admin, vendor, customer, or public access.
- Business-critical data is persisted, audited, and visible in the next operational screen.
- Known limitations are documented before release approval.

## Evidence Ladder

| Level | Evidence | Release Meaning |
|---|---|---|
| L1 | Static source inspection | Feature exists in code, but behavior is not proven. |
| L2 | Compile / test-compile | Code compiles, but runtime and behavior are not proven. |
| L3 | Focused automated tests | Targeted logic or security behavior is proven for named cases. |
| L4 | Startup and migration proof | Flyway, Hibernate, and application boot pass in target profile. |
| L5 | Browser or HTTP proof | Target route renders or returns expected status with correct actor. |
| L6 | End-to-end business proof | Cross-module workflow completes with persisted business result. |
| L7 | Production readiness proof | Monitoring, rollback, permissions, data retention, and operational runbooks are validated. |

## Cross-Cutting SQA Controls

| Control ID | Area | Required Tests |
|---|---|---|
| SQA-X01 | Authentication | Anonymous users are redirected or rejected for all admin, customer, vendor, and system routes. |
| SQA-X02 | Authorization | Admin, vendor, vendor staff, customer, and guest roles cannot access each other's private data. |
| SQA-X03 | Vendor ownership | Every vendor route must scope reads and mutations to active vendor membership. |
| SQA-X04 | CSRF | All browser POST, PUT, PATCH, and DELETE actions require a valid CSRF token. |
| SQA-X05 | IDOR | Changing an ID in the URL or form cannot expose another customer, vendor, order, shipment, payout, or wallet. |
| SQA-X06 | Input validation | Required fields, numeric ranges, enum values, UUIDs, files, and dates reject invalid values with clear messages. |
| SQA-X07 | File upload | Images and documents enforce type, size, name, storage, and non-executable content policy. |
| SQA-X08 | Monetary accuracy | Prices, discounts, tax-like charges, shipping, packaging, commission, wallet, payout, COD, and refunds use decimal-safe calculations. |
| SQA-X09 | Idempotency | Order placement, payment callbacks, COD collection, fraud assessment, and outbox dispatch cannot double-post critical records. |
| SQA-X10 | Auditability | Sensitive actions record actor, time, target, old/new state, and business reference. |
| SQA-X11 | Privacy | Mobile, email, payment reference, fraud data, and payout details are masked where full value is not required. |
| SQA-X12 | Accessibility | Forms have labels, focus order, visible validation, keyboard submission, and responsive layout. |
| SQA-X13 | Performance | Listings paginate and filter; cart, checkout, order, fraud, and dashboard queries avoid avoidable N+1 behavior. |
| SQA-X14 | Internationalization readiness | Country, currency, phone, address hierarchy, timezone, and payment references are not hard-coded to one market unless documented. |

## Feature SQA Matrix

### 1. Public Storefront

| SQA ID | Feature | Business Scenario With Data | Expected Result |
|---|---|---|---|
| SQA-PS-001 | Home and public pages | Guest opens `/index`, `/public/about-us`, and maintenance page while store is active. | Public pages render without login and show current storefront data. |
| SQA-PS-002 | Product browsing | Guest selects Dhaka District and opens SQA-P01 details from `/public/product`. | Product details show price, stock/availability, image, delivery eligibility, and SEO metadata. |
| SQA-PS-003 | Search suggestions | Guest searches `Galaxy` and `Panjabi`. | Suggestions return matching active products only. |
| SQA-PS-004 | Public blog | Guest opens a published article, category, tag, comment form, and subscription form. | Published content is visible; moderation and subscription validation work. |
| SQA-PS-005 | SEO discovery | Guest opens `/robots.txt`, `/sitemap.xml`, and `/llms.txt`. | Files return valid discovery output without exposing protected URLs. |
| SQA-PS-006 | Social share tracking | Guest shares SQA-P01 using `/public/share-track`. Admin opens share analytics. | Share event is recorded and visible in admin reporting without leaking personal data. |

### 2. Identity and Access

| SQA ID | Feature | Business Scenario With Data | Expected Result |
|---|---|---|---|
| SQA-IAM-001 | User administration | Admin creates SQA-A02 and SQA-V02 from `/users/registrations`. | Password is encrypted; status, role, and user type persist correctly. |
| SQA-IAM-002 | Role and privilege setup | Admin creates a role for vendor packing staff with shipment-read and shipment-update privilege only. | Role saves with exact privileges and cannot grant platform admin permissions. |
| SQA-IAM-003 | Password change | SQA-A02 changes password through `/users/change-password`; old password is rejected. | Credential version/session rules invalidate stale sessions where applicable. |
| SQA-IAM-004 | Forgot password | SQA-C01 requests reset from `/forgotpassword/showemail` and uses a valid token once. | Token is time-bound, single-use, and cannot reset another account. |
| SQA-IAM-005 | Login history | Admin opens `/users/login-history` for SQA-C01. | Login attempts are visible with actor and timestamp; sensitive values are masked. |
| SQA-IAM-006 | Permission catalogue seed | Admin posts `/admin/system/seed/permission-catalogue`. | Missing permissions are inserted from the 132-row catalogue without duplicate slugs. |
| SQA-IAM-007 | Endpoint inventory | Admin opens `/admin/system/endpoints`. | Inventory is visible to authorized admin only and aligns with the 742-row security artifact. |

### 3. Customer Account

| SQA ID | Feature | Business Scenario With Data | Expected Result |
|---|---|---|---|
| SQA-CUS-001 | Customer registration | SQA-C01 registers from `/customer_registration/registration`. | Active customer account, role, profile, and referral record are created. |
| SQA-CUS-002 | Referred registration | SQA-C02 registers using SQA-C01 referral code. | Referral link is stored and reward profile is initialized once. |
| SQA-CUS-003 | Profile update | SQA-C01 updates name, phone, billing address, and profile image. | Data persists; image policy is enforced; unrelated users cannot read or update it. |
| SQA-CUS-004 | Billing address | SQA-C01 adds Dhanmondi billing address. | Address appears in checkout and account screens. |
| SQA-CUS-005 | Order list and details | SQA-C01 opens order list after placing a multi-vendor order. | Only SQA-C01 orders are visible with item, payment, shipping, and status details. |
| SQA-CUS-006 | Customer payment | Customer pays remaining balance for a partially paid order. | Overpayment is blocked; payment state and order payable are recalculated. |
| SQA-CUS-007 | EMI | Customer opens EMI details for an eligible order. | Plan details are visible only to the owning customer. |
| SQA-CUS-008 | COD mobile verification | SQA-C01 sends and verifies OTP before COD checkout. | COD verification is bound to normalized mobile and authenticated customer. |

### 4. Product Catalog

| SQA ID | Feature | Business Scenario With Data | Expected Result |
|---|---|---|---|
| SQA-CAT-001 | Category setup | Admin creates Electronics > Mobile and Fashion > Men. | Category hierarchy saves and appears in product forms and storefront browse. |
| SQA-CAT-002 | Dynamic attributes | Admin creates `RAM`, `Storage`, `Size`, and `Color` attributes and maps them to categories. | Product forms show required dynamic fields based on category. |
| SQA-CAT-003 | Admin product | Admin creates SQA-P01 with valid price, stock, dimension, image, and warranty. | Product saves and details page shows all commercial data. |
| SQA-CAT-004 | Vendor product | SQA-V01 creates SQA-P02 from `/productvendor/create`. | Product is linked to active vendor and not editable by another vendor. |
| SQA-CAT-005 | Variant product | SQA-V03 creates SQA-P03 with size/color variants. | Customer must select valid variant before cart add. |
| SQA-CAT-006 | Price invariant | Admin attempts sales price lower than purchase price. | Save is rejected with field or cross-field validation. |
| SQA-CAT-007 | Product media | Admin uploads product image and rejects executable or oversized files. | Valid image is stored; unsafe file is rejected. |
| SQA-CAT-008 | Stock receiving | Admin receives 25 units of SQA-P01 and adjusts 1 damaged unit out. | Current stock and stock transactions match arithmetic. |

### 5. Cart and Checkout

| SQA ID | Feature | Business Scenario With Data | Expected Result |
|---|---|---|---|
| SQA-CHK-001 | Cart add | SQA-C01 adds SQA-P01, SQA-P02, and SQA-P03. | Cart groups items by vendor and preserves selected variants. |
| SQA-CHK-002 | Quantity update | Customer increases SQA-P01 above available stock. | Update is rejected and cart quantity remains valid. |
| SQA-CHK-003 | Shipping option | Customer selects REDX-DHK and premium packaging for Dhaka. | Shipping and packaging charges update per vendor group. |
| SQA-CHK-004 | Coupon preview | Customer applies `EID10`. | Discount respects max 500 BDT and eligible customer rules. |
| SQA-CHK-005 | Guest OTP checkout | SQA-G01 starts guest checkout, sends OTP, verifies, and changes mobile. | OTP lifecycle enforces normalized mobile, expiry, resend, and session binding. |
| SQA-CHK-006 | Availability gate | Customer checks out with virtual SQA-P05 and physical SQA-P01. | Physical item requires delivery coverage; virtual item does not require shipment. |
| SQA-CHK-007 | Idempotent placement | Customer double-clicks place order or refreshes callback. | Only one order placement result is committed. |

### 6. Orders and Sales

| SQA ID | Feature | Business Scenario With Data | Expected Result |
|---|---|---|---|
| SQA-ORD-001 | Multi-vendor order split | Cart contains SQA-P01 from SQA-V01 and SQA-P03 from SQA-V03. | Checkout creates vendor-scoped orders with correct subtotals and shared customer reference. |
| SQA-ORD-002 | Admin order operations | Admin changes order from NEW_ORDER to CONFIRMED to PROCESSING. | Status transition is allowed, audited, and visible to customer and vendor. |
| SQA-ORD-003 | Vendor order operations | SQA-V01 opens only its own order and marks it PACKED. | Cross-vendor order ID access is denied. |
| SQA-ORD-004 | Customer cancellation | Customer cancels a CONFIRMED order before shipment. | Cancellation is accepted; stock/payment state is recalculated. |
| SQA-ORD-005 | Late cancellation block | Customer tries to cancel after shipment is IN_TRANSIT. | Cancellation is blocked and return flow is suggested. |
| SQA-ORD-006 | Return request | Customer requests return for SQA-P01 but not virtual SQA-P05. | Physical item return is recorded; virtual item return is rejected. |
| SQA-ORD-007 | PDF and barcode | Admin and vendor generate PDF for the order. | PDF contains order summary, barcode/QR, and actor-scoped data only. |
| SQA-ORD-008 | Sales dashboard | Admin and vendor open sales dashboards. | Metrics match order data and vendor dashboard is vendor-scoped. |

### 7. Vendor Portal

| SQA ID | Feature | Business Scenario With Data | Expected Result |
|---|---|---|---|
| SQA-VEN-001 | Vendor profile | SQA-V01 creates Dhaka Gadget Hub vendor profile. | Vendor code is generated and profile details are visible to owner. |
| SQA-VEN-002 | Vendor verification | Vendor completes email and mobile verification. | Tokens/OTP expire correctly and verification status updates once. |
| SQA-VEN-003 | Vendor staff assignment | Vendor owner assigns SQA-V02 to packing role. | Staff can access allowed vendor screens only. |
| SQA-VEN-004 | Vendor role grant ceiling | Vendor owner tries to grant finance payout approval when role lacks authority. | Privilege escalation is blocked. |
| SQA-VEN-005 | Vendor stock | Vendor receives and adjusts SQA-P02 stock. | Stock changes apply only to that vendor's products. |
| SQA-VEN-006 | Low-stock alerts | SQA-P04 stock drops below threshold. | Vendor low-stock page lists only active-vendor products needing attention. |
| SQA-VEN-007 | Vendor delivery persons | Vendor creates rider SQA-D01. | Rider appears for eligible vendor shipment forms only. |

### 8. Shipping and Fulfillment

| SQA ID | Feature | Business Scenario With Data | Expected Result |
|---|---|---|---|
| SQA-SHP-001 | Location hierarchy | Admin creates Bangladesh > Dhaka Division > Dhaka > Dhanmondi. | Hierarchy saves and can be selected by zone/rate setup. |
| SQA-SHP-002 | Shipping zone | Admin creates `DHAKA-METRO` zone from Dhaka coverage. | Zone appears in carrier rate and quote configuration. |
| SQA-SHP-003 | Carrier and rate | Admin creates REDX-DHK rate and slabs. | Checkout quote calculates base and extra-weight charge accurately. |
| SQA-SHP-004 | Shipping profile | Admin configures vendor-specific profile for SQA-V01. | SQA-V01 products use allowed carrier and coverage only. |
| SQA-SHP-005 | Shipping rules | Admin disables COD above 100,000 BDT or high fraud risk. | Checkout keeps carrier if eligible but blocks COD where rule matches. |
| SQA-SHP-006 | Shipment creation | Vendor creates shipment for packed SQA-V01 order. | Shipment syncs order, carrier, COD, and settlement snapshots. |
| SQA-SHP-007 | COD collection | Vendor collects 20,000 BDT COD. | COD payment records once, pending amount reduces, and overcollection is blocked. |
| SQA-SHP-008 | Label, manifest, invoice | Admin creates label, manifest, and shipment invoice. | Documents snapshot shipment cost, COD fee, vendor payable, and marketplace payable. |
| SQA-SHP-009 | Cross-vendor shipment IDOR | SQA-V03 attempts to open SQA-V01 shipment. | Access is denied and no data leaks. |

### 9. Finance and Commission

| SQA ID | Feature | Business Scenario With Data | Expected Result |
|---|---|---|---|
| SQA-FIN-001 | Commission rate | Admin configures 8% electronics commission. | Applicable-rate API returns the correct commission for SQA-P01. |
| SQA-FIN-002 | Vendor ledger | Delivered order creates vendor earning transaction. | Ledger has pending earning with order reference and correct commission deduction. |
| SQA-FIN-003 | Settlement dashboard | Admin opens finance settlement dashboard. | Shipment, COD, marketplace payable, and vendor payable totals reconcile. |
| SQA-FIN-004 | Payout method | Vendor saves bank payout method. | Sensitive account data is validated and masked where displayed. |
| SQA-FIN-005 | Payout request | Vendor requests 15,000 BDT payout. | Request cannot exceed available balance and uses active payout method. |
| SQA-FIN-006 | Admin payout processing | Admin marks payout PROCESSING then PAID with `BNK-SQA-001`. | Balance moves correctly and audit trail records reference. |
| SQA-FIN-007 | Refund reversal | Customer return causes refund and vendor earning reversal. | Finance ledger and order payment summary remain balanced. |

### 10. Rewards and Promotions

| SQA ID | Feature | Business Scenario With Data | Expected Result |
|---|---|---|---|
| SQA-RWD-001 | Referral chain | SQA-C01 refers SQA-C02. | Referral relationship appears in admin and customer referral screens. |
| SQA-RWD-002 | Level rate | Admin sets level 1 at 10%, level 2 at 5%. | Rates save uniquely and apply to the commission pool. |
| SQA-RWD-003 | Order reward | SQA-C02 places eligible order. | SQA-C01 receives level commission wallet transaction with source order reference. |
| SQA-RWD-004 | Coupon | Customer applies `EID10` on eligible cart. | Discount is calculated once and respects cap and expiry. |
| SQA-RWD-005 | Cashback | Prepaid order qualifies for 2% cashback. | Cashback transaction is recorded and visible to customer/admin. |
| SQA-RWD-006 | Gift card purchase | Customer buys and pays for `GC-SQA-1000`. | Gift card balance and transaction record are created. |
| SQA-RWD-007 | Wallet top-up | Admin tops up customer wallet. | Wallet balance and transaction history are consistent. |
| SQA-RWD-008 | Cashout request | Customer requests cashout; admin approves, rejects, or marks paid. | Balance is debited/restored/settled exactly once according to action. |
| SQA-RWD-009 | Fraud flags | Promotion abuse is flagged. | Fraud/promotion report shows actionable record without exposing private data unnecessarily. |

### 11. Fraud Detection and Prevention

| SQA ID | Feature | Business Scenario With Data | Expected Result |
|---|---|---|---|
| SQA-FRD-001 | Fraud assessment | High-value COD order over 100,000 BDT is assessed. | Risk level and signals are stored with reason codes. |
| SQA-FRD-002 | Device signal | Same device places many guest COD attempts. | Velocity/device signal increases risk score. |
| SQA-FRD-003 | COD risk | Unverified mobile attempts COD. | COD eligibility is blocked until verified. |
| SQA-FRD-004 | Blocklist | Admin blocklists a risky mobile hash. | Future checkout using same normalized mobile is flagged or blocked by configured rule. |
| SQA-FRD-005 | Manual review | Admin reviews assessment and changes decision. | Review history records actor, decision, note, and timestamp. |
| SQA-FRD-006 | Case workflow | Admin opens fraud case and assigns priority. | Case state changes persist and show in dashboard metrics. |
| SQA-FRD-007 | Rule engine | Admin creates threshold rule for COD amount. | Rule executes deterministically and logs execution. |
| SQA-FRD-008 | Outbox and idempotency | Fraud event dispatch is retried. | Event is not duplicated and retry state is auditable. |
| SQA-FRD-009 | Privacy | Fraud dashboard renders mobile/email/order data. | Sensitive fields are masked according to privacy support. |

### 12. Communication

| SQA ID | Feature | Business Scenario With Data | Expected Result |
|---|---|---|---|
| SQA-COM-001 | Templates | Admin creates order-confirmation SMS and email templates. | Template validates variables and stores active version. |
| SQA-COM-002 | Providers | Admin configures sandbox SMS/email provider. | Provider secret is not displayed after save. |
| SQA-COM-003 | Routing rules | Admin routes COD OTP SMS to mobile provider. | Message jobs use the selected provider. |
| SQA-COM-004 | Manual admin message | Admin sends support message to SQA-C01. | Message appears in customer inbox and admin sent history. |
| SQA-COM-005 | Vendor message | Vendor sends order update to customer. | Message is scoped to vendor-owned order/customer context. |
| SQA-COM-006 | Notifications | Customer, vendor, and admin open notification lists. | Each actor sees only their own notifications. |
| SQA-COM-007 | Unsubscribe | Customer unsubscribes from marketing. | Future marketing messages respect preference while transactional messages remain allowed. |

### 13. Content and Marketing

| SQA ID | Feature | Business Scenario With Data | Expected Result |
|---|---|---|---|
| SQA-MKT-001 | Ads | Admin creates active homepage banner ad for Eid campaign. | Ad appears only in active schedule and correct placement. |
| SQA-MKT-002 | Admin blog | Admin creates draft, previews, publishes, duplicates, and deletes a blog article. | Workflow states and audit data are correct. |
| SQA-MKT-003 | Blog moderation | Customer comments on public blog. Admin approves/rejects. | Only approved comments appear publicly. |
| SQA-MKT-004 | Customer blog | Customer writes and bookmarks a post. | Customer sees own posts and saved posts only. |
| SQA-MKT-005 | Subscribers | Guest subscribes to blog/news. | Duplicate subscription is handled gracefully. |
| SQA-MKT-006 | Legacy content | Admin manages contact, FAQ, gallery, news, services, clients, testimonials. | CRUD screens persist content and public screens render active records. |

### 14. Settings and System Tools

| SQA ID | Feature | Business Scenario With Data | Expected Result |
|---|---|---|---|
| SQA-SET-001 | Global settings | Admin updates basic, SEO, store, payment, delivery, order, social, and policy settings. | Settings persist and are visible in relevant public/admin screens. |
| SQA-SET-002 | Image settings | Admin uploads logo, favicon, and OG image. | Images are validated, resized or stored according to policy, and preview correctly. |
| SQA-SET-003 | Maintenance mode | Admin enables maintenance mode. | Public storefront shows maintenance page while authorized admin access remains possible. |
| SQA-SET-004 | Bangladesh location seed | Admin posts seed location action. | Seed is idempotent and does not duplicate existing location rows. |
| SQA-SET-005 | Endpoint dashboard | Admin opens endpoint dashboard. | Counts match current generated security inventory. |
| SQA-SET-006 | Permission catalogue | Admin installs all permissions. | Catalogue seed is repeatable and does not create duplicate modules/privileges. |

### 15. Wishlist and Reviews

| SQA ID | Feature | Business Scenario With Data | Expected Result |
|---|---|---|---|
| SQA-WRV-001 | Wishlist add | SQA-C01 adds SQA-P01 to wishlist from product list. | Wishlist count increments and item appears on `/wishlist`. |
| SQA-WRV-002 | Wishlist remove | SQA-C01 removes SQA-P01 from wishlist. | Wishlist count decrements and item disappears. |
| SQA-WRV-003 | Wishlist IDOR | SQA-C02 attempts to remove SQA-C01 wishlist item by ID. | Request is denied or ignored without modifying SQA-C01 data. |
| SQA-WRV-004 | Product review | SQA-C01 reviews delivered SQA-P01. | Review is saved once for eligible purchase and visible according to moderation rules. |
| SQA-WRV-005 | Legacy rating/comment | Customer submits product comment/rating. | Input is validated and displayed only where intended. |

## End-To-End Business Regression Suites

### Suite E2E-01: Marketplace Setup To First Order

1. Admin creates vendor SQA-V01 and SQA-V03.
2. Admin creates shipping location hierarchy, carrier, zone, rate, and packaging.
3. Admin creates categories, attributes, commission settings, and coupon.
4. Vendors create SQA-P01, SQA-P02, and SQA-P03 with stock.
5. SQA-C01 registers, verifies mobile, adds multi-vendor cart, applies coupon, selects shipping, and places COD order.
6. Expected result: two vendor-scoped orders exist, stock is reserved, COD due is recorded, customer/vendor/admin order screens reconcile.

### Suite E2E-02: Fulfillment, COD, Settlement, Payout

1. Vendor packs SQA-V01 order and creates shipment.
2. Vendor prints label and manifest.
3. Vendor collects COD and marks shipment delivered.
4. Finance ledger records vendor earning.
5. Vendor requests payout.
6. Admin processes payout with bank reference.
7. Expected result: COD, order, shipment, ledger, payout, and settlement reports balance.

### Suite E2E-03: Return, Refund, Restock, Finance Reversal

1. Customer requests return for SQA-P01.
2. Vendor or admin approves and processes returned item.
3. System restocks quantity, recalculates order total, records refund, and reverses vendor earning.
4. Expected result: order status, payment summary, stock, and ledger all reconcile.

### Suite E2E-04: Referral, Rewards, Coupon, Cashback, Cashout

1. SQA-C01 refers SQA-C02.
2. SQA-C02 places prepaid order with coupon.
3. Reward commission and cashback are calculated.
4. SQA-C01 requests wallet cashout.
5. Admin approves and marks paid.
6. Expected result: wallet balance, reward history, cashout list, and admin records are consistent.

### Suite E2E-05: Fraud And Communication

1. Guest places repeated high-value COD attempts from same mobile/device.
2. Fraud assessment records device, mobile, velocity, COD, and address signals.
3. Admin reviews case and blocklists mobile hash.
4. System sends transactional notifications to admin/vendor/customer.
5. Expected result: risky order is blocked or held according to configuration and all messages/audit records are traceable.

## Security Regression Suite

| Suite ID | Scenario | Expected Result |
|---|---|---|
| SEC-001 | Anonymous opens all admin, vendor, customer protected routes from endpoint inventory. | Login redirect or 401/403; no protected data exposure. |
| SEC-002 | Customer attempts vendor/admin routes. | 403 or redirect without data leakage. |
| SEC-003 | Vendor staff changes vendor/order/shipment/payout IDs to another vendor. | 403 or not found; no mutation. |
| SEC-004 | Browser POST without CSRF token for each mutating route family. | 403. |
| SEC-005 | Admin without specific permission attempts system seed, fraud, payout, or user-management action. | Authorization failure. |
| SEC-006 | Upload malicious file with image extension. | Rejected; file is not stored as executable content. |
| SEC-007 | Password reset token reuse and token ownership mismatch. | Rejected. |
| SEC-008 | Payment callback replay. | Idempotent handling; no duplicate payment. |

## Non-Functional Test Matrix

| Area | Target |
|---|---|
| Performance | Public product list, cart, checkout, admin order list, fraud dashboard, and finance dashboards should return within agreed SLA with production-like data. |
| Load | Cart add/update, checkout placement, OTP send/verify, payment callback, COD collection, and notification dispatch should survive concurrent usage without duplicate records. |
| Reliability | Failed payment, failed message provider, failed carrier API, and retryable fraud outbox events should leave recoverable state. |
| Data integrity | Monetary tables reconcile to order, shipment, payout, wallet, and refund references. |
| Backup and migration | Flyway migrations apply from baseline; rollback plan exists for schema changes. |
| Observability | Errors are logged with correlation reference but without leaking secrets or personal data. |
| Accessibility | Admin, vendor, customer, and public forms meet keyboard and screen-reader basics. |
| Responsive UI | Mobile checkout, customer account, vendor order list, and admin dashboards remain usable on small screens. |

## Release Sign-Off Template

| Item | Owner | Status | Evidence Link / Notes |
|---|---|---|---|
| Feature scope matches catalog | Product / BA | Not Tested | |
| Compile and test-compile | Engineering | Not Tested | |
| Focused automated tests | Engineering / QA | Not Tested | |
| Migration and startup | Engineering / DevOps | Not Tested | |
| Browser smoke | QA | Not Tested | |
| Security smoke | Security / QA | Not Tested | |
| Data reconciliation | QA / Finance Owner | Not Tested | |
| Accessibility smoke | QA | Not Tested | |
| Performance smoke | QA / DevOps | Not Tested | |
| Release approval | Product Owner | Not Tested | |

## Known Proof Boundaries

- The current security endpoint decision ledger records many routes as pending manual confirmation or pending migration/runtime proof; use the security CSVs for exact row status before release sign-off.
- Some business flows depend on provider integrations such as payment, SMS, email, and carrier systems; sandbox or mock evidence must be named explicitly.
- Vendor payout balance release from pending earning to available balance should be rechecked before production cash-out use.
- Fraud and promotion flows have broad source coverage, but release proof must include runtime, browser, and database validation for the configured deployment profile.
- This document intentionally references the canonical endpoint and permission CSVs instead of duplicating all 742 route rows.
