/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.customer.controller;

import com.ecommerce.app.model.cart.CartItem;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.order.model.OrderItem;
import com.ecommerce.app.order.model.OrderStatus;
import com.ecommerce.app.order.model.SalesOrder;
import com.ecommerce.app.order.repository.OrderItemRepository;
import com.ecommerce.app.ripository.BloodGroupRepository;
import com.ecommerce.app.ripository.ProfileRepository;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ecommerce.app.order.repository.SalesOrderRepository;



/**
 *
 * @author User
 */
@Controller
@RequestMapping("/customerorder")
//@PreAuthorize("hasAuthority('customer')")
public class CustomerOrderController {

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    BloodGroupRepository bloodGroupRepository;

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    SalesOrderRepository salesOrderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model, SalesOrder order, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        Users userId = new Users();

        userId.setId(loggedUserService.activeUserid());

        model.addAttribute("orderlist", salesOrderRepository.findByCustomer(userId));

        model.addAttribute("orderlist-panding", salesOrderRepository.findByCustomerAndStatusOrderByIdDesc(userId, OrderStatus.Pending));

        return "customer/order/index";
    }

    @RequestMapping(value = {"/save"})
    public String save(Model model, HttpSession session) {

        double grandtotal = 0.00;
        List<CartItem> cartitem = new ArrayList<>();
        if (session.getAttribute("sessioncart") != null) {

            cartitem = (List<CartItem>) session.getAttribute("sessioncart");

            grandtotal = total(cartitem);
        }

        SalesOrder order = new SalesOrder();

        Users userId = new Users();

        userId.setId(loggedUserService.activeUserid());

        order.setCustomer(userId);

        order.setTotal(grandtotal);

        if (grandtotal > 0) {

            order.setStatus(OrderStatus.Pending);

        } else {
            order.setStatus(OrderStatus.Complete);
        }

        salesOrderRepository.save(order);

        // sales order end
        // sales order item start 
        SalesOrder lastid = salesOrderRepository.findFirstByOrderByIdDesc();

        List<OrderItem> orderitems = new ArrayList<>();

        for (int i = 0; i < cartitem.size(); i++) {

            OrderItem orderitem = new OrderItem();

            orderitem.setSalesOrder(lastid);
            orderitem.setProduct(cartitem.get(i).getProduct());
            orderitem.setQuantity(cartitem.get(i).getQuantity());
            orderitems.add(orderitem);
        }
        orderItemRepository.saveAll(orderitems);
        session.removeAttribute("sessioncart");
        return "redirect:/customer/";
    }

    private double total(List< CartItem> cartitem) {

//        cartitem = new ArrayList<>();
        double total = 0.00;

        if (!cartitem.isEmpty()) {

            for (int i = 0; i < cartitem.size(); i++) {

                total += cartitem.get(i).getProduct().getPrice() * cartitem.get(i).getQuantity();
            }
            return total;
        }
        return total;
    }

    @RequestMapping(value = {"/details/{oid}"})
    public String details(Model model, @PathVariable Long oid, SalesOrder salesOrder) {

        model.addAttribute("orderdetails", salesOrderRepository.getOne(oid));

        return "customer/order/order_details";
    }

    @RequestMapping(value = {"/payment/{orderid}"})
    public String payment(Model model, @PathVariable Long orderid, SalesOrder order, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        Users userId = new Users();

        userId.setId(loggedUserService.activeUserid());

        SalesOrder orders = salesOrderRepository.getOne(orderid);

        model.addAttribute("orderinfo", orders);

        return "customer/order/payment";
    }

    @RequestMapping(value = {"/payment_success/{orderid}"})
    public String paymentsuccess(Model model, @PathVariable Long orderid, SalesOrder order, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        Users userId = new Users();
        userId.setId(loggedUserService.activeUserid());

        SalesOrder orders = salesOrderRepository.getOne(orderid);
        orders.setStatus(OrderStatus.Complete);
        salesOrderRepository.save(orders);
        model.addAttribute("orderinfo", orders);
        return "customer/order/payment_success";
    }

    @RequestMapping(value = {"/payment_failed/{orderid}"})
    public String paymentfailed(Model model, @PathVariable Long orderid, SalesOrder order, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        Users userId = new Users();

        userId.setId(loggedUserService.activeUserid());

        SalesOrder orders = salesOrderRepository.getOne(orderid);

        model.addAttribute("orderinfo", orders);

        return "customer/order/payment_failed";
    }

    @RequestMapping(value = {"/payment_cancelled/{orderid}"})
    public String paymentcancelled(Model model, @PathVariable Long orderid, SalesOrder order, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        Users userId = new Users();

        userId.setId(loggedUserService.activeUserid());

        SalesOrder orders = salesOrderRepository.getOne(orderid);

        model.addAttribute("orderinfo", orders);

        return "customer/order/payment_cancelled";
    }

}
