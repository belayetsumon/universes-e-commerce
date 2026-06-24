package com.ecommerce.app.module.shipping.controller;

import com.ecommerce.app.module.shipping.model.PickupAddress;
import com.ecommerce.app.module.shipping.services.ShippingLocationService;
import com.ecommerce.app.module.shipping.services.PickupAddressService;
import com.ecommerce.app.vendor.services.VendorprofileService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/pickup-addresses")
public class PickupAddressController {

    private final PickupAddressService service;
    private final VendorprofileService vendorprofileService;
    private final ShippingLocationService locationService;

    public PickupAddressController(PickupAddressService service, VendorprofileService vendorprofileService,
            ShippingLocationService locationService) {
        this.service = service;
        this.vendorprofileService = vendorprofileService;
        this.locationService = locationService;
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("addresses", service.getAll());
        return "admin/shipping/pickup_addresses/list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("address", new PickupAddress());
        populate(model);
        return "admin/shipping/pickup_addresses/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        PickupAddress address = service.getById(id);
        if (address == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pickup address not found.");
            return "redirect:/admin/pickup-addresses/list";
        }
        model.addAttribute("address", address);
        populate(model);
        return "admin/shipping/pickup_addresses/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("address") PickupAddress address,
            BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            populate(model);
            return "admin/shipping/pickup_addresses/form";
        }
        service.save(address);
        redirectAttributes.addFlashAttribute("successMessage", "Pickup address saved successfully.");
        return "redirect:/admin/pickup-addresses/list";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Pickup address deleted successfully.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete pickup address because it is used by shipments.");
        }
        return "redirect:/admin/pickup-addresses/list";
    }

    private void populate(Model model) {
        model.addAttribute("vendors", vendorprofileService.findAll());
        model.addAttribute("districts", locationService.getActiveLocations());
    }
}
