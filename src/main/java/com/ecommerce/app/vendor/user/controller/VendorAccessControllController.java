/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.vendor.user.controller;

import com.ecommerce.app.vendor.user.model.UserVendorRole;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.services.VendorStaffAdministrationService;
import com.ecommerce.app.vendor.user.services.VendorStaffAdministrationService.VendorStaffAssignmentException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/vendor-users")
public class VendorAccessControllController {

    private final VendorUserContext vendorUserContext;
    private final VendorStaffAdministrationService vendorStaffAdministrationService;

    public VendorAccessControllController(
            VendorUserContext vendorUserContext,
            VendorStaffAdministrationService vendorStaffAdministrationService) {
        this.vendorUserContext = vendorUserContext;
        this.vendorStaffAdministrationService = vendorStaffAdministrationService;
    }

    @GetMapping("/userlist")
    @PreAuthorize(VendorStaffAdministrationService.STAFF_MANAGE_AUTHORIZATION)
    public String vendoruserlist(Model model) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();

        model.addAttribute("list", vendorStaffAdministrationService.listStaff(vendor));

        return "vendor/users/vendor_users_list";
    }

    @GetMapping("/add_vendor_user")
    @PreAuthorize(VendorStaffAdministrationService.STAFF_MANAGE_AUTHORIZATION)
    public String addVendorUser(Model model, @ModelAttribute("userVendorRole") UserVendorRole userVendorRole) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();
        userVendorRole.setVendor(vendor);
        model.addAttribute("companyName", vendor.getCompanyName());

        model.addAttribute("rolelist", vendorStaffAdministrationService.findStaffAssignableRoles(vendor));
        return "vendor/users/vendor_users_form";
    }

    @PostMapping("/save")
    @PreAuthorize(VendorStaffAdministrationService.STAFF_MANAGE_AUTHORIZATION)
    public String assignExistingUserToVendor(
            @RequestParam(value = "email", required = true) String usersEmail,
            @RequestParam(value = "vendorRoleId", required = true) Long vendorRoleId,
            RedirectAttributes redirectAttributes
    ) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();
        try {
            vendorStaffAdministrationService.assignExistingUserToVendor(vendor, usersEmail, vendorRoleId);
        } catch (VendorStaffAssignmentException exception) {
            redirectAttributes.addFlashAttribute("globalErrors", exception.getErrors());
            redirectAttributes.addFlashAttribute("email", usersEmail);
            redirectAttributes.addFlashAttribute("vendorRoleId", vendorRoleId);
            return "redirect:/vendor-users/add_vendor_user";
        } catch (AccessDeniedException | IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("globalErrors", java.util.List.of(exception.getMessage()));
            redirectAttributes.addFlashAttribute("email", usersEmail);
            redirectAttributes.addFlashAttribute("vendorRoleId", vendorRoleId);
            return "redirect:/vendor-users/add_vendor_user";
        }
        redirectAttributes.addFlashAttribute("message", "Saved successfully!");
        return "redirect:/vendor-users/userlist";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize(VendorStaffAdministrationService.STAFF_MANAGE_AUTHORIZATION)
    public String delete(@PathVariable long id, RedirectAttributes redirectAttributes) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();
        try {
            vendorStaffAdministrationService.removeStaffAssignment(vendor, id);
            redirectAttributes.addFlashAttribute("message", "Deleted successfully!");
        } catch (AccessDeniedException | IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/vendor-users/userlist";
    }

//    @PostMapping("/assign-existing")
//    //  @PreAuthorize("@vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')")
//    public String assignExistingUserToVendor(@RequestParam String username,
//            @RequestParam Long roleId) {
//        Vendorprofile vendor = vendorUserContext.getActiveVendor();
//
//        Users user = usersRepository.findByEmailAndStatus(username, Status.Active)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        boolean alreadyAssigned = userVendorRoleRepository.
//                existsByUsers_EmailAndVendor_IdAndRole_Name(username, roleId, username);
//
//        if (alreadyAssigned) {
//            throw new RuntimeException("User already assigned to this vendor");
//        }
//
//        VendorRole vendorRole = vendorRoleRepository.findById(roleId).orElseThrow();
//
//        UserVendorRole uvr = new UserVendorRole();
//        uvr.setUsers(user);
//        uvr.setVendor(vendor);
//        uvr.setVendorRole(vendorRole);
//        userVendorRoleRepository.save(uvr);
//        return "redirect:/vendor/users";
//    }
}
