package com.ecommerce.app.product.ripository;

import com.ecommerce.app.product.model.ProductVariant;
import com.ecommerce.app.product.model.StockTransaction;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    List<StockTransaction> findByProduct_IdOrderByIdDesc(Long productId);

    List<StockTransaction> findByCatalogVariant_IdOrderByIdDesc(Long catalogVariantId);

    boolean existsByOrderItem_IdAndTransactionType(Long orderItemId, com.ecommerce.app.product.model.StockTransactionTypeEnum transactionType);

    Optional<StockTransaction> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariant v WHERE v.uuid = :uuid")
    Optional<ProductVariant> findCatalogVariantByUuidForUpdate(String uuid);
}
