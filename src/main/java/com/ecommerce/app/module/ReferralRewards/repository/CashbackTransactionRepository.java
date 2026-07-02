package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.enumvalue.CashbackStatus;
import com.ecommerce.app.module.ReferralRewards.model.CashbackTransaction;
import com.ecommerce.app.module.user.model.Users;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CashbackTransactionRepository extends JpaRepository<CashbackTransaction, Long> {

    @Query("""
            SELECT ct
            FROM CashbackTransaction ct
            LEFT JOIN FETCH ct.user u
            LEFT JOIN FETCH ct.policy p
            ORDER BY ct.id DESC
            """)
    List<CashbackTransaction> findAllForAdminList();

    List<CashbackTransaction> findByUserOrderByIdDesc(Users user);

    Optional<CashbackTransaction> findByOrderId(String orderId);

    @Query("""
            SELECT ct
            FROM CashbackTransaction ct
            LEFT JOIN FETCH ct.user u
            LEFT JOIN FETCH ct.policy p
            WHERE ct.orderId = :orderId
              AND ct.status = :status
            ORDER BY ct.id DESC
            """)
    List<CashbackTransaction> findByOrderIdAndStatusForProcessing(@Param("orderId") String orderId, @Param("status") CashbackStatus status);

    @Query("""
            SELECT COUNT(ct)
            FROM CashbackTransaction ct
            WHERE ct.orderId = :orderId
              AND ct.status IN :statuses
            """)
    long countByOrderIdAndStatusIn(@Param("orderId") String orderId, @Param("statuses") List<CashbackStatus> statuses);
}
