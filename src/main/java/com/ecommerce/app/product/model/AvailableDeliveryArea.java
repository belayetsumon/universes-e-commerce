/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import com.ecommerce.app.module.shipping.model.ShippingLocation;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
@Table(name = "product_available_delivery_area")
public class AvailableDeliveryArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Product  cannot be blank.")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Product product;

    @Convert(converter = AvailableDeliveryAreaModeConverter.class)
    @Column(nullable = false)
    private AvailableDeliveryAreaMode mode = AvailableDeliveryAreaMode.SPECIFIC_AREA;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "district_location_id")
    private ShippingLocation district;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "available_delivery_area_selected_locations",
            joinColumns = @JoinColumn(name = "available_delivery_area_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    private List<ShippingLocation> selectedDistricts = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "available_delivery_area_excluded_locations",
            joinColumns = @JoinColumn(name = "available_delivery_area_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    private List<ShippingLocation> excludedDistricts = new ArrayList<>();

    @Lob
    private String description;

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

    public AvailableDeliveryArea() {
    }

    public AvailableDeliveryArea(Long id, Product product, AvailableDeliveryAreaMode mode, ShippingLocation district, List<ShippingLocation> selectedDistricts, List<ShippingLocation> excludedDistricts, String description, String createdBy, LocalDateTime created, String modifiedBy, LocalDateTime modified) {
        this.id = id;
        this.product = product;
        this.mode = mode;
        this.district = district;
        this.selectedDistricts = selectedDistricts;
        this.excludedDistricts = excludedDistricts;
        this.description = description;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public AvailableDeliveryAreaMode getMode() {
        return mode;
    }

    public void setMode(AvailableDeliveryAreaMode mode) {
        this.mode = mode;
    }

    public ShippingLocation getDistrict() {
        return district;
    }

    public void setDistrict(ShippingLocation district) {
        this.district = district;
    }

    public List<ShippingLocation> getSelectedDistricts() {
        return selectedDistricts;
    }

    public void setSelectedDistricts(List<ShippingLocation> selectedDistricts) {
        this.selectedDistricts = selectedDistricts;
    }

    public List<ShippingLocation> getExcludedDistricts() {
        return excludedDistricts;
    }

    public void setExcludedDistricts(List<ShippingLocation> excludedDistricts) {
        this.excludedDistricts = excludedDistricts;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @Transient
    public boolean matchesLocation(ShippingLocation customerLocation) {
        if (customerLocation == null) {
            return false;
        }

        // Prefer structured location rules and keep description text as fallback for older records.
        if (mode == AvailableDeliveryAreaMode.ALL_AREA) {
            return true;
        }

        if (mode == AvailableDeliveryAreaMode.ALL_AREA_EXCEPT) {
            return excludedDistricts == null || excludedDistricts.stream()
                    .filter(java.util.Objects::nonNull)
                    .noneMatch(location -> location.isSameOrAncestorOf(customerLocation));
        }

        if (mode == AvailableDeliveryAreaMode.SPECIFIC_AREA) {
            if (selectedDistricts != null && !selectedDistricts.isEmpty()) {
                return selectedDistricts.stream()
                        .filter(java.util.Objects::nonNull)
                        .anyMatch(location -> location.isSameOrAncestorOf(customerLocation));
            }
            if (district != null) {
                return district.isSameOrAncestorOf(customerLocation);
            }
        }

        if (description == null || description.isBlank()) {
            return false;
        }

        String normalizedDescription = description.trim();
        return normalizedDescription.equalsIgnoreCase(customerLocation.getCode())
                || normalizedDescription.equalsIgnoreCase(customerLocation.getName())
                || normalizedDescription.equalsIgnoreCase(customerLocation.getDisplayLabel())
                || normalizedDescription.equalsIgnoreCase("All");
    }

    @Transient
    public String getCoverageLabel() {
        if (mode == AvailableDeliveryAreaMode.ALL_AREA) {
            return "All Area";
        }

        if (mode == AvailableDeliveryAreaMode.ALL_AREA_EXCEPT) {
            String excluded = excludedDistricts == null || excludedDistricts.isEmpty()
                    ? "None"
                    : excludedDistricts.stream()
                            .map(ShippingLocation::getDisplayLabel)
                            .collect(Collectors.joining(", "));
            return "All Area Except: " + excluded;
        }

        if (selectedDistricts != null && !selectedDistricts.isEmpty()) {
            return selectedDistricts.stream()
                    .map(ShippingLocation::getDisplayLabel)
                    .collect(Collectors.joining(", "));
        }

        if (district != null) {
            return district.getDisplayLabel();
        }

        return description;
    }
}
