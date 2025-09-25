/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.product.controller;

import com.ecommerce.app.product.model.ProductColor;
import com.ecommerce.app.product.services.ProductColorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
@RequestMapping("/productcolor")
public class ProductColorController {

    @Autowired
    private ProductColorService service;

    public ProductColorController(ProductColorService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public String list(Model model, ProductColor productColor) {
        model.addAttribute("list", service.findAll());
        return "product/variants/color_list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("productColor", new ProductColor());
        return "product/variants/color_form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("productColor") ProductColor productColor,
            BindingResult result,
            Model model) {

        if (service.existsByName(productColor.getName(), productColor.getId())) {
            result.rejectValue("name", "error.productColor", "This color already exists.");
        }

        if (result.hasErrors()) {
            model.addAttribute("list", service.findAll());
            return "product/variants/color_list";
        }

        service.save(productColor);
        return "redirect:/productcolor/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        ProductColor color = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid color ID:" + id));
        model.addAttribute("productColor", color);
        model.addAttribute("list", service.findAll());
        return "product/variants/color_list";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/productcolor/list";
    }

}
