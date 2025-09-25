/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.product.ripository;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductStatusEnum;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author User
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

//    List<Product> findByProductsubcategoryOrderByIdDesc(Productsubcategory productsubcategory);
    List<Product> findByUserIdOrderByIdDesc(Users userid);

    List<Product> findByUserIdOrderByIdDesc(Users userid, Pageable pageable);

    List<Product> findByUserIdAndStatusOrderByIdDesc(Users userid, ProductStatusEnum status);

    List<Product> findByStatusOrderByIdDesc(ProductStatusEnum status);

    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.productcategory.id IN "
            + "(SELECT c.id FROM Productcategory c WHERE c.parent.id = :categoryId OR c.id = :categoryId)"
            + "ORDER BY p.id DESC")
    List<Product> findActiveProductsByCategoryOrChildren(@Param("categoryId") Long categoryId);
    
   

}
