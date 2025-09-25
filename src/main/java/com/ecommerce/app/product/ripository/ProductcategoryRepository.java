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
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author User
 */
public interface ProductcategoryRepository extends JpaRepository<Productcategory, Long> {

    List<Productcategory> findByStatus(ProductStatusEnum status);
    
    List<Productcategory> findByStatusAndFeaturedCat(ProductStatusEnum status,Boolean featuredCat);
    
    List<Productcategory> findByStatusAndParentIsNull(ProductStatusEnum status);
    
     List<Productcategory> findByStatusAndParent(ProductStatusEnum status, Productcategory parent);
    
    Productcategory  findBySlug(String slug);
    
     
    /// new 
    
    
    
    // Fetch root categories (those with no parent)
    List<Productcategory> findByParentIsNull();

    // Use EntityGraph to fetch categories with all their children
//    @EntityGraph(attributePaths = "children")
//    List<Productcategory> findAllWithChildren();

    // Alternatively, use a custom query to fetch categories with recursive relationships
    @Query("SELECT c FROM Productcategory c LEFT JOIN FETCH c.children WHERE c.parent IS NULL")
    List<Productcategory> findRootCategoriesWithChildren();

}
