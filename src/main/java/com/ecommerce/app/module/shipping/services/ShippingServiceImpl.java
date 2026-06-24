/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.dto.ShippingOption;
import com.ecommerce.app.module.shipping.model.Carrier;
import com.ecommerce.app.module.shipping.model.CarrierMode;
import com.ecommerce.app.module.shipping.model.DeliverySpeed;
import com.ecommerce.app.module.shipping.model.DeliveryType;
import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.model.ShipmentStatus;
import com.ecommerce.app.module.shipping.repository.ShipmentRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.app.module.shipping.repository.CarrierRepository;

/**
 *
 * @author libertyerp_local
 */
@Service
public class ShippingServiceImpl implements ShippingService {

    @Autowired
    private ShipmentRepository shipmentRepository;
    @Autowired
    private final CarrierRepository carrierRepository;

    private final Map<String, CarrierAdapter> adapters;
    private final ShipmentTrackingService shipmentTrackingService;

    public ShippingServiceImpl(ShipmentRepository shipmentRepository,
            CarrierRepository carrierRepository,
            List<CarrierAdapter> adapterList,
            ShipmentTrackingService shipmentTrackingService) {
        this.shipmentRepository = shipmentRepository;
        this.carrierRepository = carrierRepository;
        this.shipmentTrackingService = shipmentTrackingService;
        this.adapters = new HashMap<>();

        for (CarrierAdapter a : adapterList) {
            adapters.put(a.getCarrierCode(), a);
        }
    }

    @Override
    public List<ShippingOption> calculateOptionsForCart(Map<String, Object> cart) {
        ShippingOption standard = new ShippingOption(
                "local_sim_standard",
                "Local Standard",
                new BigDecimal("40.00"),
                "3-5 days",
                "local_sim"
        );

        ShippingOption express = new ShippingOption(
                "local_sim_express",
                "Local Express",
                new BigDecimal("80.00"),
                "1-2 days",
                "local_sim"
        );

        return Arrays.asList(standard, express);
    }

    @Override
    @Transactional
    public Shipment createShipmentForOrder(Long orderId, Long vendorId,
            ShippingOption option, Map<String, Object> meta
    ) {
        // 2026-04-22: Resolve carrier first because Shipment requires a non-null carrier before the first save.
        Carrier carrier = carrierRepository.findByCode(option.getCarrierCode())
                .orElseThrow(() -> new IllegalArgumentException("Carrier not found for code: " + option.getCarrierCode()));

        Shipment sh = new Shipment();
        sh.setSalesOrderId(orderId);
        sh.setVendorId(vendorId);
        sh.setShippingCost(option.getPrice());
        sh.setCarrier(carrier);
        sh.setDistrict(extractLocation(meta));
        sh.setSpeed(option.getSpeed() != null ? option.getSpeed() : DeliverySpeed.STANDARD);
        sh.setDeliveryType(option.getDeliveryType() != null ? option.getDeliveryType() : DeliveryType.HOME_DELIVERY);
        sh.setCarrierModeSnapshot(carrier.getMode());
        sh.setSettlementMode(carrier.getSettlementMode());
        sh.setShippingChargeOwner(carrier.getShippingChargeOwner());
        sh.setCodCollectionMode(carrier.getCodCollectionMode());
        sh.setStatus(ShipmentStatus.PENDING);
        sh.setCod(extractBoolean(meta, "cod"));
        sh.setTotalOrderAmount(extractAmount(meta, "totalOrderAmount", "grandTotal"));
        sh.setCodCollected(extractAmount(meta, "codCollected"));
        sh.setCodPending(extractAmount(meta, "codPending", "remainingCodDue", "codDue"));
        sh.setProductNetAmount(extractAmount(meta, "productNetAmount", "vendorProductAmount", "totalVendorAmount"));
        sh.setMarketplaceCommissionAmount(extractAmount(meta, "marketplaceCommissionAmount", "totalMarketPlaceCommissionAmount"));
        sh.setCodFeeAmount(extractAmount(meta, "codFeeAmount", "codFee"));
        sh.setShippingPaidToMarketplace(extractBoolean(meta, "shippingPaidToMarketplace"));
        if (sh.isCod() && sh.getCodPending().compareTo(BigDecimal.ZERO) <= 0) {
            sh.setCodPending(sh.getTotalOrderAmount());
        }
        sh.setMetadataJson(buildInitialMetadata(option, carrier, meta));
        sh.recalculateSettlementAmounts();

        // 2026-04-22: Vendor/self/rider/pickup deliveries stay local and skip external carrier API calls.
        boolean shouldCreateExternalShipment = carrier.getMode() == CarrierMode.THIRD_PARTY && carrier.isRequiresApi();
        CarrierAdapter adapter = adapters.get(option.getCarrierCode());

        if (shouldCreateExternalShipment && adapter != null) {
            Map<String, Object> req = new HashMap<>();
            req.put("orderId", orderId);
            req.put("vendorId", vendorId);
            req.put("option", option);
            req.put("meta", meta);
            String tracking = adapter.createShipment(req);
            sh.setTrackingNumber(tracking);
            sh.setStatus(ShipmentStatus.IN_TRANSIT);
        } else if (carrier.isTrackable()) {
            sh.setTrackingNumber(buildManualTrackingNumber(carrier));
        }

        Shipment savedShipment = shipmentRepository.save(sh);
        shipmentTrackingService.recordStatusChange(savedShipment, null, savedShipment.getStatus(), "shipping service");
        return savedShipment;
    }

