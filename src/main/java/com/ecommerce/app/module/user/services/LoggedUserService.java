/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.user.services;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author User
 */
@Service

public class LoggedUserService {
    
    @Autowired
    UsersRepository usersRepository;
    
    @Transactional
    public String activeUserName() {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Users users = usersRepository.findByEmail(auth.getName()).orElse(null);
        return users.getName();
    }
    
    public Long activeUserid() {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Users users = usersRepository.findByEmail(auth.getName()).orElse(null);
        return users.getId();
    }
    
}
