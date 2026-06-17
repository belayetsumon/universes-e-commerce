# Incentive System Migration Plan

This note maps the target enterprise incentive architecture to the current codebase.

## Database Naming Rule

Keep the `promotions_` prefix for all incentive-system tables.

Reason:

- easier schema tracking
- faster reporting/query grouping
- consistent with the current codebase
- safer migration path from existing tables

Recommended rule:

- keep existing `promotions_*` tables
- all new incentive tables must also start with `promotions_`
- do not introduce mixed naming like some prefixed and some unprefixed tables

## Short Answer

Do not remove the whole existing `ReferralRewards` module.

Instead:

- keep the parts that already implement real business behavior
- change the data model where wallet money and reward points are mixed together
- retire duplicate or legacy controller flows that will create confusion in production
- add missing ledger, rule, reporting, and fraud layers

## Current Code That Can Stay

These parts are useful foundations and should be refactored forward instead of deleted:

- `ReferralService`
  - referral profile creation
  - referral code resolution
  - level commission distribution
- `WalletService`
  - credit/debit entry creation
  - transaction metadata such as `sourceType`, `sourceReference`, `levelNumber`
- `CouponService`
  - validation by status, expiry, and usage limit
  - discount computation
- `GiftCardService`
  - balance usage flow
  - per-order transaction record
- `CashbackService`
  - pending cashback creation
  - delayed release on delivered/completed order
- `CheckoutIncentiveService`
  - server-side quote preparation
  - coupon, reward point, and gift card allocation across vendor-split orders
  - checkout-time reward redemption
- `OrderIncentiveUsage`
  - order-level incentive snapshot table already uses `promotions_order_incentive_usage`
  - keep this model and expand it instead of introducing a second tracking table
- `RewardAccount` and `RewardTransaction`
  - useful start for separating points from wallet money
  - keep the model direction and finish the migration away from the old mixed wallet balance
- `AuditLogPromotionsService`
  - useful base for promotion audit logging
  - extend it to every money/points/status-changing action
- admin/customer list screens
  - wallet transactions
  - referral list
  - cashback list
  - cashout list

## Current Code That Must Change

### 1. Separate Wallet Money From Reward Points

This is the most important change.

Right now the code mixes:

- wallet payment
- referral reward points
- cashback credits
- cashout redemption

inside one balance concept.

Target design:

- `promotions_wallet_account` for stored money
- `promotions_wallet_transaction` for money ledger
- `promotions_reward_account` for points balance
- `promotions_reward_transaction` for points ledger

Do not let checkout wallet payment and reward-point cashout consume the same balance.

Current completion status:

- `RewardAccount` and `RewardTransaction` already exist and should become the source of truth for points.
- Some controllers and labels still call points a wallet balance.
- Existing `Wallet` / `WalletTransaction` should remain only for stored money after migration.

Completion steps:

1. Move reward earning, referral commission, reward redemption, and cashout redemption to `RewardAccount`.
2. Keep checkout wallet payment and wallet refund/restore in `Wallet`.
3. Rename UI labels that show `RewardAccount` from "wallet" to "reward points" or "rewards balance".
4. Add database constraints so one customer has only one wallet account and one reward account.
5. Add a one-time migration that copies historical reward balances from the old wallet table into reward accounts when those rows represent points.
6. Leave old mixed wallet columns read-only during the transition, then remove them after production reconciliation.

### 2. Replace Balance-First Logic With Ledger-First Logic

Current code updates wallet balance directly and also writes transactions.

Target behavior:

1. create transaction
2. lock or validate account
3. update derived balance
4. keep full audit trail

For enterprise flow, every incentive type should have:

- `PENDING`
- `AVAILABLE`
- `REDEEMED`
- `REVERSED`
- `EXPIRED`

Required ledger fields:

- account id
- customer id
- transaction type
- status
- amount or points
- balance before
- balance after
- source type
- source reference
- order id
- idempotency key
- reversal transaction id
- created by
- created at

Completion steps:

1. Create ledger rows before changing account balances.
2. Use one idempotency key per business event, such as `ORDER:{orderId}:CASHBACK:PENDING`.
3. Reject duplicate ledger writes with the same idempotency key.
4. Store reversal rows instead of editing or deleting original rows.
5. Recompute and reconcile account balances from ledger rows during admin audit.

### 3. Redesign Referral Data Structure

