package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.VelocityCounterScope;
import com.ecommerce.app.module.fraud.services.OrderVelocityService;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import com.ecommerce.app.module.user.model.Users;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultOrderVelocitySignalEvaluatorTest {

    private SalesOrderRepository salesOrderRepository;
    private OrderVelocityService orderVelocityService;
    private DefaultOrderVelocitySignalEvaluator evaluator;

    @BeforeEach
    void setUp() {
        salesOrderRepository = mock(SalesOrderRepository.class);
        orderVelocityService = mock(OrderVelocityService.class);
        evaluator = new DefaultOrderVelocitySignalEvaluator(salesOrderRepository, orderVelocityService);
    }

    @Test
    void triggersWhenCurrentAttemptWouldBeFourthOrderInWindow() {
        SalesOrder order = orderForCustomer(42L);
        when(salesOrderRepository.countByCustomer(order.getCustomer())).thenReturn(2L);
        when(orderVelocityService.count(
                eq(VelocityCounterScope.CUSTOMER),
                eq("42"),
                any(Duration.class)
        )).thenReturn(3L);

        assertTrue(orderCountSignal(evaluator.evaluate(order, new FraudContext())).isTriggered());
    }

    @Test
    void doesNotTriggerBeforeFourthOrderInWindow() {
        SalesOrder order = orderForCustomer(42L);
        when(salesOrderRepository.countByCustomer(order.getCustomer())).thenReturn(2L);
        when(orderVelocityService.count(any(), any(), any())).thenReturn(2L);

        assertFalse(orderCountSignal(evaluator.evaluate(order, new FraudContext())).isTriggered());
    }

    private SalesOrder orderForCustomer(Long customerId) {
        Users customer = new Users();
        customer.setId(customerId);
        SalesOrder order = new SalesOrder();
        order.setCustomer(customer);
        order.setGrandTotal(BigDecimal.valueOf(100));
        return order;
    }

    private FraudSignalResult orderCountSignal(List<FraudSignalResult> signals) {
        return signals.stream()
                .filter(signal -> "ORDER_COUNT_15M".equals(signal.getSignalCode()))
                .findFirst()
                .orElseThrow();
    }
}
