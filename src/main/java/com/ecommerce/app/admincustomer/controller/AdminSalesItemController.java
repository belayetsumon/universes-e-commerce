package com.ecommerce.app.admincustomer.controller;

import com.ecommerce.app.module.order.model.OrderItem;
import com.ecommerce.app.module.order.model.OrderItemReturnStatus;
import com.ecommerce.app.module.order.model.OrderStatus;
import com.ecommerce.app.module.order.services.SalesItemReportService;
import com.ecommerce.app.module.order.dto.SalesItemListResult;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/sales/items")
public class AdminSalesItemController {

    private final SalesItemReportService salesItemReportService;

    public AdminSalesItemController(SalesItemReportService salesItemReportService) {
        this.salesItemReportService = salesItemReportService;
    }

    @GetMapping
    public String index(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "status", required = false) OrderStatus status,
            @RequestParam(name = "returnStatus", required = false) OrderItemReturnStatus returnStatus,
            @RequestParam(name = "dateRange", required = false, defaultValue = SalesItemReportService.DATE_RANGE_TODAY) String dateRange,
            @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "25") int size,
            Model model
    ) {
        Pageable pageable = pageable(page, size);
        Page<OrderItem> salesItems = Page.empty(pageable);
        SalesItemListResult result = null;
        try {
            result = salesItemReportService.findSalesItems(
                    null,
                    status,
                    returnStatus,
                    q,
                    dateRange,
                    fromDate,
                    toDate,
                    pageable
            );
            salesItems = result.getSalesItems();
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Runtime error while loading sales items: " + userFacingMessage(ex));
        }
        populateModel(model, salesItems, result, q, status, returnStatus, dateRange, fromDate, toDate, size);
        return "admin/sales/items";
    }

    private void populateModel(
            Model model,
            Page<OrderItem> salesItems,
            SalesItemListResult result,
            String q,
            OrderStatus status,
            OrderItemReturnStatus returnStatus,
            String dateRange,
            LocalDate fromDate,
            LocalDate toDate,
            int size
    ) {
        model.addAttribute("salesItems", salesItems);
        model.addAttribute("topProducts", result == null ? List.of() : result.getTopProducts());
        model.addAttribute("pageNumbers", pageNumbers(salesItems));
        model.addAttribute("selectedQ", q);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedReturnStatus", returnStatus);
        model.addAttribute("selectedDateRange", result == null ? dateRange : result.getSelectedDateRange());
        model.addAttribute("selectedFromDate", result == null ? fromDate : result.getSelectedFromDate());
        model.addAttribute("selectedToDate", result == null ? toDate : result.getSelectedToDate());
        model.addAttribute("reportDateRangeLabel", result == null ? "Selected date range" : result.getReportDateRangeLabel());
        model.addAttribute("size", normalizePageSize(size));
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("returnStatuses", OrderItemReturnStatus.values());
        model.addAttribute("dateRangeOptions", salesItemReportService.dateRangeOptions());
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(
                Math.max(page, 0),
                normalizePageSize(size),
                Sort.by(Sort.Order.desc("created"), Sort.Order.desc("id"))
        );
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

    private List<Integer> pageNumbers(Page<?> page) {
        if (page.getTotalPages() <= 0) {
            return List.of();
        }
        int start = Math.max(0, page.getNumber() - 2);
        int end = Math.min(page.getTotalPages() - 1, start + 4);
        start = Math.max(0, end - 4);
        List<Integer> numbers = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            numbers.add(i);
        }
        return numbers;
    }

    private String userFacingMessage(Exception ex) {
        Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(ex);
        String message = rootCause == null ? ex.getMessage() : rootCause.getMessage();
        return message == null || message.isBlank() ? ex.getClass().getSimpleName() : message;
    }
}
