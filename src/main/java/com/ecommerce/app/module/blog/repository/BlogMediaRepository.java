package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogMedia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogMediaRepository extends JpaRepository<BlogMedia, Long> {

    List<BlogMedia> findByBlogAndDeletedFlagFalseOrderBySortOrderAscIdAsc(Blog blog);
}
