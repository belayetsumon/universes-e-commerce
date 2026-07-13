package com.ecommerce.app.module.sales.dashboard.dto;

import java.time.LocalDate;

public enum SalesDashboardDateRange {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    LAST_7_DAYS("Last 7 days"),
    LAST_30_DAYS("Last 30 days"),
    MONTH_TO_DATE("Month to date"),
    QUARTER_TO_DATE("Quarter to date"),
    YEAR_TO_DATE("Year to date"),
    CUSTOM("Custom range");

    private final String label;

    SalesDashboardDateRange(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public DateWindow resolve(LocalDate today) {
        return switch (this) {
            case TODAY -> new DateWindow(today, today);
            case YESTERDAY -> new DateWindow(today.minusDays(1), today.minusDays(1));
            case LAST_7_DAYS -> new DateWindow(today.minusDays(6), today);
            case LAST_30_DAYS -> new DateWindow(today.minusDays(29), today);
            case MONTH_TO_DATE -> new DateWindow(today.withDayOfMonth(1), today);
            case QUARTER_TO_DATE -> new DateWindow(today.withMonth(((today.getMonthValue() - 1) / 3) * 3 + 1).withDayOfMonth(1), today);
            case YEAR_TO_DATE -> new DateWindow(today.withDayOfYear(1), today);
            case CUSTOM -> new DateWindow(today.minusDays(29), today);
        };
    }

    public record DateWindow(LocalDate start, LocalDate end) {
    }
}
