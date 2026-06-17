/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.GiftCard;
import com.ecommerce.app.module.ReferralRewards.model.GiftCardStatus;
import com.ecommerce.app.module.user.model.Users;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author libertyerp_local
 */
public interface GiftCardRepository extends JpaRepository<GiftCard, Long> {

    Optional<GiftCard> findByCodeIgnoreCase(String code);

    @Query("""
            SELECT g
            FROM GiftCard g
            LEFT JOIN FETCH g.issuedTo
            ORDER BY g.id DESC
            """)
    List<GiftCard> findAllForAdminList();

    @Query("""
            SELECT g
            FROM GiftCard g
            WHERE g.issuedTo = :user
            ORDER BY g.id DESC
            """)
    List<GiftCard> findByIssuedTo(@Param("user") Users user);

    @Query("""
            SELECT g
            FROM GiftCard g
            WHERE g.status = :status
              AND g.redeemed = false
            ORDER BY g.id DESC
            """)
    List<GiftCard> findActiveUnused(@Param("status") GiftCardStatus status);

}
