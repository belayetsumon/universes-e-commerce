package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogComment;
import com.ecommerce.app.module.blog.model.BlogModerationStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {

    Page<BlogComment> findByModerationStatusAndDeletedFlagFalseOrderByCreatedAtDesc(BlogModerationStatus status, Pageable pageable);

    Page<BlogComment> findByDeletedFlagFalseOrderByCreatedAtDesc(Pageable pageable);

    List<BlogComment> findByBlogAndModerationStatusAndDeletedFlagFalseOrderByCreatedAtAsc(Blog blog, BlogModerationStatus status);

    long countByModerationStatusAndDeletedFlagFalse(BlogModerationStatus status);
}
