/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.ripository;

import com.ecommerce.app.model.Testimonial;
import com.ecommerce.app.model.enumvalue.Status;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author User
 */
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {
  
    List<Testimonial> findByStatusOrderByIdDesc(Status status,Pageable pageable);
}
