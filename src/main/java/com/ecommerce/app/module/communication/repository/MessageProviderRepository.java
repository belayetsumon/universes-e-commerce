package com.ecommerce.app.module.communication.repository;

import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageProvider;
import com.ecommerce.app.module.communication.model.MessageStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MessageProviderRepository extends JpaRepository<MessageProvider, Long>, JpaSpecificationExecutor<MessageProvider> {

    List<MessageProvider> findByChannelAndStatusOrderByPriorityAscIdAsc(MessageChannel channel, MessageStatus status);
}
