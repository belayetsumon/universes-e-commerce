/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.order.services;

import com.ecommerce.app.order.model.OrderItem;
import com.ecommerce.app.order.model.PackagingCost;
import com.ecommerce.app.order.model.SalesOrder;
import com.ecommerce.app.order.model.ShippingCharge;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import com.ecommerce.app.vendor.model.Vendorprofile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author libertyerp_local
 */
@Service
public class SalesOrderService {

    @Autowired
    SalesOrderRepository salesOrderRepository;

    @PersistenceContext
    private EntityManager em;

    public List<Map<String, Object>> admin_all_Sales_order_list(Long vendorId, Long customerId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();

        Root<SalesOrder> salesOrder = cq.from(SalesOrder.class);

        // Subqueries for aggregated fields from OrderItem
        Subquery<BigDecimal> totalDiscountAmountSubquery = cq.subquery(BigDecimal.class);
        Root<OrderItem> orderItem1 = totalDiscountAmountSubquery.from(OrderItem.class);
        totalDiscountAmountSubquery.select(cb.sum(orderItem1.get("discountAmount")))
                .where(cb.equal(orderItem1.get("salesOrder").get("id"), salesOrder.get("id")));

        Subquery<BigDecimal> totalMarketPlaceCommissionSubquery = cq.subquery(BigDecimal.class);
        Root<OrderItem> orderItem2 = totalMarketPlaceCommissionSubquery.from(OrderItem.class);
        totalMarketPlaceCommissionSubquery.select(cb.sum(orderItem2.get("marketPlaceCommissionAmount")))
                .where(cb.equal(orderItem2.get("salesOrder").get("id"), salesOrder.get("id")));

        Subquery<BigDecimal> totalVatSubquery = cq.subquery(BigDecimal.class);
        Root<OrderItem> orderItem3 = totalVatSubquery.from(OrderItem.class);
        totalVatSubquery.select(cb.sum(orderItem3.get("vatAmount")))
                .where(cb.equal(orderItem3.get("salesOrder").get("id"), salesOrder.get("id")));

        Subquery<BigDecimal> totalVendorAmountSubquery = cq.subquery(BigDecimal.class);
        Root<OrderItem> orderItem4 = totalVendorAmountSubquery.from(OrderItem.class);
        totalVendorAmountSubquery.select(cb.sum(orderItem4.get("vendorAmount")))
                .where(cb.equal(orderItem4.get("salesOrder").get("id"), salesOrder.get("id")));

        Subquery<BigDecimal> totalItemAmountSubquery = cq.subquery(BigDecimal.class);
        Root<OrderItem> orderItem5 = totalItemAmountSubquery.from(OrderItem.class);
        totalItemAmountSubquery.select(cb.sum(orderItem5.get("itemTotal")))
                .where(cb.equal(orderItem5.get("salesOrder").get("id"), salesOrder.get("id")));

        // Vendor company name subquery
        Subquery<String> vendorNameSubquery = cq.subquery(String.class);
        Root<Vendorprofile> vendor = vendorNameSubquery.from(Vendorprofile.class);
        vendorNameSubquery.select(vendor.get("companyName"))
                .where(cb.equal(vendor.get("id"), salesOrder.get("vendorId")));

        // Shipping Charge Subquery
        Subquery<BigDecimal> shippingChargeSubquery = cq.subquery(BigDecimal.class);
        Root<ShippingCharge> shippingCharge = shippingChargeSubquery.from(ShippingCharge.class);
        shippingChargeSubquery.select(cb.coalesce(shippingCharge.get("shippingChargeAmount"), BigDecimal.ZERO))
                .where(cb.equal(shippingCharge.get("order").get("id"), salesOrder.get("id")));

        // Packaging Cost Subquery
        Subquery<BigDecimal> packagingCostSubquery = cq.subquery(BigDecimal.class);
        Root<PackagingCost> packagingCost = packagingCostSubquery.from(PackagingCost.class);
        packagingCostSubquery.select(cb.coalesce(packagingCost.get("packagingCost"), BigDecimal.ZERO))
                .where(cb.equal(packagingCost.get("order").get("id"), salesOrder.get("id")));

        // Expression for grand total
        Expression<BigDecimal> totalItemExpr = cb.coalesce(totalItemAmountSubquery.getSelection(), BigDecimal.ZERO);
        Expression<BigDecimal> shippingChargeExpr = cb.coalesce(shippingChargeSubquery.getSelection(), BigDecimal.ZERO);
        Expression<BigDecimal> packagingCostExpr = cb.coalesce(packagingCostSubquery.getSelection(), BigDecimal.ZERO);

        Expression<BigDecimal> grandTotal = cb.sum(cb.sum(totalItemExpr, shippingChargeExpr), packagingCostExpr);

        // Filter predicates
        List<Predicate> predicates = new ArrayList<>();
        if (vendorId != null) {
            predicates.add(cb.equal(salesOrder.get("vendorId"), vendorId));
        }
        if (customerId != null) {
            predicates.add(cb.equal(salesOrder.get("customer").get("id"), customerId));
        }

        // Main select clause
        cq.multiselect(
                salesOrder.get("id").alias("orderId"),
                salesOrder.get("uuid").alias("uuid"),
                salesOrder.get("customer").get("firstName").alias("firstName"),
                salesOrder.get("customer").get("lastName").alias("lastName"),
                salesOrder.get("customer").get("email").alias("email"),
                salesOrder.get("customer").get("mobile").alias("mobile"),
                totalDiscountAmountSubquery.alias("totalDiscountAmount"),
                totalMarketPlaceCommissionSubquery.alias("totalMarketPlaceCommissionAmount"),
                totalVatSubquery.alias("totalVatAmount"),
                totalVendorAmountSubquery.alias("totalVendorAmount"),
                totalItemAmountSubquery.alias("itemTotal"),
                shippingChargeSubquery.alias("shippingChargeAmount"),
                packagingCostSubquery.alias("packagingCost"),
                grandTotal.alias("grandTotal"),
                salesOrder.get("status").alias("status"),
                salesOrder.get("createdBy").alias("createdBy"),
                salesOrder.get("created").alias("created"),
                salesOrder.get("modifiedBy").alias("modifiedBy"),
                salesOrder.get("modified").alias("modified"),
                salesOrder.get("vendorId").alias("vendorId"),
                vendorNameSubquery.alias("vendorName")
        );

        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(cb.desc(salesOrder.get("id")));

        // Execute
        List<Tuple> tuples = em.createQuery(cq).getResultList();

        // Map to List<Map<String, Object>>
        List<Map<String, Object>> result = new ArrayList<>();
        for (Tuple tuple : tuples) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", tuple.get("orderId"));
            map.put("uuid", tuple.get("uuid"));
            map.put("firstName", tuple.get("firstName"));
            map.put("lastName", tuple.get("lastName"));
            map.put("email", tuple.get("email"));
            map.put("mobile", tuple.get("mobile"));
            map.put("totalDiscountAmount", tuple.get("totalDiscountAmount"));
            map.put("totalMarketPlaceCommissionAmount", tuple.get("totalMarketPlaceCommissionAmount"));
            map.put("totalVatAmount", tuple.get("totalVatAmount"));
            map.put("totalVendorAmount", tuple.get("totalVendorAmount"));
            map.put("itemTotal", tuple.get("itemTotal"));
            map.put("shippingChargeAmount", tuple.get("shippingChargeAmount"));
            map.put("packagingCost", tuple.get("packagingCost"));
            map.put("grandTotal", tuple.get("grandTotal"));
            map.put("status", tuple.get("status"));
            map.put("createdBy", tuple.get("createdBy"));
            map.put("created", tuple.get("created"));
            map.put("modifiedBy", tuple.get("modifiedBy"));
            map.put("modified", tuple.get("modified"));
            map.put("vendorId", tuple.get("vendorId"));
            map.put("vendorName", tuple.get("vendorName"));
            result.add(map);
        }

