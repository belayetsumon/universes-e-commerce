package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogBookmark;
import com.ecommerce.app.module.user.model.Users;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogBookmarkRepository extends JpaRepository<BlogBookmark, Long> {

    Optional<BlogBookmark> findByBlogAndUser(Blog blog, Users user);

    boolean existsByBlogAndUserAndDeletedFlagFalseAndActiveFlagTrue(Blog blog, Users user);

    List<BlogBookmark> findByUserAndDeletedFlagFalseAndActiveFlagTrueOrderByCreatedAtDesc(Users user);
}
