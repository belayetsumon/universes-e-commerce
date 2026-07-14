package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.BlogReaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogReactionRepository extends JpaRepository<BlogReaction, Long> {
}
