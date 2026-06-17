/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.order.repository;

import com.ecommerce.app.order.model.OrderItem;
import com.ecommerce.app.order.model.OrderStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author User
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findBySalesOrder_Id(Long id);

    List<OrderItem> findBySalesOrder_IdAndIdIn(Long salesOrderId, Collection<Long> itemIds);

    @Query("""
            SELECT oi
            FROM OrderItem oi
            JOIN FETCH oi.salesOrder so
            WHERE so.customer.id = :customerId
              AND oi.product.id = :productId
              AND so.status IN :statuses
            ORDER BY so.id DESC, oi.id DESC
            """)
    List<OrderItem> findReviewEligibleItems(
            @Param("customerId") Long customerId,
            @Param("productId") Long productId,
            @Param("statuses") Collection<OrderStatus> statuses
    );
}
