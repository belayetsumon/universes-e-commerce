package com.ecommerce.app.module.order.repository;

import com.ecommerce.app.module.order.model.CustomerOrderGroup;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerOrderGroupRepository extends JpaRepository<CustomerOrderGroup, Long> {

    Optional<CustomerOrderGroup> findByUuid(String uuid);

    Optional<CustomerOrderGroup> findByOrderGroupCode(String orderGroupCode);

    @Query("""
            SELECT g.orderGroupCode
            FROM CustomerOrderGroup g
            WHERE g.orderGroupCode LIKE :prefix
            ORDER BY g.orderGroupCode DESC
            """)
    List<String> findLatestOrderGroupCode(@Param("prefix") String prefix, Pageable pageable);
}
