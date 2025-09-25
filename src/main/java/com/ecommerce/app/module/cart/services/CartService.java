/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.cart.services;

import com.ecommerce.app.module.cart.model.CartItem;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class CartService {

    @Autowired
    private HttpSession session;

    public BigDecimal subtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;

        // Retrieve cart from session
        Object sessionCart = session.getAttribute("sessioncart");

        if (sessionCart instanceof List<?>) {
            List<CartItem> cartItems = (List<CartItem>) sessionCart;

            for (CartItem item : cartItems) {
                subtotal = subtotal.add(item.getItemTotal());
            }
        }

        return subtotal;
    }

    public BigDecimal subtotal(List<CartItem> cartItems) {
        BigDecimal subtotal = BigDecimal.ZERO;

        if (cartItems == null || cartItems.isEmpty()) {
            return subtotal;
        }

        for (CartItem item : cartItems) {
            subtotal = subtotal.add(item.getItemTotal());
        }

        return subtotal;
    }

    public BigDecimal total(List< CartItem> cartitem) {

//        cartitem = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        if (!cartitem.isEmpty()) {

            for (int i = 0; i < cartitem.size(); i++) {

                total = cartitem.get(i).getItemTotal().multiply(cartitem.get(i).getQuantity());
            }
            return total;
        }
        return total;
    }

    public BigDecimal discountTotal(List<CartItem> cartItems) {
        BigDecimal discountTotal = BigDecimal.ZERO;

        if (cartItems == null || cartItems.isEmpty()) {
            return discountTotal;
        }

        for (CartItem item : cartItems) {
            discountTotal = discountTotal.add(item.getDiscountAmount());
        }

        return discountTotal;
    }

    public BigDecimal totalVat(List<CartItem> cartItems) {
        BigDecimal totalVat = BigDecimal.ZERO;

        if (cartItems == null || cartItems.isEmpty()) {
            return totalVat;
        }

        for (CartItem item : cartItems) {
            totalVat = totalVat.add(item.getVatAmount());
        }

        return totalVat;
    }

}
