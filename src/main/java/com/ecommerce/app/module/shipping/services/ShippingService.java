/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.dto.ShippingOption;
import com.ecommerce.app.module.shipping.model.Shipment;
import java.util.List;
import java.util.Map;

/**
 *
 * @author libertyerp_local
 */
public interface ShippingService {

    List<ShippingOption> calculateOptionsForCart(Map<String, Object> cart);

    Shipment createShipmentForOrder(Long orderId, Long vendorId, ShippingOption option, Map<String, Object> meta);

    void handleCarrierWebhook(String carrierCode, Map<String, Object> payload);
}
