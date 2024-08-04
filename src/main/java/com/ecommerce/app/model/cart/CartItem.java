/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.model.cart;

import com.ecommerce.app.product.model.Product;
import java.io.Serializable;
import org.springframework.context.annotation.Scope;

/**
 *
 * @author User
 */

@Scope("session")
public class CartItem implements Serializable {

    private Product exam;

    private int quantity;

    public CartItem() {
    }

    public CartItem(Product exam, int quantity) {
        this.exam = exam;
        this.quantity = quantity;
    }

    public Product getExam() {
        return exam;
    }

    public void setExam(Product exam) {
        this.exam = exam;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}
