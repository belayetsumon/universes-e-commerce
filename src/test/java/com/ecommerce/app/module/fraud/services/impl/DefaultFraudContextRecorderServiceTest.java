package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.model.DeviceIdentity;
import com.ecommerce.app.module.fraud.model.VelocityCounterScope;
import com.ecommerce.app.module.fraud.repository.DeviceIdentityRepository;
import com.ecommerce.app.module.fraud.services.OrderVelocityService;
import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

class DefaultFraudContextRecorderServiceTest {

    @Test
    void recorderNeverPersistsRawDeviceIdentifiers() {
        DeviceIdentityRepository repository = mock(DeviceIdentityRepository.class);
        OrderVelocityService velocityService = mock(OrderVelocityService.class);
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(mock(TransactionStatus.class));
        when(repository.findByIdentityKeyForUpdate(anyString())).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(DeviceIdentity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        DefaultFraudContextRecorderService recorder = new DefaultFraudContextRecorderService(
                repository,
                velocityService,
                transactionManager
        );
        FraudContext context = new FraudContext();
        context.setDeviceIdentifier("raw-browser-token");
        context.setDeviceFingerprint("raw-fingerprint");
        context.setSessionIdentifier("session-123");

        recorder.recordOrderAttempt(null, context);

        String expectedIdentifierHash = FraudHashingSupport.sha256("raw-browser-token");
        verify(velocityService).increment(VelocityCounterScope.DEVICE, expectedIdentifierHash);
        ArgumentCaptor<DeviceIdentity> identityCaptor = ArgumentCaptor.forClass(DeviceIdentity.class);
        verify(repository).saveAndFlush(identityCaptor.capture());
        DeviceIdentity saved = identityCaptor.getValue();
        assertEquals(expectedIdentifierHash, saved.getDeviceIdentifier());
        assertEquals(FraudHashingSupport.sha256("raw-fingerprint"), saved.getDeviceFingerprintHash());
        assertEquals(FraudHashingSupport.sha256("session-123"), saved.getSessionIdentifier());
        assertNotEquals("raw-browser-token", saved.getDeviceIdentifier());
    }
}
