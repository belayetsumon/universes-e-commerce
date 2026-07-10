package com.ecommerce.app.module.communication.repository;

import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageLog;
import com.ecommerce.app.module.communication.model.MessageProvider;
import com.ecommerce.app.module.communication.model.MessageStatus;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MessageLogRepository extends JpaRepository<MessageLog, Long>, JpaSpecificationExecutor<MessageLog> {

    long countByChannelAndRecipientIgnoreCaseAndStatusAndSentAtAfter(
            MessageChannel channel,
            String recipient,
            MessageStatus status,
            LocalDateTime sentAt);

    long countByProviderAndStatusAndSentAtAfter(
            MessageProvider provider,
            MessageStatus status,
            LocalDateTime sentAt);
}
