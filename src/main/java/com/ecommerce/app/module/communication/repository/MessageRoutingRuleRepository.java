package com.ecommerce.app.module.communication.repository;

import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.communication.model.MessageRoutingRule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MessageRoutingRuleRepository extends JpaRepository<MessageRoutingRule, Long>, JpaSpecificationExecutor<MessageRoutingRule> {

    List<MessageRoutingRule> findByEventTypeAndChannelAndActiveTrueOrderByMinVolumeDescIdDesc(
            MessageEventType eventType,
            MessageChannel channel);
}
