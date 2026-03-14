/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.model.ShipmentItem;
import com.ecommerce.app.module.shipping.model.ShipmentStatus;
import com.ecommerce.app.module.shipping.repository.ShipmentRepository;
import com.ecommerce.app.order.model.OrderItem;
import com.ecommerce.app.order.repository.OrderItemRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class ShipmentService {

    private final ShipmentRepository repo;
    @Autowired
    OrderItemRepository orderItemRepository;

    public ShipmentService(ShipmentRepository repo) {
        this.repo = repo;
    }

    public List<Shipment> getAll() {
        return repo.findAll();
    }

    public List<Shipment> getByVendor(Long vendorId) {
        return repo.findByVendorId(vendorId);
    }

    public Shipment getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Shipment save(Shipment shipment) {
        return repo.save(shipment);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public Shipment saves(Shipment shipment) {

        Shipment savedShipment = repo.save(shipment);

        // Auto-link order items
//        List<OrderItem> orderItems = orderItemRepository.findBySalesOrder_Id(shipment.getSalesOrderId());
//        List<ShipmentItem> shipmentItems = orderItems.stream().map(oi -> {
//            ShipmentItem si = new ShipmentItem();
//            si.setShipment(savedShipment);
//            si.setOrderItemId(oi.getId());
//            si.setQty(oi.getQuantity());
//            return si;
//        }).toList();
//        savedShipment.setItems(shipmentItems);
        // COD init
//        if (shipment.isCod()) {
//            BigDecimal total = shipmentItems.stream()
//                    .map(si -> si.getQty().multiply(getProductPrice(si.getOrderItemId())))
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//            savedShipment.setTotalAmount(total);
//            savedShipment.setCollectedAmount(BigDecimal.ZERO);
//            savedShipment.setPendingAmount(total);
//        }
        return repo.save(savedShipment);
    }

    // Update collection
    public void collectPayment(Long shipmentId, BigDecimal amount) {
        Shipment s = repo.findById(shipmentId).orElseThrow();
        s.setCodCollected(s.getCodCollected().add(amount));
        s.setCodPending(s.getTotalOrderAmount().subtract(s.getCodCollected()));
        if (s.getCodPending().compareTo(BigDecimal.ZERO) <= 0) {
            s.setStatus(ShipmentStatus.DELIVERED);
        }
        repo.save(s);
    }

    private BigDecimal getProductPrice(Long orderItemId) {
        // fetch product price from orderItem or product entity
        return new BigDecimal("100"); // demo
    }
}
