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
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
        model.addAttribute("vendorprofile", vendorprofileRepository.findByUserId(users));
        return "vendor/profile/index";
    }

    @RequestMapping(value = {"/create"})
    public String create(Model model, Vendorprofile vendorprofile) {

        Users users = new Users();

        users.setId(loggedUserService.activeUserid());

        vendorprofile.setUserId(users);

        return "customer/vendor_profile_create";
    }

    @RequestMapping("/save")
    public String save(Model model, @Valid Vendorprofile vendorprofile, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            Users users = new Users();
            users.setId(loggedUserService.activeUserid());
            vendorprofile.setUserId(users);
            return "vendor/profile/vendor_profile_create";
        }

        vendorprofile.setVendorCode(vendorCodeGenerator.generateNextVendorCode());
        vendorprofileRepository.save(vendorprofile);
        return "redirect:/vendorprofile/details";
    }

    @RequestMapping("/edit")
    public String edit(Model model, Vendorprofile vendorprofile, HttpSession session) {

        vendorprofile = vendorUserContext.getActiveVendor();
        model.addAttribute("vendorprofile", vendorprofileRepository.findById(vendorprofile.getId()).orElse(null));
        return "vendor/profile/vendor_profile_create";
    }

    @RequestMapping("/details")
    public String details(Model model, Vendorprofile vendorprofile, HttpSession session) {

        vendorprofile = vendorUserContext.getActiveVendor();
        model.addAttribute("vendorprofile", vendorprofileRepository.findById(vendorprofile.getId()).orElse(null));
        return "vendor/profile/details";
    }

}
