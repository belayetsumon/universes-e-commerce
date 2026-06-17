package com.ecommerce.app.product.ripository;

import com.ecommerce.app.product.model.AttributeOption;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeOptionRepository extends JpaRepository<AttributeOption, Long> {

    Optional<AttributeOption> findByUuid(String uuid);

    boolean existsByUuidAndAttribute_Id(String uuid, Long attributeId);

    boolean existsByUuidAndAttribute_IdAndActiveTrue(String uuid, Long attributeId);

    Optional<AttributeOption> findByUuidAndAttribute_IdAndActiveTrue(String uuid, Long attributeId);

    Optional<AttributeOption> findByAttribute_IdAndCodeIgnoreCase(Long attributeId, String code);

    List<AttributeOption> findByAttribute_UuidOrderBySortOrderAscIdAsc(String attributeUuid);
}
