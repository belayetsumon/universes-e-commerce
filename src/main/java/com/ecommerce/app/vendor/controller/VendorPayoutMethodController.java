/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.module.ReferralRewards.model.CustomerCashOutPaymentMethod;
import com.ecommerce.app.vendor.model.VendorPayoutMethod;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.services.VendorPayoutMethodService;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/vendor-payout-methods")
public class VendorPayoutMethodController {

    @Autowired
    private final VendorPayoutMethodService service;

    @Autowired
    private VendorUserContext vendorUserContext;

    public VendorPayoutMethodController(VendorPayoutMethodService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public String listByVendor(Model model) {
        Long vendorId = vendorUserContext.getActiveVendor().getId();

        model.addAttribute("list", service.findByVendorId(vendorId));

        return "vendor/payoutmethod/list";
    }

    @GetMapping("/create")
    public String createForm(Model model, VendorPayoutMethod vendorPayoutMethod
    ) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();

        vendorPayoutMethod.setVendor(vendor);
        model.addAttribute("pMethod", CustomerCashOutPaymentMethod.values());
        return "vendor/payoutmethod/payout_method_form";
    }

    @PostMapping("/save")
    public String save(Model model,
            @Valid VendorPayoutMethod vendorPayoutMethod,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            Vendorprofile vendor = vendorUserContext.getActiveVendor();

            vendorPayoutMethod.setVendor(vendor);
            model.addAttribute("pMethod", CustomerCashOutPaymentMethod.values());
            return "vendor/payoutmethod/payout_method_form";
        }

        service.save(vendorPayoutMethod);

        redirectAttributes.addFlashAttribute("message", "Payout method saved successfully!");
        return "redirect:/vendor-payout-methods/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, VendorPayoutMethod vendorPayoutMethod) {
        model.addAttribute("vendorPayoutMethod", service.findById(id));
        Vendorprofile vendor = vendorUserContext.getActiveVendor();

        vendorPayoutMethod.setVendor(vendor);
        model.addAttribute("pMethod", CustomerCashOutPaymentMethod.values());
        return "vendor/payoutmethod/payout_method_form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        service.delete(id);
        redirectAttributes.addFlashAttribute("message", "Payout method deleted successfully!");
        return "redirect:/vendor-payout-methods/list";
    }

}
