/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.shipping.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ShippingProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long vendorId;

    private String name;

    @Enumerated(EnumType.STRING)
    private ProfileType type; // ORIGIN / DESTINATION

    @ManyToMany
    @JoinTable(
            name = "profile_carriers",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "carrier_id")
    )
    @JsonIgnore
    private List<Carrier> allowedCarriers; // Many-to-Many relation

    /* Expose lightweight carrier info instead */
    @JsonProperty("carriers")
    public List<Map<String, Object>> getCarrierInfo() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Carrier c : allowedCarriers) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("code", c.getCode());
            map.put("name", c.getName());
            list.add(map);
        }
        return list;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "shipping_profile_locations",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    private List<ShippingLocation> allowedDistricts = new ArrayList<>();

    private boolean active;

    // Enum for type
    public enum ProfileType {
        Shipping_From, DESTINATION
    }

    /// Audit ///
    @CreatedBy
    @Column(nullable = false, updatable = false)
    public String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    public LocalDateTime created;

    @LastModifiedBy
    @Column(insertable = false)
    public String modifiedBy;

    @LastModifiedDate
    @Column(insertable = false)
    public LocalDateTime modified;

    public ShippingProfile() {
    }

    public ShippingProfile(Long id, Long vendorId, String name, ProfileType type, List<Carrier> allowedCarriers, List<ShippingLocation> allowedDistricts, boolean active, String createdBy, LocalDateTime created, String modifiedBy, LocalDateTime modified) {
        this.id = id;
        this.vendorId = vendorId;
        this.name = name;
        this.type = type;
        this.allowedCarriers = allowedCarriers;
        this.allowedDistricts = allowedDistricts;
        this.active = active;
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

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProfileType getType() {
        return type;
    }

    public void setType(ProfileType type) {
        this.type = type;
    }

    public List<Carrier> getAllowedCarriers() {
        return allowedCarriers;
    }

    public void setAllowedCarriers(List<Carrier> allowedCarriers) {
        this.allowedCarriers = allowedCarriers;
    }

    public List<ShippingLocation> getAllowedDistricts() {
        return allowedDistricts;
    }

    public void setAllowedDistricts(List<ShippingLocation> allowedDistricts) {
        this.allowedDistricts = allowedDistricts;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
