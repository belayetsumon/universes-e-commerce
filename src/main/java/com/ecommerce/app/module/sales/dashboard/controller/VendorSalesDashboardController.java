package com.ecommerce.app.module.sales.dashboard.controller;

import com.ecommerce.app.module.sales.dashboard.dto.SalesDashboardDateRange;
import com.ecommerce.app.module.sales.dashboard.dto.SalesDashboardFilter;
import com.ecommerce.app.module.sales.dashboard.dto.SalesDashboardView;
import com.ecommerce.app.module.sales.dashboard.service.SalesDashboardService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/vendor/sales")
public class VendorSalesDashboardController {

    private final SalesDashboardService salesDashboardService;
    private final VendorUserContext vendorUserContext;
    private final VendorprofileRepository vendorprofileRepository;
    private final LoggedUserService loggedUserService;

    public VendorSalesDashboardController(SalesDashboardService salesDashboardService,
            VendorUserContext vendorUserContext, VendorprofileRepository vendorprofileRepository,
            LoggedUserService loggedUserService) {
        this.salesDashboardService = salesDashboardService;
        this.vendorUserContext = vendorUserContext;
        this.vendorprofileRepository = vendorprofileRepository;
        this.loggedUserService = loggedUserService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@ModelAttribute SalesDashboardFilter filter, Model model) {
        Vendorprofile vendor = resolveVendor();
        if (vendor == null) {
            return "redirect:/vendorprofile/index";
        }
        model.addAttribute("pageTitle", "Vendor Sales Dashboard");
        model.addAttribute("ranges", SalesDashboardDateRange.values());
        model.addAttribute("categories", salesDashboardService.categoryOptions());
        model.addAttribute("brands", salesDashboardService.brandOptions());
        model.addAttribute("products", salesDashboardService.productOptions(vendor.getId()));
        model.addAttribute("customers", salesDashboardService.customerOptions());
        model.addAttribute("carriers", salesDashboardService.carrierOptions());
        model.addAttribute("orderStatuses", salesDashboardService.orderStatuses());
        model.addAttribute("paymentMethods", salesDashboardService.paymentMethods());
        model.addAttribute("salesChannels", salesDashboardService.salesChannels());
        model.addAttribute("currencies", salesDashboardService.currencies());
        model.addAttribute("filter", filter);
        model.addAttribute("sales", salesDashboardService.build(filter, vendor.getId(), false));
        return "vendor/sales/dashboard";
    }

    @GetMapping("/dashboard/data")
    @ResponseBody
    public SalesDashboardView dashboardData(@ModelAttribute SalesDashboardFilter filter) {
        Vendorprofile vendor = resolveVendor();
        if (vendor == null) {
            throw new IllegalStateException("No active vendor profile is available for this session.");
        }
        return salesDashboardService.build(filter, vendor.getId(), false);
    }

    private Vendorprofile resolveVendor() {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor != null && activeVendor.getId() != null) {
            return activeVendor;
        }

        Users currentUser = new Users();
        currentUser.setId(loggedUserService.activeUserid());
        List<Vendorprofile> vendors = vendorprofileRepository.findByUserId(currentUser);
        if (vendors.isEmpty()) {
            return null;
        }
        vendorUserContext.setActiveVendor(vendors.get(0));
        return vendors.get(0);
    }
}
