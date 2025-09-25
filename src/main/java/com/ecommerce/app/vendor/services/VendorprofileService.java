/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.vendor.services;

import com.ecommerce.app.vendor.model.VendorStatusEnum;
import com.ecommerce.app.vendor.model.Vendorprofile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class VendorprofileService {

    @PersistenceContext
    private EntityManager em;

    public List<Map<String, Object>> all_vendor_list(
            VendorStatusEnum status,
            String vendorCode,
            String phone
    ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Vendorprofile> root = cq.from(Vendorprofile.class);
        // Collect predicates dynamically
        List<Predicate> predicates = new ArrayList<>();

        if (status != null) {
            predicates.add(cb.equal(root.get("vendorStatusEnum"), status));
        }
        if (vendorCode != null && !vendorCode.isEmpty()) {
            predicates.add(cb.equal(root.get("vendorCode"), vendorCode));
        }
        if (phone != null && !phone.isEmpty()) {
            predicates.add(cb.equal(root.get("phone"), phone));
        }

        cq.multiselect(
                root.get("id").alias("id"),
                root.get("vendorCode").alias("vendorCode"),
                root.get("companyName").alias("companyName"),
                root.get("firstName").alias("firstName"),
                root.get("lastName").alias("lastName"),
                root.get("designation").alias("designation"),
                root.get("phone").alias("phone"),
                root.get("email").alias("email"),
                root.get("vendorStatusEnum").alias("status"),
                root.get("created").alias("createdDate"),
                root.get("createdBy").alias("createdBy"),
                root.get("modifiedBy").alias("modifiedBy"),
                root.get("modified").alias("modified")
        );

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        cq.orderBy(cb.desc(root.get("id")));
        List<Tuple> resultList = em.createQuery(cq).getResultList();

        // Avoid streams, use loop instead
        List<Map<String, Object>> finalList = new ArrayList<>();
        for (Tuple tuple : resultList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", tuple.get("id"));
            map.put("vendorCode", tuple.get("vendorCode"));
            map.put("companyName", tuple.get("companyName"));
            map.put("firstName", tuple.get("firstName"));
            map.put("lastName", tuple.get("lastName"));
            map.put("designation", tuple.get("designation"));
            map.put("phone", tuple.get("phone"));
            map.put("email", tuple.get("email"));
            map.put("status", tuple.get("status"));
            map.put("createdDate", tuple.get("createdDate"));
            map.put("createdBy", tuple.get("createdBy"));
            map.put("modifiedBy", tuple.get("modifiedBy"));
            map.put("modified", tuple.get("modified"));
            finalList.add(map);
        }

        return finalList;
    }

    public Map<String, Object> findVendorById(Long id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Vendorprofile> root = cq.from(Vendorprofile.class);

        // Select all fields you care about
        cq.multiselect(
                root.get("id").alias("id"),
                root.get("vendorCode").alias("vendorCode"),
                root.get("companyName").alias("companyName"),
                root.get("userId").alias("userId"),
                root.get("firstName").alias("firstName"),
                root.get("lastName").alias("lastName"),
                root.get("designation").alias("designation"),
                root.get("phone").alias("phone"),
                root.get("email").alias("email"),
                root.get("address").alias("address"),
                root.get("description").alias("description"),
                root.get("vendorStatusEnum").alias("vendorStatusEnum"),
                root.get("createdBy").alias("createdBy"),
                root.get("created").alias("created"),
                root.get("modifiedBy").alias("modifiedBy"),
                root.get("modified").alias("modified")
        );

        cq.where(cb.equal(root.get("id"), id));

        Tuple tuple = em.createQuery(cq).getSingleResult();

        // Map manually (no loop)
        Map<String, Object> map = new HashMap<>();
        map.put("id", tuple.get("id"));
        map.put("vendorCode", tuple.get("vendorCode"));
        map.put("companyName", tuple.get("companyName"));
        map.put("userId", tuple.get("userId"));
        map.put("firstName", tuple.get("firstName"));
        map.put("lastName", tuple.get("lastName"));
        map.put("designation", tuple.get("designation"));
        map.put("phone", tuple.get("phone"));
        map.put("email", tuple.get("email"));
        map.put("address", tuple.get("address"));
        map.put("description", tuple.get("description"));
        map.put("vendorStatusEnum", tuple.get("vendorStatusEnum"));
        map.put("createdBy", tuple.get("createdBy"));
        map.put("created", tuple.get("created"));
        map.put("modifiedBy", tuple.get("modifiedBy"));
        map.put("modified", tuple.get("modified"));

        return map;
    }

}
