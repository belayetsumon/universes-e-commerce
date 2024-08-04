/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.user.ripository;



import com.ecommerce.app.module.user.model.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;



/**
 *
 * @author Md Belayet Hossin
 */
public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);
    
    

    Users findByMobile(String mobile);
    
    Users findByReferralcode(String referralcode);
    
    
    List<Users> findByRole(Role role);

    List<Users> findByRoleAndStatusOrderByIdDesc(Role role, Status status);

    Users findByEmailAndStatus(String email, Status status);

    Users findByIdAndStatus(Long id, Status status);

    List<Users> findByStatus(Status status);

    List<Users> findByStatusAndProfileImageNotNullOrderByIdDesc(Status status, Pageable pageable);
}
