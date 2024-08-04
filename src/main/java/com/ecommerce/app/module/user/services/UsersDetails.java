/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.user.services;



import com.ecommerce.app.module.user.model.*;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 *
 * @author Md Belayet Hossin
 */
@Service
public class UsersDetails implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;

    //String governmentId= "123456";
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Users user = usersRepository.findByEmailAndStatus(username,Status.Active);
        
//        user.setEmail("sumon@gmail.com");
//        user.setPassword("$2a$10$hxQ4AeLpn0uy//SKeTvDl.2iZwWJEA2lM.z.ixuYMB0giBUbXUxrK");

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

        Set<Role> userrole = user.getRole();
//        for (Role role : userrole) {
//            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
//        }
        
            for (Role role : user.getRole()) {
            
            for( Privilege privilegelist : role.getPrivilege())
            {
               // grantedAuthorities.add(new SimpleGrantedAuthority("admin"));
            grantedAuthorities.add(new SimpleGrantedAuthority( privilegelist.getSlug()));
            }
            
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), grantedAuthorities);
    }

}
