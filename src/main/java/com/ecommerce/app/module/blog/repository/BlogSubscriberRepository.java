package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.BlogSubscriber;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogSubscriberRepository extends JpaRepository<BlogSubscriber, Long> {

    Optional<BlogSubscriber> findByEmailIgnoreCase(String email);

    Page<BlogSubscriber> findByDeletedFlagFalseOrderByCreatedAtDesc(Pageable pageable);
}
