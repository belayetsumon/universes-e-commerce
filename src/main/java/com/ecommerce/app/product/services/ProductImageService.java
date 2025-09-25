/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.product.services;

import com.ecommerce.app.product.model.ProductImage;
import com.ecommerce.app.product.ripository.ProductImageRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class ProductImageService {

    @Autowired
    private ProductImageRepository productImageRepository;

    public Optional<ProductImage> findById(Long productId) {
        return productImageRepository.findById(productId);
    }

    public boolean deleteProductById(Long productId) {

        if (productImageRepository.existsById(productId)) {

            productImageRepository.deleteById(productId);

            return true;
        }
        return false;
    }
}