    @Override
    @Transactional
    public void handleCarrierWebhook(String carrierCode, Map<String, Object> payload
    ) {
        CarrierAdapter adapter = adapters.get(carrierCode);
        if (adapter == null) {
            return;
        }
        Map<String, Object> parsed = adapter.parseWebhook(payload);
        Object tObj = parsed.get("trackingNumber");
        if (tObj == null) {
            return;
        }
        String tracking = String.valueOf(tObj);
        String status = String.valueOf(parsed.getOrDefault("status", ShipmentStatus.IN_TRANSIT));

        Optional<Shipment> os = shipmentRepository.findByTrackingNumber(tracking);
        if (os.isPresent()) {
            Shipment s = os.get();
            ShipmentStatus previousStatus = s.getStatus();
            ShipmentStatus shipmentStatus;
            try {
                shipmentStatus = ShipmentStatus.valueOf(status.toUpperCase());
            } catch (Exception ex) {
                shipmentStatus = ShipmentStatus.IN_TRANSIT;
            }
            s.setStatus(shipmentStatus);
            Shipment savedShipment = shipmentRepository.save(s);
            shipmentTrackingService.recordStatusChange(savedShipment, previousStatus, shipmentStatus, "carrier webhook");
        }
    }

    private String extractLocation(Map<String, Object> meta) {
        if (meta == null || meta.isEmpty()) {
            return "Unknown Location";
        }

        Object location = meta.get("location");
        if (location == null) {
            location = meta.get("shippingLocation");
        }
        if (location == null) {
            location = meta.get("district");
        }

        String locationText = location != null ? String.valueOf(location).trim() : "";
        return locationText.isEmpty() ? "Unknown Location" : locationText;
    }

    private String buildInitialMetadata(ShippingOption option, Carrier carrier, Map<String, Object> meta) {
        StringBuilder metadata = new StringBuilder("{");
        metadata.append("\"carrierCode\":\"").append(carrier.getCode()).append("\"");
        metadata.append(",\"carrierMode\":\"").append(carrier.getMode().name()).append("\"");
        metadata.append(",\"deliveryOption\":\"").append(option.getTitle() != null ? option.getTitle().replace("\"", "\\\"") : "").append("\"");
        metadata.append(",\"settlementMode\":\"").append(carrier.getSettlementMode().name()).append("\"");
        metadata.append(",\"shippingChargeOwner\":\"").append(carrier.getShippingChargeOwner().name()).append("\"");
        metadata.append(",\"codCollectionMode\":\"").append(carrier.getCodCollectionMode().name()).append("\"");
        if (meta != null && !meta.isEmpty()) {
            metadata.append(",\"notes\":\"Manual/vendor delivery metadata captured in request context\"");
        }
        metadata.append("}");
        return metadata.toString();
    }

    private String buildManualTrackingNumber(Carrier carrier) {
        String prefix = carrier.getCode() != null ? carrier.getCode().toUpperCase() : "MANUAL";
        if (prefix.length() > 8) {
            prefix = prefix.substring(0, 8);
        }
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BigDecimal extractAmount(Map<String, Object> meta, String... keys) {
        if (meta == null || keys == null) {
            return BigDecimal.ZERO;
        }
        for (String key : keys) {
            Object value = meta.get(key);
            if (value == null) {
                continue;
            }
            try {
                return new BigDecimal(String.valueOf(value).trim());
            } catch (NumberFormatException ex) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    private boolean extractBoolean(Map<String, Object> meta, String key) {
        if (meta == null || key == null) {
            return false;
        }
        Object value = meta.get(key);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
