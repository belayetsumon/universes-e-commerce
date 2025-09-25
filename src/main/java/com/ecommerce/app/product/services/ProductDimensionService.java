/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.product.services;

import com.ecommerce.app.product.model.ProductDimension;
import com.ecommerce.app.product.model.ProductVariants;
import com.ecommerce.app.product.ripository.ProductDimensionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class ProductDimensionService {

    @Autowired
    private ProductDimensionRepository repository;

    public List<ProductDimension> findAll() {
        return repository.findAll();
    }

    public ProductDimension findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<ProductDimension> findAllById(Long id) {
        return repository.findByProduct_Id(id);

    }

    public ProductDimension save(ProductDimension dimension) {
        return repository.save(dimension);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
