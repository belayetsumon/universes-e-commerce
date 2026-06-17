package com.ecommerce.app.order.services;

import com.ecommerce.app.order.model.OrderPaymentPlan;
import com.ecommerce.app.order.model.OrderPaymentState;
import com.ecommerce.app.order.model.OrderStatus;
import com.ecommerce.app.order.model.Payment;
import com.ecommerce.app.order.model.PaymentMethod;
import com.ecommerce.app.order.model.PaymentStatus;
import com.ecommerce.app.order.model.SalesOrder;
import com.ecommerce.app.order.repository.PaymentRepository;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsForOrder(Long orderId) {
        return paymentRepository.findByOrder_IdOrderByIdDesc(orderId);
    }

    @Transactional(readOnly = true)
    public PaymentSummary getPaymentSummary(SalesOrder order) {
        if (order == null || order.getId() == null) {
            return PaymentSummary.empty();
        }

        return buildSummary(order, getPaymentsForOrder(order.getId()));
    }

    @Transactional
    public SalesOrder refreshOrderPaymentTracking(SalesOrder order) {
        if (order == null || order.getId() == null) {
            return order;
        }

        PaymentSummary summary = getPaymentSummary(order);
        order.setPaymentPlan(summary.getPaymentPlan());
        order.setAdvancePaid(summary.getAdvancePaid());
        order.setCodDue(summary.getCodDue());
        order.setPaymentState(summary.getOrderPaymentState());
        return salesOrderRepository.save(order);
    }

    @Transactional
    public Payment recordPayment(SalesOrder order, PaymentMethod paymentMethod, BigDecimal paymentAmount,
            String transactionId, String paymentDetails) {

        if (order == null || order.getId() == null) {
            throw new IllegalArgumentException("Order not found for payment.");
        }

        if (isReversedOrder(order)) {
            throw new IllegalArgumentException("Payments are not accepted for cancelled or returned orders.");
        }

        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method is required.");
        }

        BigDecimal normalizedAmount = safeMoney(paymentAmount);
        if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        PaymentSummary currentSummary = getPaymentSummary(order);
        if (currentSummary.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("This order is already fully paid.");
        }

        validateAmountAgainstPlan(currentSummary, paymentMethod, normalizedAmount);

        BigDecimal remainingAfterPayment = currentSummary.getRemainingAmount().subtract(normalizedAmount);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);
        payment.setTransactionId(
                transactionId != null && !transactionId.isBlank()
                ? transactionId.trim()
                : buildTransactionId(paymentMethod)
        );
        payment.setPaymentDetails(paymentDetails != null ? paymentDetails.trim() : null);
        payment.setTotalAmount(currentSummary.getOrderTotal().doubleValue());
        payment.setPaidAmount(normalizedAmount.doubleValue());
        payment.setRemainingAmount(remainingAfterPayment.doubleValue());
        payment.setPaymentStatus(remainingAfterPayment.compareTo(BigDecimal.ZERO) == 0
                ? PaymentStatus.Paid
                : PaymentStatus.Partial);

        Payment savedPayment = paymentRepository.save(payment);
        refreshOrderPaymentTracking(order);
        return savedPayment;
    }

    @Transactional
    public RefundResult refundPaidAmount(SalesOrder order, String paymentDetails) {
        if (order == null || order.getId() == null) {
            throw new IllegalArgumentException("Order not found for refund.");
        }

        PaymentSummary currentSummary = getPaymentSummary(order);
        return refundAmount(order, currentSummary.getTotalPaid(), paymentDetails);
    }

    @Transactional
    public RefundResult refundAmount(SalesOrder order, BigDecimal refundAmount, String paymentDetails) {
        if (order == null || order.getId() == null) {
            throw new IllegalArgumentException("Order not found for refund.");
        }

        PaymentSummary currentSummary = getPaymentSummary(order);
        BigDecimal refundableAmount = safeMoney(refundAmount);
        if (refundableAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return RefundResult.none();
        }

        if (refundableAmount.compareTo(currentSummary.getTotalPaid()) > 0) {
            throw new IllegalArgumentException("Refund amount cannot be greater than the amount already paid.");
        }

        PaymentMethod refundMethod = currentSummary.getLatestPaymentMethod() != null
                ? currentSummary.getLatestPaymentMethod()
                : PaymentMethod.WALLET;

        BigDecimal remainingAfterRefund = currentSummary.getOrderTotal()
                .subtract(currentSummary.getTotalPaid().subtract(refundableAmount));
        if (remainingAfterRefund.compareTo(BigDecimal.ZERO) < 0) {
            remainingAfterRefund = BigDecimal.ZERO;
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(refundMethod);
        payment.setTransactionId(buildRefundTransactionId(refundMethod));
        payment.setPaymentDetails(paymentDetails != null ? paymentDetails.trim() : null);
        payment.setTotalAmount(currentSummary.getOrderTotal().doubleValue());
        payment.setPaidAmount(refundableAmount.doubleValue());
        payment.setRemainingAmount(remainingAfterRefund.doubleValue());
        payment.setPaymentStatus(PaymentStatus.Refunded);

        Payment savedPayment = paymentRepository.save(payment);
        refreshOrderPaymentTracking(order);
        return new RefundResult(savedPayment, refundableAmount, refundMethod);
    }

    private PaymentSummary buildSummary(SalesOrder order, List<Payment> payments) {
        BigDecimal orderTotal = safeMoney(order.getGrandTotal());
        BigDecimal grossPaidAmount = BigDecimal.ZERO;
        BigDecimal refundedAmount = BigDecimal.ZERO;
        BigDecimal totalCodPaid = BigDecimal.ZERO;
        PaymentMethod latestSuccessfulMethod = null;

        for (Payment payment : payments) {
            if (isIgnoredPayment(payment)) {
                continue;
            }

            BigDecimal amount = safeMoney(payment.getPaidAmount());
            if (payment.getPaymentStatus() == PaymentStatus.Refunded) {
                refundedAmount = refundedAmount.add(amount);
                continue;
            }

            grossPaidAmount = grossPaidAmount.add(amount);
            if (payment.getPaymentMethod() == PaymentMethod.COD) {
                totalCodPaid = totalCodPaid.add(amount);
            }
            if (latestSuccessfulMethod == null) {
                latestSuccessfulMethod = payment.getPaymentMethod();
            }
        }

        BigDecimal totalPaid = grossPaidAmount.subtract(refundedAmount);
        if (totalPaid.compareTo(BigDecimal.ZERO) < 0) {
            totalPaid = BigDecimal.ZERO;
        }

        boolean reversedOrder = isReversedOrder(order);
        BigDecimal remaining = reversedOrder ? BigDecimal.ZERO : orderTotal.subtract(totalPaid);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        PaymentStatus currentStatus = resolvePaymentStatus(reversedOrder, refundedAmount, totalPaid, remaining);
        OrderPaymentPlan paymentPlan = resolvePaymentPlan(order, grossPaidAmount, totalCodPaid, latestSuccessfulMethod);

        BigDecimal plannedCodDue = reversedOrder
                ? BigDecimal.ZERO
                : resolvePlannedCodDue(order, orderTotal, paymentPlan);
        BigDecimal advanceTarget = reversedOrder
                ? BigDecimal.ZERO
                : orderTotal.subtract(plannedCodDue);
        if (advanceTarget.compareTo(BigDecimal.ZERO) < 0) {
            advanceTarget = BigDecimal.ZERO;
        }

        BigDecimal nonCodPaid = grossPaidAmount.subtract(totalCodPaid);
        if (nonCodPaid.compareTo(BigDecimal.ZERO) < 0) {
            nonCodPaid = BigDecimal.ZERO;
        }

        BigDecimal advancePaid = reversedOrder || paymentPlan == OrderPaymentPlan.FULL_COD
                ? BigDecimal.ZERO
                : min(nonCodPaid, advanceTarget);

        BigDecimal remainingAdvanceDue = reversedOrder
                ? BigDecimal.ZERO
                : advanceTarget.subtract(advancePaid);
        if (remainingAdvanceDue.compareTo(BigDecimal.ZERO) < 0) {
            remainingAdvanceDue = BigDecimal.ZERO;
        }

        BigDecimal remainingCodDue = reversedOrder
                ? BigDecimal.ZERO
                : plannedCodDue.subtract(totalCodPaid);
        if (remainingCodDue.compareTo(BigDecimal.ZERO) < 0) {
            remainingCodDue = BigDecimal.ZERO;
        }

        OrderPaymentState orderPaymentState = reversedOrder
                ? resolveTerminalPaymentState(refundedAmount)
                : resolvePaymentState(paymentPlan, totalPaid, remaining, remainingAdvanceDue, remainingCodDue);

        return new PaymentSummary(
                orderTotal,
                grossPaidAmount,
                refundedAmount,
                totalPaid,
                remaining,
                currentStatus,
                latestSuccessfulMethod,
                paymentPlan,
                orderPaymentState,
                advancePaid,
                plannedCodDue,
                advanceTarget,
                remainingAdvanceDue,
                remainingCodDue
        );
    }

    private void validateAmountAgainstPlan(PaymentSummary currentSummary, PaymentMethod paymentMethod,
            BigDecimal normalizedAmount) {
        if (paymentMethod == PaymentMethod.COD) {
            if (!currentSummary.isCodCollectionPending()) {
                throw new IllegalArgumentException("This order has no COD balance left to collect.");
            }

            if (normalizedAmount.compareTo(currentSummary.getRemainingCodDue()) > 0) {
                throw new IllegalArgumentException(
                        "COD collection cannot be greater than the remaining COD balance of "
                        + formatMoney(currentSummary.getRemainingCodDue()) + " BDT."
                );
            }
            return;
        }

        if (paymentMethod == PaymentMethod.EMI) {
            if (currentSummary.getPaymentPlan() != OrderPaymentPlan.EMI) {
                throw new IllegalArgumentException("This order is not using an EMI payment plan.");
            }

            if (normalizedAmount.compareTo(currentSummary.getRemainingAmount()) > 0) {
                throw new IllegalArgumentException(
                        "Payment amount cannot be greater than the remaining balance of "
                        + formatMoney(currentSummary.getRemainingAmount()) + " BDT."
                );
            }
            return;
        }

        BigDecimal allowedAmount = currentSummary.getMaxCustomerPaymentAmount();
        if (allowedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(currentSummary.getPaymentPlan() == OrderPaymentPlan.FULL_COD
                    ? "This order is marked for full Cash on Delivery. Online advance payment is not available."
                    : "This order has already received the planned advance payment. The remaining balance will be collected as COD.");
        }

        if (normalizedAmount.compareTo(allowedAmount) > 0) {
            throw new IllegalArgumentException(
                    "Payment amount cannot be greater than the current payable amount of "
                    + formatMoney(allowedAmount) + " BDT."
            );
        }
    }

    private boolean isIgnoredPayment(Payment payment) {
        return payment == null
                || payment.getPaymentStatus() == PaymentStatus.Failed
                || payment.getPaymentStatus() == PaymentStatus.Cancelled;
    }

    private boolean isReversedOrder(SalesOrder order) {
        return order != null
                && (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.RETURNED);
    }

    private PaymentStatus resolvePaymentStatus(boolean reversedOrder, BigDecimal refundedAmount,
            BigDecimal totalPaid, BigDecimal remaining) {
        if (reversedOrder) {
            if (refundedAmount.compareTo(BigDecimal.ZERO) > 0) {
                return PaymentStatus.Refunded;
            }
            return PaymentStatus.Cancelled;
        }

        if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            return PaymentStatus.Paid;
        }

        return totalPaid.compareTo(BigDecimal.ZERO) > 0
                ? PaymentStatus.Partial
                : PaymentStatus.Remaining;
    }

    private BigDecimal safeMoney(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safeMoney(Double amount) {
        return amount == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }

    private String buildTransactionId(PaymentMethod paymentMethod) {
        String prefix = paymentMethod != null ? paymentMethod.name() : "PAY";
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String buildRefundTransactionId(PaymentMethod paymentMethod) {
        String prefix = paymentMethod != null ? paymentMethod.name() : "REFUND";
        return "REFUND-" + prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String formatMoney(BigDecimal amount) {
        return safeMoney(amount).toPlainString();
    }

    private OrderPaymentPlan resolvePaymentPlan(SalesOrder order, BigDecimal totalPaid,
            BigDecimal totalCodPaid, PaymentMethod latestSuccessfulMethod) {
        if (order != null && order.getPaymentPlan() != null) {
            return order.getPaymentPlan();
        }

        if (latestSuccessfulMethod == PaymentMethod.EMI) {
            return OrderPaymentPlan.EMI;
        }

        if (totalPaid.compareTo(BigDecimal.ZERO) > 0 && totalCodPaid.compareTo(totalPaid) < 0) {
            return OrderPaymentPlan.FULL_PREPAID;
        }

        return OrderPaymentPlan.FULL_COD;
    }

    private BigDecimal resolvePlannedCodDue(SalesOrder order, BigDecimal orderTotal, OrderPaymentPlan paymentPlan) {
        if (paymentPlan == OrderPaymentPlan.FULL_PREPAID || paymentPlan == OrderPaymentPlan.EMI) {
            return BigDecimal.ZERO;
        }

        BigDecimal storedCodDue = safeMoney(order != null ? order.getCodDue() : BigDecimal.ZERO);
        if (storedCodDue.compareTo(BigDecimal.ZERO) <= 0 && paymentPlan == OrderPaymentPlan.FULL_COD) {
            return orderTotal;
        }

        return min(storedCodDue, orderTotal);
    }

    private OrderPaymentState resolvePaymentState(OrderPaymentPlan paymentPlan, BigDecimal totalPaid,
            BigDecimal remainingAmount, BigDecimal remainingAdvanceDue, BigDecimal remainingCodDue) {
        if (paymentPlan == OrderPaymentPlan.EMI && remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            return OrderPaymentState.EMI_PENDING;
        }

        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return OrderPaymentState.PAID;
        }

        if (paymentPlan == OrderPaymentPlan.FULL_COD) {
            return OrderPaymentState.COD_PENDING;
        }

        if (paymentPlan == OrderPaymentPlan.PARTIAL_ADVANCE_COD) {
            return remainingAdvanceDue.compareTo(BigDecimal.ZERO) > 0
                    ? OrderPaymentState.ADVANCE_PENDING
                    : OrderPaymentState.COD_PENDING;
        }

        return totalPaid.compareTo(BigDecimal.ZERO) > 0
                ? OrderPaymentState.PARTIALLY_PAID
                : OrderPaymentState.UNPAID;
    }

    private OrderPaymentState resolveTerminalPaymentState(BigDecimal refundedAmount) {
        return refundedAmount.compareTo(BigDecimal.ZERO) > 0
                ? OrderPaymentState.REFUNDED
                : OrderPaymentState.CANCELLED;
    }

    private BigDecimal min(BigDecimal first, BigDecimal second) {
        return first.compareTo(second) <= 0 ? first : second;
    }

    public static class RefundResult {

        private final Payment payment;
        private final BigDecimal amount;
        private final PaymentMethod paymentMethod;

        private RefundResult(Payment payment, BigDecimal amount, PaymentMethod paymentMethod) {
            this.payment = payment;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
        }

        public static RefundResult none() {
            return new RefundResult(null, BigDecimal.ZERO, null);
        }

        public Payment getPayment() {
            return payment;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public PaymentMethod getPaymentMethod() {
            return paymentMethod;
        }

        public boolean isRefunded() {
            return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
        }
    }

    public static class PaymentSummary {

        private final BigDecimal orderTotal;
        private final BigDecimal grossPaidAmount;
        private final BigDecimal refundedAmount;
        private final BigDecimal totalPaid;
        private final BigDecimal remainingAmount;
        private final PaymentStatus paymentStatus;
        private final PaymentMethod latestPaymentMethod;
        private final OrderPaymentPlan paymentPlan;
        private final OrderPaymentState orderPaymentState;
        private final BigDecimal advancePaid;
        private final BigDecimal codDue;
        private final BigDecimal advanceTarget;
        private final BigDecimal remainingAdvanceDue;
        private final BigDecimal remainingCodDue;

        public PaymentSummary(BigDecimal orderTotal, BigDecimal grossPaidAmount, BigDecimal refundedAmount,
                BigDecimal totalPaid, BigDecimal remainingAmount, PaymentStatus paymentStatus,
                PaymentMethod latestPaymentMethod, OrderPaymentPlan paymentPlan,
                OrderPaymentState orderPaymentState, BigDecimal advancePaid, BigDecimal codDue,
                BigDecimal advanceTarget, BigDecimal remainingAdvanceDue, BigDecimal remainingCodDue) {
            this.orderTotal = orderTotal;
            this.grossPaidAmount = grossPaidAmount;
            this.refundedAmount = refundedAmount;
            this.totalPaid = totalPaid;
            this.remainingAmount = remainingAmount;
            this.paymentStatus = paymentStatus;
            this.latestPaymentMethod = latestPaymentMethod;
            this.paymentPlan = paymentPlan;
            this.orderPaymentState = orderPaymentState;
            this.advancePaid = advancePaid;
            this.codDue = codDue;
            this.advanceTarget = advanceTarget;
            this.remainingAdvanceDue = remainingAdvanceDue;
            this.remainingCodDue = remainingCodDue;
        }

        public static PaymentSummary empty() {
            return new PaymentSummary(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    PaymentStatus.Remaining,
                    null,
                    OrderPaymentPlan.FULL_COD,
                    OrderPaymentState.UNPAID,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        public BigDecimal getOrderTotal() {
            return orderTotal;
        }

        public BigDecimal getGrossPaidAmount() {
            return grossPaidAmount;
        }

        public BigDecimal getRefundedAmount() {
            return refundedAmount;
        }

        public BigDecimal getTotalPaid() {
            return totalPaid;
        }

        public BigDecimal getRemainingAmount() {
            return remainingAmount;
        }

        public PaymentStatus getPaymentStatus() {
            return paymentStatus;
        }

        public PaymentMethod getLatestPaymentMethod() {
            return latestPaymentMethod;
        }

        public OrderPaymentPlan getPaymentPlan() {
            return paymentPlan;
        }

        public OrderPaymentState getOrderPaymentState() {
            return orderPaymentState;
        }

        public BigDecimal getAdvancePaid() {
            return advancePaid;
        }

        public BigDecimal getCodDue() {
            return codDue;
        }

        public BigDecimal getAdvanceTarget() {
            return advanceTarget;
        }

        public BigDecimal getRemainingAdvanceDue() {
            return remainingAdvanceDue;
        }

        public BigDecimal getRemainingCodDue() {
            return remainingCodDue;
        }

        public boolean isFullyPaid() {
            return remainingAmount.compareTo(BigDecimal.ZERO) <= 0;
        }

        public boolean isCustomerPaymentAllowed() {
            return getMaxCustomerPaymentAmount().compareTo(BigDecimal.ZERO) > 0;
        }

        public boolean isAdvancePaymentPending() {
            return remainingAdvanceDue.compareTo(BigDecimal.ZERO) > 0;
        }

        public BigDecimal getMaxCustomerPaymentAmount() {
            if (paymentPlan == OrderPaymentPlan.FULL_COD || paymentPlan == OrderPaymentPlan.EMI) {
                return BigDecimal.ZERO;
            }

            if (paymentPlan == OrderPaymentPlan.PARTIAL_ADVANCE_COD) {
                return remainingAdvanceDue;
            }

            return remainingAmount;
        }

        public boolean isCodCollectionPending() {
            return remainingCodDue.compareTo(BigDecimal.ZERO) > 0;
        }
    }
}
