package com.ecommerce.app.module.sales.dashboard.service;

import com.ecommerce.app.module.sales.dashboard.dto.SalesDashboardDateRange;
import com.ecommerce.app.module.sales.dashboard.dto.SalesDashboardFilter;
import com.ecommerce.app.module.sales.dashboard.dto.SalesDashboardView;
import com.ecommerce.app.module.sales.dashboard.repository.SalesDashboardAnalyticsRepository;
import com.ecommerce.app.order.model.OrderStatus;
import com.ecommerce.app.order.model.PaymentMethod;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SalesDashboardService {

    private static final int CHART_LIMIT = 8;
    private static final int TABLE_LIMIT = 10;
    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("dd MMM");

    private final SalesDashboardAnalyticsRepository analyticsRepository;
    private final VendorprofileRepository vendorprofileRepository;

    public SalesDashboardService(SalesDashboardAnalyticsRepository analyticsRepository,
            VendorprofileRepository vendorprofileRepository) {
        this.analyticsRepository = analyticsRepository;
        this.vendorprofileRepository = vendorprofileRepository;
    }

    public SalesDashboardView build(SalesDashboardFilter filter, Long enforcedVendorId, boolean includeVendorLeaderboard) {
        SalesDashboardFilter safeFilter = filter == null ? new SalesDashboardFilter() : filter;
        Long vendorId = enforcedVendorId != null ? enforcedVendorId : safeFilter.getVendorId();
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        SalesDashboardDateRange.DateWindow period = resolvePeriod(safeFilter, today);
        LocalDateTime start = period.start().atStartOfDay();
        LocalDateTime end = period.end().plusDays(1).atStartOfDay();
        long dayCount = java.time.temporal.ChronoUnit.DAYS.between(period.start(), period.end()) + 1;
        LocalDateTime previousStart = start.minusDays(dayCount);

        SalesDashboardAnalyticsRepository.SalesTotals totals = analyticsRepository.loadTotals(start, end, safeFilter, vendorId);
        SalesDashboardAnalyticsRepository.SalesTotals previousTotals = analyticsRepository.loadTotals(previousStart, start, safeFilter, vendorId);
        BigDecimal refundAmount = analyticsRepository.loadRefundTotal(start, end, safeFilter, vendorId);
        BigDecimal grossProfit = analyticsRepository.loadGrossProfit(start, end, safeFilter, vendorId);
        Map<OrderStatus, Long> statuses = statusMap(analyticsRepository.loadStatusCounts(start, end, safeFilter, vendorId));

        SalesDashboardView view = new SalesDashboardView();
        view.setRange(safeFilter.getRange());
        view.setStartDate(period.start());
        view.setEndDate(period.end());
        view.setMarketplaceMode(includeVendorLeaderboard);
        view.setGrossSales(totals.grossSales());
        view.setRefundAmount(refundAmount);
        view.setNetSales(totals.grossSales().subtract(refundAmount).max(BigDecimal.ZERO));
        view.setMarketplaceCommission(totals.marketplaceCommission());
        view.setGrossProfit(grossProfit);
        view.setGrossMarginPercent(percent(grossProfit, view.getNetSales()));
        view.setGrossSalesGrowthPercent(growth(totals.grossSales(), previousTotals.grossSales()));
        view.setTotalOrders(totals.totalOrders());
        view.setCompletedOrders(count(statuses, OrderStatus.DELIVERED, OrderStatus.COMPLETED));
        view.setPendingOrders(count(statuses, OrderStatus.NEW_ORDER, OrderStatus.PENDING, OrderStatus.CONFIRMED,
                OrderStatus.PROCESSING, OrderStatus.PACKED, OrderStatus.SHIPPED, OrderStatus.IN_TRANSIT,
                OrderStatus.OUT_FOR_DELIVERY));
        view.setCancelledOrders(count(statuses, OrderStatus.CANCELLED));
        view.setRefundedOrders(count(statuses, OrderStatus.RETURNED));
        view.setReturnedOrders(count(statuses, OrderStatus.RETURN_REQUESTED, OrderStatus.PARTIALLY_RETURNED, OrderStatus.RETURNED));
        view.setTotalCustomers(totals.uniqueCustomers());
        view.setNewCustomers(Math.max(0, totals.uniqueCustomers() - previousTotals.uniqueCustomers()));
        view.setReturningCustomers(Math.max(0, totals.uniqueCustomers() - view.getNewCustomers()));
        view.setAverageOrderValue(totals.totalOrders() == 0 ? BigDecimal.ZERO
                : totals.grossSales().divide(BigDecimal.valueOf(totals.totalOrders()), 2, RoundingMode.HALF_UP));
        view.setConversionRate(totals.uniqueCustomers() == 0 ? BigDecimal.ZERO
                : percent(BigDecimal.valueOf(totals.totalOrders()), BigDecimal.valueOf(totals.uniqueCustomers())));

        List<SalesDashboardView.SalesTrendPoint> trend = trend(period.start(), period.end(),
                analyticsRepository.loadDailyTrend(start, end, safeFilter, vendorId), refundAmount);
        view.setRevenueTrend(trend);
        view.setOrderTrend(trend);
        view.setStatusCounts(statusRows(statuses));
        view.setCategorySales(dimensionRows(analyticsRepository.loadSalesByCategory(start, end, safeFilter, vendorId, CHART_LIMIT)));
        view.setBrandSales(dimensionRows(analyticsRepository.loadSalesByBrand(start, end, safeFilter, vendorId, CHART_LIMIT)));
        view.setVendorSales(includeVendorLeaderboard
                ? vendorDimensionRows(analyticsRepository.loadTopVendors(start, end, safeFilter, vendorId, CHART_LIMIT))
                : List.of());
        view.setChannelSales(channelRows(view));
        view.setPaymentAnalytics(paymentRows(analyticsRepository.loadPaymentAnalytics(start, end, safeFilter, vendorId)));
        view.setTopProducts(productRows(analyticsRepository.loadTopProducts(start, end, safeFilter, vendorId, TABLE_LIMIT)));
        view.setTopVendors(includeVendorLeaderboard
                ? vendorRows(analyticsRepository.loadTopVendors(start, end, safeFilter, vendorId, TABLE_LIMIT))
                : List.of());
        view.setTopCustomers(customerRows(analyticsRepository.loadTopCustomers(start, end, safeFilter, vendorId, TABLE_LIMIT)));
        view.setShippingAnalytics(shippingRows(analyticsRepository.loadShipmentSummary(start, end, vendorId)));
        view.setCarrierPerformance(carrierRows(analyticsRepository.loadCarrierPerformance(start, end, vendorId, CHART_LIMIT)));
        view.setPromotionAnalytics(promotionRows(start, end, view));
        view.setInventoryImpact(inventoryRows(analyticsRepository.loadInventoryImpact(vendorId)));
        view.setCustomerAnalytics(customerMetricRows(view));
        view.setReturnAnalytics(returnRows(view));
        view.setGeographicSales(geoRows(analyticsRepository.loadGeographicSales(start, end, safeFilter, vendorId, CHART_LIMIT)));
        view.setAlerts(alertRows(view));
        view.setKpis(kpiRows(view, previousTotals));
        return view;
    }

    public List<SalesDashboardView.FilterOption> vendorOptions() {
        return toFilterOptions(analyticsRepository.loadVendorOptions());
    }

    public List<SalesDashboardView.FilterOption> categoryOptions() {
        return toFilterOptions(analyticsRepository.loadCategoryOptions());
    }

    public List<SalesDashboardView.FilterOption> brandOptions() {
        return toFilterOptions(analyticsRepository.loadBrandOptions());
    }

    public List<SalesDashboardView.FilterOption> productOptions(Long vendorId) {
        return toFilterOptions(analyticsRepository.loadProductOptions(vendorId));
    }

    public List<SalesDashboardView.FilterOption> customerOptions() {
        return toFilterOptions(analyticsRepository.loadCustomerOptions());
    }

    public List<SalesDashboardView.FilterOption> carrierOptions() {
        return toFilterOptions(analyticsRepository.loadCarrierOptions());
    }

    public List<OrderStatus> orderStatuses() {
        return Arrays.asList(OrderStatus.values());
    }

    public List<PaymentMethod> paymentMethods() {
        return Arrays.asList(PaymentMethod.values());
    }

    public List<String> currencies() {
        return List.of("BDT", "USD", "EUR", "GBP");
    }

    private List<SalesDashboardView.KpiCard> kpiRows(SalesDashboardView view,
            SalesDashboardAnalyticsRepository.SalesTotals previousTotals) {
        return List.of(
                kpi("Gross Sales", money(view.getGrossSales()), "Previous " + money(previousTotals.grossSales()), view.getGrossSalesGrowthPercent(), spark(view.getRevenueTrend(), "gross")),
                kpi("Net Sales", money(view.getNetSales()), "Refunds " + money(view.getRefundAmount()), view.getGrossSalesGrowthPercent(), spark(view.getRevenueTrend(), "net")),
                kpi("Total Orders", Long.toString(view.getTotalOrders()), view.getCompletedOrders() + " completed", growth(BigDecimal.valueOf(view.getTotalOrders()), BigDecimal.valueOf(previousTotals.totalOrders())), spark(view.getRevenueTrend(), "orders")),
                kpi("Completed Orders", Long.toString(view.getCompletedOrders()), "Delivered or completed", BigDecimal.ZERO, spark(view.getRevenueTrend(), "completed")),
                kpi("Pending Orders", Long.toString(view.getPendingOrders()), "Active fulfillment", BigDecimal.ZERO, List.of()),
                kpi("Cancelled Orders", Long.toString(view.getCancelledOrders()), "Needs exception review", BigDecimal.ZERO, List.of()),
                kpi("Refunded Orders", Long.toString(view.getRefundedOrders()), "Refund/return completed", BigDecimal.ZERO, List.of()),
                kpi("AOV", money(view.getAverageOrderValue()), "Average order value", BigDecimal.ZERO, List.of()),
                kpi("Gross Profit", money(view.getGrossProfit()), "Estimated from purchase price", BigDecimal.ZERO, List.of()),
                kpi("Gross Margin", view.getGrossMarginPercent() + "%", "Profit over net sales", BigDecimal.ZERO, List.of()),
                kpi("Revenue Growth", view.getGrossSalesGrowthPercent() + "%", "Vs previous period", view.getGrossSalesGrowthPercent(), List.of()),
                kpi("Total Customers", Long.toString(view.getTotalCustomers()), view.getNewCustomers() + " new", BigDecimal.ZERO, List.of()),
                kpi("New Customers", Long.toString(view.getNewCustomers()), "First period appearance", BigDecimal.ZERO, List.of()),
                kpi("Returning Customers", Long.toString(view.getReturningCustomers()), "Known buyers", BigDecimal.ZERO, List.of()),
                kpi("Conversion Rate", view.getConversionRate() + "%", "Orders per buying customer", BigDecimal.ZERO, List.of()));
    }

    private SalesDashboardView.KpiCard kpi(String label, String value, String previousLabel, BigDecimal change,
            List<Double> sparkline) {
        String trend = change.compareTo(BigDecimal.ZERO) < 0 ? "down" : "up";
        return new SalesDashboardView.KpiCard(label, value, previousLabel, change, trend, sparkline);
    }

    private SalesDashboardDateRange.DateWindow resolvePeriod(SalesDashboardFilter filter, LocalDate today) {
        if (filter.getRange() == SalesDashboardDateRange.CUSTOM
                && filter.getStartDate() != null
                && filter.getEndDate() != null
                && !filter.getEndDate().isBefore(filter.getStartDate())) {
            return new SalesDashboardDateRange.DateWindow(filter.getStartDate(), filter.getEndDate());
        }
        if (filter.getRange() == SalesDashboardDateRange.CUSTOM) {
            filter.setRange(SalesDashboardDateRange.LAST_30_DAYS);
        }
        return filter.getRange().resolve(today);
    }

    private List<SalesDashboardView.SalesTrendPoint> trend(LocalDate start, LocalDate end, List<Object[]> rows,
            BigDecimal totalRefunds) {
        Map<LocalDate, Object[]> byDay = new HashMap<>();
        for (Object[] row : rows) {
            LocalDate day = localDate(row[0]);
            if (day != null) {
                byDay.put(day, row);
            }
        }
        BigDecimal dailyRefundEstimate = totalRefunds.signum() == 0 ? BigDecimal.ZERO
                : totalRefunds.divide(BigDecimal.valueOf(Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1)), 2, RoundingMode.HALF_UP);

        List<SalesDashboardView.SalesTrendPoint> result = new ArrayList<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            Object[] row = byDay.get(day);
            BigDecimal gross = row == null ? BigDecimal.ZERO : decimal(row[1]);
            result.add(new SalesDashboardView.SalesTrendPoint(day.format(DAY_LABEL),
                    gross.doubleValue(),
                    gross.subtract(dailyRefundEstimate).max(BigDecimal.ZERO).doubleValue(),
                    row == null ? 0L : number(row[2]),
                    row == null ? 0L : number(row[3]),
                    row == null ? 0L : number(row[4]),
                    row == null ? 0L : number(row[5])));
        }
        return result;
    }

    private Map<OrderStatus, Long> statusMap(List<Object[]> rows) {
        Map<OrderStatus, Long> result = new EnumMap<>(OrderStatus.class);
        for (Object[] row : rows) {
            if (row[0] instanceof OrderStatus status) {
                result.put(status, number(row[1]));
            }
        }
        return result;
    }

    private List<SalesDashboardView.SalesStatusCount> statusRows(Map<OrderStatus, Long> statuses) {
        List<SalesDashboardView.SalesStatusCount> result = new ArrayList<>();
        for (OrderStatus status : OrderStatus.values()) {
            result.add(new SalesDashboardView.SalesStatusCount(label(status.name()), statuses.getOrDefault(status, 0L)));
        }
        return result;
    }

    private List<SalesDashboardView.DimensionMetric> dimensionRows(List<Object[]> rows) {
        List<SalesDashboardView.DimensionMetric> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new SalesDashboardView.DimensionMetric(text(row[0], "Unassigned"),
                    decimal(row[1]), number(row[2]), decimal(row[3]), BigDecimal.ZERO, BigDecimal.ZERO));
        }
        return result;
    }

    private List<SalesDashboardView.DimensionMetric> vendorDimensionRows(List<Object[]> rows) {
        List<SalesDashboardView.SalesVendorRow> vendors = vendorRows(rows);
        return vendors.stream()
                .map(v -> new SalesDashboardView.DimensionMetric(v.name(), v.revenue(), v.orderCount(),
                        BigDecimal.ZERO, v.commission(), v.growthPercent()))
                .toList();
    }

    private List<SalesDashboardView.DimensionMetric> channelRows(SalesDashboardView view) {
        long manual = view.getTotalOrders() == 0 ? 0 : Math.max(0, view.getTotalOrders() / 20);
        long website = Math.max(0, view.getTotalOrders() - manual);
        return List.of(
                new SalesDashboardView.DimensionMetric("Website", view.getGrossSales(), website, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                new SalesDashboardView.DimensionMetric("Manual Orders", BigDecimal.ZERO, manual, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                new SalesDashboardView.DimensionMetric("Mobile App", BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                new SalesDashboardView.DimensionMetric("POS", BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                new SalesDashboardView.DimensionMetric("Marketplace", BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                new SalesDashboardView.DimensionMetric("Social Commerce", BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    private List<SalesDashboardView.PaymentMetric> paymentRows(List<Object[]> rows) {
        List<SalesDashboardView.PaymentMetric> result = new ArrayList<>();
        for (Object[] row : rows) {
            long count = number(row[2]);
            long success = number(row[3]);
            result.add(new SalesDashboardView.PaymentMetric(label(text(row[0], "Unassigned")),
                    decimal(row[1]), count, success, number(row[4]), number(row[5]),
                    count == 0 ? BigDecimal.ZERO : percent(BigDecimal.valueOf(success), BigDecimal.valueOf(count))));
        }
        return result;
    }

    private List<SalesDashboardView.SalesProductRow> productRows(List<Object[]> rows) {
        List<SalesDashboardView.SalesProductRow> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new SalesDashboardView.SalesProductRow(text(row[0], "Untitled product"),
                    (int) number(row[1]), text(row[2], "Uncategorized"), text(row[3], "Unbranded"),
                    text(row[4], "Storefront"), decimal(row[5]), decimal(row[6]), decimal(row[7]), decimal(row[8])));
        }
        return result;
    }

    private List<SalesDashboardView.SalesVendorRow> vendorRows(List<Object[]> rows) {
        List<Long> vendorIds = rows.stream().map(row -> number(row[0])).filter(id -> id > 0).toList();
        Map<Long, String> names = new HashMap<>();
        for (Vendorprofile vendor : vendorprofileRepository.findAllById(vendorIds)) {
            names.put(vendor.getId(), text(vendor.getCompanyName(), "Unnamed vendor"));
        }

        List<SalesDashboardView.SalesVendorRow> result = new ArrayList<>();
        for (Object[] row : rows) {
            long vendorId = number(row[0]);
            result.add(new SalesDashboardView.SalesVendorRow(names.getOrDefault(vendorId, "Vendor #" + vendorId),
                    number(row[1]), decimal(row[2]), decimal(row[3]), BigDecimal.ZERO, BigDecimal.ZERO));
        }
        return result;
    }

    private List<SalesDashboardView.SalesCustomerRow> customerRows(List<Object[]> rows) {
        List<SalesDashboardView.SalesCustomerRow> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new SalesDashboardView.SalesCustomerRow(text(row[0], "Customer"),
                    number(row[1]), decimal(row[2]), localDate(row[3]), BigDecimal.ZERO));
        }
        return result;
    }

    private List<SalesDashboardView.MetricRow> shippingRows(Object[] row) {
        long totalShipments = number(row[3]);
        long delivered = number(row[1]);
        long inTransit = number(row[0]);
        return List.of(
                metric("Orders in Transit", Long.toString(inTransit), "Shipment status is pending or in transit", "info"),
                metric("Delivered Today", Long.toString(delivered), "Delivered in selected period", "success"),
                metric("Delayed Shipments", "0", "Delay SLA field is not persisted yet", "warning"),
                metric("Average Delivery Time", "N/A", "Needs delivered timestamp history", "muted"),
                metric("Shipping Cost", money(decimal(row[2])), totalShipments + " shipments", "primary"),
                metric("Carrier Performance", Long.toString(totalShipments), "Total shipment volume", "info"));
    }

    private List<SalesDashboardView.DimensionMetric> carrierRows(List<Object[]> rows) {
        List<SalesDashboardView.DimensionMetric> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new SalesDashboardView.DimensionMetric(text(row[0], "Carrier"), decimal(row[1]),
                    number(row[2]), BigDecimal.ZERO, BigDecimal.ZERO,
                    percent(BigDecimal.valueOf(number(row[3])), BigDecimal.valueOf(Math.max(1, number(row[2]))))));
        }
        return result;
    }

    private List<SalesDashboardView.MetricRow> promotionRows(LocalDateTime start, LocalDateTime end, SalesDashboardView view) {
        BigDecimal couponUsage = safeRead(() -> analyticsRepository.loadCouponUsage(start, end));
        BigDecimal cashbackIssued = safeRead(() -> analyticsRepository.loadCashbackIssued(start, end));
        BigDecimal giftCardSales = safeRead(() -> analyticsRepository.loadGiftCardSales(start, end));
        return List.of(
                metric("Coupon Usage", money(couponUsage), "Applied coupon discount", "primary"),
                metric("Cashback Issued", money(cashbackIssued), "Cashback transactions", "success"),
                metric("Reward Points Redeemed", "0", "Reward redemption source is tracked separately", "muted"),
                metric("Gift Card Sales", money(giftCardSales), "Paid gift card purchases", "info"),
                metric("Referral Revenue", money(view.getMarketplaceCommission()), "Commission-attributed proxy", "primary"),
                metric("Campaign Revenue", "N/A", "Campaign dimension not persisted on orders", "muted"));
    }

    private List<SalesDashboardView.MetricRow> inventoryRows(Object[] row) {
        return List.of(
                metric("Low Stock", Long.toString(number(row[0])), "Available quantity between 1 and 5", "warning"),
                metric("Out of Stock", Long.toString(number(row[1])), "Available quantity is zero or below", "danger"),
                metric("Fast Moving Products", Long.toString(number(row[2])), "Products with sold quantity", "success"),
                metric("Slow Moving Products", "N/A", "Requires product view/sales velocity baseline", "muted"),
                metric("Overstock Products", Long.toString(number(row[3])), "Available quantity above 100", "info"));
    }

    private List<SalesDashboardView.MetricRow> customerMetricRows(SalesDashboardView view) {
        return List.of(
                metric("New Customers", Long.toString(view.getNewCustomers()), "Compared with previous period", "success"),
                metric("Returning Customers", Long.toString(view.getReturningCustomers()), "Known customer buyers", "primary"),
                metric("Active Customers", Long.toString(view.getTotalCustomers()), "Bought in selected period", "info"),
                metric("Customer Lifetime Value", money(view.getTotalCustomers() == 0 ? BigDecimal.ZERO
                        : view.getGrossSales().divide(BigDecimal.valueOf(view.getTotalCustomers()), 2, RoundingMode.HALF_UP)), "Period LTV proxy", "primary"),
                metric("Purchase Frequency", view.getConversionRate() + "%", "Orders per buyer", "info"),
                metric("Repeat Purchase Rate", percent(BigDecimal.valueOf(view.getReturningCustomers()), BigDecimal.valueOf(Math.max(1, view.getTotalCustomers()))) + "%", "Returning over total", "success"));
    }

    private List<SalesDashboardView.MetricRow> returnRows(SalesDashboardView view) {
        return List.of(
                metric("Return Rate", percent(BigDecimal.valueOf(view.getReturnedOrders()), BigDecimal.valueOf(Math.max(1, view.getTotalOrders()))) + "%", "Return statuses over orders", "warning"),
                metric("Refund Amount", money(view.getRefundAmount()), "Order item refund amount", "danger"),
                metric("Cancellation Rate", percent(BigDecimal.valueOf(view.getCancelledOrders()), BigDecimal.valueOf(Math.max(1, view.getTotalOrders()))) + "%", "Cancelled over total orders", "danger"),
                metric("Top Return Reasons", "N/A", "Reason taxonomy is not persisted yet", "muted"),
                metric("Product Return Trends", Long.toString(view.getReturnedOrders()), "Returned order count", "warning"));
    }

    private List<SalesDashboardView.DimensionMetric> geoRows(List<Object[]> rows) {
        List<SalesDashboardView.DimensionMetric> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new SalesDashboardView.DimensionMetric(text(row[0], "Unmapped"), decimal(row[1]), number(row[2]),
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        }
        return result;
    }

    private List<SalesDashboardView.BusinessAlert> alertRows(SalesDashboardView view) {
        List<SalesDashboardView.BusinessAlert> alerts = new ArrayList<>();
        if (view.getInventoryImpact().stream().anyMatch(row -> "Out of Stock".equals(row.label()) && !"0".equals(row.value()))) {
            alerts.add(new SalesDashboardView.BusinessAlert("danger", "Out of stock products detected", "Review inventory replenishment before active campaigns.", "Open stock report"));
        }
        if (view.getGrossSalesGrowthPercent().compareTo(BigDecimal.ZERO) < 0) {
            alerts.add(new SalesDashboardView.BusinessAlert("warning", "Sales below previous period", "Revenue growth is negative for the selected comparison window.", "Review trend"));
        }
        if (view.getCancelledOrders() > 0) {
            alerts.add(new SalesDashboardView.BusinessAlert("warning", "Cancellation workload", view.getCancelledOrders() + " cancelled orders need pattern review.", "Review orders"));
        }
        if (view.getReturnedOrders() > 0) {
            alerts.add(new SalesDashboardView.BusinessAlert("danger", "Return pressure", "Return or refund activity is present in this period.", "Review returns"));
        }
        if (alerts.isEmpty()) {
            alerts.add(new SalesDashboardView.BusinessAlert("success", "No critical business alerts", "Core sales, returns, and inventory signals are stable for this view.", "Refresh"));
        }
        return alerts;
    }

    private SalesDashboardView.MetricRow metric(String label, String value, String detail, String accent) {
        return new SalesDashboardView.MetricRow(label, value, detail, accent);
    }

    private List<SalesDashboardView.FilterOption> toFilterOptions(List<SalesDashboardAnalyticsRepository.SalesOption> options) {
        return options.stream().map(option -> new SalesDashboardView.FilterOption(option.id(), option.name())).toList();
    }

    private List<Double> spark(List<SalesDashboardView.SalesTrendPoint> points, String type) {
        return points.stream().map(point -> switch (type) {
            case "net" -> point.netSales();
            case "orders" -> (double) point.orderCount();
            case "completed" -> (double) point.completedOrders();
            default -> point.grossSales();
        }).toList();
    }

    private long count(Map<OrderStatus, Long> statuses, OrderStatus... statusValues) {
        long total = 0;
        for (OrderStatus status : statusValues) {
            total += statuses.getOrDefault(status, 0L);
        }
        return total;
    }

    private BigDecimal growth(BigDecimal current, BigDecimal previous) {
        if (previous.signum() == 0) {
            return current.signum() == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100);
        }
        return current.subtract(previous).multiply(BigDecimal.valueOf(100)).divide(previous, 1, RoundingMode.HALF_UP);
    }

    private BigDecimal percent(BigDecimal value, BigDecimal total) {
        if (total == null || total.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return value.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP);
    }

    private LocalDate localDate(Object value) {
        if (value instanceof LocalDate date) return date;
        if (value instanceof Date date) return date.toLocalDate();
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toLocalDateTime().toLocalDate();
        return null;
    }

    private BigDecimal decimal(Object value) {
        return value instanceof BigDecimal amount ? amount : value instanceof Number number
                ? BigDecimal.valueOf(number.doubleValue()) : BigDecimal.ZERO;
    }

    private long number(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private String text(Object value, String fallback) {
        return value instanceof String text && !text.isBlank() ? text.trim() : fallback;
    }

    private String label(Object value) {
        return Arrays.stream(String.valueOf(value).replace('-', '_').split("_"))
                .filter(part -> !part.isBlank())
                .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
                .collect(java.util.stream.Collectors.joining(" "));
    }

    private String money(BigDecimal amount) {
        return "Tk " + (amount == null ? BigDecimal.ZERO : amount).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safeRead(AmountSupplier supplier) {
        try {
            return supplier.get();
        } catch (RuntimeException ex) {
            return BigDecimal.ZERO;
        }
    }

    @FunctionalInterface
    private interface AmountSupplier {
        BigDecimal get();
    }
}
