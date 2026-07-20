/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.order.repository;

import com.ecommerce.app.module.order.model.OrderItem;
import com.ecommerce.app.module.order.model.OrderItemReturnStatus;
import com.ecommerce.app.module.order.model.OrderStatus;
import com.ecommerce.app.product.model.ProductTypeEnum;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
            select oi.salesOrder.id
            from OrderItem oi
            left join oi.product p
            where oi.salesOrder.id in :orderIds
            group by oi.salesOrder.id
            having count(oi.id) > 0
               and sum(case when p is null or p.productType <> :productType then 1 else 0 end) = 0
            """)
    List<Long> findOrderIdsContainingOnlyProductType(
            @Param("orderIds") Collection<Long> orderIds,
            @Param("productType") ProductTypeEnum productType
    );

    @EntityGraph(attributePaths = {"salesOrder", "product"})
    @Query(
            value = """
            SELECT oi
            FROM OrderItem oi
            LEFT JOIN oi.salesOrder so
            LEFT JOIN oi.product p
            WHERE (:vendorId IS NULL OR oi.vendorId = :vendorId OR so.vendorId = :vendorId)
              AND (:status IS NULL OR so.status = :status)
              AND (:returnStatus IS NULL OR oi.returnStatus = :returnStatus)
              AND (:fromDate IS NULL OR oi.created >= :fromDate)
              AND (:toDate IS NULL OR oi.created < :toDate)
              AND (
                    :searchPattern IS NULL
                    OR so.orderCode LIKE :searchPattern
                    OR p.title LIKE :searchPattern
                    OR oi.variantSummary LIKE :searchPattern
                    OR oi.catalogVariantUuid LIKE :searchPattern
                    OR oi.digitalLicenseCode LIKE :searchPattern
                    OR (:numericId IS NOT NULL AND (oi.id = :numericId OR so.id = :numericId))
                    OR (:productSku IS NOT NULL AND p.sku = :productSku)
                  )
            """,
            countQuery = """
            SELECT COUNT(oi)
            FROM OrderItem oi
            LEFT JOIN oi.salesOrder so
            LEFT JOIN oi.product p
            WHERE (:vendorId IS NULL OR oi.vendorId = :vendorId OR so.vendorId = :vendorId)
              AND (:status IS NULL OR so.status = :status)
              AND (:returnStatus IS NULL OR oi.returnStatus = :returnStatus)
              AND (:fromDate IS NULL OR oi.created >= :fromDate)
              AND (:toDate IS NULL OR oi.created < :toDate)
              AND (
                    :searchPattern IS NULL
                    OR so.orderCode LIKE :searchPattern
                    OR p.title LIKE :searchPattern
                    OR oi.variantSummary LIKE :searchPattern
                    OR oi.catalogVariantUuid LIKE :searchPattern
                    OR oi.digitalLicenseCode LIKE :searchPattern
                    OR (:numericId IS NOT NULL AND (oi.id = :numericId OR so.id = :numericId))
                    OR (:productSku IS NOT NULL AND p.sku = :productSku)
                  )
            """
    )
    Page<OrderItem> searchSalesItems(
            @Param("vendorId") Long vendorId,
            @Param("status") OrderStatus status,
            @Param("returnStatus") OrderItemReturnStatus returnStatus,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("searchPattern") String searchPattern,
            @Param("numericId") Long numericId,
            @Param("productSku") Integer productSku,
            Pageable pageable
    );

    @Query("""
            SELECT
                p.id AS productId,
                p.title AS productTitle,
                p.sku AS sku,
                SUM(oi.quantity) AS quantity,
                SUM(oi.itemTotal) AS itemTotal
            FROM OrderItem oi
            LEFT JOIN oi.salesOrder so
            LEFT JOIN oi.product p
            WHERE (:vendorId IS NULL OR oi.vendorId = :vendorId OR so.vendorId = :vendorId)
              AND (:status IS NULL OR so.status = :status)
              AND (:returnStatus IS NULL OR oi.returnStatus = :returnStatus)
              AND (:fromDate IS NULL OR oi.created >= :fromDate)
              AND (:toDate IS NULL OR oi.created < :toDate)
              AND (
                    :searchPattern IS NULL
                    OR so.orderCode LIKE :searchPattern
                    OR p.title LIKE :searchPattern
                    OR oi.variantSummary LIKE :searchPattern
                    OR oi.catalogVariantUuid LIKE :searchPattern
                    OR oi.digitalLicenseCode LIKE :searchPattern
                    OR (:numericId IS NOT NULL AND (oi.id = :numericId OR so.id = :numericId))
                    OR (:productSku IS NOT NULL AND p.sku = :productSku)
                  )
            GROUP BY p.id, p.title, p.sku
            ORDER BY SUM(oi.quantity) DESC, SUM(oi.itemTotal) DESC
            """)
    List<SalesItemTopProductProjection> findTopSalesItems(
            @Param("vendorId") Long vendorId,
            @Param("status") OrderStatus status,
            @Param("returnStatus") OrderItemReturnStatus returnStatus,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("searchPattern") String searchPattern,
            @Param("numericId") Long numericId,
            @Param("productSku") Integer productSku,
            Pageable pageable
    );

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
