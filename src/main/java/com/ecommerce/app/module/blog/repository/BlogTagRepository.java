package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.BlogTag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BlogTagRepository extends JpaRepository<BlogTag, Long>, JpaSpecificationExecutor<BlogTag> {

    Optional<BlogTag> findBySlugIgnoreCaseAndDeletedFlagFalse(String slug);

    boolean existsBySlugIgnoreCaseAndDeletedFlagFalse(String slug);

    List<BlogTag> findByDeletedFlagFalseAndActiveFlagTrueOrderByNameAsc();
}
