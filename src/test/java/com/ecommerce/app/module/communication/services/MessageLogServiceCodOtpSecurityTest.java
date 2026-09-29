package com.ecommerce.app.module.communication.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.communication.dto.CommunicationSendResult;
import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.communication.model.MessageLog;
import com.ecommerce.app.module.communication.repository.MessageLogRepository;
import org.junit.jupiter.api.Test;

class MessageLogServiceCodOtpSecurityTest {

    @Test
    void codOtpProviderPayloadIsRedactedBeforeAuditPersistence() {
        MessageLogRepository repository = mock(MessageLogRepository.class);
        when(repository.save(any(MessageLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MessageLogService service = new MessageLogService(repository);
        MessageDispatchRequest request = new MessageDispatchRequest();
        request.setEventType(MessageEventType.CUSTOMER_COD_OTP);
        request.setChannel(MessageChannel.SMS);
        request.setRecipient("8801712345678");

        MessageLog log = service.record(
                request,
                null,
                CommunicationSendResult.sent("200", "provider echoed code 123456")
        );

        assertEquals("COD_OTP_DELIVERY_SENT", log.getResponseCode());
        assertFalse(log.getResponseMessage().contains("123456"));
    }
}
