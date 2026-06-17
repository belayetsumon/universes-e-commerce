package com.ecommerce.app.module.browsinghistory.repository;

import com.ecommerce.app.module.browsinghistory.model.BrowsingHistory;
import com.ecommerce.app.module.browsinghistory.model.BrowsingHistoryViewType;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.product.model.Product;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrowsingHistoryRepository extends JpaRepository<BrowsingHistory, Long> {

    List<BrowsingHistory> findByBrowserIdAndUserIsNullOrderByViewedAtAscIdAsc(String browserId);

    @EntityGraph(attributePaths = {"user", "product", "category"})
    List<BrowsingHistory> findByUserOrderByViewedAtDescIdDesc(Users user);

    @EntityGraph(attributePaths = {"user", "product", "category"})
    List<BrowsingHistory> findByBrowserIdAndUserIsNullOrderByViewedAtDescIdDesc(String browserId);

    long countByProductAndViewType(Product product, BrowsingHistoryViewType viewType);
}
