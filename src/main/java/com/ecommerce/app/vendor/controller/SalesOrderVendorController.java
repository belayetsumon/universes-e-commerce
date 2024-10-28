/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.order.model.OrderStatus;
import com.ecommerce.app.order.model.SalesOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;



/**
 *
 * @author User
 */
@Controller
@RequestMapping("/vendor-order")
//@PreAuthorize("hasAuthority('vendor-order')")
public class SalesOrderVendorController {

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    SalesOrderRepository salesOrderRepository;


    @RequestMapping(value = {"", "/", "/index"})

    public String index(Model model) {

        Users userId = new Users();
        userId.setId(loggedUserService.activeUserid());
        // model.addAttribute("orderlist", salesOrderRepository.findByCustomer(userId));
        //Pageable pageable = new PageRequest(0, 20, Sort.Direction.DESC, "id");
        //model.addAttribute("orderlist", examRepository.findByUserIdAndOrderItemSalesOrderStatus(pageable, userId,OrderStatus.Complete ));
        return "vendor/sales/index";
    }

    @RequestMapping(value = {"/details/{oid}"})
    public String details(Model model, @PathVariable Long oid, SalesOrder salesOrder) {

//        model.addAttribute("orderdetails", salesOrderRepository.getOne(oid));

        return "vendor/sales/order_details";
    }

}
