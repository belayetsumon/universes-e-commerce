package com.ecommerce.app.module.marketing.repository;

import com.ecommerce.app.module.marketing.model.SocialShareEvent;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SocialShareEventRepository extends JpaRepository<SocialShareEvent, Long> {

    long countByCreatedAtBetween(Instant start, Instant end);

    long countByReferralCodePresentTrueAndCreatedAtBetween(Instant start, Instant end);

    long countByCustomerUserIdIsNotNullAndCreatedAtBetween(Instant start, Instant end);

    long countByCustomerUserIdIsNullAndCreatedAtBetween(Instant start, Instant end);

    @Query("""
            select e.platform as label, count(e.id) as total
            from SocialShareEvent e
            where e.createdAt between :start and :end
            group by e.platform
            order by count(e.id) desc
            """)
    List<Object[]> countByPlatform(@Param("start") Instant start, @Param("end") Instant end);

    @Query("""
            select e.pageType as label, count(e.id) as total
            from SocialShareEvent e
            where e.createdAt between :start and :end
            group by e.pageType
            order by count(e.id) desc
            """)
    List<Object[]> countByPageType(@Param("start") Instant start, @Param("end") Instant end);

    @Query(value = """
            select public_entity_reference as label, count(id) as total
            from social_share_event
            where created_at between :start and :end
              and page_type = :pageType
              and public_entity_reference is not null
            group by public_entity_reference
            order by count(id) desc
            limit 10
            """, nativeQuery = true)
    List<Object[]> topEntities(@Param("pageType") String pageType, @Param("start") Instant start, @Param("end") Instant end);
}
