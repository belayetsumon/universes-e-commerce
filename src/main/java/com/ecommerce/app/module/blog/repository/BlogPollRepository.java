package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.BlogPoll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogPollRepository extends JpaRepository<BlogPoll, Long> {
}
