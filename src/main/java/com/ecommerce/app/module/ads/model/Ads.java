/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ads.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author libertyerp_local
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ads")
public class Ads {

    // Primary Key (auto-generated)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Extra UUID field (business/reference id)
    @Column(nullable = false, unique = true, length = 36)
    private String uuid = UUID.randomUUID().toString();

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    // @NotBlank(message = "Image URL is required")
    @Size(max = 255, message = "Image URL max length is 255 characters")
    //@URL(message = "Image URL must be valid")
    private String imageUrl;

    @NotNull(message = "Width is required")
    @Min(value = 100, message = "Width must be at least 100px")
    @Max(value = 2100, message = "Width cannot exceed 2100px")
    private Integer width;

    @NotNull(message = "Height is required")
    @Min(value = 100, message = "Height must be at least 100px")
    @Max(value = 1000, message = "Height cannot exceed 1000px")
    private Integer height;

    // Optional fields for ad target
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Target is required")
    private TargetType targetType; // CATEGORY, PRODUCT, EXTERNAL

    private String categoryId;  // if targetType == CATEGORY

    private String productId;   // if targetType == PRODUCT

    private String vendorId;    // if targetType == VENDOR

    @Size(max = 255)
    @URL
    private String externalUrl; // if targetType == EXTERNAL

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Placement is required")
    private Placement placement;

    private boolean active = true;

    @Min(value = 0, message = "Display order cannot be negative")
    @Max(value = 1000, message = "Display order max is 1000")
    private int displayOrder = 0;

    /// Optimistic Locking ///
    @Version
    private Long version;

    /// Audit fields ///
    @CreatedBy
    @Column(nullable = false, updatable = false, length = 50)
    private String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @LastModifiedBy
    @Column(nullable = false, length = 50)
    private String modifiedBy;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime modified;

    public Ads() {
    }

    public Ads(Long id, String title, String imageUrl, Integer width, Integer height, TargetType targetType, String categoryId, String productId, String vendorId, String externalUrl, Placement placement, Long version, String createdBy, LocalDateTime created, String modifiedBy, LocalDateTime modified) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.width = width;
        this.height = height;
        this.targetType = targetType;
        this.categoryId = categoryId;
        this.productId = productId;
        this.vendorId = vendorId;
        this.externalUrl = externalUrl;
        this.placement = placement;
        this.version = version;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public Placement getPlacement() {
        return placement;
    }

    public void setPlacement(Placement placement) {
        this.placement = placement;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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
