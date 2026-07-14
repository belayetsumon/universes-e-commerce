package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.BlogCommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogCommentReactionRepository extends JpaRepository<BlogCommentReaction, Long> {
}
