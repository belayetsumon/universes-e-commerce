package com.ecommerce.app.module.customer.controller;

import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.order.model.EmiPaymentPlan;
import com.ecommerce.app.order.services.EmiPaymentPlanService;
import com.ecommerce.app.order.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping({"/customeremi", "/customer-meritten-emi"})
public class CustomerEmiController {

    @Autowired
    private LoggedUserService loggedUserService;

    @Autowired
    private EmiPaymentPlanService emiPaymentPlanService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping({"", "/", "/index"})
    public String index(Model model) {
        Long customerId = loggedUserService.activeUserid();
        model.addAttribute("plans", emiPaymentPlanService.findPlansForCustomer(customerId));
        return "customer/emi/index";
    }

    @GetMapping("/details/{planId}")
    public String details(Model model, @PathVariable Long planId) {
        EmiPaymentPlan plan = loadOwnedPlan(planId);
        if (plan == null) {
            model.addAttribute("errorMessage", "Meritten EMI plan not found.");
            model.addAttribute("plans", emiPaymentPlanService.findPlansForCustomer(loggedUserService.activeUserid()));
            return "customer/emi/index";
        }

        model.addAttribute("plan", plan);
        model.addAttribute("paymentSummary", paymentService.getPaymentSummary(plan.getSalesOrder()));
        return "customer/emi/details";
    }

    private EmiPaymentPlan loadOwnedPlan(Long planId) {
        Long customerId = loggedUserService.activeUserid();
        return emiPaymentPlanService.findByIdForCustomer(planId, customerId)
                .orElse(null);
    }
}
