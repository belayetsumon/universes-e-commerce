package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogSeo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogSeoRepository extends JpaRepository<BlogSeo, Long> {

    Optional<BlogSeo> findByBlog(Blog blog);
}
