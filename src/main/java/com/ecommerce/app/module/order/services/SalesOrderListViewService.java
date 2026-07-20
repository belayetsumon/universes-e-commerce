package com.ecommerce.app.module.order.services;

import com.ecommerce.app.module.order.dto.DateRangeOption;
import com.ecommerce.app.module.order.dto.SalesOrderListKpi;
import com.ecommerce.app.module.order.dto.SalesOrderListResult;
import com.ecommerce.app.module.order.dto.SalesOrderStatusKpi;
import com.ecommerce.app.module.order.model.OrderPaymentPlan;
import com.ecommerce.app.module.order.model.OrderPaymentState;
import com.ecommerce.app.module.order.model.OrderStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SalesOrderListViewService {

    public static final String DATE_RANGE_ALL = "ALL";
    public static final String DATE_RANGE_TODAY = "TODAY";
    public static final String DATE_RANGE_YESTERDAY = "YESTERDAY";
    public static final String DATE_RANGE_CURRENT_WEEK = "CURRENT_WEEK";
    public static final String DATE_RANGE_CURRENT_MONTH = "CURRENT_MONTH";
    public static final String DATE_RANGE_CURRENT_YEAR = "CURRENT_YEAR";
    public static final String DATE_RANGE_CUSTOM = "CUSTOM";
    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    private static final List<DateRangeOption> DATE_RANGE_OPTIONS = List.of(
            new DateRangeOption(DATE_RANGE_ALL, "All dates"),
            new DateRangeOption(DATE_RANGE_TODAY, "Today"),
            new DateRangeOption(DATE_RANGE_YESTERDAY, "Yesterday"),
            new DateRangeOption(DATE_RANGE_CURRENT_WEEK, "Current week"),
            new DateRangeOption(DATE_RANGE_CURRENT_MONTH, "Current month"),
            new DateRangeOption(DATE_RANGE_CURRENT_YEAR, "Current year"),
            new DateRangeOption(DATE_RANGE_CUSTOM, "Custom date")
    );

    public SalesOrderListResult build(
            List<Map<String, Object>> allRows,
            String q,
            OrderStatus status,
            String shipment,
            OrderPaymentPlan paymentPlan,
            OrderPaymentState paymentState,
            String checkoutType,
            Long vendorId,
            BigDecimal minTotal,
            BigDecimal maxTotal,
            String dateRange,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {
        List<Map<String, Object>> rows = allRows == null ? List.of() : allRows;
        ResolvedDateRange resolvedDateRange = resolveDateRange(dateRange, fromDate, toDate);
        String normalizedQuery = q != null ? q.trim().toLowerCase(Locale.ROOT) : "";
        String normalizedShipment = shipment != null ? shipment.trim().toUpperCase(Locale.ROOT) : "";
        String normalizedCheckoutType = checkoutType != null ? checkoutType.trim().toUpperCase(Locale.ROOT) : "";

        List<Map<String, Object>> statusScopeRows = rows.stream()
                .filter(row -> normalizedQuery.isBlank() || rowMatchesQuery(row, normalizedQuery))
                .filter(row -> normalizedShipment.isBlank() || rowMatchesShipmentState(row, normalizedShipment))
                .filter(row -> paymentPlan == null || paymentPlan.equals(row.get("paymentPlan")))
                .filter(row -> paymentState == null || paymentState.equals(row.get("paymentState")))
                .filter(row -> vendorId == null || vendorId.equals(asLong(row.get("vendorId"))))
                .filter(row -> checkoutMatches(row, normalizedCheckoutType))
                .filter(row -> minTotal == null || asBigDecimal(row.get("grandTotal")).compareTo(minTotal) >= 0)
                .filter(row -> maxTotal == null || asBigDecimal(row.get("grandTotal")).compareTo(maxTotal) <= 0)
                .filter(row -> resolvedDateRange.fromDate() == null || !asLocalDate(row.get("created")).isBefore(resolvedDateRange.fromDate()))
                .filter(row -> resolvedDateRange.toDate() == null || !asLocalDate(row.get("created")).isAfter(resolvedDateRange.toDate()))
                .toList();
        List<Map<String, Object>> filteredRows = statusScopeRows.stream()
                .filter(row -> status == null || status.equals(row.get("status")))
                .toList();

        int normalizedSize = normalizePageSize(size);
        int totalPages = filteredRows.isEmpty() ? 0 : (int) Math.ceil((double) filteredRows.size() / normalizedSize);
        int normalizedPage = Math.min(Math.max(page, 0), Math.max(totalPages - 1, 0));
        int fromIndex = Math.min(normalizedPage * normalizedSize, filteredRows.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filteredRows.size());
        List<Map<String, Object>> pageRows = filteredRows.subList(fromIndex, toIndex);

        return new SalesOrderListResult(
                pageRows,
                kpi(rows, filteredRows, statusScopeRows, status),
                pageNumbers(normalizedPage, totalPages),
                normalizedPage,
                normalizedSize,
                totalPages,
                resolvedDateRange.fromDate(),
                resolvedDateRange.toDate(),
                resolvedDateRange.dateRange(),
                reportDateRangeLabel(resolvedDateRange)
        );
    }

    public List<DateRangeOption> dateRangeOptions() {
        return DATE_RANGE_OPTIONS;
    }

    private SalesOrderListKpi kpi(
            List<Map<String, Object>> allRows,
            List<Map<String, Object>> filteredRows,
            List<Map<String, Object>> statusScopeRows,
            OrderStatus selectedStatus
    ) {
        BigDecimal filteredGrandTotal = sumAmount(filteredRows, "grandTotal");
        int filteredCount = filteredRows.size();
        BigDecimal averageOrderValue = filteredCount == 0
                ? BigDecimal.ZERO
                : filteredGrandTotal.divide(BigDecimal.valueOf(filteredCount), 2, RoundingMode.HALF_UP);

        return new SalesOrderListKpi(
                allRows.size(),
                filteredCount,
                countShipmentState(filteredRows, "READY"),
                countShipmentState(filteredRows, "CREATED"),
                countShipmentState(filteredRows, "BLOCKED"),
                filteredGrandTotal,
                sumAmount(filteredRows, "totalVendorAmount"),
                sumAmount(filteredRows, "totalMarketPlaceCommissionAmount"),
                sumAmount(filteredRows, "totalDiscountAmount"),
                sumAmount(filteredRows, "totalVatAmount"),
                averageOrderValue,
                filteredRows.stream().filter(row -> OrderPaymentState.PAID.equals(row.get("paymentState"))).count(),
                filteredRows.stream().filter(row -> !OrderPaymentState.PAID.equals(row.get("paymentState"))).count(),
                filteredRows.stream().filter(row -> Boolean.TRUE.equals(row.get("guestCheckout"))).count(),
                filteredRows.stream().filter(row -> OrderStatus.CANCELLED.equals(row.get("status"))).count(),
                filteredRows.stream().filter(row -> row.get("status") == OrderStatus.RETURNED || row.get("status") == OrderStatus.PARTIALLY_RETURNED || row.get("status") == OrderStatus.RETURN_REQUESTED).count(),
                statusKpis(statusScopeRows, selectedStatus)
        );
    }

    private List<SalesOrderStatusKpi> statusKpis(List<Map<String, Object>> rows, OrderStatus selectedStatus) {
        List<SalesOrderStatusKpi> statusKpis = new ArrayList<>();
        for (OrderStatus status : OrderStatus.values()) {
            long count = rows.stream().filter(row -> status.equals(row.get("status"))).count();
            statusKpis.add(new SalesOrderStatusKpi(
                    status,
                    status.name().replace('_', ' '),
                    count,
                    status == selectedStatus
            ));
        }
        return statusKpis;
    }

    private boolean rowMatchesQuery(Map<String, Object> row, String query) {
        String searchable = String.join(" ",
                value(row.get("orderId")),
                value(row.get("uuid")),
                value(row.get("orderCode")),
                value(row.get("firstName")),
                value(row.get("lastName")),
                value(row.get("vendorName")),
                value(row.get("mobile")),
                value(row.get("email")),
                value(row.get("status")),
                value(row.get("paymentPlan")),
                value(row.get("paymentState"))
        ).toLowerCase(Locale.ROOT);
        return searchable.contains(query);
    }

    private boolean checkoutMatches(Map<String, Object> row, String checkoutType) {
        if (checkoutType == null || checkoutType.isBlank()) {
            return true;
        }
        boolean guestCheckout = Boolean.TRUE.equals(row.get("guestCheckout"));
        return switch (checkoutType) {
            case "GUEST" -> guestCheckout;
            case "CUSTOMER" -> !guestCheckout;
            default -> true;
        };
    }

    private long countShipmentState(List<Map<String, Object>> rows, String shipmentState) {
        return rows.stream().filter(row -> rowMatchesShipmentState(row, shipmentState)).count();
    }

    private boolean rowMatchesShipmentState(Map<String, Object> row, String shipmentState) {
        boolean hasShipment = Boolean.TRUE.equals(row.get("hasShipment"));
        boolean shipmentEligible = Boolean.TRUE.equals(row.get("shipmentEligible"));
        return switch (shipmentState) {
            case "READY" -> shipmentEligible;
            case "CREATED" -> hasShipment;
            case "BLOCKED" -> !shipmentEligible && !hasShipment;
            default -> true;
        };
    }

    private ResolvedDateRange resolveDateRange(String dateRange, LocalDate fromDate, LocalDate toDate) {
        String normalizedDateRange = normalizeDateRange(dateRange);
        LocalDate today = LocalDate.now();
        if (DATE_RANGE_TODAY.equals(normalizedDateRange)) {
            return new ResolvedDateRange(today, today, normalizedDateRange);
        }
        if (DATE_RANGE_YESTERDAY.equals(normalizedDateRange)) {
            LocalDate yesterday = today.minusDays(1);
            return new ResolvedDateRange(yesterday, yesterday, normalizedDateRange);
        }
        if (DATE_RANGE_CURRENT_WEEK.equals(normalizedDateRange)) {
            return new ResolvedDateRange(
                    today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                    today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)),
                    normalizedDateRange
            );
        }
        if (DATE_RANGE_CURRENT_MONTH.equals(normalizedDateRange)) {
            return new ResolvedDateRange(
                    today.withDayOfMonth(1),
                    today.withDayOfMonth(today.lengthOfMonth()),
                    normalizedDateRange
            );
        }
        if (DATE_RANGE_CURRENT_YEAR.equals(normalizedDateRange)) {
            return new ResolvedDateRange(
                    today.withDayOfYear(1),
                    today.withDayOfYear(today.lengthOfYear()),
                    normalizedDateRange
            );
        }
        if (DATE_RANGE_CUSTOM.equals(normalizedDateRange)) {
            return new ResolvedDateRange(fromDate, toDate, normalizedDateRange);
        }
        return new ResolvedDateRange(null, null, DATE_RANGE_ALL);
    }

    private String normalizeDateRange(String dateRange) {
        if (dateRange == null || dateRange.isBlank()) {
            return DATE_RANGE_ALL;
        }
        String normalized = dateRange.trim().toUpperCase(Locale.ROOT);
        return DATE_RANGE_OPTIONS.stream()
                .anyMatch(option -> option.getValue().equals(normalized)) ? normalized : DATE_RANGE_ALL;
    }

    private String reportDateRangeLabel(ResolvedDateRange dateRange) {
        if (dateRange == null || DATE_RANGE_ALL.equals(dateRange.dateRange())) {
            return "All dates";
        }
        String label = DATE_RANGE_OPTIONS.stream()
                .filter(option -> option.getValue().equals(dateRange.dateRange()))
                .map(DateRangeOption::getLabel)
                .findFirst()
                .orElse("Selected date range");
        String formattedRange = formatDateRange(dateRange.fromDate(), dateRange.toDate());
        return formattedRange.isBlank() ? label : label + ": " + formattedRange;
    }

    private String formatDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return "";
        }
        if (fromDate != null && toDate != null && fromDate.equals(toDate)) {
            return REPORT_DATE_FORMAT.format(fromDate);
        }
        if (fromDate != null && toDate != null) {
            return REPORT_DATE_FORMAT.format(fromDate) + " to " + REPORT_DATE_FORMAT.format(toDate);
        }
        if (fromDate != null) {
            return "From " + REPORT_DATE_FORMAT.format(fromDate);
        }
        return "Until " + REPORT_DATE_FORMAT.format(toDate);
    }

    private int normalizePageSize(int size) {
        if (size <= 10) {
            return 10;
        }
        if (size <= 25) {
            return 25;
        }
        if (size <= 50) {
            return 50;
        }
        return 100;
    }

    private List<Integer> pageNumbers(int page, int totalPages) {
        if (totalPages <= 0) {
            return List.of();
        }
        int start = Math.max(0, page - 2);
        int end = Math.min(totalPages - 1, start + 4);
        start = Math.max(0, end - 4);
        List<Integer> numbers = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            numbers.add(i);
        }
        return numbers;
    }

    private BigDecimal sumAmount(List<Map<String, Object>> rows, String key) {
        return rows.stream()
                .map(row -> asBigDecimal(row.get(key)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal asBigDecimal(Object value) {
        if (value instanceof BigDecimal amount) {
            return amount;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }

    private Long asLong(Object value) {
        if (value instanceof Long number) {
            return number;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private LocalDate asLocalDate(Object value) {
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toLocalDate();
        }
        if (value instanceof LocalDate date) {
            return date;
        }
        return LocalDate.now();
    }

    private String value(Object value) {
        return value != null ? String.valueOf(value) : "";
    }

    private record ResolvedDateRange(LocalDate fromDate, LocalDate toDate, String dateRange) {
    }
}
