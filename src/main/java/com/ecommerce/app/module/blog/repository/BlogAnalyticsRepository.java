package com.ecommerce.app.module.blog.repository;

import com.ecommerce.app.module.blog.model.BlogAnalytics;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogAnalyticsRepository extends JpaRepository<BlogAnalytics, Long> {

    List<BlogAnalytics> findTop20ByMetricDateBetweenOrderByViewsDesc(LocalDate startDate, LocalDate endDate);
}
