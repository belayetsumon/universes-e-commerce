/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.order.controller;

import com.ecommerce.app.order.model.OrderStatus;
import com.ecommerce.app.order.model.SalesOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ecommerce.app.order.repository.SalesOrderRepository;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/order")
//@PreAuthorize("hasAuthority('order')")
public class SalesOrderController {

    @Autowired
    SalesOrderRepository salesOrderRepository;

    @RequestMapping(value = {"", "/", "/index"})

    public String index(Model model) {

        model.addAttribute("orderlist", salesOrderRepository.findAll());

        return "order/order/index";
    }

    @RequestMapping(value = {"/details/{oid}"})
    public String details(Model model, @PathVariable Long oid, SalesOrder salesOrder) {

        model.addAttribute("orderdetails", salesOrderRepository.getReferenceById(oid));

        return "order/order/order_details";
    }

    @RequestMapping(value = {"statuschange/{oid}"})
    public String statusChange(Model model, @PathVariable Long oid, SalesOrder salesOrder) {

        salesOrder = salesOrderRepository.getReferenceById(oid);
        
        salesOrder.setStatus(OrderStatus.Complete);
        
        salesOrderRepository.save(salesOrder);
        
         return "redirect:/order/details/{oid}";
        
        
    }

    @RequestMapping(value = {"create"})
    public String create(Model model) {
        model.addAttribute("orderlist", "order");

        return "order/order/create";
    }

}
