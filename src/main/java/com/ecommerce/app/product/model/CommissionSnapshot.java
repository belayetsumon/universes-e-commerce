/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.product.model;

import com.ecommerce.app.module.settings.model.CommissionRuleType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;

/**
 *
 * @author libertyerp_local
 */
@Embeddable
public class CommissionSnapshot {

    private String uuid;

    @Enumerated(EnumType.STRING)
    private CommissionRuleType ruleType;

    private BigDecimal percentage;

    private BigDecimal amount;
}
