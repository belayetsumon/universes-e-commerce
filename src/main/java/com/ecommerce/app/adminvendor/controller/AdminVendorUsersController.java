package com.ecommerce.app.adminvendor.controller;

import com.ecommerce.app.adminvendor.services.AdminVendorIamService;
import com.ecommerce.app.security.permission.PlatformVendorManagementPermissions;
import com.ecommerce.app.vendor.user.model.VendorPrivilege;
import com.ecommerce.app.vendor.user.model.VendorRole;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/adminvendorusers")
@PreAuthorize(PlatformVendorManagementPermissions.CAN_ACCESS)
public class AdminVendorUsersController {

    private final AdminVendorIamService vendorIamService;

    public AdminVendorUsersController(AdminVendorIamService vendorIamService) {
        this.vendorIamService = vendorIamService;
    }

    @GetMapping("/rolelist")
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_READ)
    public String list(Model model) {
        model.addAttribute("roles", vendorIamService.findAllRoles());
        return "vendor/users/vendor_role_list";
    }

    @GetMapping("/role_add")
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_READ)
    public String addForm(Model model) {
        model.addAttribute("vendorRole", new VendorRole());
        model.addAttribute("allPrivileges", vendorIamService.findAllAssignablePrivileges());
        return "vendor/users/vendor_role_form";
    }

    @PostMapping("/role_save")
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_MANAGE)
    public String save(
            @Valid @ModelAttribute("vendorRole") VendorRole vendorRole,
            BindingResult result,
            @RequestParam(value = "privilegeIds", required = false) List<Long> privilegeIds,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("allPrivileges", vendorIamService.findAllAssignablePrivileges());
            return "vendor/users/vendor_role_form";
        }
        try {
            vendorIamService.saveRole(vendorRole, privilegeIds);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            result.reject("vendorRole.save", exception.getMessage());
            model.addAttribute("allPrivileges", vendorIamService.findAllAssignablePrivileges());
            return "vendor/users/vendor_role_form";
        }
        return "redirect:/adminvendorusers/rolelist";
    }

    @GetMapping("/role_edit/{id}")
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_READ)
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("vendorRole", vendorIamService.findRole(id));
        model.addAttribute("allPrivileges", vendorIamService.findAllAssignablePrivileges());
        return "vendor/users/vendor_role_form";
    }

    @PostMapping("/role_delete/{id}")
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_DELETE)
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            vendorIamService.deleteRole(id);
            redirectAttributes.addFlashAttribute("successMessage", "Vendor role deleted successfully.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/adminvendorusers/rolelist";
    }

    @GetMapping("/privilegeslist")
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_MANAGE_PRIVILEGES)
    public String privilegeslist(Model model) {
        model.addAttribute("vendorPrivileges", vendorIamService.findAllPrivilegesForAdministration());
        return "vendor/users/privilegelist";
    }

    @GetMapping("/privileges_add")
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_MANAGE_PRIVILEGES)
    public String privilegeslistaddForm(Model model) {
        model.addAttribute("vendorPrivilege", new VendorPrivilege());
        return "vendor/users/vendor_privilege_form";
    }

    @PostMapping("/privileges_save")
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_MANAGE_PRIVILEGES)
    public String save(
            @Valid @ModelAttribute("vendorPrivilege") VendorPrivilege vendorPrivilege,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "vendor/users/vendor_privilege_form";
        }
        try {
            vendorIamService.savePrivilege(vendorPrivilege);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            result.reject("vendorPrivilege.save", exception.getMessage());
            return "vendor/users/vendor_privilege_form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Saved successfully!");
        return "redirect:/adminvendorusers/privilegeslist";
    }

    @GetMapping("/privileges_edit/{id}")
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_MANAGE_PRIVILEGES)
    public String privilegeslisteditForm(@PathVariable Long id, Model model) {
        model.addAttribute("vendorPrivilege", vendorIamService.findPrivilege(id));
        return "vendor/users/vendor_privilege_form";
    }

    @PostMapping("/privileges_delete/{id}")
    @PreAuthorize(PlatformVendorManagementPermissions.CAN_DELETE)
    public String privilegeslistdelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            vendorIamService.deletePrivilege(id);
            redirectAttributes.addFlashAttribute("successMessage", "Deleted successfully!");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/adminvendorusers/privilegeslist";
    }
}
