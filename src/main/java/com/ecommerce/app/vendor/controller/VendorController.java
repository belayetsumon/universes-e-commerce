/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.services.LoggedUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ecommerce.app.order.repository.SalesOrderRepository;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/vendor")
//@PreAuthorize("hasAuthority('instructor')")
public class VendorController {

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    SalesOrderRepository salesOrderRepository;

  

    @RequestMapping(value = {"", "/", "/index", "dashboards"})
    public String index(Model model) {
        model.addAttribute("attribute", "value");

        Users userId = new Users();
        userId.setId(loggedUserService.activeUserid());
        // model.addAttribute("orderlist", salesOrderRepository.findByCustomer(userId));

//        Pageable pageable = new PageRequest(0, 20, Sort.Direction.DESC, "id");
//
//        List<Exam> totalexamsales = examRepository.findByUserIdAndOrderItemSalesOrderStatus(pageable, userId, OrderStatus.Complete);

//        double totalincome = 0.00;
//
//        if (!totalexamsales.isEmpty()) {
//
//            for (int i = 0; i < totalexamsales.size(); i++) {
//
//                totalincome += totalexamsales.get(i).getPrice();
//            }
//        } else {
//
//            totalincome = 0.00;
//        }
//
//        int income = (int) (totalincome * 30 / 100);
//
//        
//        model.addAttribute("totalincome", income);
//        
        return "vendor/dashboards";
    }

}
