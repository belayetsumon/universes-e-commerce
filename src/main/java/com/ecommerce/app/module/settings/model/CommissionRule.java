/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.settings.model;

import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.Productcategory;
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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
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
@Table(
        name = "global_settings_commission_rule",
        indexes = {
            @Index(name = "idx_commission_rule_uuid", columnList = "uuid"),
            @Index(name = "idx_commission_rule_active", columnList = "active"),
            @Index(name = "idx_commission_rule_category", columnList = "product_category_id"),
            @Index(name = "idx_commission_rule_seller", columnList = "vendorprofile_id"),
            @Index(name = "idx_commission_rule_product", columnList = "product_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class CommissionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String uuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommissionRuleType ruleType;

    // ===== TARGETS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_category_id")
    private Productcategory productCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendorprofile_id")
    private Vendorprofile vendorprofile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // ===== COMMISSION =====
    @NotNull(message = "Commission percentage is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Commission must be greater than 0%.")
    @DecimalMax(value = "100.0", message = "Commission cannot exceed 100%.")
    @Digits(integer = 3, fraction = 2)
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    @DecimalMin(value = "0.0", message = "Flat fee must be positive.")
    @Column(precision = 10, scale = 2)
    private BigDecimal flatFee;

    @Column(nullable = false)
    private Boolean active = true;

    // Rule priority inside same type
    @Column(nullable = false)
    private Integer priority = 0;

    // Time-based rules
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    /// ===== AUDIT =====
    @Version
    private Long version;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @LastModifiedBy
    private String modifiedBy;

    @LastModifiedDate
    private LocalDateTime modified;

    @PrePersist
    public void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
    }

    public CommissionRule(Long id, String uuid, CommissionRuleType ruleType, Productcategory productCategory, Vendorprofile vendorprofile, Product product, BigDecimal percentage, BigDecimal flatFee, LocalDateTime startDate, LocalDateTime endDate, Long version, String createdBy, LocalDateTime created, String modifiedBy, LocalDateTime modified) {
        this.id = id;
        this.uuid = uuid;
        this.ruleType = ruleType;
        this.productCategory = productCategory;
        this.vendorprofile = vendorprofile;
        this.product = product;
        this.percentage = percentage;
        this.flatFee = flatFee;
        this.startDate = startDate;
        this.endDate = endDate;
        this.version = version;
        this.createdBy = createdBy;
        this.created = created;
        this.modifiedBy = modifiedBy;
        this.modified = modified;
    }

    public CommissionRule() {
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

    public CommissionRuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(CommissionRuleType ruleType) {
        this.ruleType = ruleType;
    }

    public Productcategory getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(Productcategory productCategory) {
        this.productCategory = productCategory;
    }

    public Vendorprofile getVendorprofile() {
        return vendorprofile;
    }

    public void setVendorprofile(Vendorprofile vendorprofile) {
        this.vendorprofile = vendorprofile;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public BigDecimal getFlatFee() {
        return flatFee;
    }

    public void setFlatFee(BigDecimal flatFee) {
        this.flatFee = flatFee;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
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
