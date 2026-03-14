/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.dto.ShippingOption;
import com.ecommerce.app.module.shipping.model.Carrier;
import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.model.ShipmentStatus;
import com.ecommerce.app.module.shipping.repository.ShipmentRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public ShippingServiceImpl(ShipmentRepository shipmentRepository,
            CarrierRepository carrierRepository,
            List<CarrierAdapter> adapterList) {
        this.shipmentRepository = shipmentRepository;
        this.carrierRepository = carrierRepository;
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
//        Shipment s = Shipment.builder()
//                .salesOrderId(orderId)
//                .vendorId(vendorId)
//                .shippingCost(option.getPrice())
//                .status("PENDING")
//                .metadataJson("{}")
//                .build();
//        s = shipmentRepository.save(s);

        Shipment sh = new Shipment();
        sh.setSalesOrderId(orderId);
        sh.setVendorId(vendorId);
        sh.setShippingCost(option.getPrice());
        sh.setStatus(ShipmentStatus.PENDING);
        sh.setMetadataJson("{}");
        shipmentRepository.save(sh);

        // Call adapter to create carrier shipment
        CarrierAdapter adapter = adapters.get(option.getCarrierCode());
        if (adapter != null) {
            Map<String, Object> req = new HashMap<>();
            req.put("orderId", orderId);
            req.put("vendorId", vendorId);
            req.put("option", option);
            req.put("meta", meta);
            String tracking = adapter.createShipment(req);

            // Try to set carrier entity if exists
            Optional<Carrier> c = carrierRepository.findByCode(adapter.getCarrierCode());
            c.ifPresent(sh::setCarrier);

            sh.setTrackingNumber(tracking);
            sh.setStatus(ShipmentStatus.IN_TRANSIT);
            sh = shipmentRepository.save(sh);
        }
        return sh;
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
            s.setStatus(ShipmentStatus.IN_TRANSIT);
            shipmentRepository.save(s);
        }
    }
}
