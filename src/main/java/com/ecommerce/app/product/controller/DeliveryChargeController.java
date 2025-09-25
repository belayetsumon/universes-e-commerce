/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.product.controller;

import com.ecommerce.app.product.model.DeliveryCharge;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.ripository.DeliveryChargeRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
@RequestMapping("/deliverycharge")
public class DeliveryChargeController {

    @Autowired
    DeliveryChargeRepository deliveryChargeRepository;

    @GetMapping("/add/{pid}")
    public String add(Model model, @PathVariable Long pid) {
        Product product = new Product();
        product.setId(pid);

        DeliveryCharge deliveryCharge = new DeliveryCharge();
        deliveryCharge.setProduct(product);
        model.addAttribute("deliveryCharge", deliveryCharge);
        return "product/deliveryoption/delivery_charge";
    }

    @PostMapping("/save")
    @ResponseBody
    public void save(@Valid DeliveryCharge deliveryCharge, HttpServletResponse response) {

        deliveryChargeRepository.save(deliveryCharge);

        response.setHeader("HX-Refresh", "true");
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id) {
        Optional<DeliveryCharge> deliveryChargeopt = deliveryChargeRepository.findById(id);

        DeliveryCharge deliveryCharge = deliveryChargeopt.orElse(null);

        model.addAttribute("deliveryCharge", deliveryCharge);

        return "product/deliveryoption/delivery_charge";
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public String deleteUnit(@PathVariable Long id, HttpServletResponse response) {
        String message;
        String messageType;
        if (!deliveryChargeRepository.existsById(id)) {
            message = "Error: Item not found!";
            messageType = "danger"; // Error message type
        } else {
            deliveryChargeRepository.deleteById(id);
            message = "Deleted successfully!";
            messageType = "success"; // Success message type
        }
        deliveryChargeRepository.deleteById(id);
        response.setHeader("HX-Refresh", "true");
        return "<div id='messageContainer' class='alert alert-" + messageType + "'>" + message + "</div>";
    }

}
