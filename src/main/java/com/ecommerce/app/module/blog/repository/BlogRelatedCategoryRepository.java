package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.BlogRelatedCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRelatedCategoryRepository extends JpaRepository<BlogRelatedCategory, Long> {
}
