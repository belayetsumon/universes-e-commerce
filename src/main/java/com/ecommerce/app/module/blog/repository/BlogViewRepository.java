package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogView;
import com.ecommerce.app.module.user.model.Users;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogViewRepository extends JpaRepository<BlogView, Long> {

    long countByBlogAndViewedAtAfter(Blog blog, LocalDateTime viewedAt);

    List<BlogView> findTop30ByUserOrderByViewedAtDesc(Users user);
}
