/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.ripository;

import com.ecommerce.app.model.Ourservices;
import com.ecommerce.app.model.enumvalue.Status;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author User
 */
public interface OurservicesRepository extends JpaRepository<Ourservices, Long> {
    
    List<Ourservices> findByStatusOrderByIdDesc(Status staus);
    
}
