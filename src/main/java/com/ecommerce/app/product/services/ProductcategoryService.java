/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.product.services;

import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class ProductcategoryService {

    @Autowired
    ProductcategoryRepository productcategoryRepository;
// Method to fetch the root nodes and build the tree recursively

    public List<Productcategory> getRootNodes() {
        List<Productcategory> rootNodes = productcategoryRepository.findByParentIsNull();
        buildTree(rootNodes);
        return rootNodes;
    }

    // Recursive method to build the tree
    private void buildTree(List<Productcategory> nodes) {
        for (Productcategory node : nodes) {
            List<Productcategory> children = productcategoryRepository.findAll().stream()
                    .filter(n -> n.getParent() != null && n.getParent().getId().equals(node.getId()))
                    .collect(Collectors.toList());
            node.setChildren(children);
            buildTree(children);  // Recursively build for child nodes
        }
    }
}
