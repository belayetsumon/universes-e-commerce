/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.model;

import com.ecommerce.app.model.enumvalue.Status;
import com.ecommerce.app.product.model.Productcategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.context.annotation.Scope;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author User
 */
@Scope("session")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Ourproduct implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Sku is required.")
    private int sku;

    @NotNull(message = "Product category cannot be blank.")
    @ManyToOne(optional = true ,fetch = FetchType.LAZY)
    private Productcategory productcategory;

    @NotBlank(message = "Title  is required.")
    private String title;

    @NotBlank(message = "Slug  is required.")
    private String slug;

    private int orderno;

    @Lob
    @NotBlank(message = "Description  is required.")
    private String description;

    @NotNull(message = "Price is required.")
    private double price;

    private double discount;

    @NotBlank(message = "UoM  is required.")
    private String uom;

    private String imageName;

    @NotNull(message = "Status is required.")
    @Enumerated(EnumType.STRING)
    private Status status;

    /// Audit /// 
    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @LastModifiedBy
    @Column(insertable = false)
    private String modifiedBy;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime modified;

    /// End Audit //// 
    public Ourproduct() {
    }

    public Ourproduct(Long id, int sku, Productcategory productcategory, String title, String slug, int orderno, String description, double price, double discount, String uom, String imageName, Status status, String createdBy, LocalDateTime created, String modifiedBy, LocalDateTime modified) {
        this.id = id;
        this.sku = sku;
        this.productcategory = productcategory;
        this.title = title;
        this.slug = slug;
        this.orderno = orderno;
        this.description = description;
        this.price = price;
        this.discount = discount;
        this.uom = uom;
        this.imageName = imageName;
        this.status = status;
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

    public int getSku() {
        return sku;
    }

    public void setSku(int sku) {
        this.sku = sku;
    }

    public Productcategory getProductcategory() {
        return productcategory;
    }

    public void setProductcategory(Productcategory productcategory) {
        this.productcategory = productcategory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public int getOrderno() {
        return orderno;
    }

    public void setOrderno(int orderno) {
        this.orderno = orderno;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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
