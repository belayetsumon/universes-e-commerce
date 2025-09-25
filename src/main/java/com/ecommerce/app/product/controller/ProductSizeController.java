/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.product.controller;

import com.ecommerce.app.product.model.ProductSize;
import com.ecommerce.app.product.services.ProductSizeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/productsize")
public class ProductSizeController {

    @Autowired
    private ProductSizeService service;

    @GetMapping("/list")
    public String list(Model model, ProductSize productSize) {
        model.addAttribute("sizes", service.findAll());
        return "product/variants/size_list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("productSize", new ProductSize());
        return "product/variants/sizes_form";
    }

    @PostMapping("/save")
    public String save(@Valid ProductSize productSize,
            BindingResult result,
            Model model) {

        if (service.existsByName(productSize.getName(), productSize.getId())) {
            result.rejectValue("name", "error.productSize", "This size name already exists. Choose another.");
        }

        if (result.hasErrors()) {
            model.addAttribute("sizes", service.findAll());
            return "product/variants/size_form";
        }

        service.save(productSize);
        return "redirect:/productsize/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        ProductSize size = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid size ID:" + id));
        model.addAttribute("sizes", service.findAll());
        model.addAttribute("productSize", size);
        return "product/variants/size_list";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/productsize/list";
    }

}
