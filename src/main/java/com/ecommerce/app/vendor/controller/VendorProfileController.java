/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.vendor.controller;


import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/vendorprofile")
public class VendorProfileController {

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    VendorprofileRepository vendorprofileRepository;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model) {
        Users users = new Users();
        users.setId(loggedUserService.activeUserid());
        model.addAttribute("vendorprofile", vendorprofileRepository.findByUserId(users));
        return "vendor/profile/index";
    }

    @RequestMapping(value = {"create"})
    public String create(Model model, Vendorprofile vendorprofile) {

        Users users = new Users();
        
        users.setId(loggedUserService.activeUserid());
        
        vendorprofile.setUserId(users);
        
        return "vendor/profile/profile_add";
    }

    @RequestMapping("/save")
    public String create(Model model, @Valid Vendorprofile vendorprofile, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            Users users = new Users();
            users.setId(loggedUserService.activeUserid());
            vendorprofile.setUserId(users);
            return "vendor/profile/profile_add";
        }
        vendorprofileRepository.save(vendorprofile);
        return "redirect:/vendorprofile/index";
    }

    @RequestMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, Vendorprofile vendorprofile) {
        //model.addAttribute("vendorprofile", vendorprofileRepository.getOne(id));
        return "vendor/profile/profile_add";
    }

}
