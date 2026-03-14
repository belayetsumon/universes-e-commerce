/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.shipping.controller;

import com.ecommerce.app.module.shipping.model.Carrier;
import com.ecommerce.app.module.shipping.services.CarrierService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        List<Carrier> carriers = service.getAll();
        model.addAttribute("carriers", service.getAll());

        Map<String, Long> dependencyCountMap = new HashMap<>();

        for (Carrier carrier : carriers) {
            dependencyCountMap.put(carrier.getUuid(), service.countDependencies(carrier.getUuid()));
        }

        model.addAttribute("carriers", carriers);
        model.addAttribute("dependencyCountMap", dependencyCountMap);
        return "admin/shipping/carriers/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("carrier", new Carrier());
        return "admin/shipping/carriers/form";
    }

    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute Carrier carrier,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("carrier", carrier); // send back form data
            return "admin/carriers/form"; // return same form view
        }

        service.save(carrier);
        redirectAttributes.addFlashAttribute("success", "Carrier saved successfully!");

        return "redirect:/admin/carriers/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("carrier", service.getById(id));
        return "admin/carriers/form";
    }

    @GetMapping("/delete/{uuid}")
    public String delete(@PathVariable String uuid, RedirectAttributes redirectAttributes) {

        Map<String, Long> counts = service.getDependencies(uuid);

        long totalDependencies = counts.values().stream().mapToLong(Long::longValue).sum();

        if (totalDependencies > 0) {
            String message = String.format(
                    "Cannot delete carrier. It is linked with: %d Order(s), %d ShippingProfile(s).",
                    counts.getOrDefault("orders", 0L),
                    counts.getOrDefault("shippingProfiles", 0L)
            );
            redirectAttributes.addFlashAttribute("error", message);
            return "redirect:/admin/carriers/list";
        }

        // Safe to delete
        service.deleteByUuid(uuid);
        redirectAttributes.addFlashAttribute("success", "Carrier deleted successfully!");
        return "redirect:/admin/carriers/list";
    }

}
