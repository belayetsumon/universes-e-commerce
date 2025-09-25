/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.customer.controller;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.order.model.OrderHistory;
import com.ecommerce.app.order.model.OrderStatus;
import com.ecommerce.app.order.model.OrderStatusChangedBy;
import com.ecommerce.app.order.model.SalesOrder;
import com.ecommerce.app.order.repository.BillingAddressRepository;
import com.ecommerce.app.order.repository.OrderHistoryRepository;
import com.ecommerce.app.order.repository.OrderItemRepository;
import com.ecommerce.app.ripository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import com.ecommerce.app.order.repository.ShippingAddressRepository;
import com.ecommerce.app.order.services.OrderItemService;
import com.ecommerce.app.order.services.SalesOrderService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    LoggedUserService loggedUserService;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    SalesOrderRepository salesOrderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    BillingAddressRepository billingAddressRepository;

    @Autowired
    ShippingAddressRepository shippingAddressRepository;

    @Autowired
    OrderHistoryRepository orderHistoryRepository;

    @Autowired
    SalesOrderService salesOrderService;

    @Autowired
    OrderItemService orderItemService;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model, SalesOrder order, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        Users userId = new Users();

        userId.setId(loggedUserService.activeUserid());

        model.addAttribute("orderlist", salesOrderService.admin_all_Sales_order_list(null, userId.getId()));

        model.addAttribute("orderlist-panding", salesOrderRepository.findByCustomerAndStatusOrderByIdDesc(userId, OrderStatus.PENDING));

        return "customer/order/index";
    }

    @GetMapping("/details/{oid}")
    public String details(Model model, @PathVariable Long oid) {
        // Try to find the order by ID

        SalesOrder salesOrder = salesOrderRepository.getReferenceById(oid);

        long customerId = salesOrder.getCustomer().getId();

        model.addAttribute("orderdetails", salesOrderRepository.getReferenceById(oid));
        model.addAttribute("orderitem", orderItemService.item_List_By_SalesOrder(oid));

        model.addAttribute("total_sales_price", orderItemService.getTotalSalesPriceBySalesOrder(oid));
        model.addAttribute("total_quantity", orderItemService.getTotalQuantityBySalesOrder(oid));
        model.addAttribute("total_discount", orderItemService.getTotalDiscountBySalesOrder(oid));
        model.addAttribute("total_company_profit", orderItemService.getTotalCompanyProfitByItemBySalesOrder(oid));
        model.addAttribute("total_vat", orderItemService.getTotalVatlBySalesOrder(oid));
        model.addAttribute("total_vendorAmount", orderItemService.getTotalVendorAmountBySalesOrder(oid));
        model.addAttribute("item_total", orderItemService.getitemTotalBySalesOrder(oid));
//
//        // Add status types
        model.addAttribute("statusType", OrderStatus.values());
//
//        // Load and add order history
        List<OrderHistory> history = orderHistoryRepository.findBySalesOrderIdOrderByIdDesc(oid);
        model.addAttribute("orderhistory", history);
        // Load billing address if customer is available
        model.addAttribute("billingAddress", billingAddressRepository.findByUserId_Id(customerId).get());
        model.addAttribute("shippingAddress", shippingAddressRepository.findByOrder_Id(oid));
        return "customer/order/order_details";
    }

    @RequestMapping(value = {"/statuschange"})
    public String statusChange(Model model,
            SalesOrder salesOrder,
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "status", required = false) OrderStatus status,
            @RequestParam(name = "remark", required = false) String remark
    ) {
        salesOrder = salesOrderRepository.getReferenceById(id);

        salesOrder.setStatus(status);

        salesOrderRepository.save(salesOrder);

        OrderHistory orderHistory = new OrderHistory();
        orderHistory.setStatus(status);
        orderHistory.setOrderStatusChanged(OrderStatusChangedBy.Customer);
        orderHistory.setRemark(remark);
        orderHistory.setSalesOrder(salesOrder);

        orderHistoryRepository.save(orderHistory);

        return "redirect:/customerorder/details/" + id;

    }

    @RequestMapping(value = {"/payment/{orderid}"})
    public String payment(Model model, @PathVariable Long orderid, SalesOrder order, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        Users userId = new Users();

        userId.setId(loggedUserService.activeUserid());

        SalesOrder orders = salesOrderRepository.findById(orderid).orElse(null);

        model.addAttribute("orderinfo", orders);

        return "customer/order/payment";
    }

    @RequestMapping(value = {"/payment_success/{orderid}"})
    public String paymentsuccess(Model model, @PathVariable Long orderid, SalesOrder order, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        Users userId = new Users();
        userId.setId(loggedUserService.activeUserid());
        SalesOrder orders = salesOrderRepository.findById(orderid).orElse(null);
        orders.setStatus(OrderStatus.COMPLETED);
        salesOrderRepository.save(orders);
        model.addAttribute("orderinfo", orders);
        return "customer/order/payment_success";
    }

    @RequestMapping(value = {"/payment_failed/{orderid}"})
    public String paymentfailed(Model model, @PathVariable Long orderid, SalesOrder order, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        Users userId = new Users();

        userId.setId(loggedUserService.activeUserid());

        SalesOrder orders = salesOrderRepository.findById(orderid).orElse(null);

        model.addAttribute("orderinfo", orders);

        return "customer/order/payment_failed";
    }

    @RequestMapping(value = {"/payment_cancelled/{orderid}"})
    public String paymentcancelled(Model model, @PathVariable Long orderid, SalesOrder order, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        Users userId = new Users();

        userId.setId(loggedUserService.activeUserid());

        SalesOrder orders = salesOrderRepository.findById(orderid).orElse(null);

        model.addAttribute("orderinfo", orders);

        return "customer/order/payment_cancelled";
    }

}
