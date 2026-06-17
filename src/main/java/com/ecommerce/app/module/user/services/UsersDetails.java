/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.user.services;

import com.ecommerce.app.module.user.model.*;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.vendor.user.model.UserVendorRole;
import com.ecommerce.app.vendor.user.model.VendorPrivilege;
import com.ecommerce.app.vendor.user.repository.UserVendorRoleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Md Belayet Hossin
 */
@Service
public class UsersDetails implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UserVendorRoleRepository userVendorRoleRepository;

//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//
//        Users user = usersRepository.findByEmailAndStatus(username, Status.Active);
//        if (user == null) {
//            throw new UsernameNotFoundException("Active user not found for email: " + username);
//        }
//
//        Set<Role> userRoles = user.getRole();
//        if (userRoles == null || userRoles.isEmpty()) {
//            throw new UsernameNotFoundException("No role assigned for user: " + username);
//        }
//
//        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
//
//        for (Role role : userRoles) {
//            if (role == null) {
//                continue;
//            }
//
//            Set<Privilege> privileges = role.getPrivilege();
//            if (privileges == null || privileges.isEmpty()) {
//                continue;
//            }
//
//            for (Privilege privilege : privileges) {
//                if (privilege != null && privilege.getSlug() != null && !privilege.getSlug().isBlank()) {
//                    grantedAuthorities.add(new SimpleGrantedAuthority(privilege.getSlug()));
//                }
//            }
//        }
//
//        List<UserVendorRole> vendorAssignments = userVendorRoleRepository.findAllByUsers(user);
//        for (UserVendorRole assignment : vendorAssignments) {
//            if (assignment == null
//                    || assignment.getVendor() == null
//                    || assignment.getVendor().getId() == null
//                    || assignment.getVendorRole() == null
//                    || assignment.getVendorRole().getVendorPrivilege() == null) {
//                continue;
//            }
//
//            Long vendorId = assignment.getVendor().getId();
//            for (VendorPrivilege privilege : assignment.getVendorRole().getVendorPrivilege()) {
//                if (privilege == null || privilege.getSlug() == null || privilege.getSlug().isBlank()) {
//                    continue;
//                }
//
//                grantedAuthorities.add(new SimpleGrantedAuthority(
//                        "VENDOR_" + vendorId + ":" + privilege.getSlug()
//                ));
//            }
//        }
//
//        if (grantedAuthorities.isEmpty()) {
//            throw new UsernameNotFoundException("No privileges assigned for user: " + username);
//        }
//
//        return new org.springframework.security.core.userdetails.User(
//                user.getEmail(),
//                user.getPassword(),
//                grantedAuthorities
//        );
//    }
    @Override
    @Transactional(readOnly = true)  // CRITICAL: Add this!
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Option 1: Use EntityGraph (recommended)
        Users user = usersRepository.findByEmailAndStatusWithRolesAndPrivileges(username, Status.Active);

        if (user == null) {
            throw new UsernameNotFoundException("Active user not found for email: " + username);
        }

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

        // With @Transactional, these will use the already fetched data
        for (Role role : user.getRole()) {
            if (role == null) {
                continue;
            }

            for (Privilege privilege : role.getPrivilege()) {
                if (privilege != null && privilege.getSlug() != null && !privilege.getSlug().isBlank()) {
                    grantedAuthorities.add(new SimpleGrantedAuthority(privilege.getSlug()));
                }
            }
        }

        // This still might cause N+1 - we'll fix it too
        List<UserVendorRole> vendorAssignments = userVendorRoleRepository.findAllByUsersWithVendorPrivileges(user);

        for (UserVendorRole assignment : vendorAssignments) {
            if (assignment == null || assignment.getVendor() == null
                    || assignment.getVendorRole() == null || assignment.getVendorRole().getVendorPrivilege() == null) {
                continue;
            }

            Long vendorId = assignment.getVendor().getId();
            for (VendorPrivilege privilege : assignment.getVendorRole().getVendorPrivilege()) {
                if (privilege != null && privilege.getSlug() != null && !privilege.getSlug().isBlank()) {
                    grantedAuthorities.add(new SimpleGrantedAuthority(
                            "VENDOR_" + vendorId + ":" + privilege.getSlug()
                    ));
                }
            }
        }

        if (grantedAuthorities.isEmpty()) {
            throw new UsernameNotFoundException("No privileges assigned for user: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                grantedAuthorities
        );
    }
}
