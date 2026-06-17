package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.finance.dto.VendorFinanceDashboardDto;
import com.ecommerce.app.finance.services.FinanceReportService;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vendor/finance")
public class VendorFinanceReportController {

    @Autowired
    private FinanceReportService financeReportService;

    @Autowired
    private VendorUserContext vendorUserContext;

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.report.read')
//            or @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.payout.read')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            if (vendorUserContext.getActiveVendor() == null || vendorUserContext.getActiveVendor().getId() == null) {
                model.addAttribute("errorMessage", "Vendor session not found. Please select or login to your vendor account again.");
                model.addAttribute("dashboard", new VendorFinanceDashboardDto());
                return "vendor/finance/dashboard";
            }

            Long vendorId = vendorUserContext.getActiveVendor().getId();
            VendorFinanceDashboardDto dashboard = financeReportService.getVendorDashboard(vendorId);
            model.addAttribute("dashboard", dashboard);
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Runtime error while loading vendor finance dashboard: " + ex.getMessage());
            model.addAttribute("dashboard", new VendorFinanceDashboardDto());
        }
        return "vendor/finance/dashboard";
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.report.read')
//            or @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.payout.read')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @GetMapping("/ledger")
    public String ledger(Model model) {
        try {
            if (vendorUserContext.getActiveVendor() == null || vendorUserContext.getActiveVendor().getId() == null) {
                model.addAttribute("errorMessage", "Vendor session not found. Please select or login to your vendor account again.");
                model.addAttribute("ledgerRows", new ArrayList<>());
                return "vendor/finance/ledger";
            }

            Long vendorId = vendorUserContext.getActiveVendor().getId();
            model.addAttribute("ledgerRows", financeReportService.getVendorLedger(vendorId));
            model.addAttribute("vendorprofile", vendorUserContext.getActiveVendor());
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Runtime error while loading vendor ledger: " + ex.getMessage());
            model.addAttribute("ledgerRows", new ArrayList<>());
            model.addAttribute("vendorprofile", vendorUserContext.getActiveVendor());
        }
        return "vendor/finance/ledger";
    }
}
