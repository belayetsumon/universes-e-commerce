/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.order.services;

import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.communication.events.CommunicationRequestedEvent;
import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.fraud.services.FraudFulfilmentGuard;
import com.ecommerce.app.module.fraud.services.FraudPostOrderMonitoringService;
import com.ecommerce.app.module.fraud.services.VendorRiskProfileService;
import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.model.ShipmentStatus;
import com.ecommerce.app.module.shipping.repository.ShipmentRepository;
import com.ecommerce.app.module.order.model.OrderHistory;
import com.ecommerce.app.module.order.model.OrderItemReturnStatus;
import com.ecommerce.app.module.order.model.OrderStatus;
import com.ecommerce.app.module.order.model.OrderStatusChangedBy;
import com.ecommerce.app.module.order.model.OrderItem;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.repository.OrderHistoryRepository;
import com.ecommerce.app.product.model.ProductTypeEnum;
import com.ecommerce.app.module.order.repository.OrderItemRepository;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import com.ecommerce.app.product.model.StockTransactionTypeEnum;
import com.ecommerce.app.product.ripository.StockTransactionRepository;
import com.ecommerce.app.product.services.StockLedgerService;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.services.VendorFinanceService;
import com.ecommerce.app.module.ReferralRewards.services.CashbackService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    StockLedgerService stockLedgerService;

    @Autowired
    StockTransactionRepository stockTransactionRepository;

    @Autowired
    OrderHistoryRepository orderHistoryRepository;

    @Autowired
    EmiPaymentPlanService emiPaymentPlanService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    VendorFinanceService vendorFinanceService;

    @Autowired
    ShipmentRepository shipmentRepository;

    @Autowired
    CashbackService cashbackService;

    @Autowired
    ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    FraudFulfilmentGuard fraudFulfilmentGuard;

    @Autowired
    FraudPostOrderMonitoringService fraudPostOrderMonitoringService;

    @Autowired
    VendorRiskProfileService vendorRiskProfileService;

    @PersistenceContext
    private EntityManager em;

    public List<Map<String, Object>> admin_all_Sales_order_list(Long vendorId, Long customerId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();

        Root<SalesOrder> salesOrder = cq.from(SalesOrder.class);

        Subquery<BigDecimal> totalDiscountAmountSubquery = sumActiveOrderItemAmount(cq, cb, salesOrder, "discountAmount");
        Subquery<BigDecimal> totalMarketPlaceCommissionSubquery = sumActiveOrderItemAmount(cq, cb, salesOrder, "marketPlaceCommissionAmount");
        Subquery<BigDecimal> totalVatSubquery = sumActiveOrderItemAmount(cq, cb, salesOrder, "vatAmount");
        Subquery<BigDecimal> totalVendorAmountSubquery = sumActiveOrderItemAmount(cq, cb, salesOrder, "vendorAmount");
        Subquery<BigDecimal> totalItemAmountSubquery = sumActiveOrderItemAmount(cq, cb, salesOrder, "itemTotal");

        Subquery<String> vendorNameSubquery = cq.subquery(String.class);
        Root<Vendorprofile> vendor = vendorNameSubquery.from(Vendorprofile.class);
        vendorNameSubquery.select(vendor.get("companyName"))
                .where(cb.equal(vendor.get("id"), salesOrder.get("vendorId")));

        List<Predicate> predicates = new ArrayList<>();
        if (vendorId != null) {
            predicates.add(cb.equal(salesOrder.get("vendorId"), vendorId));
        }
        if (customerId != null) {
            predicates.add(cb.equal(salesOrder.get("customer").get("id"), customerId));
        }

        cq.multiselect(
                salesOrder.get("id").alias("orderId"),
                salesOrder.get("uuid").alias("uuid"),
                salesOrder.get("orderCode").alias("orderCode"),
                salesOrder.get("customer").get("firstName").alias("firstName"),
                salesOrder.get("customer").get("lastName").alias("lastName"),
                salesOrder.get("customer").get("email").alias("email"),
                salesOrder.get("customer").get("mobile").alias("mobile"),
                totalDiscountAmountSubquery.alias("totalDiscountAmount"),
                totalMarketPlaceCommissionSubquery.alias("totalMarketPlaceCommissionAmount"),
                totalVatSubquery.alias("totalVatAmount"),
                totalVendorAmountSubquery.alias("totalVendorAmount"),
                totalItemAmountSubquery.alias("itemTotal"),
                salesOrder.get("packingCharge").alias("packingCharge"),
                salesOrder.get("deliveryCharge").alias("deliveryCharge"),
                salesOrder.get("grandTotal").alias("grandTotal"),
                salesOrder.get("status").alias("status"),
                salesOrder.get("paymentPlan").alias("paymentPlan"),
                salesOrder.get("paymentState").alias("paymentState"),
                salesOrder.get("advancePaid").alias("advancePaid"),
                salesOrder.get("codDue").alias("codDue"),
                salesOrder.get("guestCheckout").alias("guestCheckout"),
                salesOrder.get("createdBy").alias("createdBy"),
                salesOrder.get("created").alias("created"),
                salesOrder.get("modifiedBy").alias("modifiedBy"),
                salesOrder.get("modified").alias("modified"),
                salesOrder.get("vendorId").alias("vendorId"),
                vendorNameSubquery.alias("vendorName")
        );

        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(cb.desc(salesOrder.get("id")));

        List<Tuple> tuples = em.createQuery(cq).getResultList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Tuple tuple : tuples) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", tuple.get("orderId"));
            map.put("uuid", tuple.get("uuid"));
            map.put("orderCode", tuple.get("orderCode"));
            map.put("firstName", tuple.get("firstName"));
            map.put("lastName", tuple.get("lastName"));
            map.put("email", tuple.get("email"));
            map.put("mobile", tuple.get("mobile"));
            map.put("totalDiscountAmount", defaultAmount(tuple.get("totalDiscountAmount")));
            map.put("totalMarketPlaceCommissionAmount", defaultAmount(tuple.get("totalMarketPlaceCommissionAmount")));
            map.put("totalVatAmount", defaultAmount(tuple.get("totalVatAmount")));
            map.put("totalVendorAmount", defaultAmount(tuple.get("totalVendorAmount")));
            map.put("itemTotal", defaultAmount(tuple.get("itemTotal")));
            map.put("deliveryCharge", defaultAmount(tuple.get("deliveryCharge")));
            map.put("packingCharge", defaultAmount(tuple.get("packingCharge")));
            map.put("grandTotal", resolveListGrandTotal(map, tuple.get("grandTotal")));
            map.put("status", tuple.get("status"));
            map.put("paymentPlan", tuple.get("paymentPlan"));
            map.put("paymentState", tuple.get("paymentState"));
            map.put("advancePaid", defaultAmount(tuple.get("advancePaid")));
            map.put("codDue", defaultAmount(tuple.get("codDue")));
            map.put("guestCheckout", tuple.get("guestCheckout"));
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

        Subquery<BigDecimal> totalDiscountAmountSubquery = sumActiveOrderItemAmount(cq, cb, salesOrder, "discountAmount");
        Subquery<BigDecimal> totalMarketPlaceCommissionSubquery = sumActiveOrderItemAmount(cq, cb, salesOrder, "marketPlaceCommissionAmount");
        Subquery<BigDecimal> totalVatSubquery = sumActiveOrderItemAmount(cq, cb, salesOrder, "vatAmount");
        Subquery<BigDecimal> totalVendorAmountSubquery = sumActiveOrderItemAmount(cq, cb, salesOrder, "vendorAmount");
        Subquery<BigDecimal> totalItemAmountSubquery = sumActiveOrderItemAmount(cq, cb, salesOrder, "itemTotal");

        Subquery<String> vendorNameSubquery = cq.subquery(String.class);
        Root<Vendorprofile> vendor = vendorNameSubquery.from(Vendorprofile.class);
        vendorNameSubquery.select(vendor.get("companyName"))
                .where(cb.equal(vendor.get("id"), salesOrder.get("vendorId")));

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

        cq.orderBy(cb.desc(salesOrder.get("id")));
        cq.where(cb.equal(salesOrder.get("customer").get("id"), id));

        List<Tuple> tuples = em.createQuery(cq).getResultList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Tuple tuple : tuples) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", tuple.get("orderId"));
            map.put("uuid", tuple.get("uuid"));
            map.put("firstName", tuple.get("firstName"));
            map.put("lastName", tuple.get("lastName"));
            map.put("email", tuple.get("email"));
            map.put("mobile", tuple.get("mobile"));
            map.put("totalDiscountAmount", defaultAmount(tuple.get("totalDiscountAmount")));
            map.put("totalMarketPlaceCommissionAmount", defaultAmount(tuple.get("totalMarketPlaceCommissionAmount")));
            map.put("totalVatAmount", defaultAmount(tuple.get("totalVatAmount")));
            map.put("totalVendorAmount", defaultAmount(tuple.get("totalVendorAmount")));
            map.put("itemTotal", defaultAmount(tuple.get("itemTotal")));
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

    private Subquery<BigDecimal> sumActiveOrderItemAmount(CriteriaQuery<?> query,
            CriteriaBuilder cb,
            Root<SalesOrder> salesOrder,
            String amountField) {
        Subquery<BigDecimal> subquery = query.subquery(BigDecimal.class);
        Root<OrderItem> orderItem = subquery.from(OrderItem.class);
        subquery.select(cb.<BigDecimal>coalesce()
                .value(cb.sum(orderItem.<BigDecimal>get(amountField)))
                .value(BigDecimal.ZERO));
        subquery.where(
                cb.equal(orderItem.get("salesOrder").get("id"), salesOrder.get("id")),
                cb.or(
                        cb.isNull(orderItem.get("returnStatus")),
                        cb.notEqual(orderItem.get("returnStatus"), OrderItemReturnStatus.RETURNED)
                )
        );
        return subquery;
    }

    private BigDecimal resolveListGrandTotal(Map<String, Object> row, Object storedGrandTotal) {
        BigDecimal grandTotal = defaultAmount(storedGrandTotal);
        if (grandTotal.compareTo(BigDecimal.ZERO) > 0) {
            return grandTotal;
        }

        BigDecimal calculatedTotal = defaultAmount(row.get("itemTotal"))
                .add(defaultAmount(row.get("totalVatAmount")))
                .add(defaultAmount(row.get("deliveryCharge")))
                .add(defaultAmount(row.get("packingCharge")))
                .subtract(defaultAmount(row.get("totalDiscountAmount")));

        return calculatedTotal.compareTo(BigDecimal.ZERO) > 0 ? calculatedTotal : grandTotal;
    }

    private BigDecimal defaultAmount(Object amount) {
        if (amount instanceof BigDecimal value) {
            return value;
        }
        if (amount instanceof Number value) {
            return BigDecimal.valueOf(value.doubleValue());
        }
        return BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public SalesOrder getOrderOrThrow(Long orderId) {
        return salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));
    }

    @Transactional(readOnly = true)
    public SalesOrder getCustomerOrderForUser(Long orderId, Long customerId) {
        if (orderId == null || customerId == null) {
            throw new IllegalArgumentException("Order not found.");
        }

        return salesOrderRepository.findByIdAndCustomer_Id(orderId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));
    }

    @Transactional(readOnly = true)
    public SalesOrder getVendorOrderForVendor(Long orderId, Long vendorId) {
        if (orderId == null || vendorId == null) {
            throw new IllegalArgumentException("Order not found.");
        }

        return salesOrderRepository.findByIdAndVendorId(orderId, vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));
    }

    public boolean canCustomerCancelOrder(SalesOrder order) {
        return order != null && canCustomerCancelFromStatus(order.getStatus());
    }

    public boolean canCustomerRequestReturn(SalesOrder order) {
        return order != null
                && canRequestReturnFromStatus(order.getStatus())
                && !orderContainsOnlyVirtualItems(order.getId())
                && orderItemRepository.findBySalesOrder_Id(order.getId()).stream()
                        .anyMatch(this::isCustomerReturnRequestEligible);
    }

    public boolean canVendorCancelOrder(SalesOrder order) {
        if (order == null || order.getStatus() == null) {
            return false;
        }

        OrderStatus status = order.getStatus();
        if (status == OrderStatus.NEW_ORDER
                || status == OrderStatus.PENDING
                || status == OrderStatus.CONFIRMED
                || status == OrderStatus.PROCESSING) {
            return true;
        }

        return status == OrderStatus.PACKED && !hasShipmentForOrder(order.getId());
    }

    public boolean canMarketplaceCancelOrder(SalesOrder order) {
        return order != null && canCancelFromStatus(order.getStatus());
    }

    public String getCustomerCancellationPolicyMessage(SalesOrder order) {
        if (order == null || order.getStatus() == null) {
            return "Cancellation is not available for this order.";
        }

        if (canCustomerCancelOrder(order)) {
            return "You can cancel this order while it is still waiting for final fulfillment confirmation.";
        }

        OrderStatus status = order.getStatus();
        if (status == OrderStatus.PROCESSING || status == OrderStatus.PACKED) {
            return "This order is already being prepared. Please contact support if you need to request cancellation before shipment.";
        }
        if (isSoldStatus(status)) {
            return "This order is already shipped or delivered. Cancellation is no longer available. Please use the return flow after delivery if needed.";
        }
        if (status == OrderStatus.CANCELLED) {
            return "This order is already cancelled.";
        }
        if (status == OrderStatus.RETURN_REQUESTED
                || status == OrderStatus.PARTIALLY_RETURNED
                || status == OrderStatus.RETURNED) {
            return "This order is already in the return flow and can no longer be cancelled.";
        }
        return "Cancellation is no longer available for this order.";
    }

    public String getVendorCancellationPolicyMessage(SalesOrder order) {
        if (order == null || order.getStatus() == null) {
            return "Vendor cancellation is not available for this order.";
        }

        if (canVendorCancelOrder(order)) {
            if (order.getStatus() == OrderStatus.PACKED) {
                return "Packed orders can be cancelled by the vendor only before a shipment is created.";
            }
            return "Vendor cancellation is allowed through the processing stage.";
        }

        OrderStatus status = order.getStatus();
        if (status == OrderStatus.PACKED && hasShipmentForOrder(order.getId())) {
            return "A shipment already exists for this packed order. Please coordinate with admin instead of cancelling from the vendor screen.";
        }
        if (isSoldStatus(status)) {
            return "Shipment has already started. Use shipment or return handling instead of vendor cancellation.";
        }
        if (status == OrderStatus.CANCELLED) {
            return "This order is already cancelled.";
        }
        if (status == OrderStatus.RETURN_REQUESTED
                || status == OrderStatus.PARTIALLY_RETURNED
                || status == OrderStatus.RETURNED) {
            return "This order is already in the return flow and cannot be cancelled.";
        }
        return "Vendor cancellation is not available for this order.";
    }

    public String getMarketplaceCancellationPolicyMessage(SalesOrder order) {
        if (order == null || order.getStatus() == null) {
            return "Marketplace cancellation is not available for this order.";
        }

        if (canMarketplaceCancelOrder(order)) {
            return "Marketplace can cancel this order up to the packed stage.";
        }

        OrderStatus status = order.getStatus();
        if (isSoldStatus(status)) {
            return "Shipment has already started. Use shipment exception or return handling instead of cancellation.";
        }
        if (status == OrderStatus.CANCELLED) {
            return "This order is already cancelled.";
        }
        if (status == OrderStatus.RETURN_REQUESTED
                || status == OrderStatus.PARTIALLY_RETURNED
                || status == OrderStatus.RETURNED) {
            return "This order is already in the return flow and cannot be cancelled.";
        }
        return "Marketplace cancellation is not available for this order.";
    }

    public List<OrderStatus> getVendorStatusOptions(SalesOrder order) {
        return buildBackOfficeStatusOptions(order, false);
    }

    public List<OrderStatus> getMarketplaceStatusOptions(SalesOrder order) {
        return buildBackOfficeStatusOptions(order, true);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> enrichOrderItemsWithReturnData(List<Map<String, Object>> orderItems, OrderStatus orderStatus) {
        boolean returnWindowOpen = canRequestReturnFromStatus(orderStatus);

        if (orderItems == null) {
            return List.of();
        }

        for (Map<String, Object> orderItem : orderItems) {
            OrderItemReturnStatus returnStatus = resolveReturnStatus(orderItem.get("returnStatus"));
            ProductTypeEnum productType = resolveProductType(orderItem.get("productType"));
            boolean virtualItem = productType == ProductTypeEnum.Virtual;
            boolean itemReturnSelectable = returnWindowOpen
                    && !virtualItem
                    && returnStatus == OrderItemReturnStatus.NONE;

            orderItem.put("returnStatus", returnStatus);
            orderItem.put("returnStatusLabel", buildReturnStatusLabel(returnStatus));
            orderItem.put("itemReturnSelectable", itemReturnSelectable);
            orderItem.put("staffCanMarkReturned", returnStatus == OrderItemReturnStatus.RETURN_REQUESTED);
            orderItem.put("itemReturned", returnStatus == OrderItemReturnStatus.RETURNED);
            orderItem.put("itemReturnRequested", returnStatus == OrderItemReturnStatus.RETURN_REQUESTED);
            orderItem.put("virtualItem", virtualItem);
            orderItem.put("itemReturnBlockedReason", buildItemReturnBlockedReason(returnWindowOpen, virtualItem, returnStatus));
        }

        return orderItems;
    }

    @Transactional
    public SalesOrder changeStatusAsCustomer(Long orderId, Long customerId, OrderStatus nextStatus, String remark) {
        SalesOrder order = getCustomerOrderForUser(orderId, customerId);
        validateCustomerStatusChange(order, nextStatus);
        return applyStatusChange(order, nextStatus, OrderStatusChangedBy.Customer, remark);
    }

    @Transactional
    public SalesOrder changeStatusAsMarketplace(Long orderId, OrderStatus nextStatus, String remark) {
        SalesOrder order = getOrderOrThrow(orderId);
        validateMarketplaceStatusChange(order, nextStatus);
        return applyStatusChange(order, nextStatus, OrderStatusChangedBy.MarketPlace, remark);
    }

    @Transactional
    public SalesOrder changeStatusAsVendor(Long orderId, Long vendorId, OrderStatus nextStatus, String remark) {
        SalesOrder order = getVendorOrderForVendor(orderId, vendorId);
        validateVendorStatusChange(order, nextStatus);
        return applyStatusChange(order, nextStatus, OrderStatusChangedBy.Vendor, remark);
    }

    @Transactional
    public SalesOrder requestItemReturnsAsCustomer(Long orderId, Long customerId, List<Long> itemIds, String remark) {
        SalesOrder order = getCustomerOrderForUser(orderId, customerId);

        if (!canRequestReturnFromStatus(order.getStatus())) {
            throw new IllegalArgumentException("This order is not eligible for an item return request right now.");
        }

        Set<Long> uniqueItemIds = normalizeOrderItemIds(itemIds);
        if (uniqueItemIds.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one item to return.");
        }

        List<OrderItem> selectedItems = orderItemRepository.findBySalesOrder_IdAndIdIn(orderId, uniqueItemIds);
        if (selectedItems.size() != uniqueItemIds.size()) {
            throw new IllegalArgumentException("One or more selected items do not belong to this order.");
        }

        LocalDateTime requestedAt = LocalDateTime.now();
        String cleanedRemark = cleanText(remark);
        List<String> itemNames = new ArrayList<>();

        for (OrderItem item : selectedItems) {
            validateItemReturnRequestEligibility(item);
            item.setReturnStatus(OrderItemReturnStatus.RETURN_REQUESTED);
            item.setReturnRequestedAt(requestedAt);
            item.setReturnRequestRemark(cleanedRemark);
            orderItemRepository.save(item);
            itemNames.add(resolveOrderItemLabel(item));
        }

        order.setStatus(OrderStatus.RETURN_REQUESTED);
        salesOrderRepository.save(order);
        paymentService.refreshOrderPaymentTracking(order);
        saveOrderHistory(
                order,
                OrderStatus.RETURN_REQUESTED,
                OrderStatusChangedBy.Customer,
                buildHistoryRemark(cleanedRemark, buildItemReturnRequestNote(itemNames))
        );
        fraudPostOrderMonitoringService.recordOrderStatusChanged(
                order,
                null,
                OrderStatus.RETURN_REQUESTED.name(),
                OrderStatusChangedBy.Customer.name(),
                cleanedRemark
        );
        refreshVendorRisk(order);
        notifyOrderEvent(MessageEventType.RETURN_REQUESTED, order);
        return order;
    }

    @Transactional
    public SalesOrder processItemReturnAsMarketplace(Long orderId, Long orderItemId, String remark) {
        SalesOrder order = getOrderOrThrow(orderId);
        OrderItem orderItem = getOrderItemForOrder(order, orderItemId);
        return processItemReturn(order, orderItem, OrderStatusChangedBy.MarketPlace, remark);
    }

    @Transactional
    public SalesOrder processItemReturnAsVendor(Long orderId, Long vendorId, Long orderItemId, String remark) {
        SalesOrder order = getVendorOrderForVendor(orderId, vendorId);
        OrderItem orderItem = getOrderItemForOrder(order, orderItemId);
        return processItemReturn(order, orderItem, OrderStatusChangedBy.Vendor, remark);
    }

    @Transactional
    public void updateOrderTotals(Long orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        BigDecimal totalVat = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;
        BigDecimal totalVendor = BigDecimal.ZERO;
        BigDecimal itemTotal = BigDecimal.ZERO;
        boolean hasActivePhysicalItems = false;

        for (OrderItem item : order.getOrderItem()) {
            if (isReturnedItem(item)) {
                continue;
            }

            totalVat = totalVat.add(Optional.ofNullable(item.getVatAmount()).orElse(BigDecimal.ZERO));
            totalDiscount = totalDiscount.add(Optional.ofNullable(item.getDiscountAmount()).orElse(BigDecimal.ZERO));
            totalCommission = totalCommission.add(Optional.ofNullable(item.getMarketPlaceCommissionAmount()).orElse(BigDecimal.ZERO));
            totalVendor = totalVendor.add(Optional.ofNullable(item.getVendorAmount()).orElse(BigDecimal.ZERO));
            itemTotal = itemTotal.add(Optional.ofNullable(item.getItemTotal()).orElse(BigDecimal.ZERO));
            if (!isVirtualItem(item)) {
                hasActivePhysicalItems = true;
            }
        }

        order.setTotalVatAmount(totalVat);
        order.setTotalDiscountAmount(totalDiscount);
        order.setTotalMarketPlaceCommissionAmount(totalCommission);
        order.setTotalVendorAmount(totalVendor);
        order.setItemtotal(itemTotal);

        BigDecimal packingContribution = hasActivePhysicalItems
                ? Optional.ofNullable(order.getPackingCharge()).orElse(BigDecimal.ZERO)
                : BigDecimal.ZERO;
        BigDecimal deliveryContribution = hasActivePhysicalItems
                ? Optional.ofNullable(order.getDeliveryCharge()).orElse(BigDecimal.ZERO)
                : BigDecimal.ZERO;

        BigDecimal grandTotal = itemTotal
                .add(totalVat)
                .add(packingContribution)
                .add(deliveryContribution)
                .subtract(totalDiscount);

        order.setGrandTotal(grandTotal);

        salesOrderRepository.save(order);
    }

    public SalesOrder updateCharges(Long orderId, BigDecimal packingCharge, BigDecimal deliveryCharge) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setPackingCharge(Optional.ofNullable(packingCharge).orElse(BigDecimal.ZERO));
        order.setDeliveryCharge(Optional.ofNullable(deliveryCharge).orElse(BigDecimal.ZERO));
        updateOrderTotals(orderId);
        return order;
    }

    private SalesOrder processItemReturn(SalesOrder order, OrderItem orderItem,
            OrderStatusChangedBy changedBy, String remark) {
        validateItemReturnProcessing(order, orderItem);

        String cleanedRemark = cleanText(remark);
        LocalDateTime returnedAt = LocalDateTime.now();

        orderItem.setReturnStatus(OrderItemReturnStatus.RETURNED);
        orderItem.setReturnedAt(returnedAt);
        if (orderItem.getReturnRequestedAt() == null) {
            orderItem.setReturnRequestedAt(returnedAt);
        }
        orderItem.setReturnProcessedRemark(cleanedRemark);
        orderItem.setReturnRefundAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        orderItemRepository.save(orderItem);

        restockReturnedStockForItem(order, orderItem);
        updateOrderTotals(order.getId());

        SalesOrder refreshedOrder = getOrderOrThrow(order.getId());
        PaymentService.PaymentSummary paymentSummary = paymentService.getPaymentSummary(refreshedOrder);
        BigDecimal refundNeeded = paymentSummary.getTotalPaid().subtract(defaultMoney(refreshedOrder.getGrandTotal()));
        if (refundNeeded.compareTo(BigDecimal.ZERO) < 0) {
            refundNeeded = BigDecimal.ZERO;
        }

        PaymentService.RefundResult refundResult = refundNeeded.compareTo(BigDecimal.ZERO) > 0
                ? paymentService.refundAmount(
                        refreshedOrder,
                        refundNeeded,
                        "Refund issued after item return. Order " + refreshedOrder.getOrderCode()
                        + ", item #" + orderItem.getId()
                )
                : PaymentService.RefundResult.none();

        orderItem.setReturnRefundAmount(defaultMoney(refundResult.getAmount()));
        orderItemRepository.save(orderItem);

        BigDecimal vendorRefund = vendorFinanceService.createItemRefundTransactionIfNeeded(
                refreshedOrder,
                orderItem,
                "Vendor settlement reversed after item return. Order " + refreshedOrder.getOrderCode()
                + ", item #" + orderItem.getId()
        );

        OrderStatus resolvedStatus = resolveOrderStatusAfterItemReturn(refreshedOrder.getId());
        refreshedOrder.setStatus(resolvedStatus);
        salesOrderRepository.save(refreshedOrder);
        paymentService.refreshOrderPaymentTracking(refreshedOrder);

        saveOrderHistory(
                refreshedOrder,
                resolvedStatus,
                changedBy,
                buildHistoryRemark(
                        cleanedRemark,
                        buildItemReturnProcessedNote(orderItem, refundResult, vendorRefund, resolvedStatus)
                )
        );
        notifyOrderEvent(refundResult != null && refundResult.isRefunded()
                ? MessageEventType.REFUND_COMPLETED
                : MessageEventType.RETURN_APPROVED, refreshedOrder);
        fraudPostOrderMonitoringService.recordOrderStatusChanged(
                refreshedOrder,
                null,
                resolvedStatus.name(),
                changedBy.name(),
                cleanedRemark
        );
        refreshVendorRisk(refreshedOrder);

        return refreshedOrder;
    }

    @Transactional
    public void reserveStockForOrder(Long orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        List<OrderItem> items = orderItemRepository.findBySalesOrder_Id(orderId);

        for (OrderItem item : items) {
            if (item.getId() == null
                    || Boolean.TRUE.equals(item.getPreorder())
                    || (item.getProduct() != null && item.getProduct().getProductType() == ProductTypeEnum.Virtual)
                    || stockTransactionRepository.existsByOrderItem_IdAndTransactionType(item.getId(), StockTransactionTypeEnum.RESERVE)) {
                continue;
            }
            String idempotencyKey = UUID.randomUUID().toString();
            stockLedgerService.reserveStock(
                    item.getProductid(),
                    item.getCatalogVariantUuid(),
                    item.getQuantity(),
                    orderId,
                    item.getId(),
                    idempotencyKey,
                    "Reserved automatically from order lifecycle. Order " + order.getOrderCode()
            );
        }
    }

    @Transactional
    public void releaseReservedStockForOrder(Long orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        List<OrderItem> items = orderItemRepository.findBySalesOrder_Id(orderId);

        for (OrderItem item : items) {
            if (item.getId() == null
                    || !stockTransactionRepository.existsByOrderItem_IdAndTransactionType(item.getId(), StockTransactionTypeEnum.RESERVE)
                    || stockTransactionRepository.existsByOrderItem_IdAndTransactionType(item.getId(), StockTransactionTypeEnum.RELEASE)
                    || stockTransactionRepository.existsByOrderItem_IdAndTransactionType(item.getId(), StockTransactionTypeEnum.SALE)) {
                continue;
            }
            String idempotencyKey = UUID.randomUUID().toString();
            stockLedgerService.releaseReservedStock(
                    item.getProductid(),
                    item.getCatalogVariantUuid(),
                    item.getQuantity(),
                    orderId,
                    item.getId(),
                    idempotencyKey,
                    "Released automatically from order lifecycle. Order " + order.getOrderCode()
            );
        }
    }

    @Transactional
    public void completeStockSaleForOrder(Long orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        List<OrderItem> items = orderItemRepository.findBySalesOrder_Id(orderId);

        for (OrderItem item : items) {
            if (item.getId() == null
                    || !stockTransactionRepository.existsByOrderItem_IdAndTransactionType(item.getId(), StockTransactionTypeEnum.RESERVE)
                    || stockTransactionRepository.existsByOrderItem_IdAndTransactionType(item.getId(), StockTransactionTypeEnum.SALE)) {
                continue;
            }
            String idempotencyKey = UUID.randomUUID().toString();
            stockLedgerService.completeSale(
                    item.getProductid(),
                    item.getCatalogVariantUuid(),
                    item.getQuantity(),
                    orderId,
                    item.getId(),
                    idempotencyKey,
                    "Completed sale automatically from order lifecycle. Order " + order.getOrderCode()
            );
        }
    }

    @Transactional
    public void restockReturnedStockForOrder(Long orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        List<OrderItem> items = orderItemRepository.findBySalesOrder_Id(orderId);

        for (OrderItem item : items) {
            restockReturnedStockForItem(order, item);
        }
    }

    @Transactional
    public void syncStockWithOrderStatus(Long orderId, OrderStatus previousStatus, OrderStatus newStatus) {
        if (newStatus == null || previousStatus == newStatus) {
            return;
        }

        if (newStatus == OrderStatus.RETURNED) {
            restockReturnedStockForOrder(orderId);
            return;
        }

        if (newStatus == OrderStatus.CANCELLED) {
            releaseReservedStockForOrder(orderId);
            return;
        }

        if (isSoldStatus(newStatus)) {
            completeStockSaleForOrder(orderId);
        }
    }

    @Transactional(readOnly = true)
    public boolean orderContainsOnlyVirtualItems(Long orderId) {
        List<OrderItem> items = orderItemRepository.findBySalesOrder_Id(orderId);
        return items != null
                && !items.isEmpty()
                && items.stream().allMatch(item -> item.getProduct() != null
                && item.getProduct().getProductType() == ProductTypeEnum.Virtual);
    }

    @Transactional
    public SalesOrder finalizePaidOrder(Long orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        fulfillDigitalItems(orderId);

        OrderStatus previousStatus = order.getStatus();
        OrderStatus nextStatus = orderContainsOnlyVirtualItems(orderId)
                ? OrderStatus.COMPLETED
                : OrderStatus.CONFIRMED;
        validateFraudForOperationalStatus(order, nextStatus);

        order.setStatus(nextStatus);
        salesOrderRepository.save(order);
        syncStockWithOrderStatus(orderId, previousStatus, nextStatus);
        syncVendorFinanceWithOrderStatus(order, nextStatus, null);
        notifyOrderEvent(nextStatus == OrderStatus.COMPLETED ? MessageEventType.ORDER_DELIVERED : MessageEventType.ORDER_CONFIRMED, order);
        return order;
    }

    @Transactional
    public SalesOrder confirmOrderAfterAdvancePayment(Long orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.NEW_ORDER && order.getStatus() != OrderStatus.PENDING) {
            return order;
        }

        OrderStatus previousStatus = order.getStatus();
        validateFraudForOperationalStatus(order, OrderStatus.CONFIRMED);
        order.setStatus(OrderStatus.CONFIRMED);
        salesOrderRepository.save(order);
        syncStockWithOrderStatus(orderId, previousStatus, OrderStatus.CONFIRMED);
        notifyOrderEvent(MessageEventType.ORDER_CONFIRMED, order);
        return order;
    }

    @Transactional
    public void fulfillDigitalItems(Long orderId) {
        List<OrderItem> items = orderItemRepository.findBySalesOrder_Id(orderId);
        LocalDateTime fulfilledAt = LocalDateTime.now();

        for (OrderItem item : items) {
            if (item.getProduct() == null
                    || item.getProduct().getProductType() != ProductTypeEnum.Virtual
                    || Boolean.TRUE.equals(item.getDigitalDelivered())) {
                continue;
            }

            item.setDigitalDelivered(Boolean.TRUE);
            item.setDigitalDeliveredAt(fulfilledAt);
            orderItemRepository.save(item);
        }
    }

    @Transactional
    public void restockReturnedStockForItem(SalesOrder order, OrderItem item) {
        if (order == null || order.getId() == null || item == null || item.getId() == null
                || Boolean.TRUE.equals(item.getPreorder())
                || isVirtualItem(item)) {
            return;
        }

        boolean sold = stockTransactionRepository.existsByOrderItem_IdAndTransactionType(item.getId(), StockTransactionTypeEnum.SALE);
        boolean alreadyReturned = stockTransactionRepository.existsByOrderItem_IdAndTransactionType(item.getId(), StockTransactionTypeEnum.RETURN_TO_STOCK);

        if (sold && !alreadyReturned) {
            stockLedgerService.returnSoldStock(
                    item.getProductid(),
                    item.getCatalogVariantUuid(),
                    item.getQuantity(),
                    order.getId(),
                    item.getId(),
                    UUID.randomUUID().toString(),
                    "Returned stock moved back to available automatically. Order " + order.getOrderCode()
            );
            return;
        }

        boolean reserved = stockTransactionRepository.existsByOrderItem_IdAndTransactionType(item.getId(), StockTransactionTypeEnum.RESERVE);
        boolean released = stockTransactionRepository.existsByOrderItem_IdAndTransactionType(item.getId(), StockTransactionTypeEnum.RELEASE);

        if (reserved && !released) {
            stockLedgerService.releaseReservedStock(
                    item.getProductid(),
                    item.getCatalogVariantUuid(),
                    item.getQuantity(),
                    order.getId(),
                    item.getId(),
                    UUID.randomUUID().toString(),
                    "Returned order released reserved stock automatically. Order " + order.getOrderCode()
            );
        }
    }

    private SalesOrder applyStatusChange(SalesOrder order, OrderStatus nextStatus,
            OrderStatusChangedBy changedBy, String remark) {
        if (order == null || order.getId() == null) {
            throw new IllegalArgumentException("Order not found.");
        }

        if (nextStatus == null) {
            throw new IllegalArgumentException("Order status is required.");
        }

        OrderStatus previousStatus = order.getStatus();
        if (previousStatus == nextStatus) {
            throw new IllegalArgumentException("Order is already marked as " + nextStatus + ".");
        }

        validateCommonStatusChange(order, nextStatus);
        validateFraudForOperationalStatus(order, nextStatus);

        order.setStatus(nextStatus);
        salesOrderRepository.save(order);
        syncStockWithOrderStatus(order.getId(), previousStatus, nextStatus);

        String systemNote = buildPostStatusChangeNote(order, nextStatus);
        paymentService.refreshOrderPaymentTracking(order);
        saveOrderHistory(order, nextStatus, changedBy, buildHistoryRemark(remark, systemNote));
        fraudPostOrderMonitoringService.recordOrderStatusChanged(
                order,
                previousStatus == null ? null : previousStatus.name(),
                nextStatus.name(),
                changedBy == null ? null : changedBy.name(),
                remark
        );
        refreshVendorRisk(order);
        notifyOrderEvent(resolveCommunicationEvent(nextStatus), order);
        return order;
    }

    private void refreshVendorRisk(SalesOrder order) {
        if (order != null && order.getVendorId() != null) {
            vendorRiskProfileService.refreshVendorProfile(order.getVendorId());
        }
    }

    private void notifyOrderEvent(MessageEventType eventType, SalesOrder order) {
        try {
            if (eventType == null || order == null || order.getCustomer() == null || cleanText(order.getCustomer().getEmail()) == null) {
                return;
            }
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", safeText(order.getCustomer().getFirstName()));
            variables.put("orderNumber", safeText(order.getOrderCode()));
            variables.put("orderTotal", defaultMoney(order.getGrandTotal()).toPlainString());
            applicationEventPublisher.publishEvent(CommunicationRequestedEvent.order(
                    eventType,
                    MessageChannel.EMAIL,
                    order.getCustomer().getEmail(),
                    order.getOrderCode(),
                    variables
            ));
        } catch (Exception ignored) {
            // Communication failure must not block order processing.
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private MessageEventType resolveCommunicationEvent(OrderStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case CONFIRMED -> MessageEventType.ORDER_CONFIRMED;
            case PROCESSING -> MessageEventType.ORDER_PROCESSING;
            case PACKED -> MessageEventType.ORDER_PACKED;
            case SHIPPED, IN_TRANSIT -> MessageEventType.ORDER_SHIPPED;
            case OUT_FOR_DELIVERY -> MessageEventType.ORDER_OUT_FOR_DELIVERY;
            case DELIVERED, COMPLETED -> MessageEventType.ORDER_DELIVERED;
            case CANCELLED -> MessageEventType.ORDER_CANCELLED;
            case RETURN_REQUESTED -> MessageEventType.RETURN_REQUESTED;
            case RETURNED, PARTIALLY_RETURNED -> MessageEventType.RETURN_APPROVED;
            default -> MessageEventType.ORDER_PROCESSING;
        };
    }

    private void validateCustomerStatusChange(SalesOrder order, OrderStatus nextStatus) {
        if (nextStatus != OrderStatus.CANCELLED && nextStatus != OrderStatus.RETURN_REQUESTED) {
            throw new IllegalArgumentException("Customers can only cancel orders or request returns from this screen.");
        }

        if (nextStatus == OrderStatus.CANCELLED && !canCustomerCancelOrder(order)) {
            throw new IllegalArgumentException(getCustomerCancellationPolicyMessage(order));
        }

        if (nextStatus == OrderStatus.RETURN_REQUESTED && !canCustomerRequestReturn(order)) {
            throw new IllegalArgumentException("This order is not eligible for a return request.");
        }
    }

    private void validateVendorStatusChange(SalesOrder order, OrderStatus nextStatus) {
        if (nextStatus == OrderStatus.CANCELLED && !canVendorCancelOrder(order)) {
            throw new IllegalArgumentException(getVendorCancellationPolicyMessage(order));
        }
    }

    private void validateMarketplaceStatusChange(SalesOrder order, OrderStatus nextStatus) {
        if (nextStatus == OrderStatus.CANCELLED && !canMarketplaceCancelOrder(order)) {
            throw new IllegalArgumentException(getMarketplaceCancellationPolicyMessage(order));
        }
    }

    private void validateCommonStatusChange(SalesOrder order, OrderStatus nextStatus) {
        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.RETURNED) {
            throw new IllegalArgumentException("This order is already closed and cannot be changed.");
        }

        if (nextStatus == OrderStatus.PARTIALLY_RETURNED) {
            throw new IllegalArgumentException("Partial return status is managed automatically from item returns.");
        }

        if (nextStatus == OrderStatus.CANCELLED && !canCancelFromStatus(currentStatus)) {
            throw new IllegalArgumentException("Orders that are already shipped or completed must go through the return flow instead of cancellation.");
        }

        if (nextStatus == OrderStatus.RETURN_REQUESTED) {
            if (!canRequestReturnFromStatus(currentStatus)) {
                throw new IllegalArgumentException("Only delivered, completed, or already-returning orders can enter the return-requested state.");
            }
            if (orderContainsOnlyVirtualItems(order.getId())) {
                throw new IllegalArgumentException("Virtual-only orders cannot be returned from this flow.");
            }
        }

        if (nextStatus == OrderStatus.RETURNED) {
            if (!canMarkReturnedFromStatus(currentStatus)) {
                throw new IllegalArgumentException("The order must be delivered, completed, or already return requested before it can be marked returned.");
            }
            if (orderContainsOnlyVirtualItems(order.getId())) {
                throw new IllegalArgumentException("Virtual-only orders cannot be returned from this flow.");
            }
        }
    }

    private void validateFraudForOperationalStatus(SalesOrder order, OrderStatus nextStatus) {
        if (order == null || nextStatus == null) {
            return;
        }

        FraudGuardResult fraudGuard = null;
        if (nextStatus == OrderStatus.CONFIRMED || nextStatus == OrderStatus.PROCESSING) {
            fraudGuard = fraudFulfilmentGuard.checkFulfilmentAllowed(order);
        } else if (nextStatus == OrderStatus.PACKED) {
            fraudGuard = fraudFulfilmentGuard.checkPackingAllowed(order);
        } else if (isSoldStatus(nextStatus) || nextStatus == OrderStatus.COMPLETED) {
            fraudGuard = fraudFulfilmentGuard.checkFulfilmentAllowed(order);
        }

        if (fraudGuard != null && !fraudGuard.isAllowed()) {
            throw new IllegalArgumentException(fraudGuard.getReason());
        }
    }

    private boolean canCancelFromStatus(OrderStatus status) {
        return status == OrderStatus.NEW_ORDER
                || status == OrderStatus.PENDING
                || status == OrderStatus.CONFIRMED
                || status == OrderStatus.PROCESSING
                || status == OrderStatus.PACKED;
    }

    private boolean canCustomerCancelFromStatus(OrderStatus status) {
        return status == OrderStatus.NEW_ORDER
                || status == OrderStatus.PENDING
                || status == OrderStatus.CONFIRMED;
    }

    private boolean canRequestReturnFromStatus(OrderStatus status) {
        return status == OrderStatus.DELIVERED
                || status == OrderStatus.COMPLETED
                || status == OrderStatus.RETURN_REQUESTED
                || status == OrderStatus.PARTIALLY_RETURNED;
    }

    private boolean canMarkReturnedFromStatus(OrderStatus status) {
        return status == OrderStatus.RETURN_REQUESTED
                || status == OrderStatus.PARTIALLY_RETURNED
                || status == OrderStatus.DELIVERED
                || status == OrderStatus.COMPLETED;
    }

    private String buildPostStatusChangeNote(SalesOrder order, OrderStatus nextStatus) {
        List<String> notes = new ArrayList<>();

        if (nextStatus == OrderStatus.CANCELLED) {
            if (cancelPendingEmiIfNecessary(order)) {
                notes.add("Pending Meritten EMI request cancelled.");
            }

            PaymentService.RefundResult refundResult = paymentService.refundPaidAmount(
                    order,
                    "Refund issued after order cancellation. Order " + order.getOrderCode()
            );
            if (refundResult.isRefunded()) {
                notes.add("Refunded " + formatMoney(refundResult.getAmount())
                        + " BDT via " + refundDisplayName(refundResult) + ".");
            }

            int cancelledShipments = cancelOpenShipmentsForOrder(order);
            if (cancelledShipments > 0) {
                notes.add("Cancelled " + cancelledShipments + " linked shipment record(s).");
            }
        }

        if (nextStatus == OrderStatus.DELIVERED || nextStatus == OrderStatus.COMPLETED) {
            vendorFinanceService.createOrderTransactionIfMissing(order);
            try {
                cashbackService.approveAndPayToWalletIfPending(String.valueOf(order.getId()));
            } catch (Exception ignored) {
                // Cashback payout should never block order status change.
            }
        }

        if (nextStatus == OrderStatus.RETURNED) {
            PaymentService.RefundResult refundResult = paymentService.refundPaidAmount(
                    order,
                    "Refund issued after order return. Order " + order.getOrderCode()
            );
            if (refundResult.isRefunded()) {
                notes.add("Refunded " + formatMoney(refundResult.getAmount())
                        + " BDT via " + refundDisplayName(refundResult) + ".");
            }

            BigDecimal vendorRefund = vendorFinanceService.createRefundTransactionIfNeeded(
                    order,
                    "Vendor settlement reversed after return. Order " + order.getOrderCode()
            );
            if (vendorRefund.compareTo(BigDecimal.ZERO) > 0) {
                notes.add("Vendor ledger reversed by " + formatMoney(vendorRefund) + " BDT.");
            }
        }

        return joinNotes(notes);
    }

    private boolean cancelPendingEmiIfNecessary(SalesOrder order) {
        return emiPaymentPlanService.findByOrderId(order.getId())
                .filter(plan -> plan.isProviderPending())
                .map(plan -> {
                    emiPaymentPlanService.cancelPlan(plan, "Order cancelled before provider decision.");
                    return true;
                })
                .orElse(false);
    }

    private int cancelOpenShipmentsForOrder(SalesOrder order) {
        if (order == null || order.getId() == null) {
            return 0;
        }

        int cancelledCount = 0;
        for (Shipment shipment : shipmentRepository.findBySalesOrderId(order.getId())) {
            if (shipment == null || shipment.getStatus() == ShipmentStatus.CANCELLED
                    || shipment.getStatus() == ShipmentStatus.DELIVERED
                    || shipment.getStatus() == ShipmentStatus.RETURNED) {
                continue;
            }
            shipment.setStatus(ShipmentStatus.CANCELLED);
            shipmentRepository.save(shipment);
            cancelledCount++;
        }
        return cancelledCount;
    }

    private void syncVendorFinanceWithOrderStatus(SalesOrder order, OrderStatus nextStatus, String reason) {
        if (nextStatus == OrderStatus.DELIVERED || nextStatus == OrderStatus.COMPLETED) {
            vendorFinanceService.createOrderTransactionIfMissing(order);
        }

        if (nextStatus == OrderStatus.RETURNED) {
            vendorFinanceService.createRefundTransactionIfNeeded(order, reason);
        }
    }

    private Set<Long> normalizeOrderItemIds(List<Long> itemIds) {
        Set<Long> uniqueIds = new LinkedHashSet<>();
        if (itemIds == null) {
            return uniqueIds;
        }

        for (Long itemId : itemIds) {
            if (itemId != null) {
                uniqueIds.add(itemId);
            }
        }
        return uniqueIds;
    }

    private OrderItem getOrderItemForOrder(SalesOrder order, Long orderItemId) {
        if (order == null || order.getId() == null || orderItemId == null) {
            throw new IllegalArgumentException("Order item not found.");
        }

        return orderItemRepository.findById(orderItemId)
                .filter(item -> item.getSalesOrder() != null && order.getId().equals(item.getSalesOrder().getId()))
                .orElseThrow(() -> new IllegalArgumentException("Order item not found."));
    }

    private void validateItemReturnRequestEligibility(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Order item not found.");
        }

        if (isVirtualItem(item)) {
            throw new IllegalArgumentException("Virtual items cannot be returned from this flow.");
        }

        OrderItemReturnStatus returnStatus = resolveReturnStatus(item.getReturnStatus());
        if (returnStatus == OrderItemReturnStatus.RETURN_REQUESTED) {
            throw new IllegalArgumentException("One or more selected items already have a return request in progress.");
        }
        if (returnStatus == OrderItemReturnStatus.RETURNED) {
            throw new IllegalArgumentException("One or more selected items were already returned.");
        }
    }

    private void validateItemReturnProcessing(SalesOrder order, OrderItem orderItem) {
        if (order == null || orderItem == null) {
            throw new IllegalArgumentException("Order item not found.");
        }

        if (!canMarkReturnedFromStatus(order.getStatus())) {
            throw new IllegalArgumentException("This order is not in a returnable state.");
        }

        if (isVirtualItem(orderItem)) {
            throw new IllegalArgumentException("Virtual items cannot be marked returned.");
        }

        OrderItemReturnStatus returnStatus = resolveReturnStatus(orderItem.getReturnStatus());
        if (returnStatus == OrderItemReturnStatus.RETURNED) {
            throw new IllegalArgumentException("This item has already been returned.");
        }

        if (returnStatus != OrderItemReturnStatus.RETURN_REQUESTED) {
            throw new IllegalArgumentException("This item must be requested for return before it can be marked returned.");
        }
    }

    private OrderStatus resolveOrderStatusAfterItemReturn(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findBySalesOrder_Id(orderId);
        boolean hasPendingReturns = orderItems.stream()
                .anyMatch(item -> resolveReturnStatus(item.getReturnStatus()) == OrderItemReturnStatus.RETURN_REQUESTED);
        if (hasPendingReturns) {
            return OrderStatus.RETURN_REQUESTED;
        }

        boolean hasReturnedItems = orderItems.stream()
                .anyMatch(item -> resolveReturnStatus(item.getReturnStatus()) == OrderItemReturnStatus.RETURNED);
        if (!hasReturnedItems) {
            return getOrderOrThrow(orderId).getStatus();
        }

        SalesOrder order = getOrderOrThrow(orderId);
        return defaultMoney(order.getGrandTotal()).compareTo(BigDecimal.ZERO) == 0
                ? OrderStatus.RETURNED
                : OrderStatus.PARTIALLY_RETURNED;
    }

    private boolean isCustomerReturnRequestEligible(OrderItem item) {
        return item != null
                && !isVirtualItem(item)
                && resolveReturnStatus(item.getReturnStatus()) == OrderItemReturnStatus.NONE;
    }

    private boolean isVirtualItem(OrderItem item) {
        return item != null
                && item.getProduct() != null
                && item.getProduct().getProductType() == ProductTypeEnum.Virtual;
    }

    private boolean isReturnedItem(OrderItem item) {
        return resolveReturnStatus(item != null ? item.getReturnStatus() : null) == OrderItemReturnStatus.RETURNED;
    }

    private OrderItemReturnStatus resolveReturnStatus(OrderItemReturnStatus status) {
        return status != null ? status : OrderItemReturnStatus.NONE;
    }

    private OrderItemReturnStatus resolveReturnStatus(Object value) {
        if (value instanceof OrderItemReturnStatus status) {
            return resolveReturnStatus(status);
        }

        if (value instanceof String statusName && !statusName.isBlank()) {
            try {
                return resolveReturnStatus(OrderItemReturnStatus.valueOf(statusName.trim()));
            } catch (IllegalArgumentException ex) {
                return OrderItemReturnStatus.NONE;
            }
        }

        return OrderItemReturnStatus.NONE;
    }

    private ProductTypeEnum resolveProductType(Object value) {
        if (value instanceof ProductTypeEnum productType) {
            return productType;
        }

        if (value instanceof String productTypeName && !productTypeName.isBlank()) {
            try {
                return ProductTypeEnum.valueOf(productTypeName.trim());
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }

        return null;
    }

    private String buildReturnStatusLabel(OrderItemReturnStatus returnStatus) {
        if (returnStatus == OrderItemReturnStatus.RETURN_REQUESTED) {
            return "Return Requested";
        }
        if (returnStatus == OrderItemReturnStatus.RETURNED) {
            return "Returned";
        }
        return "Not Requested";
    }

    private String buildItemReturnBlockedReason(boolean returnWindowOpen, boolean virtualItem,
            OrderItemReturnStatus returnStatus) {
        if (virtualItem) {
            return "Virtual items cannot be returned from this flow.";
        }
        if (returnStatus == OrderItemReturnStatus.RETURN_REQUESTED) {
            return "This item already has a return request in progress.";
        }
        if (returnStatus == OrderItemReturnStatus.RETURNED) {
            return "This item has already been returned.";
        }
        if (!returnWindowOpen) {
            return "Item returns become available after delivery or completion.";
        }
        return null;
    }

    private String buildItemReturnRequestNote(List<String> itemNames) {
        if (itemNames == null || itemNames.isEmpty()) {
            return "Return requested for selected items.";
        }
        return "Return requested for " + itemNames.size() + " item(s): " + String.join(", ", itemNames) + ".";
    }

    private String buildItemReturnProcessedNote(OrderItem orderItem, PaymentService.RefundResult refundResult,
            BigDecimal vendorRefund, OrderStatus resolvedStatus) {
        List<String> notes = new ArrayList<>();
        notes.add("Returned item " + resolveOrderItemLabel(orderItem) + ".");

        if (refundResult != null && refundResult.isRefunded()) {
            notes.add("Customer refunded " + formatMoney(refundResult.getAmount())
                    + " BDT via " + refundDisplayName(refundResult) + ".");
        } else {
            notes.add("No immediate customer refund was needed after recalculating the order balance.");
        }

        if (vendorRefund != null && vendorRefund.compareTo(BigDecimal.ZERO) > 0) {
            notes.add("Vendor ledger reversed by " + formatMoney(vendorRefund) + " BDT.");
        }

        if (resolvedStatus == OrderStatus.PARTIALLY_RETURNED) {
            notes.add("Order now remains partially returned with active items still open.");
        }

        if (resolvedStatus == OrderStatus.RETURN_REQUESTED) {
            notes.add("Other item return requests are still pending on this order.");
        }

        return joinNotes(notes);
    }

    private String resolveOrderItemLabel(OrderItem orderItem) {
        if (orderItem == null) {
            return "item";
        }

        if (orderItem.getProduct() != null && cleanText(orderItem.getProduct().getTitle()) != null) {
            return "\"" + cleanText(orderItem.getProduct().getTitle()) + "\"";
        }

        return "item #" + orderItem.getId();
    }

    private void saveOrderHistory(SalesOrder order, OrderStatus status, OrderStatusChangedBy changedBy, String remark) {
        OrderHistory orderHistory = new OrderHistory();
        orderHistory.setSalesOrder(order);
        orderHistory.setStatus(status);
        orderHistory.setOrderStatusChanged(changedBy);
        orderHistory.setRemark(cleanText(remark));
        orderHistoryRepository.save(orderHistory);
    }

    private String buildHistoryRemark(String remark, String systemNote) {
        String cleanedRemark = cleanText(remark);
        String cleanedSystemNote = cleanText(systemNote);

        if (cleanedRemark == null) {
            return cleanedSystemNote;
        }

        if (cleanedSystemNote == null) {
            return cleanedRemark;
        }

        return cleanedRemark + " | " + cleanedSystemNote;
    }

    private String joinNotes(List<String> notes) {
        StringBuilder builder = new StringBuilder();
        for (String note : notes) {
            String cleaned = cleanText(note);
            if (cleaned == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(cleaned);
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String refundDisplayName(PaymentService.RefundResult refundResult) {
        return refundResult.getPaymentMethod() != null
                ? refundResult.getPaymentMethod().getDisplayName()
                : "the original payment method";
    }

    private String formatMoney(BigDecimal amount) {
        return amount == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP).toPlainString()
                : amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private BigDecimal defaultMoney(BigDecimal amount) {
        return amount == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : amount.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean hasShipmentForOrder(Long orderId) {
        return orderId != null && shipmentRepository.existsBySalesOrderId(orderId);
    }

    private List<OrderStatus> buildBackOfficeStatusOptions(SalesOrder order, boolean marketplace) {
        if (order == null || order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.RETURNED) {
            return List.of();
        }

        return Arrays.stream(OrderStatus.values())
                .filter(status -> status != OrderStatus.RETURN_REQUESTED)
                .filter(status -> status != OrderStatus.PARTIALLY_RETURNED)
                .filter(status -> status != OrderStatus.RETURNED)
                .filter(status -> status != OrderStatus.CANCELLED
                || (marketplace ? canMarketplaceCancelOrder(order) : canVendorCancelOrder(order)))
                .collect(Collectors.toList());
    }

    private boolean isSoldStatus(OrderStatus status) {
        return status == OrderStatus.SHIPPED
                || status == OrderStatus.IN_TRANSIT
                || status == OrderStatus.OUT_FOR_DELIVERY
                || status == OrderStatus.DELIVERED
                || status == OrderStatus.COMPLETED;
    }
}
