/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Component.java to edit this template
 */
package com.ecommerce.app.module.shipping.componant;

import com.ecommerce.app.module.shipping.services.CarrierAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 *
 * @author libertyerp_local
 */
@Component
public class LocalCarrierAdapter implements CarrierAdapter {

    @Override
    public String getCarrierCode() {
        return "local_sim";
    }

    @Override
    public String createShipment(Map<String, Object> request) {
        // simulate a tracking number
        return "LC-" + UUID.randomUUID().toString();
    }

    @Override
    public Map<String, Object> getTracking(String trackingNumber) {
        Map<String, Object> m = new HashMap<>();
        m.put("status", "IN_TRANSIT");
        m.put("trackingNumber", trackingNumber);
        return m;
    }

    @Override
    public Map<String, Object> parseWebhook(Map<String, Object> payload) {
        // direct passthrough simulation
        return payload;
    }

}
