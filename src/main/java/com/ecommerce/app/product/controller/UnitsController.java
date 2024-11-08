/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.product.controller;

import com.ecommerce.app.product.model.Unitofmeasurement;
import com.ecommerce.app.product.services.UnitsOfMeasureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/uom")
public class UnitsController {

    @Autowired
    private final UnitsOfMeasureService service;

    @GetMapping("/list")
    public String viewAllUnits(Model model) {
        model.addAttribute("list", service.getAllUnits());
        return "product/unit/list";
    }

    public UnitsController(UnitsOfMeasureService service) {
        this.service = service;
    }

    @GetMapping("/add")
    public String showAddUnitForm(Model model, Unitofmeasurement unitofmeasurement) {

        return "product/unit/add";
    }

    @PostMapping("/save")
    public String addUnit(Unitofmeasurement unitofmeasurement) {
        service.saveUnit(unitofmeasurement);
        return "redirect:/uom/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditUnitForm(@PathVariable Long id, Model model, Unitofmeasurement unitofmeasurement) {
        unitofmeasurement = service.getUnitById(id).orElseThrow(() -> new RuntimeException("Unit not found"));
        model.addAttribute("unitofmeasurement", unitofmeasurement);
        return "product/unit/add";
    }

    @GetMapping("/delete/{id}")
    public String deleteUnit(@PathVariable Long id) {
        service.deleteUnit(id);
        return "redirect:/uom/list";
    }

}
