/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.product.services;

import com.ecommerce.app.product.model.ProductVariants;
import com.ecommerce.app.product.ripository.ProductVariantsRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class ProductVariantsService {

    @Autowired
    private ProductVariantsRepository repository;

    public ProductVariantsService(ProductVariantsRepository repository) {
        this.repository = repository;
    }

    public List<ProductVariants> findAll() {
        return repository.findAll();
    }

    public List<ProductVariants> findById(Long id) {
        return repository.findByProduct_Id(id);

    }

    public ProductVariants save(ProductVariants variants) {
        return repository.save(variants);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
