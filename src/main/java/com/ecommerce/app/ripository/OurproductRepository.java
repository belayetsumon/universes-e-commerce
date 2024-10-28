/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.ripository;

import com.ecommerce.app.model.Ourproduct;
import com.ecommerce.app.model.enumvalue.Status;
import com.ecommerce.app.product.model.Productcategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author User
 */
public interface OurproductRepository extends JpaRepository<Ourproduct, Long> {
    
    List<Ourproduct> findByProductcategoryOrderByIdDesc(Productcategory productcategory);
    
    List<Ourproduct> findByStatusOrderByIdDesc(Status status);
    
    
}
