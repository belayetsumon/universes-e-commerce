package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.AuditLogPromotions;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AuditLogPromotionsRepository extends JpaRepository<AuditLogPromotions, Long> {

    @Query("""
            SELECT a
            FROM AuditLogPromotions a
            ORDER BY a.id DESC
            """)
    List<AuditLogPromotions> findAllForAdminList();
}

