package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.shipping.model.Shipment;

public interface VendorRiskProfileService {

    void refreshVendorProfile(Long vendorId);

    void evaluateOrderForVendorRisk(SalesOrder order, FraudContext context);

    void recordTrackingReuseAttempt(Shipment requestedShipment, Shipment existingShipment);

    void recordDeliveryConfirmation(Shipment shipment, SalesOrder order);

    boolean isVendorUnderReview(Long vendorId);

    boolean isVendorPayoutHeld(Long vendorId);
}