        return result;
    }

    public List<Map<String, Object>> Sales_order_list_By_Customer(Long id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();

        Root<SalesOrder> salesOrder = cq.from(SalesOrder.class);

        // Subqueries for aggregated fields from OrderItem
        Subquery<BigDecimal> totalDiscountAmountSubquery = cq.subquery(BigDecimal.class);
        Root<OrderItem> orderItem1 = totalDiscountAmountSubquery.from(OrderItem.class);
        totalDiscountAmountSubquery.select(cb.sum(orderItem1.get("discountAmount")))
                .where(cb.equal(orderItem1.get("salesOrder").get("id"), salesOrder.get("id")));

        Subquery<BigDecimal> totalMarketPlaceCommissionSubquery = cq.subquery(BigDecimal.class);
        Root<OrderItem> orderItem2 = totalMarketPlaceCommissionSubquery.from(OrderItem.class);
        totalMarketPlaceCommissionSubquery.select(cb.sum(orderItem2.get("marketPlaceCommissionAmount")))
                .where(cb.equal(orderItem2.get("salesOrder").get("id"), salesOrder.get("id")));

        Subquery<BigDecimal> totalVatSubquery = cq.subquery(BigDecimal.class);
        Root<OrderItem> orderItem3 = totalVatSubquery.from(OrderItem.class);
        totalVatSubquery.select(cb.sum(orderItem3.get("vatAmount")))
                .where(cb.equal(orderItem3.get("salesOrder").get("id"), salesOrder.get("id")));

        Subquery<BigDecimal> totalVendorAmountSubquery = cq.subquery(BigDecimal.class);
        Root<OrderItem> orderItem4 = totalVendorAmountSubquery.from(OrderItem.class);
        totalVendorAmountSubquery.select(cb.sum(orderItem4.get("vendorAmount")))
                .where(cb.equal(orderItem4.get("salesOrder").get("id"), salesOrder.get("id")));

        Subquery<BigDecimal> totalItemAmountSubquery = cq.subquery(BigDecimal.class);
        Root<OrderItem> orderItem5 = totalItemAmountSubquery.from(OrderItem.class);
        totalItemAmountSubquery.select(cb.sum(orderItem5.get("itemTotal")))
                .where(cb.equal(orderItem5.get("salesOrder").get("id"), salesOrder.get("id")));

        // Subquery to fetch Vendor Company Name
        Subquery<String> vendorNameSubquery = cq.subquery(String.class);
        Root<Vendorprofile> vendor = vendorNameSubquery.from(Vendorprofile.class);
        vendorNameSubquery.select(vendor.get("companyName"))
                .where(cb.equal(vendor.get("id"), salesOrder.get("vendorId")));

        // Main select clause
        cq.multiselect(
                salesOrder.get("id").alias("orderId"),
                salesOrder.get("uuid").alias("uuid"),
                salesOrder.get("customer").get("firstName").alias("firstName"),
                salesOrder.get("customer").get("lastName").alias("lastName"),
                salesOrder.get("customer").get("email").alias("email"),
                salesOrder.get("customer").get("mobile").alias("mobile"),
                totalDiscountAmountSubquery.alias("totalDiscountAmount"),
                totalMarketPlaceCommissionSubquery.alias("totalMarketPlaceCommissionAmount"),
                totalVatSubquery.alias("totalVatAmount"),
                totalVendorAmountSubquery.alias("totalVendorAmount"),
                totalItemAmountSubquery.alias("itemTotal"),
                salesOrder.get("status").alias("status"),
                salesOrder.get("createdBy").alias("createdBy"),
                salesOrder.get("created").alias("created"),
                salesOrder.get("modifiedBy").alias("modifiedBy"),
                salesOrder.get("modified").alias("modified"),
                salesOrder.get("vendorId").alias("vendorId"),
                vendorNameSubquery.alias("vendorName")
        );

        // Order by latest order
        cq.orderBy(cb.desc(salesOrder.get("id")));

        cq.where(cb.equal(salesOrder.get("customer").get("id"), id));

        List<Tuple> tuples = em.createQuery(cq).getResultList();

        // Mapping
        List<Map<String, Object>> result = new ArrayList<>();
        for (Tuple tuple : tuples) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", tuple.get("orderId"));
            map.put("uuid", tuple.get("uuid"));
            map.put("firstName", tuple.get("firstName"));
            map.put("lastName", tuple.get("lastName"));
            map.put("email", tuple.get("email"));
            map.put("mobile", tuple.get("mobile"));
            map.put("totalDiscountAmount", tuple.get("totalDiscountAmount"));
            map.put("totalMarketPlaceCommissionAmount", tuple.get("totalMarketPlaceCommissionAmount"));
            map.put("totalVatAmount", tuple.get("totalVatAmount"));
            map.put("totalVendorAmount", tuple.get("totalVendorAmount"));
            map.put("itemTotal", tuple.get("itemTotal"));
            map.put("status", tuple.get("status"));
            map.put("createdBy", tuple.get("createdBy"));
            map.put("created", tuple.get("created"));
            map.put("modifiedBy", tuple.get("modifiedBy"));
            map.put("modified", tuple.get("modified"));
            map.put("vendorId", tuple.get("vendorId"));
            map.put("vendorName", tuple.get("vendorName"));
            result.add(map);
        }

        return result;
    }

    // -----------------------
    // Recalculate totals
    // -----------------------
    @Transactional
    public void updateOrderTotals(Long orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        BigDecimal totalVat = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;
        BigDecimal totalVendor = BigDecimal.ZERO;
        BigDecimal itemTotal = BigDecimal.ZERO;

        for (OrderItem item : order.getOrderItem()) {
            totalVat = totalVat.add(Optional.ofNullable(item.getVatAmount()).orElse(BigDecimal.ZERO));
            totalDiscount = totalDiscount.add(Optional.ofNullable(item.getDiscountAmount()).orElse(BigDecimal.ZERO));
            totalCommission = totalCommission.add(Optional.ofNullable(item.getMarketPlaceCommissionAmount()).orElse(BigDecimal.ZERO));
            totalVendor = totalVendor.add(Optional.ofNullable(item.getVendorAmount()).orElse(BigDecimal.ZERO));
            itemTotal = itemTotal.add(Optional.ofNullable(item.getItemTotal()).orElse(BigDecimal.ZERO));
        }

        order.setTotalVatAmount(totalVat);
        order.setTotalDiscountAmount(totalDiscount);
        order.setTotalMarketPlaceCommissionAmount(totalCommission);
        order.setTotalVendorAmount(totalVendor);
        order.setItemtotal(itemTotal);

        BigDecimal grandTotal = itemTotal
                .add(totalVat)
                .add(Optional.ofNullable(order.getPackingCharge()).orElse(BigDecimal.ZERO))
                .add(Optional.ofNullable(order.getDeliveryCharge()).orElse(BigDecimal.ZERO))
                .subtract(totalDiscount);

        order.setGrandTotal(grandTotal);

        salesOrderRepository.save(order);
    }

    ///// shipping charge
    public SalesOrder updateCharges(Long orderId, BigDecimal packingCharge, BigDecimal deliveryCharge) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setPackingCharge(Optional.ofNullable(packingCharge).orElse(BigDecimal.ZERO));

        order.setDeliveryCharge(Optional.ofNullable(deliveryCharge).orElse(BigDecimal.ZERO));

        updateOrderTotals(orderId);
        return order;
    }

}
