package com.ecommerce.app.product.ripository;

import com.ecommerce.app.product.model.CategoryAttribute;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, Long> {

    Optional<CategoryAttribute> findByUuid(String uuid);

    Optional<CategoryAttribute> findByCategory_UuidAndAttribute_CodeIgnoreCase(String categoryUuid, String attributeCode);

    Optional<CategoryAttribute> findByCategory_IdAndAttribute_Id(Long categoryId, Long attributeId);

    List<CategoryAttribute> findByCategory_UuidOrderByDisplayOrderAscIdAsc(String categoryUuid);

    boolean existsByAttribute_Id(Long attributeId);

    @Query("""
            select distinct mapping
            from CategoryAttribute mapping
            join fetch mapping.attribute attributeRow
            left join fetch attributeRow.options optionRow
            where mapping.category.uuid = :categoryUuid
            and mapping.active = true
            order by mapping.displayOrder asc, mapping.id asc, optionRow.sortOrder asc, optionRow.id asc
            """)
    List<CategoryAttribute> findActiveMappingsWithAttributeOptions(@Param("categoryUuid") String categoryUuid);
}
