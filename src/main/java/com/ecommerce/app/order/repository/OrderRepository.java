/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.order.repository;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.order.model.OrderStatus;
import com.ecommerce.app.order.model.SalesOrder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 *
 * @author User
 */
public interface OrderRepository extends JpaRepository<SalesOrder, Long> {

    List<SalesOrder> findByCustomer(Users users);

    List<SalesOrder> findByCustomerAndStatusOrderByIdDesc(Users users, OrderStatus status);

    SalesOrder findFirstByOrderByIdDesc();
    
    
    
    

}
