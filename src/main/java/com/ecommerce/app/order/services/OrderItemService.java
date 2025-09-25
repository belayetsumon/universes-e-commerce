/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.order.services;

import com.ecommerce.app.order.model.OrderItem;
import com.ecommerce.app.order.model.SalesOrder;
import com.ecommerce.app.product.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderItemService {

    @PersistenceContext
    private EntityManager em;

    public List<Map<String, Object>> item_List_By_SalesOrder(Long id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();

        Root<OrderItem> orderItem = cq.from(OrderItem.class);
        Join<OrderItem, Product> product = orderItem.join("product", JoinType.LEFT); // assuming `OrderItem` has a `Product product` relationship

        cq.multiselect(
                orderItem.get("id").alias("id"),
                orderItem.get("salesOrder").get("id").alias("salesOrderId"),
                orderItem.get("vendorId").alias("vendorId"),
                orderItem.get("productid").alias("productid"),
                orderItem.get("quantity").alias("quantity"),
                orderItem.get("salesPrice").alias("salesPrice"),
                orderItem.get("discountRate").alias("discountRate"),
                orderItem.get("discountAmount").alias("discountAmount"),
                orderItem.get("marketPlaceCommissionAmount").alias("marketPlaceCommissionAmount"),
                orderItem.get("marketPlaceCommissionRate").alias("marketPlaceCommissionRate"),
                orderItem.get("vendorAmount").alias("vendorAmount"),
                orderItem.get("vatRate").alias("vatRate"),
                orderItem.get("vatAmount").alias("vatAmount"),
                orderItem.get("itemTotal").alias("itemTotal"),
                orderItem.get("createdBy").alias("createdBy"),
                orderItem.get("created").alias("created"),
                orderItem.get("modifiedBy").alias("modifiedBy"),
                orderItem.get("modified").alias("modified"),
                product.get("title").alias("title"),
                product.get("imageName").alias("imageName")
        );

        cq.where(cb.equal(orderItem.get("salesOrder").get("id"), id));
        cq.orderBy(cb.desc(orderItem.get("id")));

        List<Tuple> tuples = em.createQuery(cq).getResultList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Tuple tuple : tuples) {
            Map<String, Object> map = new HashMap<>();
            for (TupleElement<?> element : tuple.getElements()) {
                map.put(element.getAlias(), tuple.get(element.getAlias()));
            }
            result.add(map);
        }

        return result;
    }

    public BigDecimal getTotalQuantityBySalesOrder(Long id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<OrderItem> root = query.from(OrderItem.class);

        query.select(cb.sum(root.get("quantity")));
        query.where(cb.equal(root.get("salesOrder").get("id"), id));

        return em.createQuery(query).getSingleResult();
    }

    public BigDecimal getTotalSalesPriceBySalesOrder(Long id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<OrderItem> root = query.from(OrderItem.class);

        query.select(cb.sum(root.get("salesPrice")));
        query.where(cb.equal(root.get("salesOrder").get("id"), id));

        return em.createQuery(query).getSingleResult();
    }

    public BigDecimal getTotalDiscountBySalesOrder(Long id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<OrderItem> root = query.from(OrderItem.class);

        query.select(cb.sum(root.get("discountAmount")));
        query.where(cb.equal(root.get("salesOrder").get("id"), id));

        return em.createQuery(query).getSingleResult();
    }

    public BigDecimal getTotalVatlBySalesOrder(Long id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<OrderItem> root = query.from(OrderItem.class);

        query.select(cb.sum(root.get("vatAmount")));
        query.where(cb.equal(root.get("salesOrder").get("id"), id));

        return em.createQuery(query).getSingleResult();
    }

    public BigDecimal getTotalCompanyProfitByItemBySalesOrder(Long id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<OrderItem> root = query.from(OrderItem.class);

        query.select(cb.sum(root.get("marketPlaceCommissionAmount")));
        query.where(cb.equal(root.get("salesOrder").get("id"), id));

        return em.createQuery(query).getSingleResult();
    }

    public BigDecimal getTotalVendorAmountBySalesOrder(Long id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<OrderItem> root = query.from(OrderItem.class);

        query.select(cb.sum(root.get("vendorAmount")));
        query.where(cb.equal(root.get("salesOrder").get("id"), id));

        return em.createQuery(query).getSingleResult();
    }

    public BigDecimal getitemTotalBySalesOrder(Long id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<OrderItem> root = query.from(OrderItem.class);

        query.select(cb.sum(root.get("itemTotal")));
        query.where(cb.equal(root.get("salesOrder").get("id"), id));

        return em.createQuery(query).getSingleResult();
    }

    public void onPostUpdateTotalDiscountBySalesOrder(Long orderId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<OrderItem> root = query.from(OrderItem.class);

        query.select(cb.sum(root.get("discountAmount")));
        query.where(cb.equal(root.get("salesOrder").get("id"), orderId));

        BigDecimal total = em.createQuery(query).getSingleResult();
        if (total == null) {
            total = BigDecimal.ZERO;
        }

        SalesOrder order = em.find(SalesOrder.class, orderId);
        order.setTotalDiscountAmount(total);
        em.merge(order);
    }

    public void onPostUpdateTotalVatlBySalesOrder(Long orderId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<OrderItem> root = query.from(OrderItem.class);

        query.select(cb.sum(root.get("vatAmount")));
        query.where(cb.equal(root.get("salesOrder").get("id"), orderId));

        BigDecimal total = em.createQuery(query).getSingleResult();
        if (total == null) {
            total = BigDecimal.ZERO;
        }

        SalesOrder order = em.find(SalesOrder.class, orderId);
        order.setTotalVatAmount(total);
        em.merge(order);
    }

    public void onPostUpdateItemTotalBySalesOrder(Long orderId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<OrderItem> root = query.from(OrderItem.class);

        query.select(cb.sum(root.get("itemTotal")));
        query.where(cb.equal(root.get("salesOrder").get("id"), orderId));

        BigDecimal total = em.createQuery(query).getSingleResult();
        if (total == null) {
            total = BigDecimal.ZERO;
        }

        SalesOrder order = em.find(SalesOrder.class, orderId);
        order.setItemtotal(total);
        em.merge(order);
    }

    public void onPostUpdateTotalMarketPlaceCommissionAmountBySalesOrder(Long orderId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<OrderItem> root = query.from(OrderItem.class);

        query.select(cb.sum(root.get("marketPlaceCommissionAmount")));
        query.where(cb.equal(root.get("salesOrder").get("id"), orderId));

        BigDecimal total = em.createQuery(query).getSingleResult();
        if (total == null) {
            total = BigDecimal.ZERO;
        }

        SalesOrder order = em.find(SalesOrder.class, orderId);
        order.setTotalMarketPlaceCommissionAmount(total);
        em.merge(order);
    }

    public void onPostUpdateGrandTotal(Long orderId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> cq = cb.createQuery(BigDecimal.class);
        Root<OrderItem> root = cq.from(OrderItem.class);

        // SELECT SUM(itemTotal) FROM OrderItem WHERE salesOrder.id = :id
        cq.select(cb.sum(root.get("itemTotal")))
                .where(cb.equal(root.get("salesOrder").get("id"), orderId));

        BigDecimal total = em.createQuery(cq).getSingleResult();

        // Fallback if no items found
        if (total == null) {
            total = BigDecimal.ZERO;
        }

        // Now update the Order (or salesOrder) entity
        SalesOrder order = em.find(SalesOrder.class, orderId);
        order.setGrandTotal(total);
        em.merge(order); // optional if inside transaction and managed

        //System.out.println("Updated order total: " + total);
    }

}
