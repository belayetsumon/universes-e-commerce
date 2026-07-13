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
//public interface UsersRepository extends JpaRepository<Users, Long> {
//
//    Optional<Users> findByEmail(String email);
//
//    Users findByMobile(String mobile);
//
//    List<Users> findByRole(Role role);
//
//    List<Users> findByParent(Users users);
//
//    List<Users> findByRoleAndStatusOrderByIdDesc(Role role, Status status);
//
//    Users findByEmailAndStatus(String email, Status status);
//
//    Users findByIdAndStatus(Long id, Status status);
//
//    List<Users> findByStatus(Status status);
//
//    @EntityGraph(attributePaths = {"roles", "roles.privileges"})
//    @Query("SELECT u FROM Users u WHERE u.id = :userId")
//    Users findUserWithRolesAndPrivileges(@Param("userId") Long userId);
//
////    @EntityGraph(attributePaths = {"roles", "roles.privileges"})
////    List<Users> findAllUsers();
//    // EntityGraph method - SINGLE QUERY for all roles and privileges
//    @EntityGraph(attributePaths = {"role", "role.privilege"})
//    @Query("SELECT u FROM Users u WHERE u.email = :email AND u.status = :status")
//    Users findByEmailAndStatusWithRolesAndPrivileges(@Param("email") String email, @Param("status") Status status);
//
//    //List<Users> findByStatusAndProfileImageNotNullOrderByIdDesc(Status status, Pageable pageable);
//}
public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);

    Users findByMobile(String mobile);

    Optional<Users> findOptionalByMobile(String mobile);

    List<Users> findByRole(Role role);

    List<Users> findByParent(Users users);

    List<Users> findByRoleAndStatusOrderByIdDesc(Role role, Status status);

    Users findByEmailAndStatus(String email, Status status);

    Users findByIdAndStatus(Long id, Status status);

    List<Users> findByStatus(Status status);

    List<Users> findByUserTypeAndStatusOrderByIdDesc(UserType userType, Status status);

    @Query("""
            select u
            from Users u
            where u.userType in :userTypes
              and u.status = :status
            order by u.id desc
            """)
    List<Users> findByUserTypeInAndStatusOrderByIdDesc(
            @Param("userTypes") Collection<UserType> userTypes,
            @Param("status") Status status);

    @Query("""
            select u
            from Users u
            where u.id in :ids
              and u.userType = :userType
              and u.status = :status
            order by u.id desc
            """)
    List<Users> findByIdsAndUserTypeAndStatus(
            @Param("ids") Collection<Long> ids,
            @Param("userType") UserType userType,
            @Param("status") Status status);

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
