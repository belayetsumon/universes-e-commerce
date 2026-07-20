/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.order.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author libertyerp_local
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "customer_card_info")
public class CardInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Payment Information
    @NotBlank(message = "Card number is required")
    private String cardNumber;

    @NotBlank(message = "Card expiry date is required")
    private String cardExpiry;

    @NotBlank(message = "CVV is required")
    private String cvv;

}
