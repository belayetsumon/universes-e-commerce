package com.ecommerce.app.module.order.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.order.model.CheckoutPlacementAttemptStatus;
import com.ecommerce.app.module.order.repository.CheckoutPlacementAttemptRepository;
import org.junit.jupiter.api.Test;

class CheckoutPlacementAttemptMaintenanceServiceTest {

    @Test
    void recoversStaleClaimsAndDeletesOnlyExpiredTerminalAttempts() {
        CheckoutPlacementAttemptRepository repository = mock(CheckoutPlacementAttemptRepository.class);
        when(repository.markStaleProcessingAttemptsFailed(any(), any(), any(), any(), anyString()))
                .thenReturn(2);
        when(repository.deleteTerminalAttemptsBefore(any(), any())).thenReturn(3);
        CheckoutPlacementAttemptMaintenanceService service
                = new CheckoutPlacementAttemptMaintenanceService(repository, 30, 15);

        service.maintain();

        verify(repository).markStaleProcessingAttemptsFailed(
                eq(CheckoutPlacementAttemptStatus.PROCESSING),
                eq(CheckoutPlacementAttemptStatus.FAILED),
                any(),
                any(),
                anyString()
        );
        verify(repository).deleteTerminalAttemptsBefore(any(), any());
    }
}
