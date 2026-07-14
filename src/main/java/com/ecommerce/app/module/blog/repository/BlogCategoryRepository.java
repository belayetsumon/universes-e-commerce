package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.BlogCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Long>, JpaSpecificationExecutor<BlogCategory> {

    Optional<BlogCategory> findBySlugIgnoreCaseAndDeletedFlagFalse(String slug);

    boolean existsBySlugIgnoreCaseAndDeletedFlagFalse(String slug);

    boolean existsBySlugIgnoreCaseAndIdNotAndDeletedFlagFalse(String slug, Long id);

    List<BlogCategory> findByDeletedFlagFalseAndActiveFlagTrueOrderBySortOrderAscNameAsc();
}
