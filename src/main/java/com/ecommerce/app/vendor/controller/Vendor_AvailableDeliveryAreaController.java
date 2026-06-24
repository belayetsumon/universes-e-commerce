/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.module.shipping.services.ShippingLocationService;
import com.ecommerce.app.product.model.AvailableDeliveryArea;
import com.ecommerce.app.product.model.AvailableDeliveryAreaMode;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.ripository.AvailableDeliveryAreaRepository;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/vendor_availabledeliveryarea")
public class Vendor_AvailableDeliveryAreaController {

    @Autowired
    AvailableDeliveryAreaRepository availableDeliveryAreaRepository;

    @Autowired
    ShippingLocationService shippingLocationService;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    VendorUserContext vendorUserContext;

    @RequestMapping("/index")
    public String index(Model model) {
        model.addAttribute("attribute", "value");
        return "view.name";
    }

    @GetMapping("/list")
    public String viewAllUnits(Model model) {
        model.addAttribute("list", "");
        return "product/unit/list";
    }

    @GetMapping("/by_product/{id}")
    public String byProduct(Model model, @PathVariable Long id, AvailableDeliveryArea availableDeliveryArea) {

        return "/";
    }

    @GetMapping("/add/{pid}")
    public String add(Model model, @PathVariable Long pid) {
        Product product = productRepository.findById(pid).orElse(null);
        if (!isOwnedByActiveVendor(product)) {
            AvailableDeliveryArea availableDeliveryArea = new AvailableDeliveryArea();
            populateFormModel(model, availableDeliveryArea, "Product not found for the active vendor.");
            return "vendor/product/deliveryoption/delivery_area";
        }

        AvailableDeliveryArea availableDeliveryArea = new AvailableDeliveryArea();
        availableDeliveryArea.setProduct(product);
        populateFormModel(model, availableDeliveryArea, null);
        return "vendor/product/deliveryoption/delivery_area";
    }

