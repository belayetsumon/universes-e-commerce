package com.ecommerce.app.admin.services;

import com.ecommerce.app.model.Contact;
import com.ecommerce.app.module.user.model.Role;
import com.ecommerce.app.module.user.model.UserType;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.RoleRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.order.model.OrderItem;
import com.ecommerce.app.module.order.model.OrderItemReturnStatus;
import com.ecommerce.app.module.order.model.OrderStatus;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.repository.OrderItemRepository;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.ripository.ContactRepository;
import com.ecommerce.app.vendor.model.VendorPayout;
import com.ecommerce.app.vendor.model.VendorPayoutStatusEnum;
import com.ecommerce.app.vendor.model.VendorStatusEnum;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorPayoutRepository;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final int OVERDUE_SETTLEMENT_DAYS = 7;
    private static final int RECENT_ORDER_LIMIT = 8;
    private static final int WATCHLIST_LIMIT = 6;
    private static final int CONTACT_LIMIT = 5;
    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DAY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd MMM");
    private static final DateTimeFormatter DATE_TIME_LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VendorprofileRepository vendorprofileRepository;

    @Autowired
    private VendorPayoutRepository vendorPayoutRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ContactRepository contactRepository;

    public Map<String, Object> buildDashboard() {
        List<SalesOrder> orders = salesOrderRepository.findAll(Sort.by(Sort.Direction.DESC, "created", "id"));
        List<OrderItem> orderItems = orderItemRepository.findAll(Sort.by(Sort.Direction.DESC, "created", "id"));
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.DESC, "created", "id"));
        List<Vendorprofile> vendors = vendorprofileRepository.findAll(Sort.by(Sort.Direction.DESC, "created", "id"));
        List<VendorPayout> payouts = vendorPayoutRepository.findAll(Sort.by(Sort.Direction.DESC, "requestedAt", "id"));
        List<Contact> contacts = contactRepository.findAll(Sort.by(Sort.Direction.DESC, "created", "id"));
        List<Users> customers = loadCustomers();
        return composeDashboard(customers, orders, orderItems, products, vendors, payouts, contacts);
    }

    public Map<String, Object> emptyDashboard() {
        return composeDashboard(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private Map<String, Object> composeDashboard(List<Users> customers,
            List<SalesOrder> orders,
            List<OrderItem> orderItems,
            List<Product> products,
            List<Vendorprofile> vendors,
            List<VendorPayout> payouts,
            List<Contact> contacts) {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zoneId);
        LocalDate lastSevenDaysStart = today.minusDays(6);
        LocalDate lastThirtyDaysStart = today.minusDays(29);
        LocalDate overdueCutoff = today.minusDays(OVERDUE_SETTLEMENT_DAYS);

        BigDecimal totalGrossSales = orders.stream()
                .map(SalesOrder::getGrandTotal)
                .map(this::safeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRefundAmount = orderItems.stream()
                .map(OrderItem::getReturnRefundAmount)
                .map(this::safeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNetSales = nonNegative(totalGrossSales.subtract(totalRefundAmount));
        BigDecimal totalAdminCommission = orders.stream()
                .map(SalesOrder::getTotalMarketPlaceCommissionAmount)
                .map(this::safeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalVendorEarnings = orders.stream()
                .map(SalesOrder::getTotalVendorAmount)
                .map(this::safeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = orders.size();
        long totalCustomers = customers.size();
        long totalVendors = vendors.size();
        long uniqueBuyers = orders.stream()
                .map(SalesOrder::getCustomer)
                .filter(Objects::nonNull)
                .map(Users::getId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long returnRequestedCount = orderItems.stream()
                .filter(item -> resolveReturnStatus(item) == OrderItemReturnStatus.RETURN_REQUESTED)
                .count();
        long returnResolvedCount = orderItems.stream()
                .filter(item -> resolveReturnStatus(item) == OrderItemReturnStatus.RETURNED)
                .count();
        long returnRelatedCount = orderItems.stream()
                .filter(item -> resolveReturnStatus(item) != OrderItemReturnStatus.NONE)
                .count();

        long lowStockCount = products.stream().filter(this::isLowStock).count();
        long outOfStockCount = products.stream().filter(this::isOutOfStock).count();
        long unpublishedCount = products.stream().filter(this::isUnpublished).count();
        long publishedCount = products.stream().filter(this::isPublished).count();

        long newVendorApplications = vendors.stream()
                .filter(vendor -> isWithinRange(toLocalDate(vendor.getCreated()), lastThirtyDaysStart, today))
                .count();
        long pendingVendorApprovals = vendors.stream()
                .filter(vendor -> vendor.getVendorStatusEnum() == VendorStatusEnum.Pending)
                .count();

        List<VendorPayout> pendingPayouts = payouts.stream()
                .filter(this::isPendingPayout)
                .toList();
        BigDecimal pendingPayoutAmount = pendingPayouts.stream()
                .map(VendorPayout::getAmount)
                .map(this::safeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<VendorPayout> overduePayouts = pendingPayouts.stream()
                .filter(payout -> {
                    LocalDate requestedDate = toLocalDate(payout.getRequestedAt());
                    return requestedDate != null && requestedDate.isBefore(overdueCutoff);
                })
                .toList();
        BigDecimal overdueSettlementAmount = overduePayouts.stream()
                .map(VendorPayout::getAmount)
                .map(this::safeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = divide(totalGrossSales, totalOrders);
        BigDecimal conversionRate = percentage(uniqueBuyers, totalCustomers);
        BigDecimal returnRate = percentage(returnRelatedCount, orderItems.size());

        dashboard.put("lastUpdatedLabel", formatDateTime(LocalDateTime.now()));
        dashboard.put("lowStockThresholdLabel", "Low stock threshold is " + LOW_STOCK_THRESHOLD + " available units.");
        dashboard.put("overdueSettlementLabel", "Overdue settlements are payout requests older than " + OVERDUE_SETTLEMENT_DAYS + " days.");

        dashboard.put("heroStats", List.of(
                statCard("Total Sales", money(totalGrossSales), "Gross marketplace sales recorded across all orders.", "bi-cash-coin", "tone-teal", "/admin/finance/dashboard"),
                statCard("Total Orders", wholeNumber(totalOrders), "Marketplace order volume across the full order book.", "bi-receipt-cutoff", "tone-navy", "/admin-customer/orderlist"),
                statCard("Total Customers", wholeNumber(totalCustomers), "Registered customer accounts attached to the customer role.", "bi-people", "tone-emerald", "/admin-customer/index"),
                statCard("Total Vendors", wholeNumber(totalVendors), "Vendor storefronts currently tracked by the marketplace.", "bi-shop-window", "tone-amber", "/adminvendor/list")
        ));

        dashboard.put("periodPerformance", List.of(
                buildPeriodCard("Today", today, today, customers, orders, vendors),
                buildPeriodCard("Last 7 days", lastSevenDaysStart, today, customers, orders, vendors),
                buildPeriodCard("Last 30 days", lastThirtyDaysStart, today, customers, orders, vendors)
        ));

        dashboard.put("financialStats", List.of(
                statCard("Gross Sales", money(totalGrossSales), "Before refunds or return reversals.", "bi-graph-up-arrow", "tone-navy", "/admin/finance/dashboard"),
                statCard("Net Sales", money(totalNetSales), "Gross sales after processed item refunds.", "bi-currency-exchange", "tone-teal", "/admin/finance/dashboard"),
                statCard("Admin Commission", money(totalAdminCommission), "Marketplace commission booked from order lines.", "bi-bank2", "tone-amber", "/admin/finance/dashboard"),
                statCard("Vendor Earnings", money(totalVendorEarnings), "Vendor-side earnings before payout timing decisions.", "bi-wallet2", "tone-emerald", "/admin/finance/dashboard")
        ));

        dashboard.put("operationsStats", List.of(
                statCard("Pending Payouts", money(pendingPayoutAmount), wholeNumber(pendingPayouts.size()) + " payout requests are waiting for action.", "bi-hourglass-split", "tone-amber", "/admin/payouts/list"),
                statCard("Overdue Settlements", money(overdueSettlementAmount), wholeNumber(overduePayouts.size()) + " requests have been waiting longer than " + OVERDUE_SETTLEMENT_DAYS + " days.", "bi-alarm", "tone-rose", "/admin/payouts/list"),
                statCard("Average Order Value", money(averageOrderValue), "Calculated across " + wholeNumber(totalOrders) + " placed orders.", "bi-basket3", "tone-teal", "/admin-customer/orderlist"),
                statCard("Conversion Summary", percent(conversionRate), wholeNumber(uniqueBuyers) + " of " + wholeNumber(totalCustomers) + " customers have placed at least one order.", "bi-bullseye", "tone-navy", "/admin-customer/index")
        ));

        dashboard.put("riskStats", List.of(
                statCard("Refund Amount", money(totalRefundAmount), "Refund value already recorded on returned order items.", "bi-arrow-counterclockwise", "tone-rose", "/admin-customer/orderlist"),
                statCard("Return Rate", percent(returnRate), wholeNumber(returnRelatedCount) + " return-related order items across the order book.", "bi-box-arrow-in-left", "tone-amber", "/admin-customer/orderlist"),
                statCard("New Vendor Applications", wholeNumber(newVendorApplications), "Vendor applications submitted during the last 30 days.", "bi-building-add", "tone-emerald", "/adminvendor/list"),
                statCard("Pending Approvals", wholeNumber(pendingVendorApprovals), "Vendor profiles still waiting on admin approval.", "bi-patch-exclamation", "tone-amber", "/adminvendor/list"),
                statCard("Open Support Tickets", wholeNumber(contacts.size()), "Customer contact requests currently sitting in the support inbox.", "bi-headset", "tone-navy", null),
                statCard("Unresolved Complaints", wholeNumber(returnRequestedCount), "Return requests still waiting for a marketplace resolution.", "bi-shield-exclamation", "tone-rose", "/admin-customer/orderlist")
        ));

        dashboard.put("inventoryStats", List.of(
                statCard("Low Stock", wholeNumber(lowStockCount), "Published items with more than 0 and no more than " + LOW_STOCK_THRESHOLD + " units left.", "bi-exclamation-diamond", "tone-amber", "/product/index?manageStock=true"),
                statCard("Out Of Stock", wholeNumber(outOfStockCount), "Managed-stock items that have already hit zero available quantity.", "bi-slash-circle", "tone-rose", "/product/index?manageStock=true"),
                statCard("Unpublished Products", wholeNumber(unpublishedCount), "Catalog entries hidden from the storefront or not active yet.", "bi-eye-slash", "tone-slate", "/product/index?onlineShow=false")
        ));

        dashboard.put("recentOrders", buildRecentOrders(orders));
        dashboard.put("vendorQueueRows", buildVendorQueueRows(vendors, today, lastThirtyDaysStart));
        dashboard.put("productAttentionRows", buildProductAttentionRows(products));
        dashboard.put("supportRows", buildSupportRows(contacts));
        dashboard.put("complaintRows", buildComplaintRows(orderItems));

        dashboard.put("trendLabels", buildDailyLabels(lastThirtyDaysStart, today));
        dashboard.put("grossTrend", buildGrossTrend(lastThirtyDaysStart, today, orders));
        dashboard.put("netTrend", buildNetTrend(lastThirtyDaysStart, today, orders, orderItems));
        dashboard.put("orderTrend", buildOrderTrend(lastThirtyDaysStart, today, orders));
        dashboard.put("orderStatusLabels", buildOrderStatusLabels(lastThirtyDaysStart, today, orders));
        dashboard.put("orderStatusCounts", buildOrderStatusCounts(lastThirtyDaysStart, today, orders));
        dashboard.put("productHealthLabels", List.of("Published", "Low stock", "Out of stock", "Unpublished"));
        dashboard.put("productHealthCounts", List.of(publishedCount, lowStockCount, outOfStockCount, unpublishedCount));

        return dashboard;
    }

    private List<Users> loadCustomers() {
        Role customerRole = roleRepository.findBySlug("customer");
        List<Users> rawCustomers = customerRole != null
                ? usersRepository.findByRole(customerRole)
                : usersRepository.findAll().stream()
                        .filter(user -> user.getUserType() == UserType.customer)
                        .toList();

        Map<Long, Users> deduplicated = new LinkedHashMap<>();
        for (Users customer : rawCustomers) {
            if (customer == null || customer.getId() == null) {
                continue;
            }
            deduplicated.put(customer.getId(), customer);
        }
        return new ArrayList<>(deduplicated.values());
    }

    private Map<String, Object> buildPeriodCard(String label,
            LocalDate start,
            LocalDate end,
            List<Users> customers,
            List<SalesOrder> orders,
            List<Vendorprofile> vendors) {
        BigDecimal sales = orders.stream()
                .filter(order -> isWithinRange(toLocalDate(order.getCreated()), start, end))
                .map(SalesOrder::getGrandTotal)
                .map(this::safeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long orderCount = orders.stream()
                .filter(order -> isWithinRange(toLocalDate(order.getCreated()), start, end))
                .count();
        long customerCount = customers.stream()
                .filter(customer -> isWithinRange(toLocalDate(customer.getCreatedOn()), start, end))
                .count();
        long vendorCount = vendors.stream()
                .filter(vendor -> isWithinRange(toLocalDate(vendor.getCreated()), start, end))
                .count();

        Map<String, Object> card = new LinkedHashMap<>();
        card.put("label", label);
        card.put("rangeLabel", formatRange(start, end));
        card.put("metrics", List.of(
                miniMetric("Sales", money(sales)),
                miniMetric("Orders", wholeNumber(orderCount)),
                miniMetric("Customers", wholeNumber(customerCount)),
                miniMetric("Vendors", wholeNumber(vendorCount))
        ));
        return card;
    }

    private List<Map<String, Object>> buildRecentOrders(List<SalesOrder> orders) {
        return orders.stream()
                .sorted(Comparator.comparing(SalesOrder::getCreated, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(SalesOrder::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(RECENT_ORDER_LIMIT)
                .map(order -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("orderCode", safeText(order.getOrderCode(), "N/A"));
                    row.put("customerName", customerName(order.getCustomer()));
                    row.put("statusLabel", formatEnumLabel(order.getStatus() != null ? order.getStatus().name() : "Unknown"));
                    row.put("statusTone", orderTone(order.getStatus()));
                    row.put("createdLabel", formatDateTime(order.getCreated()));
                    row.put("totalLabel", money(order.getGrandTotal()));
                    row.put("href", "/admin-customer/order-details/" + order.getId());
                    return row;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Map<String, Object>> buildVendorQueueRows(List<Vendorprofile> vendors, LocalDate today, LocalDate recentStart) {
        return vendors.stream()
                .filter(vendor -> vendor.getVendorStatusEnum() == VendorStatusEnum.Pending
                        || isWithinRange(toLocalDate(vendor.getCreated()), recentStart, today))
                .sorted(Comparator
                        .comparing((Vendorprofile vendor) -> vendor.getVendorStatusEnum() != VendorStatusEnum.Pending)
                        .thenComparing(Vendorprofile::getCreated, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Vendorprofile::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(WATCHLIST_LIMIT)
                .map(vendor -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("companyName", safeText(vendor.getCompanyName(), "Vendor pending naming"));
                    row.put("vendorCode", safeText(vendor.getVendorCode(), "No code"));
                    row.put("statusLabel", formatEnumLabel(vendor.getVendorStatusEnum() != null ? vendor.getVendorStatusEnum().name() : "Unknown"));
                    row.put("statusTone", vendorTone(vendor.getVendorStatusEnum()));
                    row.put("submittedLabel", formatDateTime(vendor.getCreated()));
                    row.put("contactLabel", safeText(vendor.getEmail(), safeText(vendor.getPhone(), "No contact")));
                    row.put("href", "/adminvendor/edit/" + vendor.getId());
                    return row;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Map<String, Object>> buildProductAttentionRows(List<Product> products) {
        return products.stream()
                .filter(product -> isOutOfStock(product) || isLowStock(product) || isUnpublished(product))
                .sorted(Comparator
                        .comparingInt(this::productSeverity)
                        .thenComparing(Product::getCreated, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Product::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(WATCHLIST_LIMIT)
                .map(product -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("title", safeText(product.getTitle(), "Untitled product"));
                    row.put("vendorName", product.getVendorprofile() != null
                            ? safeText(product.getVendorprofile().getCompanyName(), "Marketplace catalog")
                            : "Marketplace catalog");
                    row.put("stockLabel", quantityLabel(product.getStockAvailableQuantity()));
                    row.put("stateLabel", productStateLabel(product));
                    row.put("stateTone", productTone(product));
                    row.put("href", "/product/details/" + product.getId());
                    return row;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Map<String, Object>> buildSupportRows(List<Contact> contacts) {
        return contacts.stream()
                .sorted(Comparator.comparing(Contact::getCreated, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Contact::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(CONTACT_LIMIT)
                .map(contact -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("subject", safeText(contact.getSubject(), "No subject"));
                    row.put("name", safeText(contact.getName(), "Anonymous"));
                    row.put("summary", safeText(contact.getEmail(), "No email provided"));
                    row.put("createdLabel", formatDateTime(contact.getCreated()));
                    return row;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Map<String, Object>> buildComplaintRows(List<OrderItem> orderItems) {
        return orderItems.stream()
                .filter(item -> resolveReturnStatus(item) == OrderItemReturnStatus.RETURN_REQUESTED)
                .sorted(Comparator.comparing(OrderItem::getReturnRequestedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(OrderItem::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(CONTACT_LIMIT)
                .map(item -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    SalesOrder order = item.getSalesOrder();
                    row.put("orderCode", order != null ? safeText(order.getOrderCode(), "N/A") : "N/A");
                    row.put("productTitle", item.getProduct() != null
                            ? safeText(item.getProduct().getTitle(), "Unknown product")
                            : "Unknown product");
                    row.put("summary", order != null ? customerName(order.getCustomer()) : "Customer not available");
                    row.put("createdLabel", formatDateTime(item.getReturnRequestedAt()));
                    row.put("href", order != null ? "/admin-customer/order-details/" + order.getId() : null);
                    return row;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<String> buildDailyLabels(LocalDate start, LocalDate end) {
        List<String> labels = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            labels.add(cursor.format(DAY_LABEL_FORMATTER));
            cursor = cursor.plusDays(1);
        }
        return labels;
    }

    private List<Double> buildGrossTrend(LocalDate start, LocalDate end, List<SalesOrder> orders) {
        Map<LocalDate, BigDecimal> grossByDay = initializeDailyMoneyMap(start, end);
        for (SalesOrder order : orders) {
            LocalDate orderDate = toLocalDate(order.getCreated());
            if (!grossByDay.containsKey(orderDate)) {
                continue;
            }
            grossByDay.put(orderDate, grossByDay.get(orderDate).add(safeAmount(order.getGrandTotal())));
        }
        return grossByDay.values().stream().map(BigDecimal::doubleValue).toList();
    }

    private List<Double> buildNetTrend(LocalDate start, LocalDate end, List<SalesOrder> orders, List<OrderItem> orderItems) {
        Map<LocalDate, BigDecimal> netByDay = initializeDailyMoneyMap(start, end);
        for (SalesOrder order : orders) {
            LocalDate orderDate = toLocalDate(order.getCreated());
            if (!netByDay.containsKey(orderDate)) {
                continue;
            }
            netByDay.put(orderDate, netByDay.get(orderDate).add(safeAmount(order.getGrandTotal())));
        }
        for (OrderItem item : orderItems) {
            LocalDate returnedDate = toLocalDate(item.getReturnedAt());
            if (!netByDay.containsKey(returnedDate)) {
                continue;
            }
            netByDay.put(returnedDate, nonNegative(netByDay.get(returnedDate).subtract(safeAmount(item.getReturnRefundAmount()))));
        }
        return netByDay.values().stream().map(BigDecimal::doubleValue).toList();
    }

    private List<Long> buildOrderTrend(LocalDate start, LocalDate end, List<SalesOrder> orders) {
        Map<LocalDate, Long> countByDay = initializeDailyCountMap(start, end);
        for (SalesOrder order : orders) {
            LocalDate orderDate = toLocalDate(order.getCreated());
            if (!countByDay.containsKey(orderDate)) {
                continue;
            }
            countByDay.put(orderDate, countByDay.get(orderDate) + 1L);
        }
        return new ArrayList<>(countByDay.values());
    }

    private List<String> buildOrderStatusLabels(LocalDate start, LocalDate end, List<SalesOrder> orders) {
        return buildOrderStatusMap(start, end, orders).keySet().stream()
                .map(status -> formatEnumLabel(status.name()))
                .toList();
    }

    private List<Long> buildOrderStatusCounts(LocalDate start, LocalDate end, List<SalesOrder> orders) {
        return new ArrayList<>(buildOrderStatusMap(start, end, orders).values());
    }

    private Map<OrderStatus, Long> buildOrderStatusMap(LocalDate start, LocalDate end, List<SalesOrder> orders) {
        Map<OrderStatus, Long> counts = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            long count = orders.stream()
                    .filter(order -> order.getStatus() == status)
                    .filter(order -> isWithinRange(toLocalDate(order.getCreated()), start, end))
                    .count();
            if (count > 0) {
                counts.put(status, count);
            }
        }
        return counts;
    }

    private Map<LocalDate, BigDecimal> initializeDailyMoneyMap(LocalDate start, LocalDate end) {
        Map<LocalDate, BigDecimal> values = new LinkedHashMap<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            values.put(cursor, BigDecimal.ZERO);
            cursor = cursor.plusDays(1);
        }
        return values;
    }

    private Map<LocalDate, Long> initializeDailyCountMap(LocalDate start, LocalDate end) {
        Map<LocalDate, Long> values = new LinkedHashMap<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            values.put(cursor, 0L);
            cursor = cursor.plusDays(1);
        }
        return values;
    }

    private boolean isPendingPayout(VendorPayout payout) {
        return payout != null
                && (payout.getStatus() == VendorPayoutStatusEnum.REQUESTED
                || payout.getStatus() == VendorPayoutStatusEnum.PROCESSING);
    }

    private boolean isPublished(Product product) {
        return product != null
                && product.getStatus() == ProductStatusEnum.Active
                && Boolean.TRUE.equals(product.getOnlineShow());
    }

    private boolean isUnpublished(Product product) {
        return product != null && !isPublished(product);
    }

    private boolean isOutOfStock(Product product) {
        return product != null
                && Boolean.TRUE.equals(product.getManageStock())
                && safeAmount(product.getStockAvailableQuantity()).compareTo(BigDecimal.ZERO) <= 0;
    }

    private boolean isLowStock(Product product) {
        BigDecimal available = product != null ? safeAmount(product.getStockAvailableQuantity()) : BigDecimal.ZERO;
        return product != null
                && Boolean.TRUE.equals(product.getManageStock())
                && available.compareTo(BigDecimal.ZERO) > 0
                && available.compareTo(BigDecimal.valueOf(LOW_STOCK_THRESHOLD)) <= 0;
    }

    private int productSeverity(Product product) {
        if (isOutOfStock(product)) {
            return 0;
        }
        if (isLowStock(product)) {
            return 1;
        }
        if (isUnpublished(product)) {
            return 2;
        }
        return 3;
    }

    private String productStateLabel(Product product) {
        if (isOutOfStock(product)) {
            return "Out of stock";
        }
        if (isLowStock(product)) {
            return "Low stock";
        }
        if (isUnpublished(product)) {
            return "Unpublished";
        }
        return "Healthy";
    }

    private String productTone(Product product) {
        if (isOutOfStock(product)) {
            return "tone-rose";
        }
        if (isLowStock(product)) {
            return "tone-amber";
        }
        if (isUnpublished(product)) {
            return "tone-slate";
        }
        return "tone-emerald";
    }

    private OrderItemReturnStatus resolveReturnStatus(OrderItem item) {
        return item != null && item.getReturnStatus() != null
                ? item.getReturnStatus()
                : OrderItemReturnStatus.NONE;
    }

    private String orderTone(OrderStatus status) {
        if (status == null) {
            return "badge-soft badge-neutral";
        }
        return switch (status) {
            case DELIVERED, COMPLETED -> "badge-soft badge-success";
            case RETURN_REQUESTED, PARTIALLY_RETURNED, RETURNED -> "badge-soft badge-danger";
            case CANCELLED -> "badge-soft badge-neutral";
            default -> "badge-soft badge-warning";
        };
    }

    private String vendorTone(VendorStatusEnum status) {
        if (status == null) {
            return "badge-soft badge-neutral";
        }
        return switch (status) {
            case Active -> "badge-soft badge-success";
            case Pending -> "badge-soft badge-warning";
            case Block -> "badge-soft badge-danger";
        };
    }

    private Map<String, Object> statCard(String label, String value, String note, String icon, String tone, String href) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("label", label);
        card.put("value", value);
        card.put("note", note);
        card.put("icon", icon);
        card.put("tone", tone);
        card.put("href", href);
        return card;
    }

    private Map<String, Object> miniMetric(String label, String value) {
        Map<String, Object> metric = new LinkedHashMap<>();
        metric.put("label", label);
        metric.put("value", value);
        return metric;
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private BigDecimal divide(BigDecimal amount, long divisor) {
        if (divisor <= 0) {
            return BigDecimal.ZERO;
        }
        return safeAmount(amount).divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal percentage(long numerator, long denominator) {
        if (denominator <= 0 || numerator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 1, RoundingMode.HALF_UP);
    }

    private BigDecimal nonNegative(BigDecimal value) {
        return safeAmount(value).compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : safeAmount(value);
    }

    private boolean isWithinRange(LocalDate value, LocalDate start, LocalDate end) {
        return value != null && (value.isEqual(start) || value.isAfter(start))
                && (value.isEqual(end) || value.isBefore(end));
    }

    private LocalDate toLocalDate(LocalDateTime value) {
        return value != null ? value.toLocalDate() : null;
    }

    private LocalDate toLocalDate(Date value) {
        return value != null ? value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
    }

    private String customerName(Users customer) {
        if (customer == null) {
            return "Guest / Unknown";
        }
        String firstName = safeText(customer.getFirstName(), "");
        String lastName = safeText(customer.getLastName(), "");
        String fullName = (firstName + " " + lastName).trim();
        if (!fullName.isBlank()) {
            return fullName;
        }
        if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
            return customer.getEmail();
        }
        return safeText(customer.getMobile(), "Customer");
    }

    private String formatRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return "Date range unavailable";
        }
        if (start.isEqual(end)) {
            return start.format(DATE_LABEL_FORMATTER);
        }
        return start.format(DAY_LABEL_FORMATTER) + " - " + end.format(DATE_LABEL_FORMATTER);
    }

    private String money(BigDecimal value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
        return "Tk " + format.format(safeAmount(value));
    }

    private String percent(BigDecimal value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        DecimalFormat format = new DecimalFormat("0.0", symbols);
        return format.format(safeAmount(value)) + "%";
    }

    private String wholeNumber(long value) {
        NumberFormat formatter = NumberFormat.getIntegerInstance(Locale.US);
        return formatter.format(value);
    }

    private String quantityLabel(BigDecimal value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        DecimalFormat format = new DecimalFormat("#,##0.###", symbols);
        return format.format(safeAmount(value)) + " units";
    }

    private String formatDateTime(LocalDateTime value) {
        return value != null ? value.format(DATE_TIME_LABEL_FORMATTER) : "Not available";
    }

    private String safeText(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    private String formatEnumLabel(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }
        String normalized = value.replace('_', ' ').toLowerCase(Locale.ENGLISH);
        String[] words = normalized.split("\\s+");
        List<String> titleWords = new ArrayList<>();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            titleWords.add(Character.toUpperCase(word.charAt(0)) + word.substring(1));
        }
        return String.join(" ", titleWords);
    }
}
