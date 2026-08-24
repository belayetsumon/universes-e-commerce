package com.ecommerce.app.module.order.services;

import java.util.Objects;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class CheckoutPlacementTransactionExecutor {

    private final TransactionTemplate transactionTemplate;

    public CheckoutPlacementTransactionExecutor(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        this.transactionTemplate.setTimeout(120);
    }

    public <T> T execute(Supplier<T> action) {
        Objects.requireNonNull(action, "Checkout placement action is required.");
        return transactionTemplate.execute(status -> action.get());
    }
}
