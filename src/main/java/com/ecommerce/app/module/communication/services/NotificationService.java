package com.ecommerce.app.module.communication.services;

import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.dto.RenderedMessage;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.Notification;
import com.ecommerce.app.module.communication.repository.NotificationRepository;
import com.ecommerce.app.module.user.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Notification createNotification(MessageDispatchRequest request, RenderedMessage renderedMessage) {
        Notification notification = new Notification();
        notification.setUser(request.getUser());
        notification.setEventType(request.getEventType());
        notification.setChannel(MessageChannel.IN_APP);
        notification.setTitle(renderedMessage.getSubject());
        notification.setMessage(renderedMessage.getBody());
        notification.setPayloadJson(request.getPayloadJson());
        return repository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<Notification> findUserNotifications(Users user, Pageable pageable) {
        return repository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    @Transactional(readOnly = true)
    public long countUnread(Users user) {
        return repository.countByUserAndSeenFalse(user);
    }
}