The current `Referral` table mixes referral ownership and parent linkage in one record.

Target design:

- `promotions_referral_code`
  - code owner
  - active/inactive
  - campaign/source
- `promotions_referral`
  - referred user
  - referrer user
  - code used
  - signup time
  - first order qualified
  - fraud flags
- `promotions_referral_reward`
  - pending reward
  - released reward
  - reversed reward

Keep existing business logic, but normalize the structure.

### 4. Move Reward Release To Order Lifecycle Events

Current gaps:

- referral commission is credited too early
- cashback reverse flow is weak
- return/cancel-safe incentive reversal is incomplete

Target order event flow:

- `ORDER_PLACED`
  - reserve coupon usage
  - reserve wallet/gift card usage
  - create pending cashback
  - create pending referral reward
- `ORDER_DELIVERED`
  - release cashback
  - release eligible referral reward
  - release earned points
- `ORDER_CANCELLED`
  - rollback coupon reservation if needed
  - restore wallet/gift card
  - reverse pending rewards
- `ORDER_RETURNED`
  - reverse released cashback if policy allows
  - reverse points earned from order
  - reverse referral bonus if based on qualified order

### 5. Finish Checkout Incentive Integration

The target checkout priority is correct:

1. coupon
2. reward points
3. wallet
4. gift card
5. tax/shipping
6. final payment

Current code does not fully wire coupon, reward, gift card, and usage tracking into a single checkout pipeline.

Current completion status:

- `CheckoutIncentiveService` can prepare a quote and allocate coupon, reward, and gift card values across split orders.
- Reward point redemption is applied during checkout.
- Coupon redemption is created for the primary order.
- Gift card usage can be applied per order.
- `OrderIncentiveUsage` exists but still needs consistent write/update coverage from the checkout flow.

Add:

- server-side incentive calculator service
- order-level incentive snapshot
- `promotions_order_incentive_usage` persistence for:
  - coupon id
  - coupon code
  - coupon discount
  - reward points used
  - reward discount
  - wallet used
  - wallet transaction id
  - gift card code
  - gift card used
  - gift card transaction id
  - cashback expected
  - referral bonus expected
  - quote hash or quote id
  - incentive status

Completion steps:

1. Treat checkout incentive calculation as a quote and persist a quote snapshot before order creation.
2. Revalidate the quote immediately before payment/order finalization.
3. Write one `promotions_order_incentive_usage` row for every created `SalesOrder`.
4. Allocate discounts by order total when checkout creates multiple vendor orders.
5. Store payment and incentive amounts separately so tax, shipping, COD, and vendor settlement can be audited.
6. On payment failure, cancel the quote and release any reservations.
7. On order cancellation or return, read `promotions_order_incentive_usage` and reverse only the incentives used by that order.

### 6. Expand Coupon Design

Current coupon model is too simple for enterprise promotion flow.

Add tables:

- `promotions_coupon_usage`
- `promotions_coupon_product`
- `promotions_coupon_category`

Recommended fields:

- max discount
- minimum order
- per-user usage limit
- global usage limit
- start/end date
- stackable flag
- new customer only flag
- first order only flag
- vendor or campaign scope

### 7. Expand Cashback Design

Current cashback policy supports a basic percentage model.

Add:

- `promotions_cashback_rule`
- `promotions_cashback_queue`
- release date / hold-until date
- return-period hold
- category/product/vendor targeting
- maximum reward per user/campaign/day

### 8. Finish Redemption History

Current redemption save flow is incomplete.

Current completion status:

- `RedemptionService` now creates `promotions_redemption` records for reward-point redemption.
- The model still stores mostly point-based details and does not yet cover all redemption sources cleanly.

The unified `promotions_redemption` table should store:

- redemption type
- source program
- source id
- order id
- amount or points
- status
- reversal reference
- created by
- customer id
- currency
- conversion rate
- ledger transaction id
- external payment/reference id
- reason/details
- created at
- completed at

Track all usage types in one place:

- coupon redemption
- reward redemption
- gift card redemption
- wallet redemption

Completion steps:

