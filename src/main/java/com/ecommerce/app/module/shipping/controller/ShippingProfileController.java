/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.shipping.controller;

import com.ecommerce.app.globalServices.District;
import com.ecommerce.app.module.shipping.model.Carrier;
import com.ecommerce.app.module.shipping.model.ShippingProfile;
import com.ecommerce.app.module.shipping.model.ShippingProfile.ProfileType;
import com.ecommerce.app.module.shipping.services.CarrierService;
import com.ecommerce.app.module.shipping.services.ShippingProfileService;
import com.ecommerce.app.vendor.services.VendorprofileService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping("/admin/shipping-profiles")
public class ShippingProfileController {

    @Autowired
    private ShippingProfileService service;

    @Autowired
    CarrierService carrierService;

    @Autowired
    VendorprofileService vendorprofileService;

    public ShippingProfileController(ShippingProfileService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("profiles", service.getAll());
        return "admin/shipping/shipping_profiles/admin_shipping_profiles_list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("profile", new ShippingProfile());
        model.addAttribute("vendors", vendorprofileService.findAll()); // <-- add vendors
        model.addAttribute("types", ProfileType.values());
        model.addAttribute("carriers", carrierService.getAll());
        model.addAttribute("allDistricts", District.values());
        return "admin/shipping/shipping_profiles/admin_shipping_profiles_create";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute ShippingProfile profile,
            BindingResult result,
            @RequestParam(required = false) List<Long> allowedCarriersIds,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            // Re-populate carriers and districts for the form
            model.addAttribute("types", ShippingProfile.ProfileType.values());
            model.addAttribute("vendors", vendorprofileService.findAll()); // <-- add vendors
            model.addAttribute("carriers", carrierService.getAll());
            model.addAttribute("allDistricts", District.values());
            return "admin/shipping/shipping_profiles/admin_shipping_profiles_create";
        }

        // Handle carriers selection
        if (allowedCarriersIds != null) {
            List<Carrier> selectedCarriers = carrierService.findAllById(allowedCarriersIds);
            profile.setAllowedCarriers(selectedCarriers);
        }

        service.save(profile);

        // Add success message for redirect
        redirectAttributes.addFlashAttribute("successMessage", "Shipping profile saved successfully!");
        return "redirect:/admin/shipping-profiles/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        ShippingProfile profile = service.getById(id);
        model.addAttribute("vendors", vendorprofileService.findAll()); // <-- add vendors
        model.addAttribute("profile", profile);
        model.addAttribute("types", ProfileType.values());
        model.addAttribute("carriers", carrierService.getAll());
        model.addAttribute("allDistricts", District.values());
        return "admin/shipping/shipping_profiles/admin_shipping_profiles_create";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        // Check if profile exists
        ShippingProfile profile = service.getById(id);
        if (profile == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shipping profile not found!");
            return "redirect:/admin/shipping-profiles/list";
        }

        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Shipping profile deleted successfully!");
        } catch (DataIntegrityViolationException e) {
            // This exception occurs if the profile is referenced by other entities
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete profile: it is linked to other records.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error occurred while deleting the profile.");
        }

        return "redirect:/admin/shipping-profiles/list";
    }
}
