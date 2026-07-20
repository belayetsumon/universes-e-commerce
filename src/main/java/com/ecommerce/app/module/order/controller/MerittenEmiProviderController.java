package com.ecommerce.app.module.order.controller;

import com.ecommerce.app.module.order.model.EmiPaymentPlan;
import com.ecommerce.app.module.order.model.OrderHistory;
import com.ecommerce.app.module.order.model.OrderStatusChangedBy;
import com.ecommerce.app.module.order.model.PaymentMethod;
import com.ecommerce.app.module.order.repository.OrderHistoryRepository;
import com.ecommerce.app.module.order.services.EmiPaymentPlanService;
import com.ecommerce.app.module.order.services.PaymentService;
import com.ecommerce.app.module.order.services.SalesOrderService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment/meritten-emi/provider")
public class MerittenEmiProviderController {

    @Autowired
    private EmiPaymentPlanService emiPaymentPlanService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    @PostMapping("/callback/{planId}")
    public ResponseEntity<Map<String, Object>> callback(
            @PathVariable Long planId,
            @RequestParam(name = "decision") String decision,
            @RequestParam(name = "providerName", required = false) String providerName,
            @RequestParam(name = "providerReference", required = false) String providerReference,
            @RequestParam(name = "providerNote", required = false) String providerNote) {

        EmiPaymentPlan plan = emiPaymentPlanService.findById(planId).orElse(null);
        if (plan == null) {
            return buildResponse(HttpStatus.NOT_FOUND, "Meritten EMI plan not found.", null);
        }

        String normalizedDecision = decision == null ? "" : decision.trim().toUpperCase();
        if (!"APPROVED".equals(normalizedDecision) && !"REJECTED".equals(normalizedDecision)) {
            return buildResponse(HttpStatus.BAD_REQUEST, "Decision must be either APPROVED or REJECTED.", plan);
        }

        try {
            if ("APPROVED".equals(normalizedDecision)) {
                plan = emiPaymentPlanService.approveByProvider(plan, providerName, providerReference, providerNote);

                PaymentService.PaymentSummary paymentSummary = paymentService.getPaymentSummary(plan.getSalesOrder());
                if (!paymentSummary.isFullyPaid()) {
                    paymentService.recordPayment(
                            plan.getSalesOrder(),
                            PaymentMethod.EMI,
                            plan.getOrderAmount(),
                            providerReference,
                            "Provider approved Meritten EMI and settled the full order amount to the merchant."
                    );
                }

                plan.setSalesOrder(salesOrderService.finalizePaidOrder(plan.getSalesOrder().getId()));
                createOrderHistory(
                        plan,
                        "Meritten EMI approved by provider. Full order amount settled to merchant. Reference: "
                        + (plan.getProviderReference() != null ? plan.getProviderReference() : "N/A")
                );
                return buildResponse(HttpStatus.OK, "Meritten EMI approved successfully.", plan);
            }

            plan = emiPaymentPlanService.rejectByProvider(plan, providerName, providerReference, providerNote);
            createOrderHistory(
                    plan,
                    "Meritten EMI rejected by provider."
                    + (plan.getProviderDecisionNote() != null ? " Note: " + plan.getProviderDecisionNote() : "")
            );
            return buildResponse(HttpStatus.OK, "Meritten EMI rejected successfully.", plan);
        } catch (IllegalArgumentException ex) {
            return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), plan);
        }
    }

    private void createOrderHistory(EmiPaymentPlan plan, String remark) {
        OrderHistory orderHistory = new OrderHistory();
        orderHistory.setSalesOrder(plan.getSalesOrder());
        orderHistory.setStatus(plan.getSalesOrder().getStatus());
        orderHistory.setOrderStatusChanged(OrderStatusChangedBy.MarketPlace);
        orderHistory.setRemark(remark);
        orderHistoryRepository.save(orderHistory);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message, EmiPaymentPlan plan) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", message);
        response.put("status", status.value());
        if (plan != null) {
            response.put("planId", plan.getId());
            response.put("orderId", plan.getSalesOrder() != null ? plan.getSalesOrder().getId() : null);
            response.put("emiStatus", plan.getStatus() != null ? plan.getStatus().name() : null);
            response.put("providerReference", plan.getProviderReference());
        }
        return ResponseEntity.status(status).body(response);
    }
}
