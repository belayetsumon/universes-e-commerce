package com.ecommerce.app.module.communication.repository;

import com.ecommerce.app.module.communication.model.CommunicationRecipient;
import com.ecommerce.app.module.communication.model.ReceiverType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunicationRecipientRepository extends JpaRepository<CommunicationRecipient, Long> {

    Optional<CommunicationRecipient> findByIdAndReceiverUserId(Long id, Long receiverUserId);

    List<CommunicationRecipient> findByReceiverUserIdOrderByIdDesc(Long receiverUserId);

    List<CommunicationRecipient> findByReceiverUserIdAndReceiverTypeOrderByIdDesc(Long receiverUserId, ReceiverType receiverType);

    List<CommunicationRecipient> findByReceiverUserIdAndReadStatusFalseOrderByIdDesc(Long receiverUserId);

    List<CommunicationRecipient> findByReceiverUserIdAndReceiverTypeAndReadStatusFalseOrderByIdDesc(Long receiverUserId, ReceiverType receiverType);

    List<CommunicationRecipient> findByReceiverUserIdAndReadStatusTrueOrderByIdDesc(Long receiverUserId);

    List<CommunicationRecipient> findByReceiverUserIdAndReceiverTypeAndReadStatusTrueOrderByIdDesc(Long receiverUserId, ReceiverType receiverType);

    List<CommunicationRecipient> findByMessageIdOrderByIdDesc(Long messageId);

    long countByReceiverUserIdAndReadStatusFalse(Long receiverUserId);

    long countByReceiverUserIdAndReceiverTypeAndReadStatusFalse(Long receiverUserId, ReceiverType receiverType);

    long countByMessageId(Long messageId);

    long countByMessageIdAndReadStatusTrue(Long messageId);

    long countByMessageIdAndReadStatusFalse(Long messageId);

    long countByMessageIdAndReceiverTypeAndReadStatusTrue(Long messageId, ReceiverType receiverType);

    long countByMessageIdAndReceiverTypeAndReadStatusFalse(Long messageId, ReceiverType receiverType);
}
