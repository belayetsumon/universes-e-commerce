/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.product.model.DeliveryTimeline;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.ripository.DeliveryTimelineRepository;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
@RequestMapping("/vendor_deliverytimeline")
public class Vendor_DeliveryTimelineController {
    
   @Autowired
   DeliveryTimelineRepository deliveryTimelineRepository;
   @Autowired
   ProductRepository productRepository;
   @Autowired
   VendorUserContext vendorUserContext;

    @GetMapping("/add/{pid}")
    public String add(Model model, @PathVariable Long pid) {
        Product product = productRepository.findById(pid).orElse(null);
        if (!isOwnedByActiveVendor(product)) {
            model.addAttribute("errorMessage", "Product not found for the active vendor.");
            return "vendor/product/deliveryoption/delivery_timeline";
        }

        DeliveryTimeline deliveryTimeline = new DeliveryTimeline();
        deliveryTimeline.setProduct(product);
        model.addAttribute("deliveryTimeline", deliveryTimeline);
        return "vendor/product/deliveryoption/delivery_timeline";
    }

    @PostMapping("/save")
    @ResponseBody
    public String save(@Valid DeliveryTimeline deliveryTimeline, HttpServletResponse response) {
        if (!isOwnedByActiveVendor(deliveryTimeline != null ? deliveryTimeline.getProduct() : null)) {
            return "<div class='alert alert-danger'>Product not found for the active vendor.</div>";
        }

        deliveryTimelineRepository.save(deliveryTimeline);

        response.setHeader("HX-Refresh", "true");
        return "";
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id) {
        Optional<DeliveryTimeline> deliveryTimelineopt = deliveryTimelineRepository.findById(id);

        DeliveryTimeline deliveryTimeline = deliveryTimelineopt.orElse(null);
        if (deliveryTimeline == null || !isOwnedByActiveVendor(deliveryTimeline.getProduct())) {
            model.addAttribute("errorMessage", "Delivery timeline not found for the active vendor.");
            return "vendor/product/deliveryoption/delivery_timeline";
        }

        model.addAttribute("deliveryTimeline", deliveryTimeline);

        return "vendor/product/deliveryoption/delivery_timeline";
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public String deleteUnit(@PathVariable Long id, HttpServletResponse response) {
        String message;
        String messageType;
        Optional<DeliveryTimeline> existing = deliveryTimelineRepository.findById(id);
        if (existing.isEmpty()) {
            message = "Error: Item not found!";
            messageType = "danger"; // Error message type
        } else if (!isOwnedByActiveVendor(existing.get().getProduct())) {
            message = "Error: Item not found for the active vendor!";
            messageType = "danger";
        } else {
            deliveryTimelineRepository.deleteById(id);
            message = "Deleted successfully!";
            messageType = "success"; // Success message type
        }
        response.setHeader("HX-Refresh", "true");
        return "<div id='messageContainer' class='alert alert-" + messageType + "'>" + message + "</div>";
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
