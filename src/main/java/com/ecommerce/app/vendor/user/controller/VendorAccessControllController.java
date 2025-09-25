/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.vendor.user.controller;

import com.ecommerce.app.module.user.model.Status;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.vendor.user.model.UserVendorRole;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.model.VendorRole;
import com.ecommerce.app.vendor.user.repository.UserVendorRoleRepository;
import com.ecommerce.app.vendor.user.repository.VendorRoleRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @Autowired
    private VendorUserContext vendorUserContext;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    UserVendorRoleRepository userVendorRoleRepository;

    @Autowired
    VendorRoleRepository vendorRoleRepository;

    @RequestMapping("/userlist")
    //  @PreAuthorize("@vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')")
    public String vendoruserlist(Model model) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();

        model.addAttribute("list", userVendorRoleRepository.findAllByVendor(vendor));

        return "vendor/users/vendor_users_list";
    }

    @RequestMapping("/add_vendor_user")
    //  @PreAuthorize("@vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')")
    public String addVendorUser(Model model, UserVendorRole userVendorRole) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();
        userVendorRole.setVendor(vendor);
        model.addAttribute("companyName", vendor.getCompanyName());

        model.addAttribute("rolelist", vendorRoleRepository.findAll());
        return "vendor/users/vendor_users_form";
    }

    @PostMapping("/save")
    //  @PreAuthorize("@vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')")
    public String assignExistingUserToVendor(
            @RequestParam(value = "email", required = true) String usersEmail,
            @RequestParam(value = "vendorRoleId", required = true) Long vendorRoleId,
            RedirectAttributes redirectAttributes
    ) {
        // Normal bean validation
        List<String> errors = new ArrayList<>();

        // Email validation
        if (usersEmail == null || usersEmail.isBlank()) {
            errors.add("Email is required.");
        } else if (!usersEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            errors.add("Invalid email format.");
        }

        if (vendorRoleId == null || vendorRoleId <= 0) {
            errors.add("Please select a valid vendor role.");
        }
        Vendorprofile vendor = vendorUserContext.getActiveVendor();

        Users user = usersRepository.findByEmailAndStatus(usersEmail, Status.Active);

        if (user == null) {
            errors.add("No active user found with this email.");
        }

        if (errors.isEmpty()) {
            // Only check alreadyAssigned if no previous errors
            boolean alreadyAssigned = userVendorRoleRepository
                    .existsByUsers_EmailAndVendor_Id(user.getEmail(), vendor.getId());

            if (alreadyAssigned) {
                errors.add("This user is already assigned to this vendor with the selected role.");
            }
        }
        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("globalErrors", errors);
            redirectAttributes.addFlashAttribute("email", usersEmail);
            redirectAttributes.addFlashAttribute("vendorRoleId", vendorRoleId);
            return "redirect:/vendor-users/add_vendor_user";
        }

        // 2️⃣ Check if vendor is active
        VendorRole vendorRole = vendorRoleRepository.findById(vendorRoleId).orElseThrow();

        UserVendorRole uvr = new UserVendorRole();
        uvr.setUsers(user);
        uvr.setVendor(vendor);
        uvr.setVendorRole(vendorRole);
        userVendorRoleRepository.save(uvr);
        redirectAttributes.addFlashAttribute("message", "Saved successfully!");
        return "redirect:/vendor-users/userlist";
    }

    @RequestMapping("/delete/{id}")
    //  @PreAuthorize("@vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')")
    public String delete(Model model, @PathVariable long id, RedirectAttributes redirectAttributes) {

        userVendorRoleRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Deleted successfully!");
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
