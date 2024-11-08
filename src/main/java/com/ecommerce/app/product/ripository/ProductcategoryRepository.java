/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.product.ripository;

import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.product.model.ProductStatusEnum;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author User
 */
public interface ProductcategoryRepository extends JpaRepository<Productcategory, Long> {

    List<Productcategory> findByStatus(ProductStatusEnum status);
    
    List<Productcategory> findByStatusAndParentIsNull(ProductStatusEnum status);
    
    Productcategory  findBySlug(String slug);

}
