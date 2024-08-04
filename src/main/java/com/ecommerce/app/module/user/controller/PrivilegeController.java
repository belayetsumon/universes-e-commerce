/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.user.controller;

import com.ecommerce.app.module.user.model.Privilege;
import com.ecommerce.app.module.user.ripository.ModuleRepository;
import com.ecommerce.app.module.user.ripository.PrivilegeRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Md Belayet Hossin
 */
@Controller

@RequestMapping("/privilege")
//@PreAuthorize("hasAuthority('privilege')")
public class PrivilegeController {
    
     @Autowired
    ModuleRepository moduleRepository;

    @Autowired
    PrivilegeRepository privilegeRepository;

    @RequestMapping(value = {"", "/", "/index"})

    public String index(Model model, Privilege privilege) {

        model.addAttribute("list", privilegeRepository.findAll());
        model.addAttribute("modulelist", moduleRepository.findAll());

        return "/user/privilege";

    }

    @RequestMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, Privilege privilege) {

        model.addAttribute("privilege", privilegeRepository.findById(id));

        model.addAttribute("list", privilegeRepository.findAll());
        model.addAttribute("modulelist", moduleRepository.findAll());

        return "/user/privilege";
    }

    @RequestMapping("/save")
    public String save(Model model, @Valid Privilege privilege, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("list", privilegeRepository.findAll());
            model.addAttribute("modulelist", moduleRepository.findAll());
            return "/user/privilege";
        }

        privilegeRepository.save(privilege);

        return "redirect:/privilege/index";
    }

    @RequestMapping("/delete/{id}")

    public String delete(Model model, @PathVariable Long id, Privilege privilege) {

        privilegeRepository.deleteById(id);

        return "redirect:/privilege/index";
    }

}
