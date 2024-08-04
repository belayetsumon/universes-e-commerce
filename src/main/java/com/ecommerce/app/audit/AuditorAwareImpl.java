/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.audit;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author User
 */
class AuditorAwareImpl implements AuditorAware<String> {

    @Autowired
    UsersRepository usersRepository;

    @Override
    public Optional<String> getCurrentAuditor() {
        //return Optional.of("anonymous");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {

            return Optional.of("anonymous");
        } else {

            // Users users = usersRepository.findByEmail(auth.getName());
            return Optional.of(auth.getName());
        }
    }

}
