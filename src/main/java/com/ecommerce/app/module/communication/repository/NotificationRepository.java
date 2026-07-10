package com.ecommerce.app.module.communication.repository;

import com.ecommerce.app.module.communication.model.Notification;
import com.ecommerce.app.module.user.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

    Page<Notification> findByUserOrderByCreatedAtDesc(Users user, Pageable pageable);

    long countByUserAndSeenFalse(Users user);
}
