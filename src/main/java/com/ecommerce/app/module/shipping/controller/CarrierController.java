/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.shipping.controller;

import com.ecommerce.app.module.shipping.model.Carrier;
import com.ecommerce.app.module.shipping.model.CarrierMode;
import com.ecommerce.app.module.shipping.model.CodCollectionMode;
import com.ecommerce.app.module.shipping.model.SettlementMode;
import com.ecommerce.app.module.shipping.model.ShippingChargeOwner;
import com.ecommerce.app.module.shipping.services.CarrierService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/admin/carriers")
public class CarrierController {

    private final CarrierService service;

    public CarrierController(CarrierService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public String list(Model model) {
        try {
            List<Carrier> carriers = service.getAll();
            Map<String, Long> dependencyCountMap = new HashMap<>();

            for (Carrier carrier : carriers) {
                dependencyCountMap.put(carrier.getUuid(), service.countDependencies(carrier.getUuid()));
            }

            model.addAttribute("carriers", carriers);
            model.addAttribute("dependencyCountMap", dependencyCountMap);
        } catch (Exception ex) {
            model.addAttribute("carriers", List.of());
            model.addAttribute("dependencyCountMap", Map.of());
            model.addAttribute("errorMessage", "Runtime error while loading carriers: " + ex.getMessage());
        }
        return "admin/shipping/carriers/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("carrier", new Carrier());
        model.addAttribute("carrierModes", CarrierMode.values());
        model.addAttribute("settlementModes", SettlementMode.values());
        model.addAttribute("shippingChargeOwners", ShippingChargeOwner.values());
        model.addAttribute("codCollectionModes", CodCollectionMode.values());
        return "admin/shipping/carriers/form";
    }

    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute Carrier carrier,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("carrier", carrier);
            model.addAttribute("carrierModes", CarrierMode.values());
            model.addAttribute("settlementModes", SettlementMode.values());
            model.addAttribute("shippingChargeOwners", ShippingChargeOwner.values());
            model.addAttribute("codCollectionModes", CodCollectionMode.values());
            return "admin/shipping/carriers/form";
        }

        try {
            service.save(carrier);
            redirectAttributes.addFlashAttribute("successMessage", "Carrier saved successfully!");
        } catch (Exception ex) {
            model.addAttribute("carrier", carrier);
            model.addAttribute("carrierModes", CarrierMode.values());
            model.addAttribute("settlementModes", SettlementMode.values());
            model.addAttribute("shippingChargeOwners", ShippingChargeOwner.values());
            model.addAttribute("codCollectionModes", CodCollectionMode.values());
            model.addAttribute("errorMessage", "Runtime error while saving carrier: " + ex.getMessage());
            return "admin/shipping/carriers/form";
        }

        return "redirect:/admin/carriers/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Carrier carrier = service.getById(id);
        if (carrier == null) {
            model.addAttribute("carrier", new Carrier());
            model.addAttribute("errorMessage", "Carrier not found.");
        } else {
            model.addAttribute("carrier", carrier);
        }
        model.addAttribute("carrierModes", CarrierMode.values());
        model.addAttribute("settlementModes", SettlementMode.values());
        model.addAttribute("shippingChargeOwners", ShippingChargeOwner.values());
        model.addAttribute("codCollectionModes", CodCollectionMode.values());
        return "admin/shipping/carriers/form";
    }

    @GetMapping("/delete/{uuid}")
    public String delete(@PathVariable String uuid, RedirectAttributes redirectAttributes) {

        try {
            Map<String, Long> counts = service.getDependencies(uuid);

            long totalDependencies = counts.values().stream().mapToLong(Long::longValue).sum();

            if (totalDependencies > 0) {
                String message = String.format(
                        "Cannot delete carrier. It is linked with: %d CarrierRate(s), %d Shipment(s), %d ShippingProfile(s).",
                        counts.getOrDefault("carrierRate", 0L),
                        counts.getOrDefault("shipment", 0L),
                        counts.getOrDefault("profiles", 0L)
                );
                redirectAttributes.addFlashAttribute("errorMessage", message);
                return "redirect:/admin/carriers/list";
            }

            service.deleteByUuid(uuid);
            redirectAttributes.addFlashAttribute("successMessage", "Carrier deleted successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Runtime error while deleting carrier: " + ex.getMessage());
        }
        return "redirect:/admin/carriers/list";
    }

}
