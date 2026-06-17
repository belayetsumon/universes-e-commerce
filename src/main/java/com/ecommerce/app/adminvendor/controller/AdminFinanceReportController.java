package com.ecommerce.app.adminvendor.controller;

import com.ecommerce.app.finance.dto.AdminFinanceDashboardDto;
import com.ecommerce.app.finance.services.FinanceReportService;
import java.util.ArrayList;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/finance")
public class AdminFinanceReportController {

    @Autowired
    private FinanceReportService financeReportService;

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
}
