/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.vendor.services;

import com.ecommerce.app.vendor.model.VendorPayout;
import com.ecommerce.app.vendor.model.VendorPayoutStatusEnum;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class VendorPayoutService {

    @PersistenceContext
    private EntityManager em;

    public List<Map<String, Object>> AllVendorPayouts(
            String vendorCode,
            VendorPayoutStatusEnum status,
            String requestedFrom,
            String requestedTo
    ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<VendorPayout> root = cq.from(VendorPayout.class);

        Join<Object, Object> vendorJoin = root.join("vendor", JoinType.LEFT);
        Join<Object, Object> payoutMethodJoin = root.join("payoutMethod", JoinType.LEFT);

        cq.multiselect(
                root.get("id").alias("id"),
                vendorJoin.get("id").alias("vendorId"),
                vendorJoin.get("vendorCode").alias("vendorCode"),
                vendorJoin.get("companyName").alias("vendorName"),
                root.get("amount").alias("amount"),
                root.get("status").alias("status"),
                payoutMethodJoin.get("id").alias("payoutMethodId"),
                payoutMethodJoin.get("preferredMethod").alias("preferredMethod"),
                payoutMethodJoin.get("bankName").alias("bankName"),
                payoutMethodJoin.get("accountTitle").alias("accountTitle"),
                payoutMethodJoin.get("accountNumber").alias("accountNumber"),
                root.get("payoutReference").alias("payoutReference"),
                root.get("adminNote").alias("adminNote"),
                root.get("requestedAt").alias("requestedAt"),
                root.get("processedAt").alias("processedAt"),
                root.get("paidAt").alias("paidAt"),
                root.get("createdBy").alias("createdBy"),
                root.get("created").alias("created")
        );

        List<Predicate> predicates = new ArrayList<>();

        if (vendorCode != null && !vendorCode.isBlank()) {
            predicates.add(cb.equal(vendorJoin.get("vendorCode"), vendorCode));
        }

        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get("id")));

        List<Tuple> resultList = em.createQuery(cq).getResultList();

        List<Map<String, Object>> mappedResult = new ArrayList<>();
        for (Tuple t : resultList) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (TupleElement<?> element : t.getElements()) {
                row.put(element.getAlias(), t.get(element.getAlias()));
            }
            mappedResult.add(row);
        }

        return mappedResult;
    }

}
