/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.product.services;

import com.ecommerce.app.product.model.ProductSize;
import com.ecommerce.app.product.ripository.ProductSizeRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class ProductSizeService {

    @Autowired
    private ProductSizeRepository repository;

    public ProductSizeService(ProductSizeRepository repository) {
        this.repository = repository;
    }

    public List<ProductSize> findAll() {
        return repository.findAll();
    }

    public ProductSize save(ProductSize productSize) {
        return repository.save(productSize);
    }

    public Optional<ProductSize> findById(Long id) {
        return repository.findById(id);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public boolean existsByName(String name, Long excludeId) {
        return repository.findByName(name)
                .filter(size -> !size.getId().equals(excludeId))
                .isPresent();
    }
}
