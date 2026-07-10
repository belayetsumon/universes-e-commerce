package com.ecommerce.app.module.communication.repository;

import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.communication.model.MessageStatus;
import com.ecommerce.app.module.communication.model.MessageTemplate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Long>, JpaSpecificationExecutor<MessageTemplate> {

    Optional<MessageTemplate> findFirstByEventTypeAndChannelAndLanguageIgnoreCaseAndStatusOrderByUpdatedAtDescIdDesc(
            MessageEventType eventType,
            MessageChannel channel,
            String language,
            MessageStatus status);
}
