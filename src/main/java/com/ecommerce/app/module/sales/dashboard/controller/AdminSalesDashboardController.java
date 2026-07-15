package com.ecommerce.app.module.sales.dashboard.controller;

import com.ecommerce.app.module.sales.dashboard.dto.SalesDashboardDateRange;
import com.ecommerce.app.module.sales.dashboard.dto.SalesDashboardFilter;
import com.ecommerce.app.module.sales.dashboard.dto.SalesDashboardView;
import com.ecommerce.app.module.sales.dashboard.service.SalesDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin/sales")
@PreAuthorize("hasAuthority('admin')")
public class AdminSalesDashboardController {

    private final SalesDashboardService salesDashboardService;

    public AdminSalesDashboardController(SalesDashboardService salesDashboardService) {
        this.salesDashboardService = salesDashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@ModelAttribute SalesDashboardFilter filter, Model model) {
        model.addAttribute("pageTitle", "Sales Dashboard");
        model.addAttribute("ranges", SalesDashboardDateRange.values());
        model.addAttribute("vendors", salesDashboardService.vendorOptions());
        model.addAttribute("categories", salesDashboardService.categoryOptions());
        model.addAttribute("brands", salesDashboardService.brandOptions());
        model.addAttribute("products", salesDashboardService.productOptions(filter.getVendorId()));
        model.addAttribute("customers", salesDashboardService.customerOptions());
        model.addAttribute("carriers", salesDashboardService.carrierOptions());
        model.addAttribute("orderStatuses", salesDashboardService.orderStatuses());
        model.addAttribute("paymentMethods", salesDashboardService.paymentMethods());
        model.addAttribute("salesChannels", salesDashboardService.salesChannels());
        model.addAttribute("currencies", salesDashboardService.currencies());
        model.addAttribute("filter", filter);
        model.addAttribute("sales", salesDashboardService.build(filter, null, true));
        return "admin/sales/dashboard";
    }

    @GetMapping("/dashboard/data")
    @ResponseBody
    public SalesDashboardView dashboardData(@ModelAttribute SalesDashboardFilter filter) {
        return salesDashboardService.build(filter, null, true);
    }
}
