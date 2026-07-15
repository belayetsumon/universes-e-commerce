/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.product.ripository;

import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.product.model.ProductStatusEnum;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author User
 */
public interface ProductcategoryRepository extends JpaRepository<Productcategory, Long>, JpaSpecificationExecutor<Productcategory> {

    @Override
    @EntityGraph(attributePaths = "parent")
    Page<Productcategory> findAll(Specification<Productcategory> specification, Pageable pageable);

    List<Productcategory> findByStatus(ProductStatusEnum status);
    
    List<Productcategory> findByStatusAndFeaturedCat(ProductStatusEnum status,Boolean featuredCat);
    
    List<Productcategory> findByStatusAndParentIsNull(ProductStatusEnum status);
    
     List<Productcategory> findByStatusAndParent(ProductStatusEnum status, Productcategory parent);
     
    Productcategory  findBySlug(String slug);

    Optional<Productcategory> findByUuid(String uuid);

    @Query("""
           SELECT c FROM Productcategory c
           LEFT JOIN FETCH c.parent
           WHERE c.status = :status
             AND LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))
           ORDER BY c.orderno ASC, c.name ASC
           """)
    List<Productcategory> findPublicCategorySuggestions(
            @Param("query") String query,
            @Param("status") ProductStatusEnum status,
            Pageable pageable);
     
      
    /// new 
    
    
    
    // Fetch root categories (those with no parent)
    List<Productcategory> findByParentIsNull();

    long countByStatus(ProductStatusEnum status);

    long countByParentIsNull();

    long countByFeaturedCatTrue();

    // Use EntityGraph to fetch categories with all their children
//    @EntityGraph(attributePaths = "children")
//    List<Productcategory> findAllWithChildren();

    // Alternatively, use a custom query to fetch categories with recursive relationships
    @Query("SELECT c FROM Productcategory c LEFT JOIN FETCH c.children WHERE c.parent IS NULL")
    List<Productcategory> findRootCategoriesWithChildren();

}
