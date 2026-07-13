package com.ecommerce.app.module.sales.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesDashboardView {

    private SalesDashboardDateRange range;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal grossSales = BigDecimal.ZERO;
    private BigDecimal netSales = BigDecimal.ZERO;
    private BigDecimal refundAmount = BigDecimal.ZERO;
    private BigDecimal averageOrderValue = BigDecimal.ZERO;
    private BigDecimal marketplaceCommission = BigDecimal.ZERO;
    private BigDecimal grossProfit = BigDecimal.ZERO;
    private BigDecimal grossMarginPercent = BigDecimal.ZERO;
    private BigDecimal grossSalesGrowthPercent = BigDecimal.ZERO;
    private BigDecimal conversionRate = BigDecimal.ZERO;
    private long totalOrders;
    private long completedOrders;
    private long pendingOrders;
    private long cancelledOrders;
    private long refundedOrders;
    private long returnedOrders;
    private long totalCustomers;
    private long newCustomers;
    private long returningCustomers;
    private boolean marketplaceMode;
    private List<KpiCard> kpis = new ArrayList<>();
    private List<SalesTrendPoint> revenueTrend = new ArrayList<>();
    private List<SalesTrendPoint> orderTrend = new ArrayList<>();
    private List<SalesStatusCount> statusCounts = new ArrayList<>();
    private List<DimensionMetric> categorySales = new ArrayList<>();
    private List<DimensionMetric> brandSales = new ArrayList<>();
    private List<DimensionMetric> vendorSales = new ArrayList<>();
    private List<DimensionMetric> channelSales = new ArrayList<>();
    private List<PaymentMetric> paymentAnalytics = new ArrayList<>();
    private List<SalesProductRow> topProducts = new ArrayList<>();
    private List<SalesVendorRow> topVendors = new ArrayList<>();
    private List<SalesCustomerRow> topCustomers = new ArrayList<>();
    private List<MetricRow> shippingAnalytics = new ArrayList<>();
    private List<DimensionMetric> carrierPerformance = new ArrayList<>();
    private List<MetricRow> promotionAnalytics = new ArrayList<>();
    private List<MetricRow> inventoryImpact = new ArrayList<>();
    private List<MetricRow> customerAnalytics = new ArrayList<>();
    private List<MetricRow> returnAnalytics = new ArrayList<>();
    private List<DimensionMetric> geographicSales = new ArrayList<>();
    private List<ActivityEvent> liveActivity = new ArrayList<>();
    private List<BusinessAlert> alerts = new ArrayList<>();

    public SalesDashboardDateRange getRange() { return range; }
    public void setRange(SalesDashboardDateRange range) { this.range = range; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getGrossSales() { return grossSales; }
    public void setGrossSales(BigDecimal grossSales) { this.grossSales = defaultAmount(grossSales); }
    public BigDecimal getNetSales() { return netSales; }
    public void setNetSales(BigDecimal netSales) { this.netSales = defaultAmount(netSales); }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = defaultAmount(refundAmount); }
    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = defaultAmount(averageOrderValue); }
    public BigDecimal getMarketplaceCommission() { return marketplaceCommission; }
    public void setMarketplaceCommission(BigDecimal marketplaceCommission) { this.marketplaceCommission = defaultAmount(marketplaceCommission); }
    public BigDecimal getGrossProfit() { return grossProfit; }
    public void setGrossProfit(BigDecimal grossProfit) { this.grossProfit = defaultAmount(grossProfit); }
    public BigDecimal getGrossMarginPercent() { return grossMarginPercent; }
    public void setGrossMarginPercent(BigDecimal grossMarginPercent) { this.grossMarginPercent = defaultAmount(grossMarginPercent); }
    public BigDecimal getGrossSalesGrowthPercent() { return grossSalesGrowthPercent; }
    public void setGrossSalesGrowthPercent(BigDecimal grossSalesGrowthPercent) { this.grossSalesGrowthPercent = defaultAmount(grossSalesGrowthPercent); }
    public BigDecimal getConversionRate() { return conversionRate; }
    public void setConversionRate(BigDecimal conversionRate) { this.conversionRate = defaultAmount(conversionRate); }
    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
    public long getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }
    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }
    public long getCancelledOrders() { return cancelledOrders; }
    public void setCancelledOrders(long cancelledOrders) { this.cancelledOrders = cancelledOrders; }
    public long getRefundedOrders() { return refundedOrders; }
    public void setRefundedOrders(long refundedOrders) { this.refundedOrders = refundedOrders; }
    public long getReturnedOrders() { return returnedOrders; }
    public void setReturnedOrders(long returnedOrders) { this.returnedOrders = returnedOrders; }
    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }
    public long getUniqueCustomers() { return totalCustomers; }
    public long getNewCustomers() { return newCustomers; }
    public void setNewCustomers(long newCustomers) { this.newCustomers = newCustomers; }
    public long getReturningCustomers() { return returningCustomers; }
    public void setReturningCustomers(long returningCustomers) { this.returningCustomers = returningCustomers; }
    public boolean isMarketplaceMode() { return marketplaceMode; }
    public void setMarketplaceMode(boolean marketplaceMode) { this.marketplaceMode = marketplaceMode; }
    public List<KpiCard> getKpis() { return kpis; }
    public void setKpis(List<KpiCard> kpis) { this.kpis = safeList(kpis); }
    public List<SalesTrendPoint> getRevenueTrend() { return revenueTrend; }
    public void setRevenueTrend(List<SalesTrendPoint> revenueTrend) { this.revenueTrend = safeList(revenueTrend); }
    public List<SalesTrendPoint> getOrderTrend() { return orderTrend; }
    public void setOrderTrend(List<SalesTrendPoint> orderTrend) { this.orderTrend = safeList(orderTrend); }
    public List<SalesStatusCount> getStatusCounts() { return statusCounts; }
    public void setStatusCounts(List<SalesStatusCount> statusCounts) { this.statusCounts = safeList(statusCounts); }
    public List<DimensionMetric> getCategorySales() { return categorySales; }
    public void setCategorySales(List<DimensionMetric> categorySales) { this.categorySales = safeList(categorySales); }
    public List<DimensionMetric> getBrandSales() { return brandSales; }
    public void setBrandSales(List<DimensionMetric> brandSales) { this.brandSales = safeList(brandSales); }
    public List<DimensionMetric> getVendorSales() { return vendorSales; }
    public void setVendorSales(List<DimensionMetric> vendorSales) { this.vendorSales = safeList(vendorSales); }
    public List<DimensionMetric> getChannelSales() { return channelSales; }
    public void setChannelSales(List<DimensionMetric> channelSales) { this.channelSales = safeList(channelSales); }
    public List<PaymentMetric> getPaymentAnalytics() { return paymentAnalytics; }
    public void setPaymentAnalytics(List<PaymentMetric> paymentAnalytics) { this.paymentAnalytics = safeList(paymentAnalytics); }
    public List<SalesProductRow> getTopProducts() { return topProducts; }
    public void setTopProducts(List<SalesProductRow> topProducts) { this.topProducts = safeList(topProducts); }
    public List<SalesVendorRow> getTopVendors() { return topVendors; }
    public void setTopVendors(List<SalesVendorRow> topVendors) { this.topVendors = safeList(topVendors); }
    public List<SalesCustomerRow> getTopCustomers() { return topCustomers; }
    public void setTopCustomers(List<SalesCustomerRow> topCustomers) { this.topCustomers = safeList(topCustomers); }
    public List<MetricRow> getShippingAnalytics() { return shippingAnalytics; }
    public void setShippingAnalytics(List<MetricRow> shippingAnalytics) { this.shippingAnalytics = safeList(shippingAnalytics); }
    public List<DimensionMetric> getCarrierPerformance() { return carrierPerformance; }
    public void setCarrierPerformance(List<DimensionMetric> carrierPerformance) { this.carrierPerformance = safeList(carrierPerformance); }
    public List<MetricRow> getPromotionAnalytics() { return promotionAnalytics; }
    public void setPromotionAnalytics(List<MetricRow> promotionAnalytics) { this.promotionAnalytics = safeList(promotionAnalytics); }
    public List<MetricRow> getInventoryImpact() { return inventoryImpact; }
    public void setInventoryImpact(List<MetricRow> inventoryImpact) { this.inventoryImpact = safeList(inventoryImpact); }
    public List<MetricRow> getCustomerAnalytics() { return customerAnalytics; }
    public void setCustomerAnalytics(List<MetricRow> customerAnalytics) { this.customerAnalytics = safeList(customerAnalytics); }
    public List<MetricRow> getReturnAnalytics() { return returnAnalytics; }
    public void setReturnAnalytics(List<MetricRow> returnAnalytics) { this.returnAnalytics = safeList(returnAnalytics); }
    public List<DimensionMetric> getGeographicSales() { return geographicSales; }
    public void setGeographicSales(List<DimensionMetric> geographicSales) { this.geographicSales = safeList(geographicSales); }
    public List<ActivityEvent> getLiveActivity() { return liveActivity; }
    public void setLiveActivity(List<ActivityEvent> liveActivity) { this.liveActivity = safeList(liveActivity); }
    public List<BusinessAlert> getAlerts() { return alerts; }
    public void setAlerts(List<BusinessAlert> alerts) { this.alerts = safeList(alerts); }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? new ArrayList<>() : values;
    }

    public record KpiCard(String label, String value, String previousLabel, BigDecimal changePercent,
            String trend, List<Double> sparkline) { }
    public record SalesTrendPoint(String label, double grossSales, double netSales, long orderCount,
            long completedOrders, long cancelledOrders, long returnedOrders) { }
    public record SalesStatusCount(String label, long count) { }
    public record DimensionMetric(String label, BigDecimal revenue, long orders, BigDecimal quantity,
            BigDecimal commission, BigDecimal growthPercent) { }
    public record PaymentMetric(String label, BigDecimal amount, long count, long successCount, long failedCount,
            long pendingCount, BigDecimal successRate) { }
    public record SalesProductRow(String title, int sku, String category, String brand, String vendor,
            BigDecimal quantitySold, BigDecimal revenue, BigDecimal profit, BigDecimal currentStock) { }
    public record SalesVendorRow(String name, long orderCount, BigDecimal revenue, BigDecimal commission,
            BigDecimal growthPercent, BigDecimal rating) { }
    public record SalesCustomerRow(String customer, long orders, BigDecimal lifetimeValue, LocalDate lastPurchase,
            BigDecimal rewardPoints) { }
    public record MetricRow(String label, String value, String detail, String accent) { }
    public record ActivityEvent(String type, String title, String detail, String happenedAt, String accent) { }
    public record BusinessAlert(String severity, String title, String detail, String actionLabel) { }
    public record FilterOption(Long id, String label) { }
}
