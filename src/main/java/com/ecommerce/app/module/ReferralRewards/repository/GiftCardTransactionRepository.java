package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.GiftCard;
import com.ecommerce.app.module.ReferralRewards.model.GiftCardTransaction;
import com.ecommerce.app.module.user.model.Users;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GiftCardTransactionRepository extends JpaRepository<GiftCardTransaction, Long> {

    @Query("""
            SELECT gct
            FROM GiftCardTransaction gct
            JOIN FETCH gct.giftCard gc
            LEFT JOIN FETCH gct.order o
            LEFT JOIN FETCH gct.user u
            ORDER BY gct.id DESC
            """)
    List<GiftCardTransaction> findAllForAdminList();

    List<GiftCardTransaction> findByUserOrderByIdDesc(Users user);

    boolean existsByGiftCardAndOrder_Id(GiftCard giftCard, Long orderId);
}

