package com.ecommerce.app.product.ripository;

import com.ecommerce.app.product.model.ProductVariant;
import com.ecommerce.app.product.model.StockTransaction;
import com.ecommerce.app.product.model.StockTransactionTypeEnum;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    List<StockTransaction> findByProduct_IdOrderByIdDesc(Long productId);

    List<StockTransaction> findByCatalogVariant_IdOrderByIdDesc(Long catalogVariantId);

    boolean existsByOrderItem_IdAndTransactionType(Long orderItemId, com.ecommerce.app.product.model.StockTransactionTypeEnum transactionType);

    Optional<StockTransaction> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query(
            value = """
            SELECT tx
            FROM StockTransaction tx
            JOIN FETCH tx.product p
            LEFT JOIN FETCH p.vendorprofile vendor
            LEFT JOIN FETCH tx.catalogVariant cv
            LEFT JOIN FETCH tx.salesOrder so
            WHERE (:vendorId IS NULL OR vendor.id = :vendorId)
              AND (:type IS NULL OR tx.transactionType = :type)
              AND (:createdFrom IS NULL OR tx.created >= :createdFrom)
              AND (:createdToExclusive IS NULL OR tx.created < :createdToExclusive)
              AND (
                    :searchPattern IS NULL
                    OR p.title LIKE :searchPattern
                    OR cv.sku LIKE :searchPattern
                    OR so.orderCode LIKE :searchPattern
                    OR tx.note LIKE :searchPattern
                    OR tx.createdBy LIKE :searchPattern
                    OR vendor.companyName LIKE :searchPattern
                    OR vendor.vendorCode LIKE :searchPattern
                    OR (:productSku IS NOT NULL AND p.sku = :productSku)
              )
            """,
            countQuery = """
            SELECT COUNT(tx)
            FROM StockTransaction tx
            JOIN tx.product p
            LEFT JOIN p.vendorprofile vendor
            LEFT JOIN tx.catalogVariant cv
            LEFT JOIN tx.salesOrder so
            WHERE (:vendorId IS NULL OR vendor.id = :vendorId)
              AND (:type IS NULL OR tx.transactionType = :type)
              AND (:createdFrom IS NULL OR tx.created >= :createdFrom)
              AND (:createdToExclusive IS NULL OR tx.created < :createdToExclusive)
              AND (
                    :searchPattern IS NULL
                    OR p.title LIKE :searchPattern
                    OR cv.sku LIKE :searchPattern
                    OR so.orderCode LIKE :searchPattern
                    OR tx.note LIKE :searchPattern
                    OR tx.createdBy LIKE :searchPattern
                    OR vendor.companyName LIKE :searchPattern
                    OR vendor.vendorCode LIKE :searchPattern
                    OR (:productSku IS NOT NULL AND p.sku = :productSku)
              )
            """
    )
    Page<StockTransaction> searchTransactions(
            @Param("vendorId") Long vendorId,
            @Param("type") StockTransactionTypeEnum type,
            @Param("createdFrom") LocalDateTime createdFrom,
            @Param("createdToExclusive") LocalDateTime createdToExclusive,
            @Param("searchPattern") String searchPattern,
            @Param("productSku") Integer productSku,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariant v WHERE v.uuid = :uuid")
    Optional<ProductVariant> findCatalogVariantByUuidForUpdate(String uuid);
}
