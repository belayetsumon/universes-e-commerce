package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.GiftCardPurchase;
import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GiftCardPurchaseRepository extends JpaRepository<GiftCardPurchase, Long> {

    @Query("""
            SELECT p
            FROM GiftCardPurchase p
            LEFT JOIN FETCH p.giftCard
            WHERE p.buyer = :buyer
            ORDER BY p.id DESC
            """)
    List<GiftCardPurchase> findByBuyerForList(@Param("buyer") Users buyer);

    @Query("""
            SELECT p
            FROM GiftCardPurchase p
            LEFT JOIN FETCH p.giftCard
            LEFT JOIN FETCH p.issuedTo
            WHERE p.uuid = :uuid
              AND p.buyer = :buyer
            """)
    Optional<GiftCardPurchase> findByUuidAndBuyer(@Param("uuid") String uuid, @Param("buyer") Users buyer);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p
            FROM GiftCardPurchase p
            LEFT JOIN FETCH p.giftCard
            LEFT JOIN FETCH p.issuedTo
            WHERE p.uuid = :uuid
              AND p.buyer = :buyer
            """)
    Optional<GiftCardPurchase> findByUuidAndBuyerForUpdate(@Param("uuid") String uuid, @Param("buyer") Users buyer);

    boolean existsByPaymentReferenceIgnoreCase(String paymentReference);
}
