package com.ecommerce.app.product.ripository;

import com.ecommerce.app.product.model.ProductVariantOption;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantOptionRepository extends JpaRepository<ProductVariantOption, Long> {

    Optional<ProductVariantOption> findByUuid(String uuid);

    List<ProductVariantOption> findByVariant_UuidOrderBySortOrderAscIdAsc(String variantUuid);

    boolean existsByAttribute_Id(Long attributeId);

    boolean existsByAttributeOption_Id(Long attributeOptionId);
}
