/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.order.model;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.order.controller.PaymentReaceiveBy;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author User
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String uuid = UUID.randomUUID().toString();

    @Column(length = 30, unique = true, nullable = false)
    private String orderCode;

    @NotNull(message = "User cannot be blank.")
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    public Users customer;

    public Long vendorId;

    public BigDecimal totalVatAmount = BigDecimal.ZERO;

    public BigDecimal totalDiscountAmount = BigDecimal.ZERO;
    // total compay and vendor discount

    public BigDecimal totalMarketPlaceCommissionAmount = BigDecimal.ZERO;
    // market place commission

    public BigDecimal totalVendorAmount = BigDecimal.ZERO;

    public BigDecimal itemtotal = BigDecimal.ZERO;

    private BigDecimal packingCharge = BigDecimal.ZERO;

    private BigDecimal deliveryCharge = BigDecimal.ZERO;

    public BigDecimal grandTotal;

    @NotNull(message = "Status is required.")
    @Enumerated(EnumType.STRING)
    public OrderStatus status;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItem = new ArrayList<>();

    // Relationship with Shipping and Billing addresses
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private ShippingAddress shippingAddress;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "billing_address_id")
    private BillingAddress billingAddress;

    @Enumerated(EnumType.STRING)
    PaymentReaceiveBy paymentReaceive;

    // Reference to payment
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id")
    private List<Payment> payment;

    /// Audit ///
    @CreatedBy
    @Column(nullable = false, updatable = false)
    public String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    public LocalDateTime created;

    @LastModifiedBy
    @Column(insertable = false)
    public String modifiedBy;

    @LastModifiedDate
    @Column(insertable = false)
    public LocalDateTime modified;

    /// End Audit ////
    public SalesOrder() {
    }

    public SalesOrder(Long id, String orderCode, Users customer, Long vendorId, BigDecimal grandTotal, OrderStatus status, ShippingAddress shippingAddress, BillingAddress billingAddress, PaymentReaceiveBy paymentReaceive, List<Payment> payment, String createdBy, LocalDateTime created, String modifiedBy, LocalDateTime modified) {
        this.id = id;
        this.orderCode = orderCode;
        this.customer = customer;
        this.vendorId = vendorId;
        this.grandTotal = grandTotal;
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
        this.paymentReaceive = paymentReaceive;
        this.payment = payment;
        this.createdBy = createdBy;
        this.created = created;
        this.modifiedBy = modifiedBy;
        this.modified = modified;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public Users getCustomer() {
        return customer;
    }

    public void setCustomer(Users customer) {
        this.customer = customer;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public BigDecimal getTotalVatAmount() {
        return totalVatAmount;
    }

    public void setTotalVatAmount(BigDecimal totalVatAmount) {
        this.totalVatAmount = totalVatAmount;
    }

    public BigDecimal getTotalDiscountAmount() {
        return totalDiscountAmount;
    }

    public void setTotalDiscountAmount(BigDecimal totalDiscountAmount) {
        this.totalDiscountAmount = totalDiscountAmount;
    }

    public BigDecimal getTotalMarketPlaceCommissionAmount() {
        return totalMarketPlaceCommissionAmount;
    }

    public void setTotalMarketPlaceCommissionAmount(BigDecimal totalMarketPlaceCommissionAmount) {
        this.totalMarketPlaceCommissionAmount = totalMarketPlaceCommissionAmount;
    }

    public BigDecimal getTotalVendorAmount() {
        return totalVendorAmount;
    }

    public void setTotalVendorAmount(BigDecimal totalVendorAmount) {
        this.totalVendorAmount = totalVendorAmount;
    }

    public BigDecimal getItemtotal() {
        return itemtotal;
    }

    public void setItemtotal(BigDecimal itemtotal) {
        this.itemtotal = itemtotal;
    }

    public BigDecimal getPackingCharge() {
        return packingCharge;
    }

    public void setPackingCharge(BigDecimal packingCharge) {
        this.packingCharge = packingCharge;
    }

    public BigDecimal getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(BigDecimal deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderItem> getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(List<OrderItem> orderItem) {
        this.orderItem = orderItem;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddress shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public BillingAddress getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(BillingAddress billingAddress) {
        this.billingAddress = billingAddress;
    }

    public PaymentReaceiveBy getPaymentReaceive() {
        return paymentReaceive;
    }

    public void setPaymentReaceive(PaymentReaceiveBy paymentReaceive) {
        this.paymentReaceive = paymentReaceive;
    }

    public List<Payment> getPayment() {
        return payment;
    }

    public void setPayment(List<Payment> payment) {
        this.payment = payment;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }

}
