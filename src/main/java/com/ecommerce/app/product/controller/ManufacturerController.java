/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.product.controller;

import com.ecommerce.app.product.model.Manufacturer;
import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.ripository.ManufacturerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/manufacturer")
public class ManufacturerController {

    @Autowired
    ManufacturerRepository manufacturerRepository;

    @GetMapping("/list")
    public String viewAllUnits(Model model) {
        model.addAttribute("list", manufacturerRepository.findAll());
        return "product/manufacturer/list";
    }

    @GetMapping("/add")
    public String add(Model model, Manufacturer manufacturer) {
        model.addAttribute("statuslist", ProductStatusEnum.values());
        return "product/manufacturer/add";
    }

    @PostMapping("/save")
    public String save(Model model, Manufacturer manufacturer, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("statuslist", ProductStatusEnum.values());
            return "product/productcategory/add";
        }

        manufacturerRepository.save(manufacturer);
        return "redirect:/manufacturer/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditUnitForm(@PathVariable Long id, Model model, Manufacturer manufacturer) {
        manufacturer = manufacturerRepository.findById(id).orElse(null);
        model.addAttribute("manufacturer", manufacturer);
        model.addAttribute("statuslist", ProductStatusEnum.values());
        return "product/manufacturer/add";
    }

    @GetMapping("/delete/{id}")
    public String deleteUnit(@PathVariable Long id) {
        manufacturerRepository.deleteById(id);
        return "redirect:/manufacturer/list";
    }

}
