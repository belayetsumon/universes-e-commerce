package com.ecommerce.app.product.ripository;

import com.ecommerce.app.product.model.ProductAttribute;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {

    Optional<ProductAttribute> findByUuid(String uuid);

    boolean existsByAttribute_Id(Long attributeId);

    boolean existsByAttributeOption_Id(Long attributeOptionId);

    @Query("""
            select pav
            from ProductAttribute pav
            join fetch pav.attribute a
            left join fetch pav.attributeOption ao
            where pav.product.uuid = :productUuid
            order by a.name asc, pav.sortOrder asc, pav.id asc
            """)
    List<ProductAttribute> findDisplayRowsByProductUuid(@Param("productUuid") String productUuid);

    @Query("""
            select pav
            from ProductAttribute pav
            join fetch pav.attribute a
            left join fetch pav.attributeOption ao
            where pav.product.id in :productIds
            order by pav.product.id asc, a.name asc, pav.sortOrder asc, pav.id asc
            """)
    List<ProductAttribute> findDisplayRowsByProductIds(@Param("productIds") List<Long> productIds);

    void deleteByProduct_Uuid(String productUuid);
}
