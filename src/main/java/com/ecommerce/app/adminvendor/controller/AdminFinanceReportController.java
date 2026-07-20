package com.ecommerce.app.adminvendor.controller;

import com.ecommerce.app.finance.dto.AdminFinanceDashboardDto;
import com.ecommerce.app.finance.services.FinanceReportService;
import com.ecommerce.app.module.order.dto.ReturnRefundReportDto;
import com.ecommerce.app.module.order.model.OrderItemReturnStatus;
import com.ecommerce.app.module.order.services.ReturnRefundReportService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/finance")
@PreAuthorize("hasAuthority('admin')")
public class AdminFinanceReportController {

    @Autowired
    private FinanceReportService financeReportService;

    @Autowired
    private ReturnRefundReportService returnRefundReportService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            model.addAttribute("dashboard", financeReportService.getAdminDashboard());
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Runtime error while loading admin finance dashboard: " + ex.getMessage());
            model.addAttribute("dashboard", new AdminFinanceDashboardDto());
        }
        return "admin/finance/dashboard";
    }

    @GetMapping("/settlements")
    public String settlements(Model model) {
        try {
            model.addAttribute("shipments", financeReportService.getSettlementLedger());
            model.addAttribute("vendorLookup", financeReportService.getVendorLookup());
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Runtime error while loading settlement ledger: " + ex.getMessage());
            model.addAttribute("shipments", new ArrayList<>());
            model.addAttribute("vendorLookup", new HashMap<>());
        }
        return "admin/finance/settlements";
    }

    @GetMapping({"/returns", "/returns-refunds", "/return-refund", "/return-refunds"})
    public String returnsRefunds(
            @RequestParam(required = false) OrderItemReturnStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model) {
        try {
            model.addAttribute("report", returnRefundReportService.buildReturnReport(status, fromDate, toDate));
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Runtime error while loading return report: " + ex.getMessage());
            model.addAttribute("report", new ReturnRefundReportDto());
        }
        model.addAttribute("statuses", OrderItemReturnStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "admin/finance/returns";
    }

    @GetMapping("/refunds")
    public String refunds(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model) {
        try {
            model.addAttribute("report", returnRefundReportService.buildRefundReport(fromDate, toDate));
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Runtime error while loading refund report: " + ex.getMessage());
            model.addAttribute("report", new ReturnRefundReportDto());
        }
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "admin/finance/refunds";
    }
}
