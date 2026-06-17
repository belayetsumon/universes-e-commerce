/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.product.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
        name = "product_catalog_attribute",
        indexes = {
            @Index(name = "idx_catalog_attribute_uuid", columnList = "uuid"),
            @Index(name = "idx_catalog_attribute_active", columnList = "active")
        },
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_catalog_attribute_code", columnNames = {"code"})
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Attribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false, length = 36)
    private String uuid = UUID.randomUUID().toString();

    @NotBlank(message = "Attribute name is required.")
    @Size(max = 120, message = "Attribute name must be within 120 characters.")
    @Column(nullable = false, length = 120)
    private String name;

    @Size(max = 120, message = "Attribute code must be within 120 characters.")
    @Column(nullable = false, length = 120)
    private String code;

    @Lob
    private String description;

    @NotNull(message = "Attribute input type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "input_type", nullable = false, length = 30)
    private AttributeInputType inputType = AttributeInputType.TEXT;

    @NotNull(message = "Attribute value type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 30)
    private AttributeValueType valueType = AttributeValueType.TEXT;

    @Column(name = "allow_multiple_values", nullable = false)
    private boolean allowMultipleValues = false;

    @Column(name = "variant_capable", nullable = false)
    private boolean variantCapable = false;

    @Column(name = "filterable", nullable = false)
    private boolean filterable = false;

    @Column(name = "searchable", nullable = false)
    private boolean searchable = false;

    @Column(name = "comparable", nullable = false)
    private boolean comparable = false;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Version
    @Column(name = "version")
    private Long version;

    @CreatedDate
    @Column(name = "created_on", nullable = false, updatable = false)
    private Instant createdOn;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_on")
    private Instant updatedOn;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @OneToMany(
            mappedBy = "attribute",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @org.hibernate.annotations.BatchSize(size = 20)
    private List<AttributeOption> options = new ArrayList<>();

    // ===================== Lifecycle =====================
    @PrePersist
    @PreUpdate
    private void normalizeCode() {
        if (code == null || code.isBlank()) {
            code = name;
        }
        if (code != null) {
            code = code.trim()
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9]+", "_")
                    .replaceAll("^_+|_+$", "");

            if (code.isEmpty()) {
                throw new IllegalArgumentException("Attribute code must include at least one letter or number.");
            }
        }
    }

    // ===================== Helper Methods =====================
    public void addOption(AttributeOption option) {
        options.add(option);
        option.setAttribute(this);
    }

    public void removeOption(AttributeOption option) {
        options.remove(option);
        option.setAttribute(null);
    }

    public Attribute() {
    }

    public Attribute(Long id, String name, String code, String description, Long version, Instant createdOn, String createdBy, Instant updatedOn, String updatedBy) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.version = version;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
        this.updatedOn = updatedOn;
        this.updatedBy = updatedBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AttributeInputType getInputType() {
        return inputType;
    }

    public void setInputType(AttributeInputType inputType) {
        this.inputType = inputType;
    }

    public AttributeValueType getValueType() {
        return valueType;
    }

    public void setValueType(AttributeValueType valueType) {
        this.valueType = valueType;
    }

    public boolean isAllowMultipleValues() {
        return allowMultipleValues;
    }

    public void setAllowMultipleValues(boolean allowMultipleValues) {
        this.allowMultipleValues = allowMultipleValues;
    }

    public boolean isVariantCapable() {
        return variantCapable;
    }

    public void setVariantCapable(boolean variantCapable) {
        this.variantCapable = variantCapable;
    }

    public boolean isFilterable() {
        return filterable;
    }

    public void setFilterable(boolean filterable) {
        this.filterable = filterable;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public boolean isComparable() {
        return comparable;
    }

    public void setComparable(boolean comparable) {
        this.comparable = comparable;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Instant updatedOn) {
        this.updatedOn = updatedOn;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public List<AttributeOption> getOptions() {
        return options;
    }

    public void setOptions(List<AttributeOption> options) {
        this.options = options;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
