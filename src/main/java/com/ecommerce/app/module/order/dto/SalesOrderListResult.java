package com.ecommerce.app.module.order.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class SalesOrderListResult {

    private final List<Map<String, Object>> orderRows;
    private final SalesOrderListKpi kpi;
    private final List<Integer> pageNumbers;
    private final int page;
    private final int size;
    private final int totalPages;
    private final LocalDate selectedFromDate;
    private final LocalDate selectedToDate;
    private final String selectedDateRange;
    private final String reportDateRangeLabel;

    public SalesOrderListResult(
            List<Map<String, Object>> orderRows,
            SalesOrderListKpi kpi,
            List<Integer> pageNumbers,
            int page,
            int size,
            int totalPages,
            LocalDate selectedFromDate,
            LocalDate selectedToDate,
            String selectedDateRange,
            String reportDateRangeLabel
    ) {
        this.orderRows = orderRows;
        this.kpi = kpi;
        this.pageNumbers = pageNumbers;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.selectedFromDate = selectedFromDate;
        this.selectedToDate = selectedToDate;
        this.selectedDateRange = selectedDateRange;
        this.reportDateRangeLabel = reportDateRangeLabel;
    }

    public List<Map<String, Object>> getOrderRows() {
        return orderRows;
    }

    public SalesOrderListKpi getKpi() {
        return kpi;
    }

    public List<Integer> getPageNumbers() {
        return pageNumbers;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return page <= 0;
    }

    public boolean isLast() {
        return totalPages <= 0 || page >= totalPages - 1;
    }

    public LocalDate getSelectedFromDate() {
        return selectedFromDate;
    }

    public LocalDate getSelectedToDate() {
        return selectedToDate;
    }

    public String getSelectedDateRange() {
        return selectedDateRange;
    }

    public String getReportDateRangeLabel() {
        return reportDateRangeLabel;
    }
}
