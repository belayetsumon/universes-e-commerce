package com.ecommerce.app.vendor.services;

import com.ecommerce.app.module.order.model.OrderStatus;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.vendor.dto.VendorDashboardDto;
import com.ecommerce.app.vendor.model.VendorTransactionStatusEnum;
import com.ecommerce.app.vendor.model.Vendorprofile;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VendorDashboardService {

    private static final BigDecimal LOW_STOCK_THRESHOLD = BigDecimal.valueOf(5);
    private static final int MONTH_WINDOW = 6;
    private static final int TOP_CATEGORY_LIMIT = 6;
    private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private VendorFinanceService vendorFinanceService;

    public VendorDashboardDto buildDashboard(Vendorprofile vendorprofile) {
        VendorDashboardDto dashboard = new VendorDashboardDto();
        if (vendorprofile == null || vendorprofile.getId() == null) {
            return dashboard;
        }

        Long vendorId = vendorprofile.getId();
        dashboard.setVendorName(vendorprofile.getCompanyName());
        dashboard.setVendorCode(vendorprofile.getVendorCode());

        List<Product> products = productRepository.findByVendorprofile_IdOrderByIdDesc(vendorId);
        List<SalesOrder> orders = salesOrderRepository.findByVendorIdOrderByIdDesc(vendorId);
        EnumMap<VendorTransactionStatusEnum, BigDecimal> balances = vendorFinanceService.getVendorBalance(vendorId);

        populateProductMetrics(dashboard, products);
        populateOrderMetrics(dashboard, orders);
        populateMonthlyTrends(dashboard, orders);
        populateBalanceMetrics(dashboard, balances);

        return dashboard;
    }

    private void populateProductMetrics(VendorDashboardDto dashboard, List<Product> products) {
        dashboard.setTotalProducts(products.size());

        int activeProducts = 0;
        int featuredProducts = 0;
        int lowStockProducts = 0;
        Map<String, Integer> categoryCounts = new LinkedHashMap<>();

        for (Product product : products) {
            if (product.getStatus() == ProductStatusEnum.Active) {
                activeProducts++;
            }
            if (Boolean.TRUE.equals(product.getFeaturedProduct())) {
                featuredProducts++;
            }
            if (Boolean.TRUE.equals(product.getManageStock())
                    && defaultAmount(product.getStockAvailableQuantity()).compareTo(LOW_STOCK_THRESHOLD) <= 0) {
                lowStockProducts++;
            }

            String categoryName = product.getProductcategory() != null
                    && product.getProductcategory().getName() != null
                    && !product.getProductcategory().getName().isBlank()
                    ? product.getProductcategory().getName().trim()
                    : "Uncategorized";
            categoryCounts.merge(categoryName, 1, Integer::sum);
        }

        dashboard.setActiveProducts(activeProducts);
        dashboard.setFeaturedProducts(featuredProducts);
        dashboard.setLowStockProducts(lowStockProducts);

        categoryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_CATEGORY_LIMIT)
                .forEach(entry -> {
                    dashboard.getProductCategoryLabels().add(entry.getKey());
                    dashboard.getProductCategoryCounts().add(entry.getValue());
                });
    }

    private void populateOrderMetrics(VendorDashboardDto dashboard, List<SalesOrder> orders) {
        dashboard.setTotalOrders(orders.size());

        int openOrders = 0;
        int completedOrders = 0;
        int returnIssueOrders = 0;
        Map<OrderStatus, Integer> statusCounts = new LinkedHashMap<>();

        for (OrderStatus status : OrderStatus.values()) {
            statusCounts.put(status, 0);
        }

        for (SalesOrder order : orders) {
            OrderStatus status = order.getStatus();
            if (status == null) {
                continue;
            }

            statusCounts.put(status, statusCounts.get(status) + 1);

            if (isOpenStatus(status)) {
                openOrders++;
            }
            if (status == OrderStatus.DELIVERED || status == OrderStatus.COMPLETED) {
                completedOrders++;
            }
            if (status == OrderStatus.RETURN_REQUESTED
                    || status == OrderStatus.PARTIALLY_RETURNED
                    || status == OrderStatus.RETURNED
                    || status == OrderStatus.CANCELLED) {
                returnIssueOrders++;
            }
        }

        dashboard.setOpenOrders(openOrders);
        dashboard.setCompletedOrders(completedOrders);
        dashboard.setReturnIssueOrders(returnIssueOrders);

        for (Map.Entry<OrderStatus, Integer> entry : statusCounts.entrySet()) {
            if (entry.getValue() > 0) {
                dashboard.getOrderStatusLabels().add(formatEnumLabel(entry.getKey().name()));
                dashboard.getOrderStatusCounts().add(entry.getValue());
            }
        }
    }

    private void populateMonthlyTrends(VendorDashboardDto dashboard, List<SalesOrder> orders) {
        YearMonth currentMonth = YearMonth.now();
        List<YearMonth> window = new ArrayList<>();
        Map<YearMonth, BigDecimal> grossSalesByMonth = new LinkedHashMap<>();
        Map<YearMonth, BigDecimal> earningsByMonth = new LinkedHashMap<>();
        Map<YearMonth, Integer> orderCountByMonth = new LinkedHashMap<>();

        for (int i = MONTH_WINDOW - 1; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            window.add(month);
            grossSalesByMonth.put(month, BigDecimal.ZERO);
            earningsByMonth.put(month, BigDecimal.ZERO);
            orderCountByMonth.put(month, 0);
        }

        for (SalesOrder order : orders) {
            if (order.getCreated() == null) {
                continue;
            }

            YearMonth orderMonth = YearMonth.from(order.getCreated());
            if (!grossSalesByMonth.containsKey(orderMonth)) {
                continue;
            }

            grossSalesByMonth.put(orderMonth, grossSalesByMonth.get(orderMonth).add(defaultAmount(order.getGrandTotal())));
            earningsByMonth.put(orderMonth, earningsByMonth.get(orderMonth).add(defaultAmount(order.getTotalVendorAmount())));
            orderCountByMonth.put(orderMonth, orderCountByMonth.get(orderMonth) + 1);
        }

        for (YearMonth month : window) {
            dashboard.getMonthlyLabels().add(month.format(MONTH_LABEL_FORMATTER));
            dashboard.getMonthlyGrossSales().add(grossSalesByMonth.get(month).doubleValue());
            dashboard.getMonthlyVendorEarnings().add(earningsByMonth.get(month).doubleValue());
            dashboard.getMonthlyOrderCounts().add(orderCountByMonth.get(month));
        }
    }

    private void populateBalanceMetrics(VendorDashboardDto dashboard, EnumMap<VendorTransactionStatusEnum, BigDecimal> balances) {
        BigDecimal pending = balances.getOrDefault(VendorTransactionStatusEnum.PENDING, BigDecimal.ZERO);
        BigDecimal available = balances.getOrDefault(VendorTransactionStatusEnum.AVAILABLE, BigDecimal.ZERO);
        BigDecimal requested = balances.getOrDefault(VendorTransactionStatusEnum.REQUESTED, BigDecimal.ZERO);
        BigDecimal paid = balances.getOrDefault(VendorTransactionStatusEnum.PAID, BigDecimal.ZERO);

        dashboard.setPendingBalance(pending);
        dashboard.setAvailableBalance(available);
        dashboard.setRequestedBalance(requested);
        dashboard.setPaidBalance(paid);

        dashboard.getBalanceLabels().add("Pending");
        dashboard.getBalanceLabels().add("Available");
        dashboard.getBalanceLabels().add("Requested");
        dashboard.getBalanceLabels().add("Paid");

        dashboard.getBalanceAmounts().add(pending.doubleValue());
        dashboard.getBalanceAmounts().add(available.doubleValue());
        dashboard.getBalanceAmounts().add(requested.doubleValue());
        dashboard.getBalanceAmounts().add(paid.doubleValue());
    }

    private boolean isOpenStatus(OrderStatus status) {
        return status == OrderStatus.NEW_ORDER
                || status == OrderStatus.PENDING
                || status == OrderStatus.CONFIRMED
                || status == OrderStatus.PROCESSING
                || status == OrderStatus.PACKED
                || status == OrderStatus.SHIPPED
                || status == OrderStatus.IN_TRANSIT
                || status == OrderStatus.OUT_FOR_DELIVERY;
    }

    private String formatEnumLabel(String raw) {
        String[] parts = raw.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(part.charAt(0)).append(part.substring(1).toLowerCase(Locale.ENGLISH));
        }
        return builder.toString();
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
