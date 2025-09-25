/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.adminvendor.controller;

import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.vendor.model.VendorStatusEnum;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/adminvendor")
public class AdminVendorController {

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    VendorprofileRepository vendorprofileRepository;

    @RequestMapping(value = {"/{list}"})
    public String index(Model model) {
        model.addAttribute("list", vendorprofileRepository.findAll());
        return "admin/vendor/admin_vendor_list";
    }

    @RequestMapping(value = "/edit/{id}")
    public String edit(Model model, @PathVariable("id") Long id) {
        model.addAttribute("vendorprofile", vendorprofileRepository.findById(id).orElse(null));
        model.addAttribute("vendor_status", VendorStatusEnum.values());
        return "admin/vendor/vendor_profile_create";
    }

    @RequestMapping("/save")
    public String save(Model model, @Valid Vendorprofile vendorprofile, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("vendor_status", VendorStatusEnum.values());
            return "customer/vendor_profile_create";
        }
        vendorprofileRepository.save(vendorprofile);
        return "redirect:/adminvendor/list";
    }

    @RequestMapping(value = "/view/{id}")
    public String view(Model model, @PathVariable("id") Long id) {
        model.addAttribute("vendor", vendorprofileRepository.findById(id).orElse(null));
        return "admin/vendor/details";
    }

    @RequestMapping("/delete/{id}")
    public String details(Model model, @PathVariable("id") Long id, Vendorprofile vendorprofile) {
        vendorprofileRepository.deleteById(id);
       return "redirect:/adminvendor/list";
    }

}
