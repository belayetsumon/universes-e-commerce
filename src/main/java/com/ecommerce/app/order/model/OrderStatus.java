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
    OUT_FOR_DELIVERY, DELIVERED, RETURN_REQUESTED, PARTIALLY_RETURNED, RETURNED,
    CANCELLED, COMPLETED
}

//| `VendorOrderStatus` | Meaning                             |
//| ------------------- | ----------------------------------- |
//| `PENDING`           | Order received, not yet confirmed.  |
//| `CONFIRMED`         | Vendor accepts, stock allocated.    |
//| `PACKED`            | Items packed, ready to ship.        |
//| `SHIPPED`           | Handed to courier.                  |
//| `OUT_FOR_DELIVERY`  | Courier is delivering.              |
//| `DELIVERED`         | Delivered successfully.             |
//| `RETURN_REQUESTED`  | One or more items requested return. |
//| `PARTIALLY_RETURNED`| Some items returned, order active.  |
//| `CANCELLED`         | Cancelled by vendor/customer/admin. |
//| `RETURNED`          | Returned by customer.               |
