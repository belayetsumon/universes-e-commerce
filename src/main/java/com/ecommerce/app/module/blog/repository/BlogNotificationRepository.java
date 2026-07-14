package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.BlogNotification;
import com.ecommerce.app.module.blog.model.BlogNotificationStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogNotificationRepository extends JpaRepository<BlogNotification, Long> {

    List<BlogNotification> findByNotificationStatusAndScheduledAtLessThanEqual(BlogNotificationStatus status, LocalDateTime scheduledAt);
}