    @PostMapping("/save")
    public Object save(@Valid AvailableDeliveryArea availableDeliveryArea, BindingResult bindingResult, HttpServletResponse response, Model model) {
        // 2026-04-22: Normalize fields by selected mode so validation and saved data stay predictable.
        normalizeByMode(availableDeliveryArea);
        if (!isOwnedByActiveVendor(availableDeliveryArea != null ? availableDeliveryArea.getProduct() : null)) {
            populateFormModel(model, availableDeliveryArea, "Product not found for the active vendor.");
            return "vendor/product/deliveryoption/delivery_area";
        }

        String errorMessage = validateDeliveryArea(availableDeliveryArea);
        if (bindingResult.hasErrors() || errorMessage != null) {
            if (errorMessage == null && bindingResult.hasErrors()) {
                errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            }
            // 2026-04-22: Keep 200 status so HTMX swaps the modal body with the validation message.
            populateFormModel(model, availableDeliveryArea, errorMessage);
            return "vendor/product/deliveryoption/delivery_area";
        }

        try {
            availableDeliveryAreaRepository.save(availableDeliveryArea);
            response.setHeader("HX-Refresh", "true");
            // 2026-04-22: Return an empty 200 response so HTMX can honor HX-Refresh without view resolution errors.
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            // 2026-04-22: Keep 200 status so runtime problems are rendered inside the modal.
            populateFormModel(model, availableDeliveryArea, "Runtime error while saving delivery area: " + ex.getMessage());
            return "vendor/product/deliveryoption/delivery_area";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id) {
        Optional<AvailableDeliveryArea> availableDeliveryAreaopt = availableDeliveryAreaRepository.findById(id);

        AvailableDeliveryArea availableDeliveryArea = availableDeliveryAreaopt.orElse(null);
        if (availableDeliveryArea == null || !isOwnedByActiveVendor(availableDeliveryArea.getProduct())) {
            AvailableDeliveryArea fallback = new AvailableDeliveryArea();
            populateFormModel(model, fallback, "Delivery area not found for the active vendor.");
            return "vendor/product/deliveryoption/delivery_area";
        }

        populateFormModel(model, availableDeliveryArea, null);

        return "vendor/product/deliveryoption/delivery_area";
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public String deleteUnit(@PathVariable Long id, HttpServletResponse response) {
        String message;
        String messageType;
        Optional<AvailableDeliveryArea> existing = availableDeliveryAreaRepository.findById(id);
        if (existing.isEmpty()) {
            message = "Error: Item not found!";
            messageType = "danger"; // Error message type
        } else if (!isOwnedByActiveVendor(existing.get().getProduct())) {
            message = "Error: Item not found for the active vendor!";
            messageType = "danger";
        } else {
            try {
                // 2026-04-22: Delete only once so the UI does not trigger a second missing-row failure.
                availableDeliveryAreaRepository.deleteById(id);
                message = "Deleted successfully!";
                messageType = "success"; // Success message type
            } catch (Exception ex) {
                message = "Runtime error while deleting delivery area: " + ex.getMessage();
                messageType = "danger";
            }
        }
        response.setHeader("HX-Refresh", "true");
        return "<div id='messageContainer' class='alert alert-" + messageType + "'>" + message + "</div>";
    }

    private void populateFormModel(Model model, AvailableDeliveryArea availableDeliveryArea, String errorMessage) {
        model.addAttribute("deliveryAreaModes", AvailableDeliveryAreaMode.values());
        model.addAttribute("districts", shippingLocationService.getActiveLocations());
        model.addAttribute("availableDeliveryArea", availableDeliveryArea);
        model.addAttribute("errorMessage", errorMessage);
    }

    private void normalizeByMode(AvailableDeliveryArea availableDeliveryArea) {
        if (availableDeliveryArea == null || availableDeliveryArea.getMode() == null) {
            return;
        }

        if (availableDeliveryArea.getExcludedDistricts() == null) {
            availableDeliveryArea.setExcludedDistricts(new ArrayList<>());
        }
        if (availableDeliveryArea.getSelectedDistricts() == null) {
            availableDeliveryArea.setSelectedDistricts(new ArrayList<>());
        }

        switch (availableDeliveryArea.getMode()) {
            case ALL_AREA -> {
                availableDeliveryArea.setDistrict(null);
                availableDeliveryArea.getSelectedDistricts().clear();
                availableDeliveryArea.getExcludedDistricts().clear();
            }
            case ALL_AREA_EXCEPT -> {
                availableDeliveryArea.setDistrict(null);
                availableDeliveryArea.getSelectedDistricts().clear();
            }
            case SPECIFIC_AREA -> {
                availableDeliveryArea.getExcludedDistricts().clear();
                if (availableDeliveryArea.getDistrict() != null
                        && availableDeliveryArea.getSelectedDistricts().isEmpty()) {
                    // 2026-04-22: Keep older single-district records compatible with the new multi-select mode.
                    availableDeliveryArea.getSelectedDistricts().add(availableDeliveryArea.getDistrict());
                }
                availableDeliveryArea.setDistrict(null);
            }
        }
    }

    private String validateDeliveryArea(AvailableDeliveryArea availableDeliveryArea) {
        if (availableDeliveryArea == null || availableDeliveryArea.getMode() == null) {
            return "Please select a delivery mode.";
        }

        return switch (availableDeliveryArea.getMode()) {
            case SPECIFIC_AREA -> (availableDeliveryArea.getSelectedDistricts() == null || availableDeliveryArea.getSelectedDistricts().isEmpty())
                    ? "Please select at least one district for Specific Area mode."
                    : null;
            case ALL_AREA -> null;
            case ALL_AREA_EXCEPT -> (availableDeliveryArea.getExcludedDistricts() == null || availableDeliveryArea.getExcludedDistricts().isEmpty())
                    ? "Please select at least one excluded district for All Area Except mode."
                    : null;
        };
    }

    private boolean isOwnedByActiveVendor(Product product) {
        if (product != null && product.getId() != null && product.getVendorprofile() == null) {
            product = productRepository.findById(product.getId()).orElse(product);
        }
        Vendorprofile vendorprofile = vendorUserContext.getActiveVendor();
        return product != null
                && vendorprofile != null
                && vendorprofile.getId() != null
                && product.getVendorprofile() != null
                && Objects.equals(product.getVendorprofile().getId(), vendorprofile.getId());
    }
}
