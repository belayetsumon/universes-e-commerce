/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.product.ripository;

import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.product.model.Productsubcategory;
import com.ecommerce.app.model.enumvalue.Status;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author User
 */
public interface ProductsubcategoryRepository extends JpaRepository<Productsubcategory, Long> {

    List<Productsubcategory> findByStatus(Status status);

    List<Productsubcategory> findByProductcategory(Productcategory productcategory);

    List<Productsubcategory> findByProductcategoryAndStatusOrderByName(String slug, Status status);
}
