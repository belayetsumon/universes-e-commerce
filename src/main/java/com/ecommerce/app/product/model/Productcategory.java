/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.product.model;

import com.ecommerce.app.model.*;
import com.ecommerce.app.model.enumvalue.Status;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.*;
import org.springframework.format.annotation.DateTimeFormat;

/**
 *
 * @author User
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Productcategory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name  is required.")
    private String name;

    @NotBlank(message = "slag  is required.")
    private String slug;

    private int orderno;

    @Lob
    private String description;

    private String imageName;

    private double discount;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate discountStartDate;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate discountEndDate;

    @NotNull(message = "Status is required.")
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(optional = true)
    @JoinColumn(name = "parent")
    private Productcategory parent;

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
    @OneToMany(mappedBy = "productcategory")
    public List<Ourproduct> ourproduct;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Productcategory> children;

    public Productcategory() {
    }

    public Productcategory(Long id, String name, String slug, int orderno, String description, String imageName, double discount, LocalDate discountStartDate, LocalDate discountEndDate, Status status, Productcategory parent, String createdBy, LocalDateTime created, String modifiedBy, LocalDateTime modified, List<Ourproduct> ourproduct, List<Productcategory> children) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.orderno = orderno;
        this.description = description;
        this.imageName = imageName;
        this.discount = discount;
        this.discountStartDate = discountStartDate;
        this.discountEndDate = discountEndDate;
        this.status = status;
        this.parent = parent;
        this.createdBy = createdBy;
        this.created = created;
        this.modifiedBy = modifiedBy;
        this.modified = modified;
        this.ourproduct = ourproduct;
        this.children = children;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public LocalDate getDiscountStartDate() {
        return discountStartDate;
    }

    public void setDiscountStartDate(LocalDate discountStartDate) {
        this.discountStartDate = discountStartDate;
    }

    public LocalDate getDiscountEndDate() {
        return discountEndDate;
    }

    public void setDiscountEndDate(LocalDate discountEndDate) {
        this.discountEndDate = discountEndDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Productcategory getParent() {
        return parent;
    }

    public void setParent(Productcategory parent) {
        this.parent = parent;
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

    public List<Ourproduct> getOurproduct() {
        return ourproduct;
    }

    public void setOurproduct(List<Ourproduct> ourproduct) {
        this.ourproduct = ourproduct;
    }

    public List<Productcategory> getChildren() {
        return children;
    }

    public void setChildren(List<Productcategory> children) {
        this.children = children;
    }
}
