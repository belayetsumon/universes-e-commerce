package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudBlocklistRequest;
import com.ecommerce.app.module.fraud.dto.FraudBlocklistResponse;
import com.ecommerce.app.module.fraud.model.FraudBlockType;
import java.util.List;

public interface FraudBlocklistService {

    FraudBlocklistResponse add(FraudBlocklistRequest request);

    void deactivate(Long id, String reason);

    boolean isBlocked(FraudBlockType blockType, String rawValue);

    List<FraudBlocklistResponse> findActive();
}
