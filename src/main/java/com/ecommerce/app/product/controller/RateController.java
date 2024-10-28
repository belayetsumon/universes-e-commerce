/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.product.controller;

import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.product.ripository.RateRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/rate")
public class RateController {

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    RateRepository rateRepository;

//    @RequestMapping("/index")
//    public String index(Model model) {
//
//        Pageable page = PageRequest.of(0, 500, Sort.Direction.DESC, "id");
//        model.addAttribute("ratelist", rateRepository.findAll(page));
//        return "catalog/rate/index";
//    }
//
//    @RequestMapping("/create/{examid}")
//    public String create(Model model, @PathVariable Long examid, Exam exam, Rate rate) {
//        exam.setId(examid);
//        rate.setExam(exam);
//        Users users = new Users();
//        users.setId(loggedUserService.activeUserid());
//        rate.setUserId(users);
//        return "catalog/rate/add";
//    }
//
//    @RequestMapping("/save/{examid}")
//    public String save(Model model, @PathVariable Long examid, @Valid Rate rate, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
//
//        if (bindingResult.hasErrors()) {
//            Exam exam = new Exam();
//            exam.setId(examid);
//            rate.setExam(exam);
//            Users users = new Users();
//            users.setId(loggedUserService.activeUserid());
//            rate.setUserId(users);
//         
//            return "catalog/rate/add";
//        }
//        redirectAttributes.addFlashAttribute("message", "Successfully saved.");
//        rateRepository.save(rate);
//        return "redirect:/rate/index";
//    }
//
//    @RequestMapping("/edit/{id}")
//    public String edit(Model model, @PathVariable Long id, Rate rate) {
//        model.addAttribute("rate", rateRepository.getOne(id));
//        return "catalog/rate/add";
//    }
//
//    @RequestMapping("/delete/{id}")
//
//    public String delete(Model model, @PathVariable Long id, Rate rate, RedirectAttributes redirectAttributes) {
//        rateRepository.deleteById(id);
//        redirectAttributes.addFlashAttribute("message", "Deleted successfully.");
//        return "redirect:/rate/index";
//    }

}
