package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogRelatedProduct;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRelatedProductRepository extends JpaRepository<BlogRelatedProduct, Long> {

    List<BlogRelatedProduct> findByBlogAndDeletedFlagFalseOrderBySortOrderAscIdAsc(Blog blog);
}
