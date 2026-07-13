/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.product.controller;

import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductImage;
import com.ecommerce.app.product.ripository.ProductImageRepository;
import com.ecommerce.app.product.services.ProductImageService;
import com.ecommerce.app.product.services.ProductImageStorageService;
import com.ecommerce.app.services.StorageProperties;
import java.io.File;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/productimage")
public class ProductImageController {

    @Autowired
    StorageProperties properties;

    @Autowired
    ProductImageRepository productImageRepository;

    @Autowired
    ProductImageService productImageService;

    @Autowired
    ProductImageStorageService productImageStorageService;

    @PostMapping("/upload")
    public ResponseEntity<String> save(@ModelAttribute ProductImage productImage,
            @RequestParam("product") Product product,
            @RequestParam("productimg") MultipartFile productimg) {

        try {
            String filename = productImageStorageService.storeProductImage(productimg);

            // Save file details to the database
            productImage.setProduct(product);
            productImage.setProductImageName(filename);
            productImageRepository.save(productImage);

            // Return success message as JSON response
            return ResponseEntity.ok("<div class='alert alert-success'>Successfully uploaded: " + filename + "</div>"
            );

        } catch (Exception e) {
            // Log error and return error message
            System.out.println("Error saving file: " + e.getMessage());
            return ResponseEntity.ok("<div class='alert alert-danger'>Error saving file: " + e.getMessage() + "</div>");
        }
    }

    

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long productId) {
        try {
            // Find the product by ID
            Optional<ProductImage> productImage = productImageService.findById(productId);

            if (productImage.isPresent()) {
                // Get the image associated with the product
                String imageName = productImage.get().getProductImageName();

                // Attempt to delete the product from the database
                boolean isDeleted = productImageService.deleteProductById(productId);

                if (isDeleted) {
                    // Delete the image file from the server
                    File imageFile = new File(properties.getRootPath() + File.separator + imageName);
                    if (imageFile.exists()) {
                        boolean imageDeleted = imageFile.delete();
                        if (!imageDeleted) {
                            return ResponseEntity.ok("<div class='alert alert-warning'>Product deleted, but the image could not be deleted.</div>");
                        }
                    }

                    // Return success message
                    return ResponseEntity.ok("<div class='alert alert-success'>Product and image deleted successfully.</div>");
                } else {
                    return ResponseEntity.ok("<div class='alert alert-danger'>Product not found or already deleted.</div>");
                }
            } else {
                return ResponseEntity.ok("<div class='alert alert-danger'>Product not found.</div>");
            }
        } catch (Exception e) {
            return ResponseEntity.ok("<div class='alert alert-danger'>Error deleting product and image: " + e.getMessage() + "</div>");
        }
    }

    @RequestMapping("/list/{id}")
    public String list(Model model, @PathVariable Long id) {
        model.addAttribute("img_list", productImageRepository.findByProductIdOrderByIdDesc(id));
        return "product/fragments/productImageTable::productImageTable";
    }

}
