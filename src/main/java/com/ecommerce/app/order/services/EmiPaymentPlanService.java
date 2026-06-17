package com.ecommerce.app.order.services;

import com.ecommerce.app.order.model.EmiPaymentPlan;
import com.ecommerce.app.order.model.EmiStatus;
import com.ecommerce.app.order.model.OrderItem;
import com.ecommerce.app.order.model.SalesOrder;
import com.ecommerce.app.order.repository.EmiPaymentPlanRepository;
import com.ecommerce.app.order.repository.OrderItemRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmiPaymentPlanService {

    private static final BigDecimal DEFAULT_INTEREST_RATE = BigDecimal.ZERO;
    private static final Set<Integer> ALLOWED_TENURES = Set.of(3, 6, 9, 12);

    @Autowired
    private EmiPaymentPlanRepository emiPaymentPlanRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public Optional<EmiPaymentPlan> findByOrderId(Long orderId) {
        return emiPaymentPlanRepository.findBySalesOrder_Id(orderId);
    }

    @Transactional(readOnly = true)
    public Optional<EmiPaymentPlan> findByIdForCustomer(Long planId, Long customerId) {
        return emiPaymentPlanRepository.findByIdAndCustomer_Id(planId, customerId);
    }

    @Transactional(readOnly = true)
    public Optional<EmiPaymentPlan> findById(Long planId) {
        return emiPaymentPlanRepository.findById(planId);
    }

    @Transactional(readOnly = true)
    public List<EmiPaymentPlan> findPlansForCustomer(Long customerId) {
        return emiPaymentPlanRepository.findByCustomer_IdOrderByIdDesc(customerId);
    }

    @Transactional(readOnly = true)
    public boolean orderEligibleForEmi(SalesOrder order) {
        if (order == null || order.getId() == null) {
            return false;
        }

        List<OrderItem> items = orderItemRepository.findBySalesOrder_Id(order.getId());
        return items != null
                && !items.isEmpty()
                && items.stream().allMatch(item -> item.getProduct() != null
                && Boolean.TRUE.equals(item.getProduct().getEmiavailable())
                && item.getProduct().getProductType() != com.ecommerce.app.product.model.ProductTypeEnum.Virtual);
    }

    @Transactional
    public EmiPaymentPlan createPlanForOrder(SalesOrder order, Integer tenureMonths) {
        if (order == null || order.getId() == null) {
            throw new IllegalArgumentException("Order not found for Meritten EMI plan creation.");
        }

        if (!orderEligibleForEmi(order)) {
            throw new IllegalArgumentException("This order is not eligible for Meritten EMI.");
        }

        Optional<EmiPaymentPlan> existing = emiPaymentPlanRepository.findBySalesOrder_Id(order.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        BigDecimal orderAmount = money(order.getGrandTotal());
        int normalizedTenure = normalizeTenureMonths(tenureMonths);

        EmiPaymentPlan plan = new EmiPaymentPlan();
        plan.setCustomer(order.getCustomer());
        plan.setSalesOrder(order);
        plan.setOrderAmount(orderAmount);
        plan.setDownPaymentAmount(BigDecimal.ZERO);
        plan.setFinancedAmount(orderAmount);
        plan.setInterestRate(DEFAULT_INTEREST_RATE);
        plan.setTenureMonths(normalizedTenure);
        plan.setInstallmentAmount(normalizedTenure > 0
                ? orderAmount.divide(BigDecimal.valueOf(normalizedTenure), 2, RoundingMode.HALF_UP)
                : orderAmount);
        plan.setTotalPayableAmount(orderAmount);
        plan.setRemainingBalance(orderAmount);
        plan.setStatus(EmiStatus.PENDING_PROVIDER);
        plan.setProviderRequestedOn(LocalDateTime.now());
        plan.setMerchantSettledAmount(BigDecimal.ZERO);

        return emiPaymentPlanRepository.save(plan);
    }

    @Transactional
    public EmiPaymentPlan approveByProvider(EmiPaymentPlan plan, String providerName, String providerReference, String decisionNote) {
        if (plan == null || plan.getId() == null) {
            throw new IllegalArgumentException("Meritten EMI plan not found.");
        }

        if (plan.isProviderRejected() || plan.isCancelledPlan()) {
            throw new IllegalArgumentException("This Meritten EMI request was already rejected or cancelled.");
        }

        if (plan.isProviderApproved() && money(plan.getMerchantSettledAmount()).compareTo(BigDecimal.ZERO) > 0) {
            return plan;
        }

        BigDecimal financedAmount = money(plan.getFinancedAmount()).compareTo(BigDecimal.ZERO) > 0
                ? money(plan.getFinancedAmount())
                : money(plan.getOrderAmount());

        plan.setProviderName(cleanText(providerName));
        plan.setProviderReference(cleanText(providerReference));
        plan.setProviderDecisionNote(cleanText(decisionNote));
        plan.setProviderRespondedOn(LocalDateTime.now());
        plan.setMerchantSettledAmount(money(plan.getOrderAmount()));
        plan.setMerchantSettledOn(LocalDateTime.now());
        plan.setRemainingBalance(financedAmount);
        plan.setStatus(EmiStatus.APPROVED_BY_PROVIDER);
        return emiPaymentPlanRepository.save(plan);
    }

    @Transactional
    public EmiPaymentPlan rejectByProvider(EmiPaymentPlan plan, String providerName, String providerReference, String decisionNote) {
        if (plan == null || plan.getId() == null) {
            throw new IllegalArgumentException("Meritten EMI plan not found.");
        }

        if (plan.isProviderApproved()) {
            throw new IllegalArgumentException("This Meritten EMI request has already been approved by the provider.");
        }

        if (plan.isProviderRejected() || plan.isCancelledPlan()) {
            return plan;
        }

        plan.setProviderName(cleanText(providerName));
        plan.setProviderReference(cleanText(providerReference));
        plan.setProviderDecisionNote(cleanText(decisionNote));
        plan.setProviderRespondedOn(LocalDateTime.now());
        plan.setMerchantSettledAmount(BigDecimal.ZERO);
        plan.setMerchantSettledOn(null);
        plan.setRemainingBalance(BigDecimal.ZERO);
        plan.setStatus(EmiStatus.REJECTED_BY_PROVIDER);
        return emiPaymentPlanRepository.save(plan);
    }

    @Transactional
    public EmiPaymentPlan cancelPlan(EmiPaymentPlan plan, String decisionNote) {
        if (plan == null || plan.getId() == null) {
            throw new IllegalArgumentException("Meritten EMI plan not found.");
        }

        if (plan.isProviderApproved()) {
            throw new IllegalArgumentException("An approved Meritten EMI plan cannot be cancelled from this flow.");
        }

        if (plan.getStatus() == EmiStatus.CANCELLED) {
            return plan;
        }

        plan.setProviderDecisionNote(cleanText(decisionNote));
        plan.setProviderRespondedOn(LocalDateTime.now());
        plan.setMerchantSettledAmount(BigDecimal.ZERO);
        plan.setMerchantSettledOn(null);
        plan.setRemainingBalance(BigDecimal.ZERO);
        plan.setStatus(EmiStatus.CANCELLED);
        return emiPaymentPlanRepository.save(plan);
    }

    public int normalizeTenureMonths(Integer tenureMonths) {
        return tenureMonths != null && ALLOWED_TENURES.contains(tenureMonths) ? tenureMonths : 3;
    }

    private BigDecimal money(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
