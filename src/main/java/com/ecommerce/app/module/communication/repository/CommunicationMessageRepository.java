package com.ecommerce.app.module.communication.repository;

import com.ecommerce.app.module.communication.model.CommunicationMessage;
import com.ecommerce.app.module.communication.model.MessageChannel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunicationMessageRepository extends JpaRepository<CommunicationMessage, Long> {

    List<CommunicationMessage> findByChannelOrderByIdDesc(MessageChannel channel);
}
