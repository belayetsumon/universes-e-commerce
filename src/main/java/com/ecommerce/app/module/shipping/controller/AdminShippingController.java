/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.shipping.controller;

import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.repository.CarrierRepository;
import com.ecommerce.app.module.shipping.repository.DeliveryPersonRepository;
import com.ecommerce.app.module.shipping.repository.ShipmentRepository;
import com.ecommerce.app.module.shipping.services.ShippingService;
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
@RequestMapping("/admin/shipments")
public class AdminShippingController {

    @Autowired
    private ShipmentRepository shipmentRepo;
    @Autowired
    private ShippingService shippingService;
    @Autowired
    private CarrierRepository carrierRepo; // assuming you have this
    @Autowired
    private DeliveryPersonRepository deliveryPersonRepo; // optional

    @GetMapping("/list")
    public String list(Model model) {
        List<Shipment> shipments = shipmentRepo.findAll();
        model.addAttribute("shipments", shipments);
        return "admin/shipping/shipment_list";
    }

    @GetMapping("/create")
    public String newForm(Model model, Shipment shipment) {

        model.addAttribute("carriers", carrierRepo.findAll());
        model.addAttribute("deliveryPersons", deliveryPersonRepo.findAll());
        return "admin/shipping/admin_shipments_form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
        Shipment shipment = shipmentRepo.findById(id).orElse(null);
        if (shipment == null) {
            redirectAttrs.addFlashAttribute("errorMessage", "Shipment not found");
            return "redirect:/admin/shipments/list";
        }
        model.addAttribute("shipment", shipment);
        model.addAttribute("carriers", carrierRepo.findAll());
        model.addAttribute("deliveryPersons", deliveryPersonRepo.findAll());
        return "admin/shipping/admin_shipments_form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Shipment shipment,
            BindingResult result,
            RedirectAttributes redirectAttrs,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("carriers", carrierRepo.findAll());
            model.addAttribute("deliveryPersons", deliveryPersonRepo.findAll());
            return "admin/shipping/admin_shipments_form";
        }

        shipmentRepo.save(shipment);
        redirectAttrs.addFlashAttribute("successMessage", "Shipment saved successfully");
        return "redirect:/admin/shipments/list";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            shipmentRepo.deleteById(id);
            redirectAttrs.addFlashAttribute("successMessage", "Shipment deleted successfully");
        } catch (DataIntegrityViolationException e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Cannot delete shipment. It has related items!");
        }
        return "redirect:/admin/shipments/list";
    }

}
