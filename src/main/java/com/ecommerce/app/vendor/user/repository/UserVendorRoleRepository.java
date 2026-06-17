/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.vendor.user.repository;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.model.UserVendorRole;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author libertyerp_local
 */
public interface UserVendorRoleRepository extends JpaRepository<UserVendorRole, Long> {

    List<UserVendorRole> findAllByVendor(Vendorprofile vendor);

    List<UserVendorRole> findAllByUsers(Users user);

    boolean existsByUsers_EmailAndVendor_Id(String email, Long vendorId);

    boolean existsByUsers_EmailAndVendor_IdAndVendorRole_Name(String email, Long vendorId, String roleName);

    @Query("""
            SELECT CASE WHEN COUNT(uvr) > 0 THEN true ELSE false END
            FROM UserVendorRole uvr
            WHERE LOWER(uvr.users.email) = LOWER(:email)
              AND uvr.vendor.id = :vendorId
              AND (
                    LOWER(uvr.vendorRole.name) = LOWER(:roleName)
                 OR LOWER(uvr.vendorRole.slug) = LOWER(:roleName)
              )
            """)
    boolean hasVendorRole(
            @Param("email") String email,
            @Param("vendorId") Long vendorId,
            @Param("roleName") String roleName
    );

    @Query("""
            SELECT CASE WHEN COUNT(uvr) > 0 THEN true ELSE false END
            FROM UserVendorRole uvr
            JOIN uvr.vendorRole vr
            JOIN vr.vendorPrivilege vp
            WHERE LOWER(uvr.users.email) = LOWER(:email)
              AND uvr.vendor.id = :vendorId
              AND LOWER(vp.slug) = LOWER(:privilegeSlug)
            """)
    boolean hasVendorPrivilege(
            @Param("email") String email,
            @Param("vendorId") Long vendorId,
            @Param("privilegeSlug") String privilegeSlug
    );

    // EntityGraph - SINGLE QUERY for vendor roles and privileges
    @EntityGraph(attributePaths = {"vendor", "vendorRole", "vendorRole.vendorPrivilege"})
    @Query("SELECT uvr FROM UserVendorRole uvr WHERE uvr.users = :user")
    List<UserVendorRole> findAllByUsersWithVendorPrivileges(@Param("user") Users user);

}
