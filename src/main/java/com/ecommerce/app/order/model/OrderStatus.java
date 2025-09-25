/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.order.model;

/**
 *
 * @author User
 */
public enum OrderStatus {
    NEW_ORDER, PENDING, CONFIRMED, PROCESSING, PACKED, SHIPPED, IN_TRANSIT,
    OUT_FOR_DELIVERY, DELIVERED, RETURN_REQUESTED, RETURNED,
    CANCELLED, COMPLETED
}
