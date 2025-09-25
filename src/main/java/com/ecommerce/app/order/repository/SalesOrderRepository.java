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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author User
 */
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    SalesOrder findByUuid(String uuid);

    List<SalesOrder> findByCustomerOrderByIdDesc(Users users);

    List<SalesOrder> findByCustomerAndStatusOrderByIdDesc(Users users, OrderStatus status);

    List<SalesOrder> findAllByOrderByIdDesc();

    SalesOrder findFirstByOrderByIdDesc();

    List<SalesOrder> findByVendorIdOrderByIdDesc(Long Id);

    @Query("""
  SELECT s.orderCode
  FROM SalesOrder s
  WHERE s.orderCode LIKE :prefix
  ORDER BY s.orderCode DESC
""")
    List<String> findLatestOrderCode(@Param("prefix") String prefix, Pageable pageable);

}
