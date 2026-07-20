/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.order.repository;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.order.model.OrderStatus;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

    long countByCustomer(Users users);

    long countByCustomer_Id(Long customerId);

    Optional<SalesOrder> findByIdAndCustomer_Id(Long id, Long customerId);

    List<SalesOrder> findByCustomerAndStatusOrderByIdDesc(Users users, OrderStatus status);

    List<SalesOrder> findAllByOrderByIdDesc();

    SalesOrder findFirstByOrderByIdDesc();

    List<SalesOrder> findByVendorIdOrderByIdDesc(Long Id);

    long countByVendorId(Long vendorId);

    long countByVendorIdAndStatus(Long vendorId, OrderStatus status);

    long countByVendorIdAndStatusIn(Long vendorId, Collection<OrderStatus> statuses);

    long countByVendorIdAndCreatedAfter(Long vendorId, LocalDateTime created);

    long countByVendorIdAndCustomer_Id(Long vendorId, Long customerId);

    Optional<SalesOrder> findByIdAndVendorId(Long id, Long vendorId);

    @Query("""
            select distinct s
            from SalesOrder s
            left join fetch s.shippingAddress
            where s.id in :ids
            """)
    List<SalesOrder> findByIdInWithShippingAddress(@Param("ids") Collection<Long> ids);

    @Query("""
            select distinct customer.id
            from SalesOrder s
            join s.customer customer
            where s.vendorId = :vendorId
              and customer.status = com.ecommerce.app.module.user.model.Status.Active
            """)
    List<Long> findDistinctCustomerIdsByVendorId(@Param("vendorId") Long vendorId);

    @Query("""
            select distinct customer.id
            from SalesOrder s
            join s.customer customer
            where s.vendorId = :vendorId
              and customer.id in :customerIds
              and customer.status = com.ecommerce.app.module.user.model.Status.Active
            """)
    List<Long> findDistinctCustomerIdsByVendorIdAndCustomerIds(
            @Param("vendorId") Long vendorId,
            @Param("customerIds") java.util.Collection<Long> customerIds);

    @Query("""
  SELECT s.orderCode
  FROM SalesOrder s
  WHERE s.orderCode LIKE :prefix
  ORDER BY s.orderCode DESC
""")
    List<String> findLatestOrderCode(@Param("prefix") String prefix, Pageable pageable);

}
