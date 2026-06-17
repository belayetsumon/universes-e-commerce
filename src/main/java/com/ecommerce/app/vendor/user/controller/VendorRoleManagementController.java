package com.ecommerce.app.vendor.user.controller;

import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import com.ecommerce.app.vendor.user.model.VendorPrivilege;
import com.ecommerce.app.vendor.user.model.VendorRole;
import com.ecommerce.app.vendor.user.repository.VendorPrivilegeRepository;
import com.ecommerce.app.vendor.user.services.VendorRoleService;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
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
@PreAuthorize("""
        @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.role.manage')
        or @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.staff.manage')
        or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
        or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
        or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
        """)
public class VendorRoleManagementController {

    @Autowired
    private VendorUserContext vendorUserContext;

    @Autowired
    private VendorRoleService vendorRoleService;

    @Autowired
    private VendorPrivilegeRepository vendorPrivilegeRepository;

    @GetMapping
    public String list(Model model) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();
        model.addAttribute("roles", vendorRoleService.findAllByVendor(vendor));
        model.addAttribute("companyName", vendor.getCompanyName());
        return "vendor/users/vendor_role_manage_list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();
        VendorRole vendorRole = new VendorRole();
        vendorRole.setVendor(vendor);
        populateForm(model, vendorRole, vendor.getCompanyName());
        return "vendor/users/vendor_role_manage_form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();
        VendorRole role = vendorRoleService.findByIdAndVendor(id, vendor);
        populateForm(model, role, vendor.getCompanyName());
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
        vendorRole.setVendor(vendor);

        if (vendorRoleService.slugExistsForVendor(vendor, vendorRole.getSlug(), vendorRole.getId())) {
            result.rejectValue("slug", "vendorRole.slug", "Slug already exists for this vendor.");
        }

        if (result.hasErrors()) {
            populateForm(model, vendorRole, vendor.getCompanyName());
            return "vendor/users/vendor_role_manage_form";
        }

        Set<VendorPrivilege> privileges = privilegeIds == null
                ? new HashSet<>()
                : new HashSet<>(vendorPrivilegeRepository.findAllById(privilegeIds));
        vendorRole.setVendorPrivilege(privileges);

        vendorRoleService.save(vendorRole);
        redirectAttributes.addFlashAttribute("message", "Vendor role saved successfully.");
        return "redirect:/vendor-users/roles";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();
        VendorRole role = vendorRoleService.findByIdAndVendor(id, vendor);
        vendorRoleService.deleteById(role.getId());
        redirectAttributes.addFlashAttribute("message", "Vendor role deleted successfully.");
        return "redirect:/vendor-users/roles";
    }

    private void populateForm(Model model, VendorRole vendorRole, String companyName) {
        model.addAttribute("vendorRole", vendorRole);
        model.addAttribute("companyName", companyName);
        model.addAttribute("allPrivileges", vendorPrivilegeRepository.findAll());
    }
}
