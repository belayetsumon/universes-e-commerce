package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogFaq;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogFaqRepository extends JpaRepository<BlogFaq, Long> {

    List<BlogFaq> findByBlogAndDeletedFlagFalseOrderBySortOrderAscIdAsc(Blog blog);
}
