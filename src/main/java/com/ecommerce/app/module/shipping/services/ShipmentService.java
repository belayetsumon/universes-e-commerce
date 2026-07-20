package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.communication.events.CommunicationRequestedEvent;
import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.fraud.services.CodRiskProfileService;
import com.ecommerce.app.module.fraud.services.FraudFulfilmentGuard;
import com.ecommerce.app.module.fraud.services.FraudPostOrderMonitoringService;
import com.ecommerce.app.module.fraud.services.VendorRiskProfileService;
import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.model.ShipmentStatus;
import com.ecommerce.app.module.shipping.repository.ShipmentRepository;
import com.ecommerce.app.module.order.model.OrderStatus;
import com.ecommerce.app.module.order.model.PaymentMethod;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.repository.OrderItemRepository;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import com.ecommerce.app.module.order.services.PaymentService;
import com.ecommerce.app.module.order.services.SalesOrderService;
import com.ecommerce.app.product.model.ProductTypeEnum;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShipmentService {

    private static final EnumSet<OrderStatus> SHIPMENT_ELIGIBLE_STATUSES = EnumSet.of(
            //            OrderStatus.CONFIRMED,
            //            OrderStatus.PROCESSING,
            OrderStatus.PACKED
    );

    private final ShipmentRepository repo;
    private final SalesOrderRepository salesOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SalesOrderService salesOrderService;
    private final PaymentService paymentService;
    private final ShipmentTrackingService shipmentTrackingService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final FraudFulfilmentGuard fraudFulfilmentGuard;
    private final CodRiskProfileService codRiskProfileService;
    private final FraudPostOrderMonitoringService fraudPostOrderMonitoringService;
    private final VendorRiskProfileService vendorRiskProfileService;

    public ShipmentService(ShipmentRepository repo, SalesOrderRepository salesOrderRepository,
            OrderItemRepository orderItemRepository,
            SalesOrderService salesOrderService,
            PaymentService paymentService,
            ShipmentTrackingService shipmentTrackingService,
            ApplicationEventPublisher applicationEventPublisher,
            FraudFulfilmentGuard fraudFulfilmentGuard,
            CodRiskProfileService codRiskProfileService,
            FraudPostOrderMonitoringService fraudPostOrderMonitoringService,
            VendorRiskProfileService vendorRiskProfileService) {
        this.repo = repo;
        this.salesOrderRepository = salesOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.salesOrderService = salesOrderService;
        this.paymentService = paymentService;
        this.shipmentTrackingService = shipmentTrackingService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.fraudFulfilmentGuard = fraudFulfilmentGuard;
        this.codRiskProfileService = codRiskProfileService;
        this.fraudPostOrderMonitoringService = fraudPostOrderMonitoringService;
        this.vendorRiskProfileService = vendorRiskProfileService;
    }

    public List<Shipment> getAll() {
        return repo.findAll();
    }

    public List<Shipment> getByVendor(Long vendorId) {
        return repo.findByVendorId(vendorId);
    }

    public Shipment getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Shipment getLatestByOrderId(Long orderId) {
        if (orderId == null) {
            return null;
        }
        return repo.findTopBySalesOrderIdOrderByIdDesc(orderId).orElse(null);
    }

    public boolean hasShipmentForOrder(Long orderId) {
        return orderId != null && repo.existsBySalesOrderId(orderId);
    }

    @Transactional
    public Shipment save(Shipment shipment) {
        if (shipment == null) {
            throw new IllegalArgumentException("Shipment is required.");
        }

        if (shipment.getId() == null) {
            Shipment repeatedShipment = resolveRepeatedCreate(shipment);
            if (repeatedShipment != null) {
                return repeatedShipment;
            }
            enforceSingleShipmentPerOrder(shipment);
            enforceFraudShipmentAllowed(shipment);
        }

        ShipmentStatus previousStatus = null;
        if (shipment.getId() != null) {
            previousStatus = repo.findById(shipment.getId())
                    .map(Shipment::getStatus)
                    .orElse(null);
        }
        Shipment preparedShipment = syncCodFromOrder(shipment);
        if (preparedShipment != null) {
            normalizeShipmentReferences(preparedShipment);
            enforceUniqueTrackingNumber(preparedShipment);
            applyCarrierDefaults(preparedShipment);
            preparedShipment.recalculateSettlementAmounts();
        }
        Shipment savedShipment = repo.saveAndFlush(preparedShipment);
        shipmentTrackingService.recordStatusChange(savedShipment, previousStatus, savedShipment.getStatus(), "shipment form");
        recordCodOutcomeIfNeeded(savedShipment, previousStatus);
        recordFraudShipmentEvent(savedShipment, previousStatus);
        notifyShipmentEvents(savedShipment, previousStatus);
        return savedShipment;
    }

    private Shipment resolveRepeatedCreate(Shipment shipment) {
        if (shipment == null || shipment.getId() != null) {
            return null;
        }

        String uuid = normalizeUuid(shipment.getUuid());
        shipment.setUuid(uuid);
        Shipment existingByUuid = repo.findByUuid(uuid).orElse(null);
        if (existingByUuid == null) {
            return null;
        }

        if (sameBusinessShipment(existingByUuid, shipment)) {
            return existingByUuid;
        }

        throw new IllegalArgumentException("Shipment request token was already used for another order.");
    }

    private void enforceSingleShipmentPerOrder(Shipment shipment) {
        if (shipment == null || shipment.getSalesOrderId() == null) {
            return;
        }
        Shipment existingByOrder = getLatestByOrderId(shipment.getSalesOrderId());
        if (existingByOrder != null) {
            throw new IllegalArgumentException("A shipment already exists for the selected sales order.");
        }
    }

    private boolean sameBusinessShipment(Shipment existingShipment, Shipment requestedShipment) {
        return existingShipment != null
                && requestedShipment != null
                && Objects.equals(existingShipment.getSalesOrderId(), requestedShipment.getSalesOrderId())
                && Objects.equals(existingShipment.getVendorId(), requestedShipment.getVendorId());
    }

    private String normalizeUuid(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return uuid.trim();
    }

    private void normalizeShipmentReferences(Shipment shipment) {
        if (shipment == null) {
            return;
        }
        shipment.setTrackingNumber(normalizeOptionalText(shipment.getTrackingNumber()));
        shipment.setLabelUrl(normalizeOptionalText(shipment.getLabelUrl()));
        shipment.setMetadataJson(normalizeOptionalText(shipment.getMetadataJson()));
    }

    private void enforceUniqueTrackingNumber(Shipment shipment) {
        if (shipment == null || shipment.getTrackingNumber() == null) {
            return;
        }
        Shipment shipmentWithTracking = repo.findByTrackingNumber(shipment.getTrackingNumber()).orElse(null);
        if (shipmentWithTracking != null && !Objects.equals(shipmentWithTracking.getId(), shipment.getId())) {
            vendorRiskProfileService.recordTrackingReuseAttempt(shipment, shipmentWithTracking);
            throw new IllegalArgumentException("Tracking number already exists on shipment #" + shipmentWithTracking.getId() + ".");
        }
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void applyCarrierDefaults(Shipment shipment) {
        if (shipment == null || shipment.getCarrier() == null) {
            return;
        }
        shipment.setCarrierModeSnapshot(shipment.getCarrier().getMode());
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public Shipment syncCodFromOrder(Shipment shipment) {
        if (shipment == null || shipment.getSalesOrderId() == null) {
            return shipment;
        }

        SalesOrder order = salesOrderRepository.findById(shipment.getSalesOrderId()).orElse(null);
        if (order == null) {
            return shipment;
        }

        PaymentService.PaymentSummary summary = paymentService.getPaymentSummary(order);
        BigDecimal plannedCodDue = summary.getCodDue();
        BigDecimal remainingCodDue = summary.getRemainingCodDue();

        shipment.setVendorId(order.getVendorId());
        shipment.setShippingCost(defaultAmount(order.getDeliveryCharge()));
        shipment.setDistrict(resolveShippingDistrict(order));
        shipment.setTotalOrderAmount(defaultAmount(order.getGrandTotal()));
        shipment.setCod(plannedCodDue.compareTo(BigDecimal.ZERO) > 0);
        shipment.setCodPending(remainingCodDue);
        shipment.setCodCollected(plannedCodDue.subtract(remainingCodDue).max(BigDecimal.ZERO));
        return shipment;
    }

    @Transactional
    public void collectPayment(Long shipmentId, BigDecimal amount) {
        Shipment shipment = repo.findById(shipmentId).orElseThrow();
        collectPayment(shipment, amount);
    }

    @Transactional
    public void collectVendorPayment(Long vendorId, Long shipmentId, BigDecimal amount) {
        Shipment shipment = repo.findById(shipmentId).orElseThrow();
        if (vendorId == null || shipment.getVendorId() == null || !vendorId.equals(shipment.getVendorId())) {
            throw new IllegalArgumentException("Shipment not found for the active vendor.");
        }
        collectPayment(shipment, amount);
    }

    private void collectPayment(Shipment shipment, BigDecimal amount) {
        ShipmentStatus previousStatus = shipment.getStatus();
        BigDecimal normalizedAmount = defaultAmount(amount);
        if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("COD collection amount must be greater than zero.");
        }

        SalesOrder order = shipment.getSalesOrderId() != null
                ? salesOrderRepository.findById(shipment.getSalesOrderId()).orElse(null)
                : null;

        if (order != null) {
            paymentService.recordPayment(
                    order,
                    PaymentMethod.COD,
                    normalizedAmount,
                    null,
                    "COD collected for shipment #" + shipment.getId()
            );
            PaymentService.PaymentSummary summary = paymentService.getPaymentSummary(order);
            shipment.setCod(shipment.isCod() || summary.getCodDue().compareTo(BigDecimal.ZERO) > 0);
            shipment.setTotalOrderAmount(defaultAmount(order.getGrandTotal()));
            shipment.setCodCollected(summary.getCodDue().subtract(summary.getRemainingCodDue()).max(BigDecimal.ZERO));
            shipment.setCodPending(summary.getRemainingCodDue());
        } else {
            shipment.setCodCollected(defaultAmount(shipment.getCodCollected()).add(normalizedAmount));
            shipment.setCodPending(defaultAmount(shipment.getCodPending()).subtract(normalizedAmount).max(BigDecimal.ZERO));
        }

        if (defaultAmount(shipment.getCodPending()).compareTo(BigDecimal.ZERO) <= 0) {
            shipment.setStatus(ShipmentStatus.DELIVERED);
        }

        Shipment savedShipment = repo.save(shipment);
        shipmentTrackingService.recordStatusChange(savedShipment, previousStatus, savedShipment.getStatus(), "COD collection");
        recordCodOutcomeIfNeeded(savedShipment, previousStatus);
        recordFraudShipmentEvent(savedShipment, previousStatus);
        notifyShipmentEvents(savedShipment, previousStatus);
    }

    private void notifyShipmentEvents(Shipment shipment, ShipmentStatus previousStatus) {
        try {
            SalesOrder order = shipment != null && shipment.getSalesOrderId() != null
                    ? salesOrderRepository.findById(shipment.getSalesOrderId()).orElse(null)
                    : null;
            if (order == null || order.getCustomer() == null || order.getCustomer().getEmail() == null) {
                return;
            }

            if (previousStatus == null) {
                applicationEventPublisher.publishEvent(CommunicationRequestedEvent.shipment(
                        MessageEventType.SHIPMENT_CREATED,
                        MessageChannel.EMAIL,
                        order.getCustomer().getEmail(),
                        shipment.getTrackingNumber(),
                        shipmentVariables(order, shipment)
                ));
            }

            if (shipment.getTrackingNumber() != null && !shipment.getTrackingNumber().isBlank()) {
                applicationEventPublisher.publishEvent(CommunicationRequestedEvent.shipment(
                        MessageEventType.TRACKING_NUMBER_ADDED,
                        MessageChannel.EMAIL,
                        order.getCustomer().getEmail(),
                        shipment.getTrackingNumber(),
                        shipmentVariables(order, shipment)
                ));
            }

            MessageEventType statusEvent = resolveShipmentEvent(shipment.getStatus());
            if (statusEvent != null && previousStatus != shipment.getStatus()) {
                applicationEventPublisher.publishEvent(CommunicationRequestedEvent.shipment(
                        statusEvent,
                        MessageChannel.EMAIL,
                        order.getCustomer().getEmail(),
                        shipment.getTrackingNumber(),
                        shipmentVariables(order, shipment)
                ));
            }
        } catch (Exception ignored) {
            // Communication failure must not block shipment processing.
        }
    }

    private Map<String, Object> shipmentVariables(SalesOrder order, Shipment shipment) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", order.getCustomer() != null && order.getCustomer().getFirstName() != null ? order.getCustomer().getFirstName() : "");
        variables.put("orderNumber", order.getOrderCode() != null ? order.getOrderCode() : "");
        variables.put("orderTotal", defaultAmount(order.getGrandTotal()).toPlainString());
        variables.put("trackingNumber", shipment != null && shipment.getTrackingNumber() != null ? shipment.getTrackingNumber() : "");
        return variables;
    }

    private MessageEventType resolveShipmentEvent(ShipmentStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case SHIPPED, IN_TRANSIT -> MessageEventType.ORDER_SHIPPED;
            case OUT_FOR_DELIVERY -> MessageEventType.ORDER_OUT_FOR_DELIVERY;
            case DELIVERED -> MessageEventType.ORDER_DELIVERED;
            case FAILED -> MessageEventType.DELIVERY_FAILED;
            default -> null;
        };
    }

    public boolean isShipmentEligibleStatus(OrderStatus status) {
        return status != null && SHIPMENT_ELIGIBLE_STATUSES.contains(status);
    }

    public boolean isOrderReadyForShipment(SalesOrder order) {
        return getShipmentBlockReason(order) == null;
    }

    public boolean canCreateNewShipment(SalesOrder order) {
        return isOrderReadyForShipment(order)
                && order != null
                && order.getId() != null
                && !hasShipmentForOrder(order.getId());
    }

    public String getShipmentBlockReason(SalesOrder order) {
        if (order == null || order.getId() == null) {
            return "Order not found.";
        }
        if (!isShipmentEligibleStatus(order.getStatus())) {
            return "Order status is not eligible for shipment creation.";
        }
        if (order.getVendorId() == null) {
            return "Order vendor is missing.";
        }
        if (salesOrderService.orderContainsOnlyVirtualItems(order.getId())) {
            return "This order contains only virtual items.";
        }
        FraudGuardResult fraudGuard = fraudFulfilmentGuard.checkShipmentCreationAllowed(order);
        if (!fraudGuard.isAllowed()) {
            return fraudGuard.getReason();
        }
        String district = resolveShippingDistrict(order);
        if (district.isBlank()) {
            return "Shipping address district is missing.";
        }
        return null;
    }

    public List<SalesOrder> getEligibleOrders(Long vendorId, Long includeOrderId) {
        List<SalesOrder> orders = vendorId == null
                ? salesOrderRepository.findAllByOrderByIdDesc()
                : salesOrderRepository.findByVendorIdOrderByIdDesc(vendorId);

        List<SalesOrder> eligibleOrders = orders.stream()
                .filter(this::canCreateNewShipment)
                .toList();

        if (includeOrderId == null) {
            return eligibleOrders;
        }

        boolean alreadyIncluded = eligibleOrders.stream()
                .anyMatch(order -> includeOrderId.equals(order.getId()));
        if (alreadyIncluded) {
            return eligibleOrders;
        }

        SalesOrder includedOrder = salesOrderRepository.findById(includeOrderId).orElse(null);
        if (includedOrder == null) {
            return eligibleOrders;
        }
        if (vendorId != null && !vendorId.equals(includedOrder.getVendorId())) {
            return eligibleOrders;
        }

        List<SalesOrder> merged = new java.util.ArrayList<>();
        merged.add(includedOrder);
        merged.addAll(eligibleOrders);
        return merged;
    }

    public List<Map<String, Object>> enrichOrderRowsWithShipmentData(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        Set<Long> orderIds = new HashSet<>();
        for (Map<String, Object> row : rows) {
            Long orderId = asLong(row.get("orderId"));
            if (orderId != null) {
                orderIds.add(orderId);
            }
        }

        if (orderIds.isEmpty()) {
            return rows;
        }

        Map<Long, SalesOrder> ordersById = getOrdersByIdWithShippingAddress(orderIds);
        Map<Long, Shipment> latestShipmentsByOrderId = getLatestShipmentsByOrderId(orderIds);
        Set<Long> ordersWithShipments = latestShipmentsByOrderId.keySet();
        Set<Long> virtualOnlyOrderIds = getVirtualOnlyOrderIds(orderIds);
        for (Map<String, Object> row : rows) {
            Long orderId = asLong(row.get("orderId"));
            if (orderId == null) {
                row.put("shipmentEligible", false);
                row.put("hasShipment", false);
                row.put("existingShipmentId", null);
                continue;
            }

            SalesOrder order = ordersById.get(orderId);
            Shipment existingShipment = latestShipmentsByOrderId.get(orderId);
            row.put("shipmentEligible", order != null && canCreateNewShipmentForReport(order, virtualOnlyOrderIds.contains(orderId)));
            row.put("hasShipment", ordersWithShipments.contains(orderId));
            row.put("existingShipmentId", existingShipment != null ? existingShipment.getId() : null);
        }

        return rows;
    }

    private Map<Long, SalesOrder> getOrdersByIdWithShippingAddress(Collection<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, SalesOrder> ordersById = new HashMap<>();
        for (SalesOrder order : salesOrderRepository.findByIdInWithShippingAddress(orderIds)) {
            if (order != null && order.getId() != null) {
                ordersById.put(order.getId(), order);
            }
        }
        return ordersById;
    }

    private Map<Long, Shipment> getLatestShipmentsByOrderId(Collection<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Shipment> latestShipmentsByOrderId = new HashMap<>();
        for (Shipment shipment : repo.findBySalesOrderIdIn(orderIds)) {
            if (shipment == null || shipment.getSalesOrderId() == null) {
                continue;
            }
            Shipment current = latestShipmentsByOrderId.get(shipment.getSalesOrderId());
            if (current == null
                    || current.getId() == null
                    || (shipment.getId() != null && shipment.getId() > current.getId())) {
                latestShipmentsByOrderId.put(shipment.getSalesOrderId(), shipment);
            }
        }
        return latestShipmentsByOrderId;
    }

    private Set<Long> getVirtualOnlyOrderIds(Collection<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(orderItemRepository.findOrderIdsContainingOnlyProductType(orderIds, ProductTypeEnum.Virtual));
    }

    private boolean canCreateNewShipmentForReport(SalesOrder order, boolean virtualOnlyOrder) {
        if (order == null || order.getId() == null) {
            return false;
        }
        if (!isShipmentEligibleStatus(order.getStatus())) {
            return false;
        }
        if (order.getVendorId() == null || virtualOnlyOrder) {
            return false;
        }
        return !resolveShippingDistrict(order).isBlank();
    }

    private void enforceFraudShipmentAllowed(Shipment shipment) {
        if (shipment == null || shipment.getSalesOrderId() == null) {
            return;
        }
        SalesOrder order = salesOrderRepository.findById(shipment.getSalesOrderId()).orElse(null);
        FraudGuardResult fraudGuard = fraudFulfilmentGuard.checkShipmentCreationAllowed(order);
        if (!fraudGuard.isAllowed()) {
            throw new IllegalArgumentException(fraudGuard.getReason());
        }
    }

    private void recordCodOutcomeIfNeeded(Shipment shipment, ShipmentStatus previousStatus) {
        if (shipment == null || shipment.getStatus() == null || shipment.getStatus() == previousStatus
                || shipment.getSalesOrderId() == null) {
            return;
        }
        SalesOrder order = salesOrderRepository.findById(shipment.getSalesOrderId()).orElse(null);
        if (order == null) {
            return;
        }
        if (shipment.getStatus() == ShipmentStatus.DELIVERED && !shipment.isCod()) {
            codRiskProfileService.recordSuccessfulPrepaidOrder(order, null);
            return;
        }
        codRiskProfileService.recordCodShipmentOutcome(order, shipment.getStatus(), resolveCodOutcomeReason(shipment), null);
    }

    private void recordFraudShipmentEvent(Shipment shipment, ShipmentStatus previousStatus) {
        if (shipment == null || shipment.getSalesOrderId() == null) {
            return;
        }
        SalesOrder order = salesOrderRepository.findById(shipment.getSalesOrderId()).orElse(null);
        if (order == null) {
            return;
        }
        if (previousStatus == null) {
            fraudPostOrderMonitoringService.recordShipmentCreated(order, shipment.getId(), shipment.getTrackingNumber());
        }
        if (shipment.getStatus() != null && shipment.getStatus() != previousStatus) {
            fraudPostOrderMonitoringService.recordShipmentStatus(order, shipment.getId(), shipment.getStatus(), resolveCodOutcomeReason(shipment));
            vendorRiskProfileService.recordDeliveryConfirmation(shipment, order);
        }
        if (shipment.getVendorId() != null) {
            vendorRiskProfileService.refreshVendorProfile(shipment.getVendorId());
        }
    }

    private String resolveCodOutcomeReason(Shipment shipment) {
        if (shipment == null || shipment.getMetadataJson() == null || shipment.getMetadataJson().isBlank()) {
            return shipment == null || shipment.getStatus() == null ? null : "Shipment status: " + shipment.getStatus();
        }
        return shipment.getMetadataJson();
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(String.valueOf(value).trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String resolveShippingDistrict(SalesOrder order) {
        if (order == null || order.getShippingAddress() == null || order.getShippingAddress().getDistrict() == null) {
            return "";
        }
        return order.getShippingAddress().getDistrict().trim();
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }
}
