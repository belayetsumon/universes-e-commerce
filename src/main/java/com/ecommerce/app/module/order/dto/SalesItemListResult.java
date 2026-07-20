package com.ecommerce.app.module.order.dto;

import com.ecommerce.app.module.order.model.OrderItem;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;

public class SalesItemListResult {

    private final Page<OrderItem> salesItems;
    private final List<SalesItemTopProductChartRow> topProducts;
    private final LocalDate selectedFromDate;
    private final LocalDate selectedToDate;
    private final String selectedDateRange;
    private final String reportDateRangeLabel;

    public SalesItemListResult(
            Page<OrderItem> salesItems,
            List<SalesItemTopProductChartRow> topProducts,
            LocalDate selectedFromDate,
            LocalDate selectedToDate,
            String selectedDateRange,
            String reportDateRangeLabel
    ) {
        this.salesItems = salesItems;
        this.topProducts = topProducts;
        this.selectedFromDate = selectedFromDate;
        this.selectedToDate = selectedToDate;
        this.selectedDateRange = selectedDateRange;
        this.reportDateRangeLabel = reportDateRangeLabel;
    }

    public Page<OrderItem> getSalesItems() {
        return salesItems;
    }

    public List<SalesItemTopProductChartRow> getTopProducts() {
        return topProducts;
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
