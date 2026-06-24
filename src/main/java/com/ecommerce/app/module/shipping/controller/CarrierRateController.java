/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.shipping.controller;

import com.ecommerce.app.module.shipping.model.CarrierRate;
import com.ecommerce.app.module.shipping.model.DeliverySpeed;
import com.ecommerce.app.module.shipping.model.DeliveryType;
import com.ecommerce.app.module.shipping.repository.CarrierRateRepository;
import com.ecommerce.app.module.shipping.repository.CarrierRepository;
import com.ecommerce.app.module.shipping.repository.ShippingZoneRepository;
import com.ecommerce.app.module.shipping.services.ShippingLocationService;
import jakarta.validation.Valid;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/admin/carrier-rates")
public class CarrierRateController {

    @Autowired
    private CarrierRateRepository rateRepo;
    @Autowired
    private CarrierRepository carrierRepo;
    @Autowired
    private ShippingZoneRepository zoneRepo;
    @Autowired
    private ShippingLocationService locationService;

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("rates", rateRepo.findAll());
        return "admin/shipping/carriers/carriers_rate_list";
    }

    @GetMapping("/create")
    public String createForm(Model model, CarrierRate carrierRate) {
        model.addAttribute("carrierRate", carrierRate);
        populateFormOptions(model);
        return "admin/shipping/carriers/carriers_rate_form";
    }

    @PostMapping("/save")
    public String save(Model model, @Valid @ModelAttribute CarrierRate carrierRate,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (carrierRate.getEstimatedMinDays() != null
                && carrierRate.getEstimatedMaxDays() != null
                && carrierRate.getEstimatedMinDays() > carrierRate.getEstimatedMaxDays()) {
            result.rejectValue("estimatedMaxDays", "carrierRate.estimatedMaxDays.invalid",
                    "Estimated max days must be greater than or equal to estimated min days");
        }
        if (carrierRate.getZone() == null
                && (carrierRate.getDistrict() == null || carrierRate.getDistrict().isEmpty())) {
            result.rejectValue("district", "carrierRate.coverage.required",
                    "Select a shipping zone or at least one covered location.");
        }

        if (result.hasErrors()) {
            populateFormOptions(model);
            return "admin/shipping/carriers/carriers_rate_form";
        }

        try {
            rateRepo.save(carrierRate);
        } catch (DataIntegrityViolationException ex) {
            result.reject("carrierRate.save.failed", "Carrier rate could not be saved because it conflicts with existing data.");
            populateFormOptions(model);
            return "admin/shipping/carriers/carriers_rate_form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Carrier Rate saved successfully!");
        return "redirect:/admin/carrier-rates/list";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        CarrierRate carrierRate = rateRepo.findById(id).orElse(null);
        if (carrierRate == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Carrier rate not found.");
            return "redirect:/admin/carrier-rates/list";
        }
        model.addAttribute("carrierRate", carrierRate);
        populateFormOptions(model);
        return "admin/shipping/carriers/carriers_rate_form";
    }

    private void populateFormOptions(Model model) {
        model.addAttribute("carriers", carrierRepo.findAll());
        model.addAttribute("zones", zoneRepo.findByActiveTrueOrderByPriorityAscNameAsc());
        model.addAttribute("districts", locationService.getActiveLocations());
        model.addAttribute("speeds", DeliverySpeed.values());
        model.addAttribute("types", DeliveryType.values());
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            rateRepo.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Carrier rate deleted successfully.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete this record because it is used elsewhere.");
        }
        return "redirect:/admin/carrier-rates/list";
    }

}
