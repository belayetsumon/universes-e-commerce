/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.product.ripository;

import com.ecommerce.app.product.model.ProductDimension;
import com.ecommerce.app.product.model.ProductVariants;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface ProductDimensionRepository extends JpaRepository<ProductDimension, Long> {

    List<ProductDimension> findByProduct_Id(Long id);
}
