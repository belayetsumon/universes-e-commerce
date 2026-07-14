package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.Blog;
import com.ecommerce.app.module.blog.model.BlogPublicationStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlogRepository extends JpaRepository<Blog, Long>, JpaSpecificationExecutor<Blog> {

    boolean existsBySlugIgnoreCaseAndLanguageCodeIgnoreCaseAndDeletedFlagFalse(String slug, String languageCode);

    boolean existsBySlugIgnoreCaseAndLanguageCodeIgnoreCaseAndIdNotAndDeletedFlagFalse(String slug, String languageCode, Long id);

    @EntityGraph(attributePaths = {"category", "author", "series", "tags", "seo"})
    Optional<Blog> findBySlugIgnoreCaseAndLanguageCodeIgnoreCaseAndDeletedFlagFalse(String slug, String languageCode);

    @EntityGraph(attributePaths = {"category", "author"})
    Page<Blog> findByStatusAndDeletedFlagFalseAndActiveFlagTrue(BlogPublicationStatus status, Pageable pageable);

    long countByStatusAndDeletedFlagFalse(BlogPublicationStatus status);

    long countByDeletedFlagFalse();

    List<Blog> findTop8ByStatusAndDeletedFlagFalseAndActiveFlagTrueOrderByViewCountDescPublishedAtDesc(BlogPublicationStatus status);

    List<Blog> findTop8ByStatusAndFeaturedPostTrueAndDeletedFlagFalseAndActiveFlagTrueOrderBySortOrderAscPublishedAtDesc(BlogPublicationStatus status);

    List<Blog> findTop8ByStatusAndPublishedAtLessThanEqualAndDeletedFlagFalseAndActiveFlagTrueOrderByPublishedAtDesc(BlogPublicationStatus status, LocalDateTime publishedAt);

    List<Blog> findByStatusAndScheduledAtLessThanEqualAndDeletedFlagFalse(BlogPublicationStatus status, LocalDateTime scheduledAt);

    List<Blog> findByStatusInAndDeletedFlagFalseOrderByUpdatedAtDesc(Collection<BlogPublicationStatus> statuses);

    @Query("""
            select b from Blog b
            left join b.category c
            left join b.author a
            where b.deletedFlag = false
              and b.activeFlag = true
              and b.status = :status
              and (:query is null or lower(b.title) like lower(concat('%', :query, '%'))
                   or lower(b.excerpt) like lower(concat('%', :query, '%'))
                   or lower(b.contentPlainText) like lower(concat('%', :query, '%')))
              and (:categorySlug is null or lower(c.slug) = lower(:categorySlug))
              and (:authorSlug is null or lower(a.slug) = lower(:authorSlug))
            """)
    Page<Blog> publicSearch(
            @Param("status") BlogPublicationStatus status,
            @Param("query") String query,
            @Param("categorySlug") String categorySlug,
            @Param("authorSlug") String authorSlug,
            Pageable pageable);

//    Optional<Blog> findByIdAndStatus(Long id, Status status);
//    List<Blog> findAllByBlogcategoryAndStatusOrderByIdDesc(
//            BlogCategory blogcategory,
//            Status status
//    );
//    List<BlogCategory> findAllByOrderByNameAsc();
}
