/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import com.ecommerce.app.vendor.services.VendorCodeGenerator;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/vendorprofile")
public class VendorProfileController {

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    VendorprofileRepository vendorprofileRepository;

    @Autowired
    private VendorCodeGenerator vendorCodeGenerator;

    @Autowired
    private VendorUserContext vendorUserContext;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model) {
        Users users = new Users();
        users.setId(loggedUserService.activeUserid());
        List<Vendorprofile> vendorprofiles = vendorprofileRepository.findByUserId(users);

        if (vendorprofiles.isEmpty()) {
            return "redirect:/vendorprofile/create";
        }

        vendorUserContext.setActiveVendor(vendorprofiles.get(0));
        return "redirect:/vendorprofile/details";
    }

    @RequestMapping(value = {"/create"})
    public String create(Model model, Vendorprofile vendorprofile) {

        Users users = new Users();

        users.setId(loggedUserService.activeUserid());

        vendorprofile.setUserId(users);
        model.addAttribute("vendorprofile", vendorprofile);

        return "vendor/profile/vendor_profile_create";
    }

    @RequestMapping("/save")
    public String save(Model model, @Valid Vendorprofile vendorprofile, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        Users users = new Users();
        users.setId(loggedUserService.activeUserid());

        if (bindingResult.hasErrors()) {
            vendorprofile.setUserId(users);
            model.addAttribute("vendorprofile", vendorprofile);
            return "vendor/profile/vendor_profile_create";
        }

        Vendorprofile targetVendorProfile = vendorprofile.getId() != null
                ? vendorprofileRepository.findById(vendorprofile.getId()).orElseGet(Vendorprofile::new)
                : new Vendorprofile();

        if (targetVendorProfile.getId() == null) {
            targetVendorProfile.setVendorCode(vendorCodeGenerator.generateNextVendorCode());
        }

        targetVendorProfile.setUserId(users);
        targetVendorProfile.setCompanyName(vendorprofile.getCompanyName());
        targetVendorProfile.setFirstName(vendorprofile.getFirstName());
        targetVendorProfile.setLastName(vendorprofile.getLastName());
        targetVendorProfile.setDesignation(vendorprofile.getDesignation());
        targetVendorProfile.setPhone(vendorprofile.getPhone());
        targetVendorProfile.setEmail(vendorprofile.getEmail());
        targetVendorProfile.setAddress(vendorprofile.getAddress());
        targetVendorProfile.setDescription(vendorprofile.getDescription());

        if (vendorprofile.getVendorStatusEnum() != null || targetVendorProfile.getVendorStatusEnum() == null) {
            targetVendorProfile.setVendorStatusEnum(vendorprofile.getVendorStatusEnum());
        }

        Vendorprofile savedVendorProfile = vendorprofileRepository.save(targetVendorProfile);
        vendorUserContext.setActiveVendor(savedVendorProfile);
        return "redirect:/vendorprofile/details";
    }

    @RequestMapping("/edit")
    public String edit(Model model, Vendorprofile vendorprofile) {

        vendorprofile = vendorUserContext.getActiveVendor();
        if (vendorprofile == null) {
            return "redirect:/vendorprofile/index";
        }
        model.addAttribute("vendorprofile", vendorprofileRepository.findById(vendorprofile.getId()).orElse(null));
        return "vendor/profile/vendor_profile_create";
    }

    @RequestMapping("/details")
    public String details(Model model, Vendorprofile vendorprofile) {

        vendorprofile = vendorUserContext.getActiveVendor();
        if (vendorprofile == null) {
            return "redirect:/vendorprofile/index";
        }
        model.addAttribute("vendorprofile", vendorprofileRepository.findById(vendorprofile.getId()).orElse(null));
        return "vendor/profile/details";
    }

}
