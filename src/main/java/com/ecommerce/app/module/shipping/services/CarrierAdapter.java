/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ecommerce.app.module.shipping.services;

import java.util.Map;

/**
 *
 * @author libertyerp_local
 */
public interface CarrierAdapter {

    String getCarrierCode();

    // create shipment on carrier and return tracking number
    String createShipment(Map<String, Object> request);

    Map<String, Object> getTracking(String trackingNumber);

    default Map<String, Object> parseWebhook(Map<String, Object> payload) {
        return payload;
    }
}
