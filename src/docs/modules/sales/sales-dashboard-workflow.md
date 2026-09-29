# Enterprise Sales Dashboard Workflow

## Scope

Build one sales dashboard module for both marketplace administrators and vendors:

- Admin route: `/admin/sales/dashboard`
- Vendor route: `/vendor/sales/dashboard`
- AJAX data route: `/admin/sales/dashboard/data`
- Vendor AJAX data route: `/vendor/sales/dashboard/data`

## Current Implementation Status

| Area | Status | Notes |
| --- | --- | --- |
| Existing module audit | Done | Reused `com.ecommerce.app.module.sales.dashboard` instead of creating a parallel dashboard. |
| Admin dashboard route | Done | Uses `AdminSalesDashboardController`, `SalesDashboardService`, and aggregate repository. |
| Vendor dashboard route | Done | Uses the same service with enforced vendor scoping from active vendor context. |
| Date presets | Done | Today, yesterday, last 7 days, last 30 days, month to date, quarter to date, year to date, and custom range. |
| Backed filters | Done | Vendor, category, brand, product, customer, region, payment method, shipping carrier, order status, and currency control. |
| Unbacked filters | Tracked | Store, sales channel, and campaign are visible as disabled controls until fields are persisted on orders/events. |
| KPI cards | Done | 15 KPI cards with comparison, trend state, and mini sparkline support. |
| Revenue trend | Done | Chart.js line/area plus order volume bar. |
| Order trend | Done | Completed, cancelled, and returned trend. |
| Sales by category | Done | Server-side order item aggregate. |
| Sales by brand | Done | Server-side order item aggregate. |
| Sales by vendor | Done | Admin marketplace mode only. |
| Sales by channel | Partial | Website/manual proxy shown; full channel split requires order channel persistence. |
| Payment analytics | Done | Payment method amount, count, success, failed, pending, and success rate. |
| Order funnel | Done | All `OrderStatus` values are represented. |
| Sales target | Partial | UI gauge present; real configurable targets require target table/settings. |
| Geographic sales | Done | Uses shipping country/district/city from `ShippingAddress`. |
| Top products | Done | Product, SKU, category, brand, vendor, quantity, revenue, profit, stock. |
| Top vendors | Done | Admin marketplace mode table. |
| Top customers | Done | Customer, orders, period LTV, last purchase, reward points placeholder. |
| Shipping analytics | Partial | Shipment volume, delivered, in-transit, cost, carrier performance are backed; SLA delay and average delivery time need delivery timestamps. |
| Promotion analytics | Partial | Coupon usage, cashback, gift card sales are backed when promotion tables exist; campaign/referral breakdown needs campaign/source dimensions. |
| Inventory impact | Done | Low stock, out of stock, fast moving, and overstock based on product stock fields. |
| Customer analytics | Done | New, returning, active, LTV proxy, purchase frequency, repeat rate. |
| Return analytics | Partial | Return rate, refund amount, cancellation rate, product return count backed; reason taxonomy needs return reason fields. |
| Live activity feed | Done | Recent order activity with auto-refresh. |
| Alerts panel | Done | Inventory, revenue growth, cancellation, and return signals. |
| Dark mode | Done | Local storage backed theme toggle. |
| Loading skeletons | Done | CSS skeletons shown before Chart.js initialization. |
| Print/PDF | Done | Print-friendly CSS and browser PDF support. |
| CSV export | Done | Exports KPI summary client-side. |
| Performance | Done | Repository uses aggregate JPQL and result limits; SQL index note updated. |

## Remaining Schema/Data Work

1. Add an order sales channel field or event source table for Website, Mobile App, POS, Marketplace, Social Commerce, and Manual Orders.
2. Add campaign attribution on order/incentive usage for true campaign revenue filtering.
3. Add configurable sales targets by vendor, store, currency, and period.
4. Add shipment delivered-at/SLA fields for average delivery time and delayed shipment metrics.
5. Add normalized return reason taxonomy for top return reasons and product return trend analysis.
6. Add store entity/order store relationship if single-codebase multi-store filtering is required.
7. Add reward point redemption aggregate to expose redeemed points by dashboard period.

## Verification Checklist

- Source paths were updated in the existing dashboard module.
- Admin and vendor dashboards share the same service and DTO contract.
- Vendor requests enforce the active vendor id server-side.
- No write transaction or schema mutation is introduced by the dashboard.
- PostgreSQL index recommendations are documented in `main/resources/db/sales_dashboard_indexes.sql`.
