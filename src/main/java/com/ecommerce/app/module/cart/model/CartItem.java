/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.cart.model;

import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.Unitofmeasurement;
import java.io.Serializable;
import java.math.BigDecimal;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author User
 */
@Scope("session")
public class CartItem implements Serializable {

    public Product product;

    public Long vendorId;

    private Long productId;

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

    public CartItem() {
    }

    public CartItem(Product product, Long vendorId, Long productId, BigDecimal quantity, Unitofmeasurement uom, BigDecimal salesPrice, BigDecimal discountRate, BigDecimal discountAmount, BigDecimal marketPlaceCommissionRate, BigDecimal marketPlaceCommissionAmount, BigDecimal vendorAmount, BigDecimal vatRate, BigDecimal vatAmount, BigDecimal itemTotal) {
        this.product = product;
        this.vendorId = vendorId;
        this.productId = productId;
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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

}
