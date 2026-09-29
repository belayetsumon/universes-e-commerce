package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.FraudBlockType;
import com.ecommerce.app.module.fraud.model.FraudBlocklist;
import com.ecommerce.app.module.fraud.model.FraudBlockScope;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FraudBlocklistRepository extends JpaRepository<FraudBlocklist, Long>, JpaSpecificationExecutor<FraudBlocklist> {

    Optional<FraudBlocklist> findByUuid(String uuid);

    List<FraudBlocklist> findAllByBlockTypeAndHashedValueInAndScopeAndActiveTrue(
            FraudBlockType blockType,
            Collection<String> hashedValues,
            FraudBlockScope scope);

    List<FraudBlocklist> findByActiveTrueOrderByIdDesc();

    long countByActive(boolean active);

    @Query("""
            select case when count(block) > 0 then true else false end
            from FraudBlocklist block
            where block.blockType = :blockType
              and block.hashedValue in :hashedValues
              and block.scope = com.ecommerce.app.module.fraud.model.FraudBlockScope.GLOBAL
              and block.active = true
              and (
                    block.temporary = false
                    or block.expiresAt is null
                    or block.expiresAt > :now
              )
            """)
    boolean existsEffectiveBlock(
            @Param("blockType") FraudBlockType blockType,
            @Param("hashedValues") Collection<String> hashedValues,
            @Param("now") LocalDateTime now);
}
