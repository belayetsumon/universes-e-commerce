/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.user.controller;

import com.ecommerce.app.module.user.model.Modules;
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
 * @author User
 */
@Controller
@RequestMapping("/module")
//@PreAuthorize("hasAuthority('module')")
public class ModuleController {

    @Autowired
    PrivilegeRepository privilegeRepository;

    @Autowired
    ModuleRepository moduleRepository;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model, Modules modules) {

        model.addAttribute("list", moduleRepository.findAll());

        return "/user/module";

    }

    @RequestMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, Modules module) {

        model.addAttribute("module", moduleRepository.findById(id));

        model.addAttribute("list", moduleRepository.findAll());

        return "/user/module";
    }

    @RequestMapping("/save")
    public String save(Model model, @Valid Modules module, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("list", moduleRepository.findAll());
            return "/user/module";
        }

       // moduleRepository.save(module);

        return "redirect:/module/index";
    }

    @RequestMapping("/delete/{id}")

    public String delete(Model model, @PathVariable Long id, Modules module) {

        moduleRepository.deleteById(id);

        return "redirect:/module/index";
    }

}
