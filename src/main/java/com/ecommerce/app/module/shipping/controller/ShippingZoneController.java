package com.ecommerce.app.module.shipping.controller;

import com.ecommerce.app.module.shipping.model.ShippingZone;
import com.ecommerce.app.module.shipping.services.ShippingLocationService;
import com.ecommerce.app.module.shipping.services.ShippingZoneService;
import jakarta.validation.Valid;
import java.util.List;
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

@Controller
@RequestMapping("/admin/shipping-zones")
public class ShippingZoneController {

    private final ShippingZoneService service;
    private final ShippingLocationService locationService;

    public ShippingZoneController(ShippingZoneService service, ShippingLocationService locationService) {
        this.service = service;
        this.locationService = locationService;
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("zones", service.getAll());
        return "admin/shipping/zones/list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        ShippingZone zone = new ShippingZone();
        model.addAttribute("zone", zone);
        populateFormOptions(model, zone);
        return "admin/shipping/zones/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        ShippingZone zone = service.getById(id);
        if (zone == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shipping zone not found.");
            return "redirect:/admin/shipping-zones/list";
        }
        model.addAttribute("zone", zone);
        populateFormOptions(model, zone);
        return "admin/shipping/zones/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("zone") ShippingZone zone,
            BindingResult result,
            @RequestParam(name = "coverageLocationIds", required = false) List<Long> coverageLocationIds,
            Model model,
            RedirectAttributes redirectAttributes) {
        zone.setCoverageLocations(locationService.findAllById(coverageLocationIds));
        if (zone.getCoverageLocations() == null || zone.getCoverageLocations().isEmpty()) {
            result.reject("shippingZone.coverage.required", "Select at least one country, division, district, or thana.");
        }
        if (result.hasErrors()) {
            populateFormOptions(model, zone);
            return "admin/shipping/zones/form";
        }

        try {
            service.save(zone);
            redirectAttributes.addFlashAttribute("successMessage", "Shipping zone saved successfully.");
            return "redirect:/admin/shipping-zones/list";
        } catch (DataIntegrityViolationException ex) {
            result.reject("shippingZone.save.failed", "Shipping zone could not be saved because code or data conflicts with an existing zone.");
            populateFormOptions(model, zone);
            return "admin/shipping/zones/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Shipping zone deleted successfully.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete this zone because it is used by carrier rates.");
        }
        return "redirect:/admin/shipping-zones/list";
    }

    private void populateFormOptions(Model model, ShippingZone zone) {
        model.addAttribute("locations", locationService.getActiveLocations());
        model.addAttribute("selectedCoverageLocationIds",
                zone != null && zone.getCoverageLocations() != null
                ? zone.getCoverageLocations().stream()
                        .map(location -> location.getId())
                        .filter(java.util.Objects::nonNull)
                        .toList()
                : List.of());
    }
}
