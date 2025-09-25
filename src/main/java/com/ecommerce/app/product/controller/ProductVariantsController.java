/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.product.controller;

import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductVariants;
import com.ecommerce.app.product.ripository.ProductColorRepository;
import com.ecommerce.app.product.ripository.ProductSizeRepository;
import com.ecommerce.app.product.ripository.ProductVariantsRepository;
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
@RequestMapping("/productvariants")
public class ProductVariantsController {

    @Autowired
    ProductVariantsRepository productVariantsRepository;

    @Autowired
    ProductSizeRepository productSizeRepository;
    @Autowired
    ProductColorRepository productColorRepository;

    @GetMapping("/add/{pid}")
    public String add(Model model, @PathVariable Long pid) {
        Product product = new Product();
        product.setId(pid);

        ProductVariants productVariants = new ProductVariants();
        productVariants.setProduct(product);
        model.addAttribute("productVariants", productVariants);

        model.addAttribute("colorlist", productColorRepository.findAll());

        model.addAttribute("sizelist", productSizeRepository.findAll());

        return "product/productvariants/productvariants";
    }

    @PostMapping("/save")
    @ResponseBody
    public void save(@Valid ProductVariants productVariants, HttpServletResponse response) {

        productVariantsRepository.save(productVariants);

        response.setHeader("HX-Refresh", "true");
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id) {
        Optional<ProductVariants> productVariantsopt = productVariantsRepository.findById(id);

        ProductVariants productVariants = productVariantsopt.orElse(null);

        model.addAttribute("productVariants", productVariants);

        return "product/productvariants/productvariants";
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public String deleteUnit(@PathVariable Long id, HttpServletResponse response) {
        String message;
        String messageType;
        if (!productVariantsRepository.existsById(id)) {
            message = "Error: Item not found!";
            messageType = "danger"; // Error message type
        } else {
            productVariantsRepository.deleteById(id);
            message = "Deleted successfully!";
            messageType = "success"; // Success message type
        }
        productVariantsRepository.deleteById(id);
        response.setHeader("HX-Refresh", "true");
        return "<div id='messageContainer' class='alert alert-" + messageType + "'>" + message + "</div>";
    }

}
