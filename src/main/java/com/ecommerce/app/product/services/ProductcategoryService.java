/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.product.services;

import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    EntityManager entityManager;

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

    public List<Map<String, Object>> findActiveCategoryDropDown() {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Productcategory> root = cq.from(Productcategory.class);

        // LEFT JOIN parent category
        Join<Productcategory, Productcategory> parentJoin
                = root.join("parent", JoinType.LEFT);

        cq.multiselect(
                root.get("id").alias("id"),
                root.get("uuid").alias("uuid"),
                root.get("name").alias("name")
        );

        // WHERE status = ACTIVE
        cq.where(cb.equal(root.get("status"), ProductStatusEnum.Active));

        List<Tuple> resultList = entityManager.createQuery(cq).getResultList();

        // Convert Tuple -> Map<String, Object>
        List<Map<String, Object>> finalList = new ArrayList<>();

        for (Tuple t : resultList) {
            Map<String, Object> map = new HashMap<>();
            for (TupleElement<?> elem : t.getElements()) {
                map.put(elem.getAlias(), t.get(elem.getAlias()));
            }
            finalList.add(map);
        }

        return finalList;
    }

}
