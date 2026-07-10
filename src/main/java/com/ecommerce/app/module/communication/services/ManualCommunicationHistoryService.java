package com.ecommerce.app.module.communication.services;

import com.ecommerce.app.module.communication.dto.ManualCommunicationActor;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.communication.model.MessageJob;
import com.ecommerce.app.module.communication.model.MessageLog;
import com.ecommerce.app.module.communication.model.MessageStatus;
import com.ecommerce.app.module.communication.repository.MessageJobRepository;
import com.ecommerce.app.module.communication.repository.MessageLogRepository;
import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManualCommunicationHistoryService {

    private final MessageLogRepository logRepository;
    private final MessageJobRepository jobRepository;

    public ManualCommunicationHistoryService(MessageLogRepository logRepository, MessageJobRepository jobRepository) {
        this.logRepository = logRepository;
        this.jobRepository = jobRepository;
    }

    @Transactional(readOnly = true)
    public Page<MessageLog> findManualLogs(MessageChannel channel, MessageStatus status, String search, Pageable pageable) {
        return logRepository.findAll(logSpec(null, null, channel, status, search), pageable);
    }

    @Transactional(readOnly = true)
    public Page<MessageLog> findInbox(ManualCommunicationActor actor, MessageChannel channel, MessageStatus status, String search, Pageable pageable) {
        List<String> addresses = actorAddresses(actor);
        if (addresses.isEmpty()) {
            return Page.empty(pageable);
        }
        return logRepository.findAll(logSpec(addresses, null, channel, status, search), pageable);
    }

    @Transactional(readOnly = true)
    public Page<MessageLog> findSent(ManualCommunicationActor actor, MessageChannel channel, MessageStatus status, String search, Pageable pageable) {
        return logRepository.findAll(logSpec(null, sentPrefix(actor), channel, status, search), pageable);
    }

    @Transactional(readOnly = true)
    public Page<MessageJob> findSentJobs(ManualCommunicationActor actor, MessageChannel channel, MessageStatus status, String search, Pageable pageable) {
        return jobRepository.findAll(jobSpec(sentPrefix(actor), channel, status, search), pageable);
    }

    private Specification<MessageLog> logSpec(List<String> recipients, String idempotencyPrefix, MessageChannel channel, MessageStatus status, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("eventType"), MessageEventType.MANUAL_MESSAGE));
            if (recipients != null && !recipients.isEmpty()) {
                predicates.add(root.get("recipient").in(recipients));
            }
            if (idempotencyPrefix != null && !idempotencyPrefix.isBlank()) {
                predicates.add(cb.like(root.get("idempotencyKey"), idempotencyPrefix + "%"));
            }
            if (channel != null) {
                predicates.add(cb.equal(root.get("channel"), channel));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            String cleaned = clean(search);
            if (cleaned != null) {
                String like = "%" + cleaned.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("recipient")), like),
                        cb.like(cb.lower(root.get("responseCode")), like),
                        cb.like(cb.lower(root.get("responseMessage")), like),
                        cb.like(cb.lower(root.get("idempotencyKey")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<MessageJob> jobSpec(String idempotencyPrefix, MessageChannel channel, MessageStatus status, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("eventType"), MessageEventType.MANUAL_MESSAGE));
            predicates.add(cb.like(root.get("idempotencyKey"), idempotencyPrefix + "%"));
            if (channel != null) {
                predicates.add(cb.equal(root.get("channel"), channel));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            String cleaned = clean(search);
            if (cleaned != null) {
                String like = "%" + cleaned.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("recipient")), like),
                        cb.like(cb.lower(root.get("subject")), like),
                        cb.like(cb.lower(root.get("body")), like),
                        cb.like(cb.lower(root.get("idempotencyKey")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<String> actorAddresses(ManualCommunicationActor actor) {
        List<String> addresses = new ArrayList<>();
        Users user = actor.getUser();
        if (user == null) {
            return addresses;
        }
        add(addresses, user.getEmail());
        add(addresses, user.getMobile());
        if (user.getId() != null) {
            addresses.add("user:" + user.getId());
        }
        return addresses;
    }

    private String sentPrefix(ManualCommunicationActor actor) {
        return "manual:" + actor.getActorType() + ":" + String.valueOf(actor.getActorUserId()) + ":";
    }

    private void add(List<String> values, String value) {
        String cleaned = clean(value);
        if (cleaned != null) {
            values.add(cleaned);
        }
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
