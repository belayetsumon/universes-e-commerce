/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.product.controller;

import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.Warranty;
import com.ecommerce.app.product.ripository.WarrantyRepository;
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
@RequestMapping("/warranty")
public class WarrantyController {

    @Autowired
    WarrantyRepository warrantyRepository;

    @GetMapping("/add/{pid}")
    public String add(Model model, @PathVariable Long pid) {
        Product product = new Product();
        product.setId(pid);
        Warranty warranty = new Warranty();
        warranty.setProduct(product);
        model.addAttribute("warranty", warranty);
        return "product/deliveryoption/warranty";
    }

    @PostMapping("/save")
    @ResponseBody
    public void save(@Valid Warranty warranty, HttpServletResponse response) {

        warrantyRepository.save(warranty);

        response.setHeader("HX-Refresh", "true");
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id) {
        Optional<Warranty> warrantyopt = warrantyRepository.findById(id);

        Warranty warranty = warrantyopt.orElse(null);

        model.addAttribute("warranty", warranty);

        return "product/deliveryoption/warranty";
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public String deleteUnit(@PathVariable Long id, HttpServletResponse response) {
        String message;
        String messageType;
        if (!warrantyRepository.existsById(id)) {
            message = "Error: Item not found!";
            messageType = "danger"; // Error message type
        } else {
            warrantyRepository.deleteById(id);
            message = "Deleted successfully!";
            messageType = "success"; // Success message type
        }
        warrantyRepository.deleteById(id);
        response.setHeader("HX-Refresh", "true");
        return "<div id='messageContainer' class='alert alert-" + messageType + "'>" + message + "</div>";
    }

}