1. Rename the plan reference from generic `redemptions` to actual table `promotions_redemption`.
2. Extend `Redemptions` with `orderId`, `sourceProgram`, `sourceId`, `amount`, `currency`, `conversionRate`, `ledgerTransactionId`, and `reversalReference`.
3. Save redemption records for coupons, gift cards, wallet payments, reward checkout use, and cashout requests.
4. Use `PENDING` for reserved checkout usage, `SUCCESS` after order/payment confirmation, `REVERSED` after cancellation/return, and `FAILED` for rejected payment or failed payout.
5. Link every redemption to the related ledger row so finance can trace the balance movement.
6. Keep the old `CouponRedemption` table only as a compatibility table until `promotions_redemption` and `promotions_coupon_usage` are complete.

### 9. Add Real Notification Layer

Current notification/email pieces are incomplete or unused.

Target notification events:

- reward earned
- cashback released
- referral reward qualified
- gift card issued
- coupon applied
- cashout approved
- cashout rejected
- cashout paid

Use one notification service with channel handlers:

- email
- SMS
- push
- in-app notification

Completion steps:

1. Replace placeholder repository/model use with a `PromotionNotificationService`.
2. Create `promotions_notification_log` for event name, customer id, channel, recipient, payload summary, status, retry count, and sent time.
3. Publish notification events from services after successful transaction commit.
4. Keep templates separate from business services.
5. Add retry handling for failed SMS/email/push delivery.
6. Always create an in-app notification for customer-visible incentive changes, even when email or SMS is disabled.

Recommended event names:

- `PROMOTION_REWARD_EARNED`
- `PROMOTION_REWARD_REDEEMED`
- `PROMOTION_CASHBACK_PENDING`
- `PROMOTION_CASHBACK_RELEASED`
- `PROMOTION_CASHBACK_REVERSED`
- `PROMOTION_REFERRAL_QUALIFIED`
- `PROMOTION_CASHOUT_REQUESTED`
- `PROMOTION_CASHOUT_APPROVED`
- `PROMOTION_CASHOUT_REJECTED`
- `PROMOTION_CASHOUT_PAID`

### 10. Add Reporting And Fraud Controls

Current code has list views, but not enterprise reporting.

Add reporting services for:

- coupon ROI
- wallet liabilities
- reward liabilities
- cashback cost
- gift card outstanding balance
- referral conversion
- top referrers
- abuse signals

Add fraud checks for:

- self-referral
- same device/IP referral loops
- multiple account coupon abuse
- repeated return-after-cashback patterns
- abnormal wallet adjustment activity

Completion steps:

1. Create read-only reporting queries first, then dashboards.
2. Use daily summary tables for expensive metrics such as liabilities and campaign ROI.
3. Add `promotions_fraud_flag` with customer id, order id, source type, severity, status, reason, reviewed by, and reviewed at.
4. Block only high-confidence rules automatically.
5. Send medium-confidence rules to admin review and keep incentive rewards in `PENDING` until cleared.
6. Log all manual admin overrides in `promotions_audit_log`.

Minimum reports before production:

- current wallet money liability
- current reward point liability
- pending cashback liability
- outstanding gift card balance
- coupon usage by campaign
- referral reward issued by level
- cashout requested, approved, rejected, and paid
- reversed incentives by cancellation/return reason

## Existing Code To Retire Or Consolidate

These parts should be removed later or merged into a single production-safe flow:

- duplicate public/customer registration referral controllers
  - `RegisterController`
  - `RegisterCustomerController`
- duplicate reward dashboard/history flows where one clear customer flow is enough
- customer-facing manual wallet top-up endpoint unless backed by a real payment capture flow
- incomplete placeholder notification classes
  - `CustomerNotifications` repository stub
  - partially commented `EmailService`
- commented legacy code branches after replacement logic is verified

Do not delete them first.

First replace them with the new flow, then retire them.

## Recommended Database Direction

### Keep And Rename If Useful

- `promotions_coupon` stays as coupon master, or extend carefully if fields are compatible
- `promotions_coupon_redemption` can be replaced by `promotions_coupon_usage`
- `promotions_wallet` should be split into `promotions_wallet_account` and `promotions_reward_account`
- `promotions_wallet_transaction` should be split into `promotions_wallet_transaction` and `promotions_reward_transaction`
- `promotions_cashback_policy` can evolve into `promotions_cashback_rule`
- `promotions_cashback_transaction` stays as cashback ledger/history
- `promotions_gift_card` stays as gift card master
- `promotions_gift_card_transaction` stays as usage ledger/history
- `promotions_redemption` stays as unified redemption history after model cleanup

### Add New Tables

