/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.cart.model;

import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.Unitofmeasurement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author User
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Scope("session")
public class CartItem implements Serializable {

    @JsonIgnore
    public Product product;
    public Long vendorId;
    private String vendorUuid;

    private Long productId;
    private String productUuid;

    private String catalogVariantUuid;
    private String variantSummary;
    private Boolean preorder = Boolean.FALSE;
    private LocalDate preorderAvailableFrom;

    public BigDecimal quantity;
    @JsonIgnore
    private Unitofmeasurement uom;

    public BigDecimal salesPrice;

    public BigDecimal discountRate;

    public BigDecimal discountAmount;

    public BigDecimal marketPlaceCommissionRate;

    public BigDecimal marketPlaceCommissionAmount;
    public BigDecimal vendorAmount;
    public BigDecimal vatRate;

    public BigDecimal weight;

    public BigDecimal vatAmount;

    public BigDecimal itemTotal;

    public CartItem() {
    }

    public CartItem(Product product, Long vendorId, String vendorUuid, Long productId, String productUuid, String catalogVariantUuid, String variantSummary, Boolean preorder, LocalDate preorderAvailableFrom, BigDecimal quantity, Unitofmeasurement uom, BigDecimal salesPrice, BigDecimal discountRate, BigDecimal discountAmount, BigDecimal marketPlaceCommissionRate, BigDecimal marketPlaceCommissionAmount, BigDecimal vendorAmount, BigDecimal vatRate, BigDecimal weight, BigDecimal vatAmount, BigDecimal itemTotal) {
        this.product = product;
        this.vendorId = vendorId;
        this.vendorUuid = vendorUuid;
        this.productId = productId;
        this.productUuid = productUuid;
        this.catalogVariantUuid = catalogVariantUuid;
        this.variantSummary = variantSummary;
        this.preorder = preorder;
        this.preorderAvailableFrom = preorderAvailableFrom;
        this.quantity = quantity;
        this.uom = uom;
        this.salesPrice = salesPrice;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.marketPlaceCommissionRate = marketPlaceCommissionRate;
        this.marketPlaceCommissionAmount = marketPlaceCommissionAmount;
        this.vendorAmount = vendorAmount;
        this.vatRate = vatRate;
        this.weight = weight;
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

    public String getVendorUuid() {
        if (vendorUuid != null && !vendorUuid.isBlank()) {
            return vendorUuid;
        }
        if (product != null && product.getVendorprofile() != null) {
            return product.getVendorprofile().getUuid();
        }
        return null;
    }

    public void setVendorUuid(String vendorUuid) {
        this.vendorUuid = vendorUuid;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductUuid() {
        if (productUuid != null && !productUuid.isBlank()) {
            return productUuid;
        }
        if (product != null) {
            return product.getUuid();
        }
        return null;
    }

    public void setProductUuid(String productUuid) {
        this.productUuid = productUuid;
    }

    public String getCatalogVariantUuid() {
        return catalogVariantUuid;
    }

    public void setCatalogVariantUuid(String catalogVariantUuid) {
        this.catalogVariantUuid = catalogVariantUuid;
    }

    public String getVariantSummary() {
        return variantSummary;
    }

    public void setVariantSummary(String variantSummary) {
        this.variantSummary = variantSummary;
    }

    public Boolean getPreorder() {
        return preorder;
    }

    public void setPreorder(Boolean preorder) {
        this.preorder = preorder;
    }

    public LocalDate getPreorderAvailableFrom() {
        return preorderAvailableFrom;
    }

    public void setPreorderAvailableFrom(LocalDate preorderAvailableFrom) {
        this.preorderAvailableFrom = preorderAvailableFrom;
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

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
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
