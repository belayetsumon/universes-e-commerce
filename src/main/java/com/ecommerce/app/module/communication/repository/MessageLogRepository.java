package com.ecommerce.app.module.communication.repository;

import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageLog;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.communication.model.MessageProvider;
import com.ecommerce.app.module.communication.model.MessageStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    @Query("""
            select log.id
            from MessageLog log
            where log.eventType in :eventTypes
              and log.sentAt < :cutoff
            order by log.id asc
            """)
    List<Long> findCodOtpRetentionCandidateIds(
            @Param("eventTypes") Collection<MessageEventType> eventTypes,
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from MessageLog log
            where log.id in :ids
              and log.eventType in :eventTypes
              and log.sentAt < :cutoff
            """)
    int deleteCodOtpRetentionCandidates(
            @Param("ids") Collection<Long> ids,
            @Param("eventTypes") Collection<MessageEventType> eventTypes,
            @Param("cutoff") LocalDateTime cutoff);
}
