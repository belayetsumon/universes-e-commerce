/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.user.services;

import com.ecommerce.app.module.user.model.LoginEvent;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.LoginEventRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class LoginEventService {

    @Autowired
    private LoginEventRepository loginEventRepository;

    @Autowired
    UsersRepository usersRepository;

    public void logLoginTime(String username) {

        Users user = usersRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LoginEvent loginEvent = new LoginEvent();
        loginEvent.setUser(user);
        loginEvent.setLoginTime(LocalDateTime.now());
        loginEventRepository.save(loginEvent);
    }

    public void logLogoutTime(String username) {

        Users user = usersRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LoginEvent loginEvent = loginEventRepository.findTopByUserOrderByLoginTimeDesc(user)
                .orElseThrow(() -> new RuntimeException("Login event not found"));
        loginEvent.setLogoutTime(LocalDateTime.now());
        loginEventRepository.save(loginEvent);
    }

}
