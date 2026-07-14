package com.ecommerce.app.module.marketing.repository;

import com.ecommerce.app.module.marketing.model.TrackingDeliveryEvent;
import com.ecommerce.app.module.marketing.model.TrackingDeliveryStatus;
import com.ecommerce.app.module.marketing.model.TrackingProvider;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingDeliveryEventRepository extends JpaRepository<TrackingDeliveryEvent, Long> {

    Optional<TrackingDeliveryEvent> findByProviderAndEventId(TrackingProvider provider, String eventId);

    List<TrackingDeliveryEvent> findTop50ByDeliveryStatusInAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
            List<TrackingDeliveryStatus> statuses,
            Instant nextAttemptAt
    );
}
