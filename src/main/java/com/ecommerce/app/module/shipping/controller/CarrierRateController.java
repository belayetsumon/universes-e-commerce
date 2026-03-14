/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.shipping.controller;

import com.ecommerce.app.globalServices.District;
import com.ecommerce.app.module.shipping.model.CarrierRate;
import com.ecommerce.app.module.shipping.model.DeliverySpeed;
import com.ecommerce.app.module.shipping.model.DeliveryType;
import com.ecommerce.app.module.shipping.repository.CarrierRateRepository;
import com.ecommerce.app.module.shipping.repository.CarrierRepository;
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

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("rates", rateRepo.findAll());
        return "admin/shipping/carriers/carriers_rate_list";
    }

    @GetMapping("/create")
    public String createForm(Model model, CarrierRate carrierRate) {
        model.addAttribute("carriers", carrierRepo.findAll());
        model.addAttribute("districts", District.values());
        model.addAttribute("speeds", DeliverySpeed.values());
        model.addAttribute("types", DeliveryType.values());
        return "admin/shipping/carriers/carriers_rate_form";
    }

    @PostMapping("/save")
    public String save(Model model, @Valid @ModelAttribute CarrierRate carrierRate,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("carriers", carrierRepo.findAll());
            model.addAttribute("districts", District.values());
            model.addAttribute("speeds", DeliverySpeed.values());
            model.addAttribute("types", DeliveryType.values());
            return "admin/shipping/carriers/carriers_rate_form";
        }

        rateRepo.save(carrierRate);

        // add success flash message
        redirectAttributes.addFlashAttribute("successMessage", "Carrier Rate saved successfully!");
        return "redirect:/admin/carrier-rates/list";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, CarrierRate carrierRate) {
        carrierRate = rateRepo.findById(id).orElse(new CarrierRate());
        model.addAttribute("carrierRate", carrierRate);
        model.addAttribute("carriers", carrierRepo.findAll());
        model.addAttribute("districts", District.values());
        model.addAttribute("speeds", DeliverySpeed.values());
        model.addAttribute("types", DeliveryType.values());
        return "admin/shipping/carriers/carriers_rate_form";
    }

    @GetMapping("/delete/{id}")
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
