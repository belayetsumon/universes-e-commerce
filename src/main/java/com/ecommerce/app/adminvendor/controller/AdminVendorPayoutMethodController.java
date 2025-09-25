/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.adminvendor.controller;

import com.ecommerce.app.vendor.model.VendorPayoutMethod;
import com.ecommerce.app.vendor.services.VendorPayoutMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/admin-vendor-payout-methods")
public class AdminVendorPayoutMethodController {

    @Autowired
    private VendorPayoutMethodService service;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("methods", service.findAll());
        return "vendor_payout_method/list";
    }

    @GetMapping("/create")
    public String createForm(Model model, VendorPayoutMethod vendorPayoutMethod) {
        model.addAttribute("method", vendorPayoutMethod);
        return "vendor_payout_method/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute VendorPayoutMethod method) {
        service.save(method);
        return "redirect:/vendor-payout-methods";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("method", service.findById(id));
        return "vendor_payout_method/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/vendor-payout-methods";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("method", service.findById(id));
        return "vendor_payout_method/view";
    }

}
