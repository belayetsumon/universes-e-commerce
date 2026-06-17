package com.ecommerce.app.product.ripository;

import com.ecommerce.app.product.model.Attribute;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeRepository extends JpaRepository<Attribute, Long> {

    Optional<Attribute> findByUuid(String uuid);

    Optional<Attribute> findByCodeIgnoreCase(String code);
}
