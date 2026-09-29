package com.ecommerce.app.vendor.user.controller;

import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import com.ecommerce.app.vendor.user.model.VendorRole;
import com.ecommerce.app.vendor.user.services.VendorStaffAdministrationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vendor-users/roles")
@PreAuthorize(VendorStaffAdministrationService.ROLE_MANAGE_AUTHORIZATION)
public class VendorRoleManagementController {

    private final VendorUserContext vendorUserContext;
    private final VendorStaffAdministrationService vendorStaffAdministrationService;

    public VendorRoleManagementController(
            VendorUserContext vendorUserContext,
            VendorStaffAdministrationService vendorStaffAdministrationService) {
        this.vendorUserContext = vendorUserContext;
        this.vendorStaffAdministrationService = vendorStaffAdministrationService;
    }

    @GetMapping
    public String list(Model model) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();
        model.addAttribute("roles", vendorStaffAdministrationService.listRoles(vendor));
        model.addAttribute("companyName", vendor.getCompanyName());
        return "vendor/users/vendor_role_manage_list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();
        VendorRole vendorRole = new VendorRole();
        vendorRole.setVendor(vendor);
        populateForm(model, vendorRole, vendor);
        return "vendor/users/vendor_role_manage_form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();
        VendorRole role = vendorStaffAdministrationService.findRole(vendor, id);
        populateForm(model, role, vendor);
        return "vendor/users/vendor_role_manage_form";
    }

    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("vendorRole") VendorRole vendorRole,
            BindingResult result,
            @RequestParam(value = "vendorPrivilege", required = false) List<Long> privilegeIds,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();

        if (vendorStaffAdministrationService.slugExistsForVendor(vendor, vendorRole.getSlug(), vendorRole.getId())) {
            result.rejectValue("slug", "vendorRole.slug", "Slug already exists for this vendor.");
        }

        if (result.hasErrors()) {
            populateForm(model, vendorRole, vendor);
            return "vendor/users/vendor_role_manage_form";
        }

        try {
            vendorStaffAdministrationService.saveRole(vendor, vendorRole, privilegeIds);
        } catch (AccessDeniedException | IllegalArgumentException exception) {
            result.reject("vendorRole.invalid", exception.getMessage());
            populateForm(model, vendorRole, vendor);
            return "vendor/users/vendor_role_manage_form";
        }
        redirectAttributes.addFlashAttribute("message", "Vendor role saved successfully.");
        return "redirect:/vendor-users/roles";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();
        try {
            vendorStaffAdministrationService.deleteRole(vendor, id);
            redirectAttributes.addFlashAttribute("message", "Vendor role deleted successfully.");
        } catch (AccessDeniedException | IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/vendor-users/roles";
    }

    private void populateForm(Model model, VendorRole vendorRole, Vendorprofile vendor) {
        vendorRole.setVendor(vendor);
        model.addAttribute("vendorRole", vendorRole);
        model.addAttribute("companyName", vendor.getCompanyName());
        model.addAttribute("allPrivileges", vendorStaffAdministrationService.findRoleAssignablePrivileges(vendor));
    }
}
