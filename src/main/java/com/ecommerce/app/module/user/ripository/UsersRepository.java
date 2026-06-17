/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.user.ripository;

import com.ecommerce.app.module.user.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author Md Belayet Hossin
 */
public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);

    Users findByMobile(String mobile);

    List<Users> findByRole(Role role);

    List<Users> findByParent(Users users);

    List<Users> findByRoleAndStatusOrderByIdDesc(Role role, Status status);

    Users findByEmailAndStatus(String email, Status status);

    Users findByIdAndStatus(Long id, Status status);

    List<Users> findByStatus(Status status);

    @EntityGraph(attributePaths = {"roles", "roles.privileges"})
    @Query("SELECT u FROM Users u WHERE u.id = :userId")
    Users findUserWithRolesAndPrivileges(@Param("userId") Long userId);

//    @EntityGraph(attributePaths = {"roles", "roles.privileges"})
//    List<Users> findAllUsers();
    // EntityGraph method - SINGLE QUERY for all roles and privileges
    @EntityGraph(attributePaths = {"role", "role.privilege"})
    @Query("SELECT u FROM Users u WHERE u.email = :email AND u.status = :status")
    Users findByEmailAndStatusWithRolesAndPrivileges(@Param("email") String email, @Param("status") Status status);

    //List<Users> findByStatusAndProfileImageNotNullOrderByIdDesc(Status status, Pageable pageable);
}
