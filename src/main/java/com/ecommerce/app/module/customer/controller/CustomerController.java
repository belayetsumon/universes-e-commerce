/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.customer.controller;

import com.ecommerce.app.model.Profile;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.module.user.services.LoginEventService;
import com.ecommerce.app.order.model.OrderStatus;
import com.ecommerce.app.ripository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import com.ecommerce.app.vendor.model.VendorStatusEnum;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import com.ecommerce.app.vendor.services.VendorCodeGenerator;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/customer")
@PreAuthorize("hasAuthority('customer')")
public class CustomerController {

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    SalesOrderRepository salesOrderRepository;

    @Autowired
    LoginEventService loginEventService;

    @Autowired
    VendorprofileRepository vendorprofileRepository;

    @Autowired
    VendorCodeGenerator vendorCodeGenerator;

    @RequestMapping(value = {"", "/", "/index", "dashboards"})
    public String index(Model model, Profile profile) {

        model.addAttribute("username", loggedUserService.activeUserName());
        Users userId = new Users();
        userId.setId(loggedUserService.activeUserid());
        model.addAttribute("orderlist", salesOrderRepository.findByCustomerOrderByIdDesc(userId));

        model.addAttribute("orderlist", salesOrderRepository.findByCustomerOrderByIdDesc(userId));

        model.addAttribute("orderlist_panding", salesOrderRepository.findByCustomerAndStatusOrderByIdDesc(userId, OrderStatus.PENDING));

        model.addAttribute("examlist", salesOrderRepository.findByCustomerAndStatusOrderByIdDesc(userId, OrderStatus.COMPLETED));

        return "customer/index";
    }

    @RequestMapping(value = {"/create"})
    public String create(Model model, Vendorprofile vendorprofile) {

        Users users = new Users();

        users.setId(loggedUserService.activeUserid());

        vendorprofile.setUserId(users);

        vendorprofile.setVendorCode(vendorCodeGenerator.generateNextVendorCode());

        return "customer/vendor_profile_create";
    }

    @RequestMapping("/save")
    public String save(Model model, @Valid Vendorprofile vendorprofile, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            Users users = new Users();
            users.setId(loggedUserService.activeUserid());
            vendorprofile.setUserId(users);
            return "customer/vendor_profile_create";
        }

        vendorprofile.setVendorStatusEnum(VendorStatusEnum.Pending);
        vendorprofileRepository.save(vendorprofile);
        return "redirect:/customer/storelist";
    }

    @RequestMapping(value = {"/storelist"})
    public String storeList(Model model, HttpSession session) {

        model.addAttribute("username", loggedUserService.activeUserName());

        Users userId = new Users();

        userId.setId(loggedUserService.activeUserid());

        session.removeAttribute("vendorprofile");

        model.addAttribute("storelist", vendorprofileRepository.findByUserId(userId));

        return "customer/storelist";
    }

}
