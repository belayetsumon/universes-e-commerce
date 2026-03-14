/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.shipping.controller;

import com.ecommerce.app.module.shipping.model.DeliveryPerson;
import com.ecommerce.app.module.shipping.repository.DeliveryPersonRepository;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 *
 * @author libertyerp_local
 */
@Controller
public class DeliveryPersonController {

    @Autowired
    private DeliveryPersonRepository repo;
    @Autowired
    VendorprofileRepository vendorRepo;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("list", repo.findAll());
        return "delivery/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("deliveryPerson", new DeliveryPerson());
        model.addAttribute("vendorList", vendorRepo.findAll());
        return "delivery/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute DeliveryPerson dp) {
        repo.save(dp);
        return "redirect:/delivery-person";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("deliveryPerson", repo.findById(id).orElseThrow());
        return "delivery/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        repo.deleteById(id);
        return "redirect:/delivery-person";
    }

}
