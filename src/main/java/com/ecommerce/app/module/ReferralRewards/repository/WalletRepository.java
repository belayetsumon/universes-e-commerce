/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.Wallet;
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
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    @Query("""
            SELECT w
            FROM Wallet w
            JOIN FETCH w.user
            ORDER BY w.id DESC
            """)
    List<Wallet> findAllForAdminList();

    Optional<Wallet> findByUser(Users user);

    @Query("SELECT w FROM Wallet w WHERE w.user = :user")
    Optional<Wallet> findByUsers(@Param("user") Users user);
}
