/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.order.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
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
public class ShippingChargeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    private ShippingZoneEnum zone;
    private double weightKg;
    private double chargeAmount;
    private boolean cod;
    private boolean isReturn;

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

    public ShippingChargeLog() {
    }

    public ShippingChargeLog(Long id, ShippingZoneEnum zone, double weightKg, double chargeAmount, boolean cod, boolean isReturn, String createdBy, LocalDateTime created, String modifiedBy, LocalDateTime modified) {
        this.id = id;
        this.zone = zone;
        this.weightKg = weightKg;
        this.chargeAmount = chargeAmount;
        this.cod = cod;
        this.isReturn = isReturn;
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

    public ShippingZoneEnum getZone() {
        return zone;
    }

    public void setZone(ShippingZoneEnum zone) {
        this.zone = zone;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public double getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(double chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public boolean isCod() {
        return cod;
    }

    public void setCod(boolean cod) {
        this.cod = cod;
    }

    public boolean isIsReturn() {
        return isReturn;
    }

    public void setIsReturn(boolean isReturn) {
        this.isReturn = isReturn;
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
