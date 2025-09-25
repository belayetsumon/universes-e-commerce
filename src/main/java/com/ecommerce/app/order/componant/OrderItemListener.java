/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Component.java to edit this template
 */
package com.ecommerce.app.order.componant;

import com.ecommerce.app.order.model.OrderItem;
import com.ecommerce.app.order.model.SalesOrder;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import com.ecommerce.app.order.services.OrderItemService;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author libertyerp_local
 */
@Component
public class OrderItemListener {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @PostPersist
    @PostUpdate
    @PostRemove
    public void recalcTotals(OrderItem item) {
        SalesOrder order = item.getSalesOrder();
        if (order == null || order.getId() == null) {
            return;
        }

        BigDecimal totalVat = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;
        BigDecimal totalVendor = BigDecimal.ZERO;
        BigDecimal itemTotal = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (OrderItem oi : order.getOrderItem()) {
            totalVat = totalVat.add(oi.getVatAmount() != null ? oi.getVatAmount() : BigDecimal.ZERO);
            totalDiscount = totalDiscount.add(oi.getDiscountAmount() != null ? oi.getDiscountAmount() : BigDecimal.ZERO);
            totalCommission = totalCommission.add(oi.getMarketPlaceCommissionAmount() != null ? oi.getMarketPlaceCommissionAmount() : BigDecimal.ZERO);
            totalVendor = totalVendor.add(oi.getVendorAmount() != null ? oi.getVendorAmount() : BigDecimal.ZERO);
            itemTotal = itemTotal.add(oi.getItemTotal() != null ? oi.getItemTotal() : BigDecimal.ZERO);
        }

        grandTotal = itemTotal.add(totalVat).subtract(totalDiscount);

        order.setTotalVatAmount(totalVat);
        order.setTotalDiscountAmount(totalDiscount);
        order.setTotalMarketPlaceCommissionAmount(totalCommission);
        order.setTotalVendorAmount(totalVendor);
        order.setItemtotal(itemTotal);
        order.setGrandTotal(grandTotal);

        salesOrderRepository.save(order);
    }
}
