package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogTranslation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogTranslationRepository extends JpaRepository<BlogTranslation, Long> {

    Optional<BlogTranslation> findByBlogAndLanguageCodeIgnoreCaseAndDeletedFlagFalse(Blog blog, String languageCode);
}
