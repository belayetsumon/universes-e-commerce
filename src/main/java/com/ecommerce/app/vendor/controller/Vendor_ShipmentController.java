/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.model.ShipmentStatus;
import com.ecommerce.app.module.shipping.services.CarrierService;
import com.ecommerce.app.module.shipping.services.DeliveryPersonService;
import com.ecommerce.app.module.shipping.services.ShipmentService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/vendor/shipments")
public class Vendor_ShipmentController {

    private final ShipmentService shipmentService;
    private final CarrierService carrierService;

    private final DeliveryPersonService deliveryPersonService;

    public Vendor_ShipmentController(ShipmentService shipmentService, CarrierService carrierService, DeliveryPersonService deliveryPersonService) {
        this.shipmentService = shipmentService;
        this.carrierService = carrierService;
        this.deliveryPersonService = deliveryPersonService;
    }

    // List shipments for vendor
    @GetMapping
    public String list(Model model) {
        Long vendorId = 1L; // replace with session/vendor login
        List<Shipment> shipments = shipmentService.getByVendor(vendorId);
        model.addAttribute("shipments", shipments);
        return "vendor/shipments/list";
    }

    // Create form
    @GetMapping("/new")
    public String createForm(Model model) {
        Shipment shipment = new Shipment();
        shipment.setVendorId(1L); // replace with logged-in vendor
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setShippingCost(BigDecimal.ZERO);
        model.addAttribute("shipment", shipment);
        model.addAttribute("carriers", carrierService.getAll());
        model.addAttribute("deliveryPersons", deliveryPersonService.getByVendor(1L)); // session ve
        return "vendor/shipments/form";
    }

    // Save shipment
    @PostMapping
    public String save(@ModelAttribute Shipment shipment) {

        if (shipment.getCarrier() == null && shipment.getDeliveryPerson() == null) {
            throw new IllegalArgumentException("Select at least Carrier or Delivery Person");
        }

//          if(shipment.getCarrier() != null){
//        String labelUrl = carrierService.generateShipmentLabel(shipment.getCarrier().getId(), shipment);
//        shipment.setMetadataJson("{\"labelUrl\":\"" + labelUrl + "\"}");
//        shipmentService.save(shipment);
//    }
        shipmentService.save(shipment);
        return "redirect:/vendor/shipments";
    }

    // Edit shipment
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Shipment shipment = shipmentService.getById(id);
        model.addAttribute("shipment", shipment);
        model.addAttribute("carriers", carrierService.getAll());
        return "vendor/shipments/form";
    }

    // Delete shipment
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        shipmentService.delete(id);
        return "redirect:/vendor/shipments";
    }

}
