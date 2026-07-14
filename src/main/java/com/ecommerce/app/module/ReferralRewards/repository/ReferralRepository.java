/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.user.model.Users;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author libertyerp_local
 */
public interface ReferralRepository extends JpaRepository<Referral, Long> {

    @Query("""
            SELECT r
            FROM Referral r
            JOIN FETCH r.users
            LEFT JOIN FETCH r.referredUser
            ORDER BY r.id DESC
            """)
    List<Referral> findAllForAdminList();

    // Find referral owned by a specific user (who owns the referral code)
    Optional<Referral> findByUsers(Users users);

    long countByUsers(Users users);

    long countByReferredUser(Users user);

    Optional<Referral> findByUsers_Id(Long id);

    List<Referral> findAllByUsers_Id(Long id);

    @Query("""
            SELECT r
            FROM Referral r
            JOIN FETCH r.users
            WHERE r.users.id IN :userIds
            """)
    List<Referral> findAllByUsers_IdIn(@Param("userIds") Collection<Long> userIds);

    // Find referral by the referred user's ID (should be one-to-one)
    Optional<Referral> findByReferredUser_Id(Long id);

    // Find referral by the referred user entity
    Optional<Referral> findByReferredUser(Users user);

    List<Referral> findAllByReferredUser(Users user);

    // Find referral by referral code
    Optional<Referral> findByReferralCode(String referralCode);

    // ✅ Optional: Count how many users someone has referred (used the code)
    long countByUsers_IdAndReferredUserIsNotNull(Long id);

    // ✅ Optional: Get list of referred users
    @Query("SELECT r FROM Referral r WHERE r.referredUser.id = :userId")
    List<Referral> findReferredUsersByUserId(@Param("userId") Long userId);

}
