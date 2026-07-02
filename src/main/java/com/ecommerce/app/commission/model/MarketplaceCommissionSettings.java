package com.ecommerce.app.commission.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Marketplace commission rule. Resolution priority is Product, Vendor,
 * Category, then Default.
 */
@Entity(name = "MarketplaceCommissionSettings")
@Table(
        name = "commission_settings",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_commission_settings",
                    columnNames = {"commission_type", "category_id", "vendor_id", "product_id"})
        },
        indexes = {
            @Index(name = "idx_commission_type_status", columnList = "commission_type,status"),
            @Index(name = "idx_category_id_status", columnList = "category_id,status"),
            @Index(name = "idx_vendor_id_status", columnList = "vendor_id,status"),
            @Index(name = "idx_product_id_status", columnList = "product_id,status"),
            @Index(name = "idx_created_at", columnList = "created_at")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class MarketplaceCommissionSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, updatable = false, length = 36)
    private String uuid;

    @NotNull(message = "Commission type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "commission_type", nullable = false, length = 30)
    private CommissionType commissionType = CommissionType.DEFAULT;

    @NotNull(message = "Commission rate is required.")
    @DecimalMin(value = "0.00", message = "Commission rate cannot be negative.")
    @DecimalMax(value = "100.00", message = "Commission rate cannot exceed 100%.")
    @Digits(integer = 3, fraction = 2)
    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "product_id")
    private Long productId;

    @NotNull(message = "Status is required.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CommissionStatus status = CommissionStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Version
    private Long version;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public MarketplaceCommissionSettings() {
    }

    @PrePersist
    public void prePersist() {
        if (uuid == null || uuid.isBlank()) {
            uuid = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
        if (createdBy == null || createdBy.isBlank()) {
            createdBy = "system";
        }
        if (updatedBy == null || updatedBy.isBlank()) {
            updatedBy = createdBy;
        }
        normalizeScope();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        if (updatedBy == null || updatedBy.isBlank()) {
            updatedBy = "system";
        }
        normalizeScope();
    }

    public boolean isCurrentlyApplicable() {
        return status == CommissionStatus.ACTIVE;
    }

    public String getTargetSummary() {
        if (commissionType == CommissionType.PRODUCT && productId != null) {
            return "Product #" + productId;
        }
        if (commissionType == CommissionType.VENDOR && vendorId != null) {
            return "Vendor #" + vendorId;
        }
        if (commissionType == CommissionType.CATEGORY && categoryId != null) {
            return "Category #" + categoryId;
        }
        return "Global default";
    }

    private void normalizeScope() {
        if (commissionType == null) {
            commissionType = CommissionType.DEFAULT;
        }
        switch (commissionType) {
            case DEFAULT -> {
                categoryId = null;
                vendorId = null;
                productId = null;
            }
            case CATEGORY -> {
                vendorId = null;
                productId = null;
            }
            case VENDOR -> {
                categoryId = null;
                productId = null;
            }
            case PRODUCT -> {
                categoryId = null;
                vendorId = null;
            }
        }
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

    public CommissionType getCommissionType() {
        return commissionType;
    }

    public void setCommissionType(CommissionType commissionType) {
        this.commissionType = commissionType;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

    public CommissionStatus getStatus() {
        return status;
    }

    public void setStatus(CommissionStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreated() {
        return createdAt;
    }

    public void setCreated(LocalDateTime created) {
        this.createdAt = created;
    }

    public LocalDateTime getModified() {
        return updatedAt;
    }

    public void setModified(LocalDateTime modified) {
        this.updatedAt = modified;
    }

    public String getModifiedBy() {
        return updatedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.updatedBy = modifiedBy;
    }
}
