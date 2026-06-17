/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.module.user.ripository;

import com.ecommerce.app.module.user.model.LoginHistory;
import com.ecommerce.app.module.user.model.LoginStatus;
import com.ecommerce.app.module.user.model.Users;
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

    Optional<LoginHistory> findTopByUserOrderByLoginTimeDesc(Users user);

    Optional<LoginHistory> findTopByUserAndLoginStatusAndLogoutTimeIsNullOrderByLoginTimeDesc(Users user, LoginStatus loginStatus);

    Optional<LoginHistory> findTopBySessionIdAndLoginStatusAndLogoutTimeIsNullOrderByLoginTimeDesc(String sessionId, LoginStatus loginStatus);
}
