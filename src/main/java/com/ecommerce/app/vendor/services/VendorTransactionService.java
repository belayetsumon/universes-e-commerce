/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.vendor.services;

import com.ecommerce.app.vendor.model.VendorTransaction;
import com.ecommerce.app.vendor.model.VendorTransactionStatusEnum;
import com.ecommerce.app.vendor.model.VendorTransactionTypeEnum;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
public class VendorTransactionService {

    @PersistenceContext
    private EntityManager em;

    public List<Map<String, Object>> findTransactions(
            Long vendorId,
            String fromDateStr,
            String toDateStr,
            VendorTransactionStatusEnum statusStr,
            VendorTransactionTypeEnum typeStr,
            String salesOrderStr
    ) {
        // ✅ Parse date strings safely
        LocalDateTime fromDateTime;
        LocalDateTime toDateTime;

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (fromDateStr != null && !fromDateStr.isBlank()) {
            LocalDate fromDate = LocalDate.parse(fromDateStr, dateFormatter);
            fromDateTime = fromDate.atStartOfDay();
        } else {
            fromDateTime = LocalDate.now().minusYears(1).atStartOfDay();
        }

        if (toDateStr != null && !toDateStr.isBlank()) {
            LocalDate toDate = LocalDate.parse(toDateStr, dateFormatter);
            toDateTime = toDate.atTime(LocalTime.MAX);
        } else {
            toDateTime = LocalDateTime.now();
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<VendorTransaction> root = cq.from(VendorTransaction.class);

        // ✅ Clean unique aliases
        cq.multiselect(
                root.get("id").alias("id"),
                root.get("vendor").get("id").alias("vendorId"),
                root.get("vendor").get("companyName").alias("companyName"),
                root.get("vendor").get("email").alias("email"),
                root.get("vendor").get("phone").alias("phone"),
                root.get("salesOrder").get("id").alias("salesOrderId"),
                root.get("status").alias("status"),
                root.get("transactionType").alias("transactionType"),
                root.get("amount").alias("amount"),
                root.get("description").alias("description"),
                root.get("created").alias("created"),
                root.get("modified").alias("modified")
        );

        List<Predicate> predicates = new ArrayList<>();

        // ✅ Vendor ID filter
        if (vendorId != null && vendorId > 0) {
            predicates.add(cb.equal(root.get("vendor").get("id"), vendorId));
        }

        // ✅ Date range filter
        if ((fromDateStr != null && !fromDateStr.isBlank())
                || (toDateStr != null && !toDateStr.isBlank())) {

            predicates.add(cb.between(root.get("created"), fromDateTime, toDateTime));
        }

        // ✅ Status filter
        // ✅ Status filter
        if (statusStr != null) {

            predicates.add(cb.equal(root.get("status"), statusStr));
        }

// ✅ Transaction type filter
        if (typeStr != null) {

            predicates.add(cb.equal(root.get("transactionType"), typeStr));
        }

        // ✅ Sales Order filter
        if (salesOrderStr != null && !salesOrderStr.isBlank()) {
            try {
                Long salesOrderId = Long.parseLong(salesOrderStr);
                predicates.add(cb.equal(root.get("salesOrder").get("id"), salesOrderId));
            } catch (NumberFormatException e) {
                // Optionally: handle invalid ID string
                throw new IllegalArgumentException("Invalid sales order ID: " + salesOrderStr);
            }
        }

        cq.where(predicates.toArray(new Predicate[0]));

        // ✅ Order by ID DESC
        cq.orderBy(cb.desc(root.get("id")));

        // ✅ Execute query (no pagination here)
        List<Tuple> tuples = em.createQuery(cq).getResultList();

        // ✅ Tuple → List<Map>
        return tuples.stream().map(tuple -> {
            Map<String, Object> map = new HashMap<>();
            for (TupleElement<?> elem : tuple.getElements()) {
                map.put(elem.getAlias(), tuple.get(elem));
            }
            return map;
        }).toList();
    }

}
