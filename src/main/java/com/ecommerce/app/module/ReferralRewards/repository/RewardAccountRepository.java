package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.RewardAccount;
import com.ecommerce.app.module.user.model.Users;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RewardAccountRepository extends JpaRepository<RewardAccount, Long> {

    @Query("""
            SELECT ra
            FROM RewardAccount ra
            JOIN FETCH ra.users
            ORDER BY ra.id DESC
            """)
    List<RewardAccount> findAllForAdminList();

    Optional<RewardAccount> findByUsers(Users user);
}