- `promotions_coupon_product`
- `promotions_coupon_category`
- `promotions_wallet_withdrawal`
- `promotions_referral_code`
- `promotions_referral_reward`
- `promotions_cashback_queue`
- `promotions_rule`
- `promotions_audit_log`
- `promotions_notification_log`
- `promotions_fraud_flag`

### Complete Existing Newer Tables

These tables or models already exist or are partially introduced and should be finished before adding parallel replacements:

- `promotions_order_incentive_usage`
  - add referral bonus expected
  - add linked ledger ids
  - add status
  - add quote/reference id
- `promotions_reward_account`
  - enforce one row per customer
  - store available, pending, redeemed, expired, and reversed totals if fast reporting is needed
- `promotions_reward_transaction`
  - add idempotency key
  - add balance before/after
  - add reversal transaction id
- `promotions_redemption`
  - add order/source/ledger references
  - support amount-based and point-based redemptions
- `promotions_audit_log`
  - require actor, action, entity type, entity id, before value, after value, and request reference

### Migration Sequence

1. Add new nullable columns and new tables.
2. Backfill reward accounts and reward transactions from historical mixed wallet rows.
3. Backfill redemption records from coupon, reward, gift card, wallet, and cashout histories.
4. Run reconciliation reports comparing old balances to new account balances.
5. Switch services to write both old and new tables for one release if production risk is high.
6. Switch reads to the new tables after reconciliation passes.
7. Stop writes to legacy mixed fields.
8. Remove or archive legacy columns/controllers only after a rollback window is complete.

### Rollback Rules

- Never delete historical incentive rows during the first migration release.
- Keep old read paths available until reconciliation passes.
- Make every migration script idempotent.
- Store migration batch id and migrated timestamp for copied rows.
- If balance reconciliation fails, pause checkout reward redemption and cashout, but keep normal order placement available.

## Recommended Refactor Order

### Phase 1

- split wallet money vs reward points
- create unified ledger rules
- complete redemption records
- remove customer manual top-up from live flow

### Phase 2

- complete checkout incentive orchestration
- add coupon restriction tables
- add order-level incentive usage tracking

### Phase 3

- redesign referral tables
- harden cashback release and reversal
- complete gift card checkout usage

### Phase 4

- add reporting dashboards
- add fraud engine
- add notification event handlers

### Phase 5

- remove or hide duplicate legacy controllers
- retire compatibility write paths
- lock down direct balance updates
- complete final schema cleanup
- document admin operating procedures

## Acceptance Checklist

The migration is complete only when all of these are true:

- checkout can apply coupon, reward points, wallet money, and gift card without mixing balances
- every incentive usage creates a ledger row and a redemption/history row
- every order has a `promotions_order_incentive_usage` snapshot when incentives are used
- order cancellation reverses reserved incentives
- order return reverses released incentives according to policy
- cashback stays pending until the configured release event/date
- referral reward stays pending until the configured qualification rule is met
- cashout debits reward points once and never debits again during payout completion
- admin can reconcile wallet, reward, cashback, gift card, coupon, referral, and cashout totals
- duplicate/self-referral and repeated abuse patterns create fraud flags
- all customer-visible incentive changes create notification records
- legacy mixed wallet/reward endpoints are removed or redirected to the new flows

## Verification Scenarios

Test these flows before production:

1. customer uses only a coupon at checkout
2. customer uses only reward points at checkout
3. customer uses only wallet money at checkout
4. customer uses only a gift card at checkout
5. customer combines coupon, reward points, wallet money, and gift card
6. checkout splits one cart into multiple vendor orders
7. payment fails after incentive reservation
8. order is cancelled before delivery
9. order is returned after cashback release
10. referred customer places a qualifying first order
11. referred customer order is cancelled before qualification
12. customer requests cashout, admin approves, then marks paid
13. admin rejects cashout and points are restored
14. coupon usage limit is reached globally and per user
15. fraud rule flags self-referral and keeps reward pending

## Practical Rule For Existing Code

Use this simple decision rule while refactoring:

- keep service logic that already matches the target domain
- change models where one table is carrying two different business meanings
- remove duplicate controllers and demo endpoints after the replacement flow is stable

## Final Recommendation

Yes, you need to change the existing code.

No, you should not remove everything.

Best path:

- preserve the current module as the starting point
- refactor it into separate money, points, promotion, referral, cashback, and redemption subdomains
- retire duplicate legacy controllers only after the new enterprise flow is live
