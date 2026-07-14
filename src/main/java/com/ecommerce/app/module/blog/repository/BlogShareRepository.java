package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.BlogShare;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogShareRepository extends JpaRepository<BlogShare, Long> {
}
