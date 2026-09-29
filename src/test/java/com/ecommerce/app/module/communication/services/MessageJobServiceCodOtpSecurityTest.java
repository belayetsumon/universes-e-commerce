package com.ecommerce.app.module.communication.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.dto.RenderedMessage;
import com.ecommerce.app.module.communication.model.DeliveryMode;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.communication.model.MessageJob;
import com.ecommerce.app.module.communication.model.MessageStatus;
import com.ecommerce.app.module.communication.repository.MessageJobRepository;
import org.junit.jupiter.api.Test;

class MessageJobServiceCodOtpSecurityTest {

    @Test
    void codOtpCannotEnterAnyPersistentMessageQueuePath() {
        MessageJobRepository repository = mock(MessageJobRepository.class);
        MessageJobService service = new MessageJobService(repository);
        MessageDispatchRequest request = new MessageDispatchRequest();
        request.setEventType(MessageEventType.GUEST_CHECKOUT_OTP);
        request.setChannel(MessageChannel.SMS);
        request.setRecipient("8801712345678");
        request.setBody("code 123456");

        assertThrows(IllegalArgumentException.class, () -> service.enqueueRequest(request));
        assertThrows(IllegalArgumentException.class, () -> service.queue(
                request,
                new RenderedMessage("OTP", "code 123456", false),
                null,
                DeliveryMode.DATABASE_QUEUE
        ));
        verifyNoInteractions(repository);
    }

    @Test
    void exhaustedRetryBecomesTerminallyCancelled() {
        MessageJobRepository repository = mock(MessageJobRepository.class);
        MessageJobService service = new MessageJobService(repository);
        MessageJob job = new MessageJob();
        job.setStatus(MessageStatus.FAILED);

        service.cancelRetry(job, "retry limit reached");

        assertEquals(MessageStatus.CANCELLED, job.getStatus());
    }
}
