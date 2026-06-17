package com.ecommerce.app.product.ripository;

import com.ecommerce.app.product.model.ProductVariant;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    Optional<ProductVariant> findByUuid(String uuid);

    Optional<ProductVariant> findBySkuIgnoreCase(String sku);

    boolean existsByProduct_Id(Long productId);

    List<ProductVariant> findByProduct_UuidOrderByCreatedOnAsc(String productUuid);

    @Query("""
            select distinct pv
            from ProductVariant pv
            left join fetch pv.options optionRow
            left join fetch optionRow.attribute attributeRow
            left join fetch optionRow.attributeOption attributeOptionRow
            where pv.product.uuid = :productUuid
            order by pv.createdOn asc, pv.id asc
            """)
    List<ProductVariant> findDisplayRowsByProductUuid(@Param("productUuid") String productUuid);

    @Query("""
            select distinct pv
            from ProductVariant pv
            left join fetch pv.options optionRow
            left join fetch optionRow.attribute attributeRow
            left join fetch optionRow.attributeOption attributeOptionRow
            where pv.product.id in :productIds
            order by pv.product.id asc, pv.id asc
            """)
    List<ProductVariant> findDisplayRowsByProductIds(@Param("productIds") List<Long> productIds);

    List<ProductVariant> findByProduct_Vendorprofile_IdOrderByProduct_TitleAscIdAsc(Long vendorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pv from ProductVariant pv where pv.uuid = :uuid")
    Optional<ProductVariant> findByUuidForUpdate(@Param("uuid") String uuid);
}
