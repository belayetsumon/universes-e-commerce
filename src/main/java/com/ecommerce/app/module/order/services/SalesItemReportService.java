package com.ecommerce.app.module.order.services;

import com.ecommerce.app.module.order.dto.SalesItemDateRangeOption;
import com.ecommerce.app.module.order.dto.SalesItemListResult;
import com.ecommerce.app.module.order.dto.SalesItemTopProductChartRow;
import com.ecommerce.app.module.order.model.OrderItem;
import com.ecommerce.app.module.order.model.OrderItemReturnStatus;
import com.ecommerce.app.module.order.model.OrderStatus;
import com.ecommerce.app.module.order.repository.OrderItemRepository;
import com.ecommerce.app.module.order.repository.SalesItemTopProductProjection;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SalesItemReportService {

    public static final String DATE_RANGE_ALL = "ALL";
    public static final String DATE_RANGE_TODAY = "TODAY";
    public static final String DATE_RANGE_YESTERDAY = "YESTERDAY";
    public static final String DATE_RANGE_CURRENT_WEEK = "CURRENT_WEEK";
    public static final String DATE_RANGE_CURRENT_MONTH = "CURRENT_MONTH";
    public static final String DATE_RANGE_CURRENT_YEAR = "CURRENT_YEAR";
    public static final String DATE_RANGE_CUSTOM = "CUSTOM";
    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    private static final List<SalesItemDateRangeOption> DATE_RANGE_OPTIONS = List.of(
            new SalesItemDateRangeOption(DATE_RANGE_ALL, "All dates"),
            new SalesItemDateRangeOption(DATE_RANGE_TODAY, "Today"),
            new SalesItemDateRangeOption(DATE_RANGE_YESTERDAY, "Yesterday"),
            new SalesItemDateRangeOption(DATE_RANGE_CURRENT_WEEK, "Current week"),
            new SalesItemDateRangeOption(DATE_RANGE_CURRENT_MONTH, "Current month"),
            new SalesItemDateRangeOption(DATE_RANGE_CURRENT_YEAR, "Current year"),
            new SalesItemDateRangeOption(DATE_RANGE_CUSTOM, "Custom date")
    );

    private final OrderItemRepository orderItemRepository;

    public SalesItemReportService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    public SalesItemListResult findSalesItems(
            Long vendorId,
            OrderStatus status,
            OrderItemReturnStatus returnStatus,
            String q,
            String dateRange,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        String search = normalizeSearch(q);
        ResolvedDateRange resolvedDateRange = resolveDateRange(dateRange, fromDate, toDate);
        LocalDateTime fromDateTime = startOfDay(resolvedDateRange.fromDate());
        LocalDateTime toDateTime = startOfNextDay(resolvedDateRange.toDate());
        Long numericId = parseLong(search);
        Integer productSku = parseInteger(search);
        String searchPattern = searchPattern(search);

        Page<OrderItem> salesItems = orderItemRepository.searchSalesItems(
                vendorId,
                status,
                returnStatus,
                fromDateTime,
                toDateTime,
                searchPattern,
                numericId,
                productSku,
                pageable
        );
        List<SalesItemTopProductChartRow> topProducts = topProducts(
                vendorId,
                status,
                returnStatus,
                fromDateTime,
                toDateTime,
                searchPattern,
                numericId,
                productSku
        );
        return new SalesItemListResult(
                salesItems,
                topProducts,
                resolvedDateRange.fromDate(),
                resolvedDateRange.toDate(),
                resolvedDateRange.dateRange(),
                reportDateRangeLabel(resolvedDateRange)
        );
    }

    public List<SalesItemDateRangeOption> dateRangeOptions() {
        return DATE_RANGE_OPTIONS;
    }

    private List<SalesItemTopProductChartRow> topProducts(
            Long vendorId,
            OrderStatus status,
            OrderItemReturnStatus returnStatus,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String searchPattern,
            Long numericId,
            Integer productSku
    ) {
        List<SalesItemTopProductProjection> projections = orderItemRepository.findTopSalesItems(
                vendorId,
                status,
                returnStatus,
                fromDate,
                toDate,
                searchPattern,
                numericId,
                productSku,
                PageRequest.of(0, 30)
        );
        BigDecimal maxQuantity = projections.stream()
                .map(SalesItemTopProductProjection::getQuantity)
                .filter(quantity -> quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        List<SalesItemTopProductChartRow> rows = new ArrayList<>();
        for (int i = 0; i < projections.size(); i++) {
            SalesItemTopProductProjection projection = projections.get(i);
            BigDecimal quantity = defaultAmount(projection.getQuantity());
            rows.add(new SalesItemTopProductChartRow(
                    i + 1,
                    projection.getProductId(),
                    safeProductTitle(projection.getProductTitle()),
                    projection.getSku(),
                    quantity,
                    defaultAmount(projection.getItemTotal()),
                    chartPercentage(quantity, maxQuantity)
            ));
        }
        return rows;
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
        String normalized = dateRange.trim().toUpperCase();
        return DATE_RANGE_OPTIONS.stream()
                .anyMatch(option -> option.getValue().equals(normalized)) ? normalized : DATE_RANGE_ALL;
    }

    private String reportDateRangeLabel(ResolvedDateRange dateRange) {
        if (dateRange == null || DATE_RANGE_ALL.equals(dateRange.dateRange())) {
            return "All dates";
        }
        String label = DATE_RANGE_OPTIONS.stream()
                .filter(option -> option.getValue().equals(dateRange.dateRange()))
                .map(SalesItemDateRangeOption::getLabel)
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

    private int chartPercentage(BigDecimal quantity, BigDecimal maxQuantity) {
        if (quantity == null || maxQuantity == null || maxQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return quantity.multiply(BigDecimal.valueOf(100))
                .divide(maxQuantity, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String safeProductTitle(String productTitle) {
        return productTitle == null || productTitle.isBlank() ? "Product not found" : productTitle;
    }

    private String normalizeSearch(String q) {
        return q == null || q.trim().isEmpty() ? null : q.trim();
    }

    private String searchPattern(String search) {
        return search == null ? null : "%" + search + "%";
    }

    private Long parseLong(String search) {
        if (search == null || !search.matches("\\d+")) {
            return null;
        }
        try {
            return Long.valueOf(search);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parseInteger(String search) {
        if (search == null || !search.matches("\\d+")) {
            return null;
        }
        try {
            return Integer.valueOf(search);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private LocalDateTime startOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }

    private LocalDateTime startOfNextDay(LocalDate date) {
        return date == null ? null : date.plusDays(1).atStartOfDay();
    }

    private record ResolvedDateRange(LocalDate fromDate, LocalDate toDate, String dateRange) {
    }
}
