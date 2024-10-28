/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.customer.controller;

import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.order.model.OrderStatus;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import com.ecommerce.app.product.ripository.ProductsubcategoryRepository;
import com.ecommerce.app.services.StorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.order.repository.SalesOrderRepository;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/customer-product")
@PreAuthorize("hasAuthority('customer-product)")
public class CustomerProductController {

    @Autowired
    StorageProperties properties;

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    ProductcategoryRepository productcategoryRepository;

    @Autowired
    ProductsubcategoryRepository productsubcategoryRepository;

    @Autowired
    ProductRepository examRepository;

    @Autowired
    SalesOrderRepository salesOrderRepository;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model) {

        Users userId = new Users();
        userId.setId(loggedUserService.activeUserid());
        model.addAttribute("examlist", salesOrderRepository.findByCustomerAndStatusOrderByIdDesc(userId, OrderStatus.Complete));

        return "student/exam/index";
    }

    @RequestMapping("/details/{id}")
    public String create(Model model, @PathVariable Long id, Product exam) {

        model.addAttribute("exam_details", examRepository.findById(id));

        Product examid = examRepository.getReferenceById(id);

        Users userId = new Users();
        userId.setId(loggedUserService.activeUserid());



        return "student/exam/exam_details";

    }

    @RequestMapping("/question-by-exam/{examid}")
    public String question_by_exam(Model model, @PathVariable Long examid, Product exam) {

        exam.setId(examid);
 

        model.addAttribute("examinfo", examRepository.getReferenceById(examid));

        return "student/exam/question-by-exam";
    }

}
