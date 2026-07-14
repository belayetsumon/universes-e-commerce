package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.BlogSeries;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BlogSeriesRepository extends JpaRepository<BlogSeries, Long>, JpaSpecificationExecutor<BlogSeries> {

    Optional<BlogSeries> findBySlugIgnoreCaseAndDeletedFlagFalse(String slug);

    boolean existsBySlugIgnoreCaseAndDeletedFlagFalse(String slug);

    boolean existsBySlugIgnoreCaseAndIdNotAndDeletedFlagFalse(String slug, Long id);

    List<BlogSeries> findByDeletedFlagFalseAndActiveFlagTrueOrderBySortOrderAscTitleAsc();
}
