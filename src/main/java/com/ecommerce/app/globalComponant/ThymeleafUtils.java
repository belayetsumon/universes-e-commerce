/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Component.java to edit this template
 */
package com.ecommerce.app.globalComponant;

import com.ecommerce.app.module.cart.model.CartItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 *
 * @author libertyerp_local
 */
@Component("thymeleafUtils")
public class ThymeleafUtils {

    public BigDecimal sumItemTotals(List<CartItem> carts) {
        if (carts == null || carts.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return carts.stream()
                .map(CartItem::getItemTotal)
                .filter(Objects::nonNull) // skip null itemTotal values
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String hello() {

        return "Hello";
    }
}
