/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductDimension;
import com.ecommerce.app.product.ripository.ProductDimensionRepository;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.services.ProductDimensionService;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/vendor_productdimension")
public class Vendor_ProductDimensionController {

    @Autowired
    ProductDimensionService productDimensionService;

    @Autowired
    private ProductDimensionRepository repository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private VendorUserContext vendorUserContext;

    @GetMapping("/add/{pid}")
    public String add(Model model, @PathVariable Long pid) {
        Product product = productRepository.findById(pid).orElse(null);
        if (!isOwnedByActiveVendor(product)) {
            model.addAttribute("errorMessage", "Product not found for the active vendor.");
            return "vendor/product/dimension/dimension";
        }

        ProductDimension productDimension = new ProductDimension();
        productDimension.setProduct(product);
        model.addAttribute("productDimension", productDimension);
        return "vendor/product/dimension/dimension";
    }

    @PostMapping("/save")
    @ResponseBody
    public String save(@Valid ProductDimension productDimension, HttpServletResponse response) {
        if (!isOwnedByActiveVendor(productDimension != null ? productDimension.getProduct() : null)) {
            return "<div class='alert alert-danger'>Product not found for the active vendor.</div>";
        }

        repository.save(productDimension);

        response.setHeader("HX-Refresh", "true");
        return "";
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id) {
        Optional<ProductDimension> productDimensionopt = repository.findById(id);

        ProductDimension productDimension = productDimensionopt.orElse(null);
        if (productDimension == null || !isOwnedByActiveVendor(productDimension.getProduct())) {
            model.addAttribute("errorMessage", "Product dimension not found for the active vendor.");
            return "vendor/product/dimension/dimension";
        }

        model.addAttribute("productDimension", productDimension);

        return "/vendor/product/dimension/dimension";
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public String deleteUnit(@PathVariable Long id, HttpServletResponse response) {
        String message;
        String messageType;
        Optional<ProductDimension> existing = repository.findById(id);
        if (existing.isEmpty()) {
            message = "Error: Item not found!";
            messageType = "danger"; // Error message type
        } else if (!isOwnedByActiveVendor(existing.get().getProduct())) {
            message = "Error: Item not found for the active vendor!";
            messageType = "danger";
        } else {
            repository.deleteById(id);
            message = "Deleted successfully!";
            messageType = "success"; // Success message type
        }
        response.setHeader("HX-Refresh", "true");
        return "<div id='messageContainer' class='alert alert-" + messageType + "'>" + message + "</div>";
    }

    private boolean isOwnedByActiveVendor(Product product) {
        if (product != null && product.getId() != null && product.getVendorprofile() == null) {
            product = productRepository.findById(product.getId()).orElse(product);
        }
        Vendorprofile vendorprofile = vendorUserContext.getActiveVendor();
        return product != null
                && vendorprofile != null
                && vendorprofile.getId() != null
                && product.getVendorprofile() != null
                && Objects.equals(product.getVendorprofile().getId(), vendorprofile.getId());
    }

}
