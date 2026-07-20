package com.ecommerce.app.module.review.repository;

import com.ecommerce.app.module.review.model.ProductReview;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    List<ProductReview> findByProduct_IdAndVisibleTrueOrderByCreatedDesc(Long productId);

    List<ProductReview> findByProduct_UuidAndVisibleTrueOrderByCreatedDesc(String productUuid);

    List<ProductReview> findByProduct_IdInAndVisibleTrue(Collection<Long> productIds);

    Optional<ProductReview> findByCustomer_IdAndProduct_Id(Long customerId, Long productId);

    Optional<ProductReview> findByCustomer_IdAndProduct_Uuid(Long customerId, String productUuid);

    List<ProductReview> findByCustomer_IdAndProduct_IdIn(Long customerId, Collection<Long> productIds);
}
