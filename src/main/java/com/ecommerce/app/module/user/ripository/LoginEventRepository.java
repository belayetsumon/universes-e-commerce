/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.module.user.ripository;

import com.ecommerce.app.module.user.model.LoginEvent;
import com.ecommerce.app.module.user.model.Users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface LoginEventRepository extends JpaRepository<LoginEvent, Long> {

    Optional<LoginEvent> findTopByUserOrderByLoginTimeDesc(Users user);
}
