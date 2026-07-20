package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.shipping.model.ShipmentStatus;

public interface CodRiskProfileService {

    void recordCodOrderPlaced(SalesOrder order, FraudContext context);

    void recordCodShipmentOutcome(SalesOrder order, ShipmentStatus status, String refusalReason, FraudContext context);

    void recordSuccessfulPrepaidOrder(SalesOrder order, FraudContext context);
}
