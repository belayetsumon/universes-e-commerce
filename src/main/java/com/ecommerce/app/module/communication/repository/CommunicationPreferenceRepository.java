package com.ecommerce.app.module.communication.repository;

import com.ecommerce.app.module.communication.model.CommunicationPreference;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.user.model.Users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommunicationPreferenceRepository extends JpaRepository<CommunicationPreference, Long>, JpaSpecificationExecutor<CommunicationPreference> {

    Optional<CommunicationPreference> findByRecipientIgnoreCaseAndChannel(String recipient, MessageChannel channel);

    Optional<CommunicationPreference> findByUserAndChannel(Users user, MessageChannel channel);

    Optional<CommunicationPreference> findByUnsubscribeToken(String unsubscribeToken);
}
