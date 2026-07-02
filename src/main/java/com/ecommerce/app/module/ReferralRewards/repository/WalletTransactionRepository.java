package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.enumvalue.WalletTransactionStatus;
import com.ecommerce.app.module.ReferralRewards.enumvalue.WalletTransactionType;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.model.WalletTransaction;
import com.ecommerce.app.module.user.model.Users;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    @Query("""
            SELECT wt
            FROM WalletTransaction wt
            JOIN FETCH wt.wallet w
            JOIN FETCH w.user
            ORDER BY wt.created DESC, wt.id DESC
            """)
    List<WalletTransaction> findAllForAdminList();

    @Query("""
            SELECT COALESCE(SUM(wt.amount), 0)
            FROM WalletTransaction wt
            WHERE wt.wallet = :wallet
              AND wt.type = :type
              AND wt.status = :status
            """)
    BigDecimal sumByWalletAndTypeAndStatus(
            @Param("wallet") Wallet wallet,
            @Param("type") WalletTransactionType type,
            @Param("status") WalletTransactionStatus status
    );

    @Query("""
            SELECT COALESCE(SUM(wt.amount), 0)
            FROM WalletTransaction wt
            WHERE wt.wallet.user = :user
              AND wt.type = :type
              AND wt.status = :status
            """)
    BigDecimal sumByUserAndTypeAndStatus(
            @Param("user") Users user,
            @Param("type") WalletTransactionType type,
            @Param("status") WalletTransactionStatus status
    );

    @Query("""
            SELECT COALESCE(SUM(wt.amount), 0)
            FROM WalletTransaction wt
            WHERE wt.wallet.user = :user
              AND wt.type = :type
              AND wt.created >= :date
            """)
    Optional<BigDecimal> sumByUserTypeSince(
            @Param("user") Users user,
            @Param("type") WalletTransactionType type,
            @Param("date") LocalDateTime date
    );

    @Query("""
            SELECT COALESCE(SUM(wt.amount), 0)
            FROM WalletTransaction wt
            WHERE wt.wallet = :wallet
              AND wt.type = :type
              AND wt.created > :after
            """)
    BigDecimal sumAmountByWalletAndTypeAndCreatedAtAfter(
            @Param("wallet") Wallet wallet,
            @Param("type") WalletTransactionType type,
            @Param("after") LocalDateTime after
    );

    default Optional<BigDecimal> sumCreditsByUser(Users user) {
        BigDecimal total = zeroIfNull(sumByUserAndTypeAndStatus(user, WalletTransactionType.CREDIT, WalletTransactionStatus.SUCCESS))
                .add(zeroIfNull(sumByUserAndTypeAndStatus(user, WalletTransactionType.CASHBACK, WalletTransactionStatus.SUCCESS)))
                .add(zeroIfNull(sumByUserAndTypeAndStatus(user, WalletTransactionType.REFUND, WalletTransactionStatus.SUCCESS)));
        return Optional.of(total);
    }

    default Optional<BigDecimal> sumDebitsByUser(Users user) {
        return Optional.of(zeroIfNull(sumByUserAndTypeAndStatus(user, WalletTransactionType.DEBIT, WalletTransactionStatus.SUCCESS)));
    }

    @Query("""
            SELECT COALESCE(SUM(wt.amount), 0)
            FROM WalletTransaction wt
            WHERE wt.wallet.user = :user
              AND wt.amount >= :amount
            """)
    Optional<BigDecimal> sumAmountByUserAndAmountGreaterThanEqual(
            @Param("user") Users user,
            @Param("amount") BigDecimal amount
    );

    @Query("""
            SELECT COALESCE(SUM(wt.amount), 0)
            FROM WalletTransaction wt
            WHERE wt.wallet.user = :user
              AND wt.amount < :amount
            """)
    Optional<BigDecimal> sumAmountByUserAndAmountLessThan(
            @Param("user") Users user,
            @Param("amount") BigDecimal amount
    );

    @Query("""
            SELECT wt
            FROM WalletTransaction wt
            JOIN FETCH wt.wallet w
            JOIN FETCH w.user
            WHERE w.user = :user
            ORDER BY wt.created DESC, wt.id DESC
            """)
    List<WalletTransaction> findByWallet_UsersOrderByCreatedAtDesc(@Param("user") Users user);

    @Query("""
            SELECT wt
            FROM WalletTransaction wt
            JOIN FETCH wt.wallet w
            JOIN FETCH w.user u
            WHERE u.id = :userId
            ORDER BY wt.created DESC, wt.id DESC
            """)
    List<WalletTransaction> findByWallet_Users_Id(@Param("userId") Long userId);

    List<WalletTransaction> findByWallet(Wallet wallet);

    boolean existsByWallet(Wallet wallet);

    Optional<WalletTransaction> findByIdempotencyKey(String idempotencyKey);

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
