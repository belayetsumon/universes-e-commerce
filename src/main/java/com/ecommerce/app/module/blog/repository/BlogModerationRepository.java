package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.BlogModeration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogModerationRepository extends JpaRepository<BlogModeration, Long> {
}
