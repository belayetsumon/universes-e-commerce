/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.product.model;

import jakarta.persistence.Column;

/**
 *
 * @author libertyerp_local
 */
public class Warehouse {

    @Column(nullable = false)
    private String name;

    private String location;
}
