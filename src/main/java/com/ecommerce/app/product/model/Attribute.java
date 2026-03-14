/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.product.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import java.util.List;

/**
 *
 * @author libertyerp_local
 */
public class Attribute {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    private String inputType; // select, text

    private Boolean isVariant = false;

    private Boolean isFilterable = false;

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL)
    private List<AttributeOption> options;
}
