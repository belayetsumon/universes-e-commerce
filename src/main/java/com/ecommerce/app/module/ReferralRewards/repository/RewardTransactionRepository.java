package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.RewardAccount;
import com.ecommerce.app.module.ReferralRewards.model.RewardTransaction;
import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.user.model.Users;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, Long> {

    @Query("""
            SELECT rt
            FROM RewardTransaction rt
            JOIN FETCH rt.rewardAccount ra
            JOIN FETCH ra.users
            JOIN FETCH rt.users
            ORDER BY rt.createdAt DESC, rt.id DESC
            """)
    List<RewardTransaction> findAllForAdminList();

    @Query("SELECT t FROM RewardTransaction t "
            + "WHERE t.type = 'REWARD' AND t.redeemed = false AND t.expired = false AND t.expiryDate IS NOT NULL AND t.expiryDate <= :now")
    List<RewardTransaction> findExpiredUnredeemed(@Param("now") LocalDateTime now);

    @Query("SELECT SUM(rt.amount) FROM RewardTransaction rt "
            + "WHERE rt.rewardAccount = :rewardAccount AND rt.expiryDate > :now AND rt.redeemed = false")
    Optional<BigDecimal> sumAmountByRewardAccountAndExpiryDateAfterAndRedeemedFalse(
            @Param("rewardAccount") RewardAccount rewardAccount,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT SUM(rt.amount) FROM RewardTransaction rt "
            + "WHERE rt.rewardAccount.users = :user AND rt.expiryDate > :now AND rt.redeemed = false")
    Optional<BigDecimal> sumAvailableByUser(@Param("user") Users user, @Param("now") LocalDateTime now);

    @Query("SELECT SUM(rt.amount) FROM RewardTransaction rt "
            + "WHERE rt.rewardAccount.users = :user AND rt.amount > 0")
    Optional<BigDecimal> sumCreditsByUser(@Param("user") Users user);

    @Query("SELECT SUM(rt.amount) FROM RewardTransaction rt "
            + "WHERE rt.rewardAccount.users = :user AND rt.amount < :amount")
    Optional<BigDecimal> sumDebitsByUser(@Param("user") Users user, @Param("amount") BigDecimal amount);

    @Query("SELECT SUM(rt.amount) FROM RewardTransaction rt "
            + "WHERE rt.rewardAccount.users = :user AND rt.type = :type AND rt.createdAt >= :date")
    Optional<BigDecimal> sumByUserTypeSince(
            @Param("user") Users user,
            @Param("type") TransactionType type,
            @Param("date") LocalDateTime date
    );

    List<RewardTransaction> findByExpiryDateBeforeAndRedeemedFalseAndType(LocalDateTime now, TransactionType type);

    List<RewardTransaction> findAllByExpiryDateBeforeAndExpiredFalse(LocalDateTime now);

    @Query("SELECT SUM(rt.amount) FROM RewardTransaction rt "
            + "WHERE rt.rewardAccount = :rewardAccount AND rt.type = :type AND rt.createdAt > :after")
    Optional<BigDecimal> sumByRewardAccountTypeSince(
            @Param("rewardAccount") RewardAccount rewardAccount,
            @Param("type") TransactionType type,
            @Param("after") LocalDateTime after
    );

    @Query("SELECT COALESCE(SUM(rt.amount), 0) FROM RewardTransaction rt "
            + "WHERE rt.rewardAccount = :rewardAccount AND rt.type = :type AND rt.createdAt > :after")
    BigDecimal sumAmountByRewardAccountAndTypeAndCreatedAtAfter(
            @Param("rewardAccount") RewardAccount rewardAccount,
            @Param("type") TransactionType type,
            @Param("after") LocalDateTime after
    );

    @Query("SELECT COALESCE(SUM(rt.amount), 0) FROM RewardTransaction rt "
            + "WHERE rt.rewardAccount.users = :user AND rt.expiryDate > :expiryDate AND rt.redeemed = false")
    Optional<BigDecimal> sumAmountByUserAndExpiryDateAfterAndRedeemedFalse(
            @Param("user") Users user,
            @Param("expiryDate") LocalDateTime expiryDate
    );

    @Query("SELECT COALESCE(SUM(rt.amount), 0) FROM RewardTransaction rt "
            + "WHERE rt.rewardAccount.users = :user AND rt.amount >= :amount")
    Optional<BigDecimal> sumAmountByUserAndAmountGreaterThanEqual(
            @Param("user") Users user,
            @Param("amount") BigDecimal amount
    );

    @Query("SELECT COALESCE(SUM(rt.amount), 0) FROM RewardTransaction rt "
            + "WHERE rt.rewardAccount.users = :user AND rt.amount < :amount")
    Optional<BigDecimal> sumAmountByUserAndAmountLessThan(
            @Param("user") Users user,
            @Param("amount") BigDecimal amount
    );

    Page<RewardTransaction> findByRewardAccount_UsersAndTypeInAndCreatedAtBetween(
            Users users,
            List<TransactionType> types,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    List<RewardTransaction> findByRewardAccount(RewardAccount rewardAccount);

    boolean existsByRewardAccount(RewardAccount rewardAccount);

    List<RewardTransaction> findByRewardAccount_UsersOrderByCreatedAtDesc(Users user);

    List<RewardTransaction> findByRewardAccount_Users_Id(Long userId);

    Optional<RewardTransaction> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
