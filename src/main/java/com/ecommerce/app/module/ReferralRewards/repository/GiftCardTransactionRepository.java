package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.GiftCard;
import com.ecommerce.app.module.ReferralRewards.model.GiftCardTransaction;
import com.ecommerce.app.module.user.model.Users;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GiftCardTransactionRepository extends JpaRepository<GiftCardTransaction, Long> {

    @Query("""
            SELECT gct
            FROM GiftCardTransaction gct
            JOIN FETCH gct.giftCard gc
            LEFT JOIN FETCH gc.issuedTo u
            ORDER BY gct.id DESC
            """)
    List<GiftCardTransaction> findAllForAdminList();

    @Query("""
            SELECT gct
            FROM GiftCardTransaction gct
            JOIN FETCH gct.giftCard gc
            WHERE gc.issuedTo = :user
            ORDER BY gct.id DESC
            """)
    List<GiftCardTransaction> findByUserOrderByIdDesc(@Param("user") Users user);

    boolean existsByGiftCardAndOrderId(GiftCard giftCard, String orderId);
}
