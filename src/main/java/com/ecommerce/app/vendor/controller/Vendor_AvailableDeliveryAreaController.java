/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.product.model.AvailableDeliveryArea;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.ripository.AvailableDeliveryAreaRepository;
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
@RequestMapping("/vendor_availabledeliveryarea")
public class Vendor_AvailableDeliveryAreaController {

    @Autowired
    AvailableDeliveryAreaRepository availableDeliveryAreaRepository;

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
        Product product = new Product();
        product.setId(pid);

        AvailableDeliveryArea availableDeliveryArea = new AvailableDeliveryArea();
        availableDeliveryArea.setProduct(product);
        model.addAttribute("availableDeliveryArea", availableDeliveryArea);
        return "vendor/product/deliveryoption/delivery_area";
    }

    @PostMapping("/save")
    @ResponseBody
    public void save(@Valid AvailableDeliveryArea availableDeliveryArea, HttpServletResponse response) {

        availableDeliveryAreaRepository.save(availableDeliveryArea);

        response.setHeader("HX-Refresh", "true");
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id) {
        Optional<AvailableDeliveryArea> availableDeliveryAreaopt = availableDeliveryAreaRepository.findById(id);

        AvailableDeliveryArea availableDeliveryArea = availableDeliveryAreaopt.orElse(null);

        model.addAttribute("availableDeliveryArea", availableDeliveryArea);

        return "vendor/product/deliveryoption/delivery_area";
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public String deleteUnit(@PathVariable Long id, HttpServletResponse response) {
        String message;
        String messageType;
        if (!availableDeliveryAreaRepository.existsById(id)) {
            message = "Error: Item not found!";
            messageType = "danger"; // Error message type
        } else {
            availableDeliveryAreaRepository.deleteById(id);
            message = "Deleted successfully!";
            messageType = "success"; // Success message type
        }
        availableDeliveryAreaRepository.deleteById(id);
        response.setHeader("HX-Refresh", "true");
        return "<div id='messageContainer' class='alert alert-" + messageType + "'>" + message + "</div>";
    }

//    
}
