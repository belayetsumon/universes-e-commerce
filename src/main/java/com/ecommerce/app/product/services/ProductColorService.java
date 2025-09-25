/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.product.services;

import com.ecommerce.app.product.model.ProductColor;
import com.ecommerce.app.product.ripository.ProductColorRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class ProductColorService {

    @Autowired
    private ProductColorRepository repository;

    public ProductColorService(ProductColorRepository repository) {
        this.repository = repository;
    }

    public List<ProductColor> findAll() {
        return repository.findAll();
    }

    public ProductColor save(ProductColor productColor) {
        return repository.save(productColor);
    }

    public Optional<ProductColor> findById(Long id) {
        return repository.findById(id);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public boolean existsByName(String name, Long excludeId) {
        return repository.findByName(name)
                .filter(color -> !color.getId().equals(excludeId))
                .isPresent();
    }
}
