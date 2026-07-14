package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogRevision;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRevisionRepository extends JpaRepository<BlogRevision, Long> {

    List<BlogRevision> findByBlogOrderByRevisionNumberDesc(Blog blog);

    Optional<BlogRevision> findTopByBlogOrderByRevisionNumberDesc(Blog blog);
}
