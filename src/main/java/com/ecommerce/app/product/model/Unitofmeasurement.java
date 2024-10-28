/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.product.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.io.Serializable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


/**
 *
 * @author libertyerp_local
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Unitofmeasurement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    String unitAbbreviation;

    String descriptiveName;

    int decimalPlaces;

    public Unitofmeasurement() {
    }

    public Unitofmeasurement(Long id, String unitAbbreviation, String descriptiveName, int decimalPlaces) {
        this.id = id;
        this.unitAbbreviation = unitAbbreviation;
        this.descriptiveName = descriptiveName;
        this.decimalPlaces = decimalPlaces;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUnitAbbreviation() {
        return unitAbbreviation;
    }

    public void setUnitAbbreviation(String unitAbbreviation) {
        this.unitAbbreviation = unitAbbreviation;
    }

    public String getDescriptiveName() {
        return descriptiveName;
    }

    public void setDescriptiveName(String descriptiveName) {
        this.descriptiveName = descriptiveName;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }
}
