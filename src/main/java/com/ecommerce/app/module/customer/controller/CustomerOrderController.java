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
import com.ecommerce.app.order.model.EmiPaymentPlan;
import com.ecommerce.app.order.model.PaymentMethod;
import com.ecommerce.app.order.model.SalesOrder;
import com.ecommerce.app.order.repository.BillingAddressRepository;
import com.ecommerce.app.order.repository.OrderHistoryRepository;
import com.ecommerce.app.order.repository.OrderItemRepository;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import com.ecommerce.app.order.repository.ShippingAddressRepository;
import com.ecommerce.app.order.services.EmiPaymentPlanService;
import com.ecommerce.app.order.services.OrderItemService;
import com.ecommerce.app.order.services.PaymentService;
import com.ecommerce.app.order.services.PaymentService.PaymentSummary;
import com.ecommerce.app.order.services.SalesOrderService;
import com.ecommerce.app.review.services.ProductReviewService;
import com.ecommerce.app.ripository.ProfileRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @Autowired
    PaymentService paymentService;

    @Autowired
    EmiPaymentPlanService emiPaymentPlanService;

    @Autowired
    ProductReviewService productReviewService;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model, SalesOrder order, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        Users userId = new Users();

        userId.setId(loggedUserService.activeUserid());

        List<EmiPaymentPlan> customerEmiPlans = emiPaymentPlanService.findPlansForCustomer(userId.getId());
        model.addAttribute("orderlist", salesOrderService.admin_all_Sales_order_list(null, userId.getId()));
        model.addAttribute(
                "emiPlanByOrderId",
                customerEmiPlans.stream()
                        .filter(plan -> plan.getSalesOrder() != null && plan.getSalesOrder().getId() != null)
                        .collect(Collectors.toMap(plan -> plan.getSalesOrder().getId(), EmiPaymentPlan::getId, (first, ignored) -> first))
        );
        model.addAttribute(
                "emiPlanBlocksPaymentByOrderId",
                customerEmiPlans.stream()
                        .filter(plan -> plan.getSalesOrder() != null && plan.getSalesOrder().getId() != null)
                        .collect(Collectors.toMap(plan -> plan.getSalesOrder().getId(), EmiPaymentPlan::getBlocksDirectPayment, (first, ignored) -> first))
        );

        model.addAttribute("orderlist-panding", salesOrderRepository.findByCustomerAndStatusOrderByIdDesc(userId, OrderStatus.PENDING));

        return "customer/order/index";
    }

    @GetMapping("/details/{oid}")
    public String details(Model model, @PathVariable Long oid, RedirectAttributes redirectAttributes) {
        SalesOrder salesOrder;
        try {
            salesOrder = salesOrderService.getCustomerOrderForUser(oid, loggedUserService.activeUserid());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/customerorder/index";
        }

        long customerId = salesOrder.getCustomer().getId();
        List<Map<String, Object>> orderItems = productReviewService.enrichOrderItemsWithReviewData(
                orderItemService.item_List_By_SalesOrder(oid),
                customerId,
                salesOrder.getStatus()
        );
        orderItems = salesOrderService.enrichOrderItemsWithReturnData(orderItems, salesOrder.getStatus());

        model.addAttribute("orderdetails", salesOrder);
        model.addAttribute("orderitem", orderItems);
        model.addAttribute("total_sales_price", orderItemService.getTotalSalesPriceBySalesOrder(oid));
        model.addAttribute("total_quantity", orderItemService.getTotalQuantityBySalesOrder(oid));
        model.addAttribute("total_discount", orderItemService.getTotalDiscountBySalesOrder(oid));
        model.addAttribute("total_company_profit", orderItemService.getTotalCompanyProfitByItemBySalesOrder(oid));
        model.addAttribute("total_vat", orderItemService.getTotalVatlBySalesOrder(oid));
        model.addAttribute("total_vendorAmount", orderItemService.getTotalVendorAmountBySalesOrder(oid));
        model.addAttribute("item_total", orderItemService.getitemTotalBySalesOrder(oid));
        model.addAttribute("statusType", OrderStatus.values());
        List<OrderHistory> history = orderHistoryRepository.findBySalesOrderIdOrderByIdDesc(oid);
        model.addAttribute("orderhistory", history);
        // Date: 2026-04-20: some customers have multiple billing addresses; pick latest.
        model.addAttribute("billingAddress", billingAddressRepository.findFirstByUserId_IdOrderByIdDesc(customerId).orElse(null));
        model.addAttribute("shippingAddress", shippingAddressRepository.findByOrder_Id(oid));
        model.addAttribute("payments", paymentService.getPaymentsForOrder(oid));
        model.addAttribute("paymentSummary", paymentService.getPaymentSummary(salesOrder));
        EmiPaymentPlan emiPlan = emiPaymentPlanService.findByOrderId(oid).orElse(null);
        model.addAttribute("emiPlan", emiPlan);
        model.addAttribute("emiPlanBlocksDirectPayment", emiPlan != null && emiPlan.getBlocksDirectPayment());
        model.addAttribute("customerCanCancelOrder", salesOrderService.canCustomerCancelOrder(salesOrder));
        model.addAttribute("customerCanRequestReturn", salesOrderService.canCustomerRequestReturn(salesOrder));
        model.addAttribute("customerCancellationMessage", salesOrderService.getCustomerCancellationPolicyMessage(salesOrder));
        return "customer/order/order_details";
    }

    @PostMapping("/statuschange")
    public String statusChange(Model model,
            SalesOrder salesOrder,
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "status", required = false) OrderStatus status,
            @RequestParam(name = "remark", required = false) String remark,
            RedirectAttributes redirectAttributes
    ) {
        try {
            salesOrderService.changeStatusAsCustomer(id, loggedUserService.activeUserid(), status, remark);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/customerorder/details/" + id;

    }

    @PostMapping("/request-item-return")
    public String requestItemReturn(
            @RequestParam(name = "orderId") Long orderId,
            @RequestParam(name = "itemIds", required = false) List<Long> itemIds,
            @RequestParam(name = "remark", required = false) String remark,
            RedirectAttributes redirectAttributes
    ) {
        try {
            salesOrderService.requestItemReturnsAsCustomer(orderId, loggedUserService.activeUserid(), itemIds, remark);
            redirectAttributes.addFlashAttribute("successMessage", "Return request submitted for the selected item(s).");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/customerorder/details/" + orderId;
    }

    @RequestMapping(value = {"/payment/{orderid}"})
    public String payment(Model model,
            @PathVariable Long orderid,
            @RequestParam(name = "method", required = false) String method,
            RedirectAttributes redirectAttributes) {
        SalesOrder orders;
        try {
            orders = salesOrderService.getCustomerOrderForUser(orderid, loggedUserService.activeUserid());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/customerorder/index";
        }

        EmiPaymentPlan emiPlan = emiPaymentPlanService.findByOrderId(orderid).orElse(null);
        if (emiPlan != null && emiPlan.getBlocksDirectPayment()) {
            return "redirect:/customer-meritten-emi/details/" + emiPlan.getId();
        }

        Object selectedMethod = model.asMap().get("selectedPaymentMethod");
        PaymentSummary paymentSummary = paymentService.getPaymentSummary(orders);

        model.addAttribute("orderinfo", orders);
        model.addAttribute("payments", paymentService.getPaymentsForOrder(orderid));
        model.addAttribute("paymentSummary", paymentSummary);
        if (selectedMethod instanceof PaymentMethod paymentMethod) {
            model.addAttribute("selectedPaymentMethod", paymentMethod);
        } else if (selectedMethod instanceof String paymentMethodName) {
            model.addAttribute("selectedPaymentMethod", resolvePaymentMethod(paymentMethodName));
        } else {
            model.addAttribute("selectedPaymentMethod", resolvePaymentMethod(method));
        }
        model.addAttribute("methods", List.of(PaymentMethod.SSLCOMMERZ, PaymentMethod.BKASH));
        return "customer/order/payment";
    }

    @PostMapping("/payment/{orderid}")
    public String submitPayment(@PathVariable Long orderid,
            @RequestParam(name = "paymentMethod") PaymentMethod paymentMethod,
            @RequestParam(name = "paidAmount") BigDecimal paidAmount,
            @RequestParam(name = "transactionId", required = false) String transactionId,
            @RequestParam(name = "paymentDetails", required = false) String paymentDetails,
            RedirectAttributes redirectAttributes) {

        SalesOrder order;
        try {
            order = salesOrderService.getCustomerOrderForUser(orderid, loggedUserService.activeUserid());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Order not found.");
            return "redirect:/customerorder/index";
        }

        EmiPaymentPlan emiPlan = emiPaymentPlanService.findByOrderId(orderid).orElse(null);
        if (emiPlan != null && emiPlan.getBlocksDirectPayment()) {
            redirectAttributes.addFlashAttribute("errorMessage", "This order is waiting on a provider-managed Meritten EMI decision. Please review the Meritten EMI status page.");
            return "redirect:/customer-meritten-emi/details/" + emiPlan.getId();
        }

        if (!paymentMethod.isOnlineGateway()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please choose SSLCommerz or bKash for online payment.");
            redirectAttributes.addFlashAttribute("selectedPaymentMethod", PaymentMethod.SSLCOMMERZ.name());
            return "redirect:/customerorder/payment/" + orderid;
        }

        try {
            paymentService.recordPayment(order, paymentMethod, paidAmount, transactionId, paymentDetails);
            PaymentSummary summary = paymentService.getPaymentSummary(order);

            if (summary.isFullyPaid()) {
                SalesOrder paidOrder = salesOrderService.finalizePaidOrder(orderid);
                createPaymentHistory(
                        paidOrder,
                        paymentMethod.getDisplayName() + " payment completed. Remaining due cleared."
                );
                redirectAttributes.addFlashAttribute("successMessage", "Payment completed successfully.");
            } else {
                String historyRemark = "Partial payment received via " + paymentMethod.getDisplayName()
                        + ". Remaining due: " + summary.getRemainingAmount() + " BDT.";
                String successMessage = "Payment received successfully.";

                if (summary.getRemainingAdvanceDue().compareTo(BigDecimal.ZERO) > 0) {
                    historyRemark = "Advance payment received via " + paymentMethod.getDisplayName()
                            + ". Remaining advance due: " + summary.getRemainingAdvanceDue()
                            + " BDT. Remaining COD due: " + summary.getRemainingCodDue() + " BDT.";
                    successMessage = "Advance payment received successfully.";
                } else if (summary.getRemainingCodDue().compareTo(BigDecimal.ZERO) > 0) {
                    order = salesOrderService.confirmOrderAfterAdvancePayment(orderid);
                    historyRemark = "Advance payment completed via " + paymentMethod.getDisplayName()
                            + ". Remaining COD due: " + summary.getRemainingCodDue()
                            + " BDT. Order moved to confirmed status for fulfillment.";
                    successMessage = "Advance payment received successfully.";
                }

                createPaymentHistory(
                        order,
                        historyRemark
                );
                redirectAttributes.addFlashAttribute("successMessage", successMessage);
            }

            return "redirect:/customerorder/payment_success/" + orderid;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("selectedPaymentMethod", paymentMethod.name());
            return "redirect:/customerorder/payment/" + orderid;
        }
    }

    @RequestMapping(value = {"/payment_success/{orderid}"})
    public String paymentsuccess(Model model, @PathVariable Long orderid, SalesOrder order, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        SalesOrder orders;
        try {
            orders = salesOrderService.getCustomerOrderForUser(orderid, loggedUserService.activeUserid());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/customerorder/index";
        }

        model.addAttribute("orderinfo", orders);
        model.addAttribute("orderitem", orderItemService.item_List_By_SalesOrder(orderid));
        model.addAttribute("payments", paymentService.getPaymentsForOrder(orderid));
        model.addAttribute("paymentSummary", paymentService.getPaymentSummary(orders));
        return "customer/order/payment_success";
    }

    @RequestMapping(value = {"/payment_failed/{orderid}"})
    public String paymentfailed(Model model, @PathVariable Long orderid, SalesOrder order, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        SalesOrder orders;
        try {
            orders = salesOrderService.getCustomerOrderForUser(orderid, loggedUserService.activeUserid());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/customerorder/index";
        }

        model.addAttribute("orderinfo", orders);
        model.addAttribute("paymentSummary", paymentService.getPaymentSummary(orders));

        return "customer/order/payment_failed";
    }

    @RequestMapping(value = {"/payment_cancelled/{orderid}"})
    public String paymentcancelled(Model model, @PathVariable Long orderid, SalesOrder order, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        SalesOrder orders;
        try {
            orders = salesOrderService.getCustomerOrderForUser(orderid, loggedUserService.activeUserid());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/customerorder/index";
        }

        model.addAttribute("orderinfo", orders);
        model.addAttribute("paymentSummary", paymentService.getPaymentSummary(orders));

        return "customer/order/payment_cancelled";
    }

    private PaymentMethod resolvePaymentMethod(String method) {
        if (method == null || method.isBlank()) {
            return PaymentMethod.SSLCOMMERZ;
        }

        try {
            PaymentMethod paymentMethod = PaymentMethod.valueOf(method.trim().toUpperCase());
            return paymentMethod.isOnlineGateway() ? paymentMethod : PaymentMethod.SSLCOMMERZ;
        } catch (IllegalArgumentException ex) {
            return PaymentMethod.SSLCOMMERZ;
        }
    }

    private void createPaymentHistory(SalesOrder order, String remark) {
        OrderHistory orderHistory = new OrderHistory();
        orderHistory.setSalesOrder(order);
        orderHistory.setStatus(order.getStatus());
        orderHistory.setOrderStatusChanged(OrderStatusChangedBy.Customer);
        orderHistory.setRemark(remark);
        orderHistoryRepository.save(orderHistory);
    }

}
