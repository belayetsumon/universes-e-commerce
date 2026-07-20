package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.model.FraudIdempotencyRecord;
import java.util.Optional;

public interface FraudIdempotencyService {

    Optional<FraudIdempotencyRecord> findCompleted(String operationScope, String idempotencyKey);

    FraudIdempotencyRecord start(String operationScope, String idempotencyKey, String requestHash);

    void complete(String operationScope, String idempotencyKey, String responseJson);

    void fail(String operationScope, String idempotencyKey, String responseJson);

    String hashPayload(String payload);
}
