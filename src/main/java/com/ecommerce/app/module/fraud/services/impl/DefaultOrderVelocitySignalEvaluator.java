package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.FraudReasonCode;
import com.ecommerce.app.module.fraud.model.FraudSignalSeverity;
import com.ecommerce.app.module.fraud.model.VelocityCounterScope;
import com.ecommerce.app.module.fraud.repository.VelocityCounterRepository;
import com.ecommerce.app.module.fraud.services.evaluator.OrderVelocitySignalEvaluator;
import com.ecommerce.app.module.order.model.OrderItem;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DefaultOrderVelocitySignalEvaluator extends AbstractFraudSignalEvaluator implements OrderVelocitySignalEvaluator {

    private static final BigDecimal HIGH_VALUE_ORDER_AMOUNT = new BigDecimal("10000.00");
    private static final BigDecimal EXCESSIVE_QUANTITY_THRESHOLD = new BigDecimal("10.00");

    private final SalesOrderRepository salesOrderRepository;
    private final VelocityCounterRepository velocityCounterRepository;

    public DefaultOrderVelocitySignalEvaluator(SalesOrderRepository salesOrderRepository,
            VelocityCounterRepository velocityCounterRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.velocityCounterRepository = velocityCounterRepository;
    }

    @Override
    public List<FraudSignalResult> evaluate(SalesOrder order, FraudContext context) {
        List<FraudSignalResult> signals = new ArrayList<>();
        long customerOrderCount = order == null || order.getCustomer() == null ? 0 : salesOrderRepository.countByCustomer(order.getCustomer());
        boolean firstOrder = customerOrderCount <= 1;
        boolean highValueFirstOrder = firstOrder && money(order == null ? null : order.getGrandTotal()).compareTo(HIGH_VALUE_ORDER_AMOUNT) >= 0;
        signals.add(signal("HIGH_VALUE_FIRST_ORDER", category(), highValueFirstOrder, 20, FraudSignalSeverity.HIGH,
                FraudReasonCode.NEW_ACCOUNT_HIGH_VALUE, String.valueOf(money(order == null ? null : order.getGrandTotal())),
                "order-velocity", "{\"threshold\":10000}"));

        BigDecimal maxQuantity = maxItemQuantity(order);
        signals.add(signal("EXCESSIVE_QUANTITY", category(), maxQuantity.compareTo(EXCESSIVE_QUANTITY_THRESHOLD) > 0, 15,
                FraudSignalSeverity.MEDIUM, null, maxQuantity.toPlainString(), "order-velocity", "{\"threshold\":10}"));

        Long customerId = customerId(order);
        String customerHash = customerId == null ? null : sha256(String.valueOf(customerId));
        long recentCustomerCounters = customerHash == null ? 0
                : velocityCounterRepository.countByCounterScopeAndCounterValueHashAndWindowEndAtAfter(
                        VelocityCounterScope.CUSTOMER, customerHash, LocalDateTime.now());
        signals.add(signal("ORDER_COUNT_15M", category(), recentCustomerCounters > 3, 25, FraudSignalSeverity.HIGH,
                FraudReasonCode.EXCESSIVE_ORDER_VELOCITY, String.valueOf(recentCustomerCounters), "velocity-counter", "{\"threshold\":3}"));

        return signals;
    }

    private BigDecimal maxItemQuantity(SalesOrder order) {
        if (order == null || order.getOrderItem() == null) {
            return BigDecimal.ZERO;
        }
        return order.getOrderItem().stream()
                .map(OrderItem::getQuantity)
                .map(this::money)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
}
