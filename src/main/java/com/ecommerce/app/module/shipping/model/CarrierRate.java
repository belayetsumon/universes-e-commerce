/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.shipping.model;

import com.ecommerce.app.globalServices.District;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@EntityListeners(AuditingEntityListener.class)
public class CarrierRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique UUID for external reference
    @Column(nullable = false, unique = true, updatable = false)
    private String uuid = UUID.randomUUID().toString();

    @ManyToOne(optional = false)
    @NotNull(message = "Carrier is required")
    @JsonIgnore // prevent recursion
    private Carrier carrier;

    /**
     * Expose lightweight carrier info for JSON
     */
    @JsonProperty("carrier")
    public Map<String, Object> getCarrierInfo() {
        if (carrier == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", carrier.getId());
        map.put("code", carrier.getCode());
        map.put("name", carrier.getName());
        return map;
    }

    @ElementCollection(targetClass = District.class)
    @CollectionTable(
            name = "carrier_rate_districts",
            joinColumns = @JoinColumn(name = "carrier_rate_id")
    )
    @Column(name = "district", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotEmpty(message = "At least one district is required")
    private List<District> district = new ArrayList<>();

    @Column(nullable = false)
    @DecimalMin(value = "0.0", inclusive = true, message = "Base price must be 0 or more")
    @Digits(integer = 10, fraction = 2, message = "Base price format is invalid")
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(nullable = false)
    @DecimalMin(value = "0.0", inclusive = true, message = "Per Kg must be 0 or more")
    @Digits(integer = 10, fraction = 2, message = "Per Kg format is invalid")
    private BigDecimal perKg = BigDecimal.ZERO;

//    @Column(nullable = false)
//    @DecimalMin(value = "0.0", inclusive = true, message = "Per Km must be 0 or more")
//    @Digits(integer = 10, fraction = 2, message = "Per Km format is invalid")
//    private BigDecimal perKm = BigDecimal.ZERO;
    @Column(nullable = false)
    @DecimalMin(value = "0.0", inclusive = true, message = "COD Fee must be 0 or more")
    @Digits(integer = 10, fraction = 2, message = "COD Fee format is invalid")
    private BigDecimal codFee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Delivery speed is required")
    private DeliverySpeed speed = DeliverySpeed.STANDARD; // STANDARD / EXPRESS

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Delivery type is required")
    private DeliveryType deliveryType = DeliveryType.HOME_DELIVERY; // HOME_DELIVERY / OFFICE_PICKUP

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

    public CarrierRate() {
    }

    public CarrierRate(Long id, Carrier carrier, String createdBy, LocalDateTime created, String modifiedBy, LocalDateTime modified) {
        this.id = id;
        this.carrier = carrier;
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

    public Carrier getCarrier() {
        return carrier;
    }

    public void setCarrier(Carrier carrier) {
        this.carrier = carrier;
    }

    public List<District> getDistrict() {
        return district;
    }

    public void setDistrict(List<District> district) {
        this.district = district;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getPerKg() {
        return perKg;
    }

    public void setPerKg(BigDecimal perKg) {
        this.perKg = perKg;
    }

    public BigDecimal getCodFee() {
        return codFee;
    }

    public void setCodFee(BigDecimal codFee) {
        this.codFee = codFee;
    }

    public DeliverySpeed getSpeed() {
        return speed;
    }

    public void setSpeed(DeliverySpeed speed) {
        this.speed = speed;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
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
