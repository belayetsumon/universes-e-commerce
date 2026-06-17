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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
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
        name = "product_catalog_product_variant_option",
        indexes = {
            @Index(name = "idx_catalog_product_variant_option_uuid", columnList = "uuid"),
            @Index(name = "idx_catalog_product_variant_option_variant", columnList = "variant_id"),
            @Index(name = "idx_catalog_product_variant_option_attribute", columnList = "attribute_id"),
            @Index(name = "idx_catalog_product_variant_option_option", columnList = "attribute_option_id")
        },
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_catalog_product_variant_option_variant_attribute", columnNames = {"variant_id", "attribute_id"})
        }
)
@EntityListeners(AuditingEntityListener.class)
public class ProductVariantOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false, length = 36)
    private String uuid = UUID.randomUUID().toString();
    @NotNull(message = "Variant is required.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @NotNull(message = "Attribute is required.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_option_id")
    private AttributeOption attributeOption;

    @Column(name = "text_value", length = 500)
    private String textValue;

    @Column(name = "display_value", length = 500)
    private String displayValue;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

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

    public ProductVariant getVariant() {
        return variant;
    }

    public void setVariant(ProductVariant variant) {
        this.variant = variant;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public AttributeOption getAttributeOption() {
        return attributeOption;
    }

    public void setAttributeOption(AttributeOption attributeOption) {
        this.attributeOption = attributeOption;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public ProductVariantOption(Long id, ProductVariant variant, Attribute attribute, AttributeOption attributeOption, String textValue, String displayValue, Long version, Instant createdOn, String createdBy, Instant updatedOn, String updatedBy) {
        this.id = id;
        this.variant = variant;
        this.attribute = attribute;
        this.attributeOption = attributeOption;
        this.textValue = textValue;
        this.displayValue = displayValue;
        this.version = version;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
        this.updatedOn = updatedOn;
        this.updatedBy = updatedBy;
    }

    public ProductVariantOption() {
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

}
