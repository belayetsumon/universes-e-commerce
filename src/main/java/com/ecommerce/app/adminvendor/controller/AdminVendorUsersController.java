/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.adminvendor.controller;

import com.ecommerce.app.vendor.user.model.VendorPrivilege;
import com.ecommerce.app.vendor.user.model.VendorRole;
import com.ecommerce.app.vendor.user.repository.VendorPrivilegeRepository;
import com.ecommerce.app.vendor.user.services.VendorPrivilegeService;
import com.ecommerce.app.vendor.user.services.VendorRoleService;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/adminvendorusers")
public class AdminVendorUsersController {

    @Autowired
    private VendorPrivilegeService vpservice;

    @Autowired
    private VendorRoleService roleService;

    @Autowired
    private VendorPrivilegeRepository privilegeRepository; // To list privileges in form

    @GetMapping("/rolelist")
    public String list(Model model) {
        model.addAttribute("roles", roleService.findAll());
        return "vendor/users/vendor_role_list";
    }

    @GetMapping("/role_add")
    public String addForm(Model model) {
        model.addAttribute("vendorRole", new VendorRole());
        model.addAttribute("allPrivileges", privilegeRepository.findAll());
        return "vendor/users/vendor_role_form";
    }

    @PostMapping("/role_save")
    public String save(
            @Valid VendorRole vendorRole,
            BindingResult result,
            @RequestParam(value = "vendorPrivilege", required = false) List<Long> privilegeIds,
            Model model) {
        System.out.println("error here 0#########################################");
        if (result.hasErrors()) {
            model.addAttribute("allPrivileges", privilegeRepository.findAll());
            return "vendor/users/vendor_role_form";
        }
        System.out.println("error here 1#########################################");
        if (privilegeIds != null) {
            Set<VendorPrivilege> privileges = new HashSet<>(privilegeRepository.findAllById(privilegeIds));
            vendorRole.setVendorPrivilege(privileges);
        } else {
            vendorRole.setVendorPrivilege(new HashSet<>());
        }
        System.out.println("error here 2 #########################################");
        roleService.save(vendorRole);

        return "redirect:/adminvendorusers/rolelist";
    }

    @GetMapping("/role_edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        VendorRole role = roleService.findById(id);
        model.addAttribute("vendorRole", role);
        model.addAttribute("allPrivileges", privilegeRepository.findAll());
        return "vendor/users/vendor_role_form";
    }

    @GetMapping("/role_delete/{id}")
    public String delete(@PathVariable Long id) {
        roleService.deleteById(id);
        return "redirect:/adminvendorusers/rolelist";
    }

    // ##################################################
    //// prevelage
    @GetMapping("/privilegeslist")
    public String privilegeslist(Model model) {
        model.addAttribute("vendorPrivileges", vpservice.findAll());
        return "vendor/users/privilegelist";
    }

    @GetMapping("/privileges_add")
    public String privilegeslistaddForm(Model model) {
        model.addAttribute("vendorPrivilege", new VendorPrivilege());
        return "vendor/users/vendor_privilege_form";
    }

    @PostMapping("/privileges_save")
    public String save(@Valid @ModelAttribute VendorPrivilege vendorPrivilege, BindingResult result, Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "vendor/users/vendor_privilege_form";
        }
        vpservice.save(vendorPrivilege);
        redirectAttributes.addFlashAttribute("successMessage", "Saved successfully!");
        return "redirect:/adminvendorusers/privilegeslist";
    }

    @GetMapping("/privileges_edit/{id}")
    public String privilegeslisteditForm(@PathVariable Long id, Model model) {
        VendorPrivilege vp = vpservice.findById(id);
        model.addAttribute("vendorPrivilege", vp);
        return "vendor/users/vendor_privilege_form";
    }

    @GetMapping("/privileges_delete/{id}")
    public String privilegeslistdelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        vpservice.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Deleted successfully!");
        return "redirect:/adminvendorusers/privilegeslist";
    }

}
