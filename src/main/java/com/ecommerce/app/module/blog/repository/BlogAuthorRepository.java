package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.BlogAuthor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BlogAuthorRepository extends JpaRepository<BlogAuthor, Long>, JpaSpecificationExecutor<BlogAuthor> {

    Optional<BlogAuthor> findBySlugIgnoreCaseAndDeletedFlagFalse(String slug);

    boolean existsBySlugIgnoreCaseAndDeletedFlagFalse(String slug);

    boolean existsBySlugIgnoreCaseAndIdNotAndDeletedFlagFalse(String slug, Long id);

    List<BlogAuthor> findByDeletedFlagFalseAndActiveFlagTrueOrderByDisplayNameAsc();
}
