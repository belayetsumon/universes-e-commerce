/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.product.ripository;

import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.model.enumvalue.Status;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author User
 */
public interface ProductcategoryRepository extends JpaRepository<Productcategory, Long> {

    List<Productcategory> findByStatus(Status status);
    
    List<Productcategory> findByStatusAndParentIsNull(Status status);
    
    Productcategory  findBySlug(String slug);

}
