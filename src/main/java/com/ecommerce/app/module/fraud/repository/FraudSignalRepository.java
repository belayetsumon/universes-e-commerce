package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.FraudSignal;
import com.ecommerce.app.module.fraud.model.FraudSignalCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FraudSignalRepository extends JpaRepository<FraudSignal, Long> {

    List<FraudSignal> findByAssessment_IdOrderByIdAsc(Long assessmentId);

    List<FraudSignal> findBySignalCategoryAndTriggered(FraudSignalCategory signalCategory, boolean triggered);

    long countBySignalCodeAndTriggered(String signalCode, boolean triggered);

    @Query("""
            select s.signalCode, count(s)
            from FraudSignal s
            where s.triggered = true
            group by s.signalCode
            order by count(s) desc
            """)
    List<Object[]> countTriggeredBySignalCode();

    @Query("""
            select s.signalCategory, s.signalValue, count(s)
            from FraudSignal s
            where s.triggered = true
              and s.signalValue is not null
            group by s.signalCategory, s.signalValue
            order by count(s) desc
            """)
    List<Object[]> countTriggeredValues();
}
