/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.order.model;

import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.Unitofmeasurement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@EntityListeners({AuditingEntityListener.class})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull(message = "Sales order cannot be blank.")
    @ManyToOne(optional = true)
    public SalesOrder salesOrder;

    @NotNull(message = "product cannot be blank.")
    @ManyToOne(optional = true)
    public Product product;

    public Long vendorId;

    public Long productid;

    public BigDecimal quantity;

    private Unitofmeasurement uom;

    public BigDecimal salesPrice;

    public BigDecimal discountRate;

    public BigDecimal discountAmount;

    public BigDecimal marketPlaceCommissionRate;

    public BigDecimal marketPlaceCommissionAmount;

    public BigDecimal vendorAmount;

    public BigDecimal vatRate;

    public BigDecimal vatAmount;

    public BigDecimal itemTotal;

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
    public OrderItem() {
    }

    public OrderItem(Long id, SalesOrder salesOrder, Product product, Long vendorId, Long productid, BigDecimal quantity, Unitofmeasurement uom, BigDecimal salesPrice, BigDecimal discountRate, BigDecimal discountAmount, BigDecimal marketPlaceCommissionRate, BigDecimal marketPlaceCommissionAmount, BigDecimal vendorAmount, BigDecimal vatRate, BigDecimal vatAmount, BigDecimal itemTotal, String createdBy, LocalDateTime created, String modifiedBy, LocalDateTime modified) {
        this.id = id;
        this.salesOrder = salesOrder;
        this.product = product;
        this.vendorId = vendorId;
        this.productid = productid;
        this.quantity = quantity;
        this.uom = uom;
        this.salesPrice = salesPrice;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.marketPlaceCommissionRate = marketPlaceCommissionRate;
        this.marketPlaceCommissionAmount = marketPlaceCommissionAmount;
        this.vendorAmount = vendorAmount;
        this.vatRate = vatRate;
        this.vatAmount = vatAmount;
        this.itemTotal = itemTotal;
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

    public SalesOrder getSalesOrder() {
        return salesOrder;
    }

    public void setSalesOrder(SalesOrder salesOrder) {
        this.salesOrder = salesOrder;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public Long getProductid() {
        return productid;
    }

    public void setProductid(Long productid) {
        this.productid = productid;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Unitofmeasurement getUom() {
        return uom;
    }

    public void setUom(Unitofmeasurement uom) {
        this.uom = uom;
    }

    public BigDecimal getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(BigDecimal salesPrice) {
        this.salesPrice = salesPrice;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getMarketPlaceCommissionRate() {
        return marketPlaceCommissionRate;
    }

    public void setMarketPlaceCommissionRate(BigDecimal marketPlaceCommissionRate) {
        this.marketPlaceCommissionRate = marketPlaceCommissionRate;
    }

    public BigDecimal getMarketPlaceCommissionAmount() {
        return marketPlaceCommissionAmount;
    }

    public void setMarketPlaceCommissionAmount(BigDecimal marketPlaceCommissionAmount) {
        this.marketPlaceCommissionAmount = marketPlaceCommissionAmount;
    }

    public BigDecimal getVendorAmount() {
        return vendorAmount;
    }

    public void setVendorAmount(BigDecimal vendorAmount) {
        this.vendorAmount = vendorAmount;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public void setVatRate(BigDecimal vatRate) {
        this.vatRate = vatRate;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(BigDecimal itemTotal) {
        this.itemTotal = itemTotal;
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
