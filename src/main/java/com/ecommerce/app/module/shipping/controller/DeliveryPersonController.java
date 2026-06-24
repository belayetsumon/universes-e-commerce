/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.shipping.controller;

import com.ecommerce.app.module.shipping.model.DeliveryPerson;
import com.ecommerce.app.module.shipping.repository.DeliveryPersonRepository;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
@RequestMapping("/delivery-person")
public class DeliveryPersonController {

    @Autowired
    private DeliveryPersonRepository repo;
    @Autowired
    VendorprofileRepository vendorRepo;

    @GetMapping
    public String list(Model model) {
        List<DeliveryPerson> deliveryPersons = repo.findAll();
        long activeCount = deliveryPersons.stream().filter(DeliveryPerson::isActive).count();
        model.addAttribute("list", deliveryPersons);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", deliveryPersons.size() - activeCount);
        model.addAttribute("vendorLookup", buildVendorLookup());
        return "admin/shipping/delivery_person/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("deliveryPerson", new DeliveryPerson());
        model.addAttribute("vendorList", vendorRepo.findAll());
        return "admin/shipping/delivery_person/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute DeliveryPerson dp) {
        repo.save(dp);
        return "redirect:/delivery-person";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("deliveryPerson", repo.findById(id).orElseThrow());
        model.addAttribute("vendorList", vendorRepo.findAll());
        return "admin/shipping/delivery_person/form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        repo.deleteById(id);
        return "redirect:/delivery-person";
    }

    private Map<Long, Vendorprofile> buildVendorLookup() {
        Map<Long, Vendorprofile> lookup = new HashMap<>();
        for (Vendorprofile vendor : vendorRepo.findAll()) {
            lookup.put(vendor.getId(), vendor);
        }
        return lookup;
    }

}
