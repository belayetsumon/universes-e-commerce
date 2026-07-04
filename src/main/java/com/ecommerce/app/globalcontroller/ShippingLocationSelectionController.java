/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.globalcontroller;

import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import com.ecommerce.app.module.shipping.model.ShippingLocationType;
import com.ecommerce.app.module.shipping.services.ShippingLocationService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/district")
public class ShippingLocationSelectionController {

    private final ShippingLocationService locationService;

    public ShippingLocationSelectionController(ShippingLocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/select-district")
    public String districtPage(Model model) {

        model.addAttribute("districts", locationService.getActiveDistricts());
        return "/district/select-district"; // Name of Thymeleaf HTML file
    }

    @GetMapping("/thanas")
    @ResponseBody
    public List<LocationOption> thanas(@RequestParam(name = "districtId") Long districtId) {
        return locationService.getActiveChildren(districtId, ShippingLocationType.THANA).stream()
                .map(location -> new LocationOption(location.getId(), location.getName(), location.getDisplayLabel()))
                .toList();
    }

    @PostMapping("/save-district")
    @ResponseBody
    public String saveLocation(@RequestParam(name = "location") String location, HttpSession session) {
        return saveLocationSelection(location, session);
    }

    @PostMapping("/select")
    @ResponseBody
    public String selectLocation(@RequestParam(name = "location") String location, HttpSession session) {
        return saveLocationSelection(location, session);
    }

    private String saveLocationSelection(String locationValue, HttpSession session) {
        ShippingLocation location = resolveLocation(locationValue);
        if (location != null && location.isActive()) {
            session.setAttribute("shippingLocation", location);
            session.setAttribute("shippingLocationId", location.getId());
            clearShippingSelections(session);
            return "success";
        }
        return "error";
    }

    private ShippingLocation resolveLocation(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return locationService.getById(Long.valueOf(value.trim()));
        } catch (NumberFormatException ignored) {
            String normalized = value.trim();
            return locationService.getActiveLocations().stream()
                    .filter(location -> normalized.equalsIgnoreCase(location.getCode())
                    || normalized.equalsIgnoreCase(location.getName())
                    || normalized.equalsIgnoreCase(location.getDisplayLabel()))
                    .findFirst()
                    .orElse(null);
        }
    }

    private void clearShippingSelections(HttpSession session) {
        session.removeAttribute("shippingCosts");

        List<CartItem> cart = (List<CartItem>) session.getAttribute("sessioncart");
        if (cart == null || cart.isEmpty()) {
            return;
        }

        cart.stream()
                .map(CartItem::getVendorId)
                .distinct()
                .forEach(vendorId -> {
                    session.removeAttribute("shippingCost_" + vendorId);
                    session.removeAttribute("shippingOption_" + vendorId);
                });

        cart.stream()
                .map(CartItem::getVendorUuid)
                .filter(vendorUuid -> vendorUuid != null && !vendorUuid.isBlank())
                .distinct()
                .forEach(vendorUuid -> {
                    session.removeAttribute("shippingCost_" + vendorUuid);
                    session.removeAttribute("shippingOption_" + vendorUuid);
                });
    }

    public record LocationOption(Long id, String name, String displayLabel) {
    }

}
