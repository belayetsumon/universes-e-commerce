/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.module.user.ripository;

import com.ecommerce.app.module.user.model.LoginHistory;
import com.ecommerce.app.module.user.model.LoginStatus;
import com.ecommerce.app.module.user.model.Users;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author libertyerp_local
 */
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    @Query("SELECT lh FROM LoginHistory lh LEFT JOIN FETCH lh.user ORDER BY lh.loginTime DESC, lh.id DESC")
    java.util.List<LoginHistory> findAllForAdminList();

    @Query("SELECT lh FROM LoginHistory lh LEFT JOIN FETCH lh.user WHERE lh.user.id = :userId ORDER BY lh.loginTime DESC, lh.id DESC")
    java.util.List<LoginHistory> findByUserIdForAdminList(@Param("userId") Long userId);

    @Query("""
            SELECT lh FROM LoginHistory lh
            LEFT JOIN FETCH lh.user loginUser
            WHERE (:userId IS NULL OR loginUser.id = :userId)
              AND (:loginStatus IS NULL OR lh.loginStatus = :loginStatus)
              AND (:fromDateTime IS NULL OR lh.loginTime >= :fromDateTime)
              AND (:toDateTime IS NULL OR lh.loginTime < :toDateTime)
              AND (
                    :keyword IS NULL
                    OR LOWER(COALESCE(lh.attemptedUsername, '')) LIKE :keyword
                    OR LOWER(COALESCE(lh.ipAddress, '')) LIKE :keyword
                    OR LOWER(COALESCE(lh.sessionId, '')) LIKE :keyword
                    OR LOWER(COALESCE(lh.userAgent, '')) LIKE :keyword
                    OR LOWER(COALESCE(lh.failureReason, '')) LIKE :keyword
                    OR LOWER(COALESCE(loginUser.firstName, '')) LIKE :keyword
                    OR LOWER(COALESCE(loginUser.lastName, '')) LIKE :keyword
                    OR LOWER(COALESCE(loginUser.email, '')) LIKE :keyword
                    OR LOWER(COALESCE(loginUser.mobile, '')) LIKE :keyword
              )
            ORDER BY lh.loginTime DESC, lh.id DESC
            """)
    List<LoginHistory> findForAdminListFilters(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("loginStatus") LoginStatus loginStatus,
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime);

    Optional<LoginHistory> findTopByUserOrderByLoginTimeDesc(Users user);

    Optional<LoginHistory> findTopByUserAndLoginStatusAndLogoutTimeIsNullOrderByLoginTimeDesc(Users user, LoginStatus loginStatus);

    Optional<LoginHistory> findTopBySessionIdAndLoginStatusAndLogoutTimeIsNullOrderByLoginTimeDesc(String sessionId, LoginStatus loginStatus);
}
