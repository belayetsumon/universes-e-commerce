/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author libertyerp_local
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
public class ProductVariants {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ProductSize size;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ProductColor color;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal stock_quantity;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal slaesPrice;

    public ProductVariants(Long id, Product product, ProductSize size, ProductColor color, BigDecimal stock_quantity, BigDecimal slaesPrice) {
        this.id = id;
        this.product = product;
        this.size = size;
        this.color = color;
        this.stock_quantity = stock_quantity;
        this.slaesPrice = slaesPrice;
    }

    public ProductVariants() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public ProductSize getSize() {
        return size;
    }

    public void setSize(ProductSize size) {
        this.size = size;
    }

    public ProductColor getColor() {
        return color;
    }

    public void setColor(ProductColor color) {
        this.color = color;
    }

    public BigDecimal getStock_quantity() {
        return stock_quantity;
    }

    public void setStock_quantity(BigDecimal stock_quantity) {
        this.stock_quantity = stock_quantity;
    }

    public BigDecimal getSlaesPrice() {
        return slaesPrice;
    }

    public void setSlaesPrice(BigDecimal slaesPrice) {
        this.slaesPrice = slaesPrice;
    }

}
