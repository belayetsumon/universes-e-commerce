package com.ecommerce.app.module.shipping.controller;

import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.shipping.model.ShippingLocationType;
import com.ecommerce.app.module.shipping.services.ShippingLocationService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/shipping-locations")
public class ShippingLocationController {

    private final ShippingLocationService service;

    public ShippingLocationController(ShippingLocationService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("locations", service.getAll());
        return "admin/shipping/locations/list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("location", new ShippingLocation());
        populateForm(model, null);
        return "admin/shipping/locations/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        ShippingLocation location = service.getById(id);
        if (location == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shipping location not found.");
            return "redirect:/admin/shipping-locations/list";
        }
        model.addAttribute("location", location);
        populateForm(model, location);
        return "admin/shipping/locations/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("location") ShippingLocation location,
            BindingResult result,
            @RequestParam(name = "parentId", required = false) Long parentId,
            Model model,
            RedirectAttributes redirectAttributes) {
        location.setParent(service.getById(parentId));
        validateHierarchy(location, result);
        if (result.hasErrors()) {
            populateForm(model, location);
            return "admin/shipping/locations/form";
        }

        service.save(location);
        redirectAttributes.addFlashAttribute("successMessage", "Shipping location saved successfully.");
        return "redirect:/admin/shipping-locations/list";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Shipping location deleted successfully.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete this location because it is used by zones or child locations.");
        }
        return "redirect:/admin/shipping-locations/list";
    }

    private void populateForm(Model model, ShippingLocation location) {
        model.addAttribute("types", ShippingLocationType.values());
        model.addAttribute("parents", service.getActiveLocations());
        model.addAttribute("selectedParentId", location != null && location.getParent() != null ? location.getParent().getId() : null);
    }

    private void validateHierarchy(ShippingLocation location, BindingResult result) {
        if (location.getType() == ShippingLocationType.COUNTRY && location.getParent() != null) {
            result.rejectValue("parent", "shippingLocation.parent.invalid", "Country cannot have a parent location.");
        }
        if (location.getType() != ShippingLocationType.COUNTRY && location.getParent() == null) {
            result.rejectValue("parent", "shippingLocation.parent.required", "Division, district, and thana require a parent location.");
        }
        if (location.getId() != null && location.getParent() != null && location.getId().equals(location.getParent().getId())) {
            result.rejectValue("parent", "shippingLocation.parent.self", "Location cannot be its own parent.");
        }
        if (location.getId() != null && hasParentCycle(location)) {
            result.rejectValue("parent", "shippingLocation.parent.cycle", "Location cannot be assigned under one of its own child locations.");
        }
        if (location.getParent() != null && !isValidParentType(location.getType(), location.getParent().getType())) {
            result.rejectValue("parent", "shippingLocation.parent.type",
                    "Invalid parent type. Division requires country, district requires division, and thana requires district.");
        }
    }

    private boolean hasParentCycle(ShippingLocation location) {
        ShippingLocation current = location.getParent();
        while (current != null) {
            if (location.getId().equals(current.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private boolean isValidParentType(ShippingLocationType type, ShippingLocationType parentType) {
        if (type == null) {
            return false;
        }
        return switch (type) {
            case COUNTRY -> parentType == null;
            case DIVISION -> parentType == ShippingLocationType.COUNTRY;
            case DISTRICT -> parentType == ShippingLocationType.DIVISION;
            case THANA -> parentType == ShippingLocationType.DISTRICT;
        };
    }
}
