package com.ecommerce.app.module.communication.services;

import com.ecommerce.app.module.communication.dto.AdminCommunicationMessageRow;
import com.ecommerce.app.module.communication.dto.ManualCommunicationActor;
import com.ecommerce.app.module.communication.dto.ManualMessageRequest;
import com.ecommerce.app.module.communication.dto.ManualRecipient;
import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.dto.MessageReadReport;
import com.ecommerce.app.module.communication.dto.MessageReceiverTypeReport;
import com.ecommerce.app.module.communication.dto.RenderedMessage;
import com.ecommerce.app.module.communication.model.CommunicationMessage;
import com.ecommerce.app.module.communication.model.CommunicationRecipient;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageStatus;
import com.ecommerce.app.module.communication.model.MessageType;
import com.ecommerce.app.module.communication.model.Notification;
import com.ecommerce.app.module.communication.model.ReceiverType;
import com.ecommerce.app.module.communication.repository.CommunicationMessageRepository;
import com.ecommerce.app.module.communication.repository.CommunicationRecipientRepository;
import com.ecommerce.app.module.communication.repository.NotificationRepository;
import com.ecommerce.app.module.user.model.UserType;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunicationNotificationService {

    private final CommunicationMessageRepository messageRepository;
    private final CommunicationRecipientRepository recipientRepository;
    private final NotificationRepository notificationRepository;
    private final UsersRepository usersRepository;

    public CommunicationNotificationService(
            CommunicationMessageRepository messageRepository,
            CommunicationRecipientRepository recipientRepository,
            NotificationRepository notificationRepository,
            UsersRepository usersRepository) {
        this.messageRepository = messageRepository;
        this.recipientRepository = recipientRepository;
        this.notificationRepository = notificationRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional
    public CommunicationMessage createManualInAppMessage(ManualCommunicationActor actor, ManualMessageRequest request, List<ManualRecipient> recipients) {
        CommunicationMessage message = createMessage(
                clean(request.getSubject()) == null ? "Manual message" : clean(request.getSubject()),
                request.getBody(),
                request.getMessageType() == null ? MessageType.CUSTOM : request.getMessageType(),
                actor == null ? null : actor.getActorUserId(),
                MessageStatus.SENT
        );

        if (recipients != null) {
            for (ManualRecipient recipient : recipients) {
                if (recipient == null || recipient.getUser() == null || recipient.getUser().getId() == null) {
                    continue;
                }
                CommunicationRecipient row = buildRecipient(
                        message,
                        receiverType(recipient),
                        recipient.getUser().getId(),
                        receiverType(recipient) == ReceiverType.VENDOR ? recipient.getRecipientReferenceId() : null,
                        recipient.getDisplayName(),
                        recipient.getEmail(),
                        recipient.getMobile()
                );
                recipientRepository.save(row);
                createLegacyNotification(message, row, recipient.getUser(), com.ecommerce.app.module.communication.model.MessageEventType.MANUAL_MESSAGE);
            }
        }

        return message;
    }

    @Transactional
    public CommunicationRecipient createRecipientFromDispatch(MessageDispatchRequest request, RenderedMessage renderedMessage) {
        Users user = resolveReceiverUser(request);
        Long receiverUserId = user != null ? user.getId() : parseUserRecipient(request.getRecipient());
        if (receiverUserId == null) {
            throw new IllegalArgumentException("IN_APP notification requires a receiver user id.");
        }
        if (user == null) {
            user = usersRepository.findById(receiverUserId).orElse(null);
        }

        CommunicationMessage message = createMessage(
                renderedMessage.getSubject(),
                renderedMessage.getBody(),
                request.getMessageType() == null ? MessageType.CUSTOM : request.getMessageType(),
                null,
                MessageStatus.SENT
        );

        ReceiverType receiverType = request.getReceiverType() != null ? request.getReceiverType() : receiverType(user);
        CommunicationRecipient recipient = buildRecipient(
                message,
                receiverType,
                receiverUserId,
                request.getVendorId(),
                clean(request.getReceiverDisplayName()) == null ? displayName(user) : request.getReceiverDisplayName(),
                clean(request.getReceiverEmail()) == null ? (user == null ? null : user.getEmail()) : request.getReceiverEmail(),
                clean(request.getReceiverMobile()) == null ? (user == null ? null : user.getMobile()) : request.getReceiverMobile()
        );
        CommunicationRecipient saved = recipientRepository.save(recipient);
        createLegacyNotification(message, saved, user, request.getEventType());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<CommunicationRecipient> findNotifications(Long loggedInUserId, String view) {
        if ("unread".equalsIgnoreCase(view)) {
            return recipientRepository.findByReceiverUserIdAndReadStatusFalseOrderByIdDesc(loggedInUserId);
        }
        if ("read".equalsIgnoreCase(view)) {
            return recipientRepository.findByReceiverUserIdAndReadStatusTrueOrderByIdDesc(loggedInUserId);
        }
        return recipientRepository.findByReceiverUserIdOrderByIdDesc(loggedInUserId);
    }

    @Transactional(readOnly = true)
    public List<CommunicationRecipient> findNotifications(Long loggedInUserId, ReceiverType receiverType, String view) {
        if (receiverType == null) {
            return findNotifications(loggedInUserId, view);
        }
        if ("unread".equalsIgnoreCase(view)) {
            return recipientRepository.findByReceiverUserIdAndReceiverTypeAndReadStatusFalseOrderByIdDesc(loggedInUserId, receiverType);
        }
        if ("read".equalsIgnoreCase(view)) {
            return recipientRepository.findByReceiverUserIdAndReceiverTypeAndReadStatusTrueOrderByIdDesc(loggedInUserId, receiverType);
        }
        return recipientRepository.findByReceiverUserIdAndReceiverTypeOrderByIdDesc(loggedInUserId, receiverType);
    }

    @Transactional
    public CommunicationRecipient markAsRead(Long recipientId, Long loggedInUserId) {
        return markAsRead(recipientId, loggedInUserId, null);
    }

    @Transactional
    public CommunicationRecipient markAsRead(Long recipientId, Long loggedInUserId, ReceiverType receiverType) {
        CommunicationRecipient recipient = recipientRepository
                .findByIdAndReceiverUserId(recipientId, loggedInUserId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (receiverType != null && recipient.getReceiverType() != receiverType) {
            throw new RuntimeException("Notification not found");
        }

        LocalDateTime now = LocalDateTime.now();
        if (!recipient.isReadStatus()) {
            recipient.setReadStatus(true);
            recipient.setReadAt(now);
        }
        if (recipient.getClickedAt() == null) {
            recipient.setClickedAt(now);
        }
        return recipientRepository.save(recipient);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long loggedInUserId) {
        if (loggedInUserId == null) {
            return 0L;
        }
        return recipientRepository.countByReceiverUserIdAndReadStatusFalse(loggedInUserId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long loggedInUserId, ReceiverType receiverType) {
        if (loggedInUserId == null) {
            return 0L;
        }
        if (receiverType == null) {
            return getUnreadCount(loggedInUserId);
        }
        return recipientRepository.countByReceiverUserIdAndReceiverTypeAndReadStatusFalse(loggedInUserId, receiverType);
    }

    @Transactional(readOnly = true)
    public List<AdminCommunicationMessageRow> findInAppMessageHistory() {
        List<AdminCommunicationMessageRow> rows = new ArrayList<>();
        for (CommunicationMessage message : messageRepository.findByChannelOrderByIdDesc(MessageChannel.IN_APP)) {
            rows.add(new AdminCommunicationMessageRow(message, getReadReport(message.getId())));
        }
        return rows;
    }

    @Transactional(readOnly = true)
    public CommunicationMessage requireMessage(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
    }

    @Transactional(readOnly = true)
    public List<CommunicationRecipient> findRecipients(Long messageId) {
        return recipientRepository.findByMessageIdOrderByIdDesc(messageId);
    }

    @Transactional(readOnly = true)
    public MessageReadReport getReadReport(Long messageId) {
        long total = recipientRepository.countByMessageId(messageId);
        long read = recipientRepository.countByMessageIdAndReadStatusTrue(messageId);
        long unread = recipientRepository.countByMessageIdAndReadStatusFalse(messageId);
        double readPercentage = total == 0 ? 0 : (read * 100.0) / total;
        return new MessageReadReport(total, read, unread, readPercentage);
    }

    @Transactional(readOnly = true)
    public MessageReceiverTypeReport getReceiverTypeReport(Long messageId) {
        return new MessageReceiverTypeReport(
                recipientRepository.countByMessageIdAndReceiverTypeAndReadStatusTrue(messageId, ReceiverType.CUSTOMER),
                recipientRepository.countByMessageIdAndReceiverTypeAndReadStatusFalse(messageId, ReceiverType.CUSTOMER),
                recipientRepository.countByMessageIdAndReceiverTypeAndReadStatusTrue(messageId, ReceiverType.VENDOR),
                recipientRepository.countByMessageIdAndReceiverTypeAndReadStatusFalse(messageId, ReceiverType.VENDOR),
                recipientRepository.countByMessageIdAndReceiverTypeAndReadStatusTrue(messageId, ReceiverType.ADMIN),
                recipientRepository.countByMessageIdAndReceiverTypeAndReadStatusFalse(messageId, ReceiverType.ADMIN)
        );
    }

    private CommunicationMessage createMessage(String subject, String body, MessageType messageType, Long createdByUserId, MessageStatus status) {
        CommunicationMessage message = new CommunicationMessage();
        message.setSubject(defaultText(subject, "Notification"));
        message.setMessageBody(defaultText(body, "A new notification is available."));
        message.setChannel(MessageChannel.IN_APP);
        message.setMessageType(messageType == null ? MessageType.CUSTOM : messageType);
        message.setCreatedByUserId(createdByUserId);
        message.setStatus(status == null ? MessageStatus.SENT : status);
        return messageRepository.save(message);
    }

    private CommunicationRecipient buildRecipient(
            CommunicationMessage message,
            ReceiverType receiverType,
            Long receiverUserId,
            Long vendorId,
            String receiverName,
            String receiverEmail,
            String receiverMobile) {
        CommunicationRecipient recipient = new CommunicationRecipient();
        recipient.setMessage(message);
        recipient.setReceiverType(receiverType == null ? ReceiverType.USER : receiverType);
        recipient.setReceiverUserId(receiverUserId);
        recipient.setVendorId(vendorId);
        recipient.setReceiverName(clean(receiverName));
        recipient.setReceiverEmail(clean(receiverEmail));
        recipient.setReceiverMobile(clean(receiverMobile));
        recipient.setDelivered(true);
        recipient.setReadStatus(false);
        recipient.setDeliveredAt(LocalDateTime.now());
        recipient.setStatus(MessageStatus.SENT);
        return recipient;
    }

    private void createLegacyNotification(
            CommunicationMessage message,
            CommunicationRecipient recipient,
            Users user,
            com.ecommerce.app.module.communication.model.MessageEventType eventType) {
        if (user == null || user.getId() == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setEventType(eventType == null ? com.ecommerce.app.module.communication.model.MessageEventType.MANUAL_MESSAGE : eventType);
        notification.setChannel(MessageChannel.IN_APP);
        notification.setTitle(message.getSubject());
        notification.setMessage(message.getMessageBody());
        notification.setPayloadJson("{\"communicationRecipientId\":" + recipient.getId() + "}");
        notificationRepository.save(notification);
    }

    private Users resolveReceiverUser(MessageDispatchRequest request) {
        if (request == null) {
            return null;
        }
        if (request.getUser() != null && request.getUser().getId() != null) {
            return request.getUser();
        }
        Long receiverUserId = parseUserRecipient(request.getRecipient());
        return receiverUserId == null ? null : usersRepository.findById(receiverUserId).orElse(null);
    }

    private Long parseUserRecipient(String recipient) {
        String cleaned = clean(recipient);
        if (cleaned == null || !cleaned.startsWith("user:")) {
            return null;
        }
        try {
            return Long.valueOf(cleaned.substring("user:".length()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private ReceiverType receiverType(ManualRecipient recipient) {
        return switch (recipient.getRecipientType()) {
            case CUSTOMER -> ReceiverType.CUSTOMER;
            case VENDOR -> ReceiverType.VENDOR;
            case ADMIN -> ReceiverType.ADMIN;
        };
    }

    private ReceiverType receiverType(Users user) {
        if (user == null || user.getUserType() == null) {
            return ReceiverType.USER;
        }
        UserType userType = user.getUserType();
        if (userType == UserType.customer) {
            return ReceiverType.CUSTOMER;
        }
        if (userType == UserType.administrator || userType == UserType.systemadmin) {
            return ReceiverType.ADMIN;
        }
        return ReceiverType.USER;
    }

    private String displayName(Users user) {
        if (user == null) {
            return null;
        }
        String name = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        return name.isBlank() ? user.getEmail() : name;
    }

    private String defaultText(String value, String fallback) {
        String cleaned = clean(value);
        return cleaned == null ? fallback : cleaned;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
