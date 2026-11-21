package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.model.FraudModuleMode;

public interface FraudModuleControlService {
    FraudModuleMode currentMode();
    void changeMode(FraudModuleMode mode, String actor, String reason);
}
