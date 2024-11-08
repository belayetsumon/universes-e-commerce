/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.product.model;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.vendor.model.Vendorprofile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

/**
 *
 * @author User
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Sku is required.")
    private int sku;

    @NotNull(message = " User cannot be blank.")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Users userId;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(nullable = true)
    private Vendorprofile vendorprofile;

//    @NotNull(message = "Please select minimum one sub category")
//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(name = "exam_productsubcategory",
//            joinColumns = @JoinColumn(name = "exam_id", referencedColumnName = "id"),
//            inverseJoinColumns = @JoinColumn(name = "productsubcategory_id", referencedColumnName = "id"))
//
//    private Set<Productsubcategory> productsubcategory;
    @NotNull(message = "Product category cannot be blank.")
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Productcategory productcategory;

    @NotBlank(message = "Title  is required.")
    private String title;

    private String slug;

    private int orderno;// position of product serial 

    @Lob
    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Lob
    private String video;

    private double price;

    private double purchasePrice;

    private double salesPrice;

    private double companyProfit;

    @NotNull(message = "Product Type")
    private ProductTypeEnum productType;

    private double companyDiscount;

    private double vendordiscount;

    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime discountStartDate;

    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime discountEndDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Unitofmeasurement uom;

    private String imageName;

    private Boolean newProduct;

    private Boolean featuredProduct;

    private Boolean manageStock;

    private Boolean emiavailable;

    @NotNull(message = "Status is required.")
    @Enumerated(EnumType.STRING)
    private ProductStatusEnum status;

    // meta description
    @Lob()
    private String metaTitle;

    @Lob
    private String metaDescription;

    @Lob
    private String metaKeywords;

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

    public Product() {
    }

    public Product(Long id, int sku, Users userId, Vendorprofile vendorprofile, Productcategory productcategory, String title, String slug, int orderno, String shortDescription, String description, String video, double price, double purchasePrice, double salesPrice, double companyProfit, ProductTypeEnum productType, double companyDiscount, double vendordiscount, LocalDateTime discountStartDate, LocalDateTime discountEndDate, Unitofmeasurement uom, String imageName, Boolean newProduct, Boolean featuredProduct, Boolean manageStock, Boolean emiavailable, ProductStatusEnum status, String metaTitle, String metaDescription, String metaKeywords, String createdBy, LocalDateTime created, String modifiedBy, LocalDateTime modified) {
        this.id = id;
        this.sku = sku;
        this.userId = userId;
        this.vendorprofile = vendorprofile;
        this.productcategory = productcategory;
        this.title = title;
        this.slug = slug;
        this.orderno = orderno;
        this.shortDescription = shortDescription;
        this.description = description;
        this.video = video;
        this.price = price;
        this.purchasePrice = purchasePrice;
        this.salesPrice = salesPrice;
        this.companyProfit = companyProfit;
        this.productType = productType;
        this.companyDiscount = companyDiscount;
        this.vendordiscount = vendordiscount;
        this.discountStartDate = discountStartDate;
        this.discountEndDate = discountEndDate;
        this.uom = uom;
        this.imageName = imageName;
        this.newProduct = newProduct;
        this.featuredProduct = featuredProduct;
        this.manageStock = manageStock;
        this.emiavailable = emiavailable;
        this.status = status;
        this.metaTitle = metaTitle;
        this.metaDescription = metaDescription;
        this.metaKeywords = metaKeywords;
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

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    public Vendorprofile getVendorprofile() {
        return vendorprofile;
    }

    public void setVendorprofile(Vendorprofile vendorprofile) {
        this.vendorprofile = vendorprofile;
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

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public double getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(double salesPrice) {
        this.salesPrice = salesPrice;
    }

    public double getCompanyProfit() {
        return companyProfit;
    }

    public void setCompanyProfit(double companyProfit) {
        this.companyProfit = companyProfit;
    }

    public ProductTypeEnum getProductType() {
        return productType;
    }

    public void setProductType(ProductTypeEnum productType) {
        this.productType = productType;
    }

    public double getCompanyDiscount() {
        return companyDiscount;
    }

    public void setCompanyDiscount(double companyDiscount) {
        this.companyDiscount = companyDiscount;
    }

    public double getVendordiscount() {
        return vendordiscount;
    }

    public void setVendordiscount(double vendordiscount) {
        this.vendordiscount = vendordiscount;
    }

    public LocalDateTime getDiscountStartDate() {
        return discountStartDate;
    }

    public void setDiscountStartDate(LocalDateTime discountStartDate) {
        this.discountStartDate = discountStartDate;
    }

    public LocalDateTime getDiscountEndDate() {
        return discountEndDate;
    }

    public void setDiscountEndDate(LocalDateTime discountEndDate) {
        this.discountEndDate = discountEndDate;
    }

    public Unitofmeasurement getUom() {
        return uom;
    }

    public void setUom(Unitofmeasurement uom) {
        this.uom = uom;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Boolean getNewProduct() {
        return newProduct;
    }

    public void setNewProduct(Boolean newProduct) {
        this.newProduct = newProduct;
    }

    public Boolean getFeaturedProduct() {
        return featuredProduct;
    }

    public void setFeaturedProduct(Boolean featuredProduct) {
        this.featuredProduct = featuredProduct;
    }

    public Boolean getManageStock() {
        return manageStock;
    }

    public void setManageStock(Boolean manageStock) {
        this.manageStock = manageStock;
    }

    public Boolean getEmiavailable() {
        return emiavailable;
    }

    public void setEmiavailable(Boolean emiavailable) {
        this.emiavailable = emiavailable;
    }

    public ProductStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ProductStatusEnum status) {
        this.status = status;
    }

    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getMetaKeywords() {
        return metaKeywords;
    }

    public void setMetaKeywords(String metaKeywords) {
        this.metaKeywords = metaKeywords;
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
