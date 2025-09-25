package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.model.WalletTransaction;
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

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    // 1. Expired & unredeemed REWARD transactions
    @Query("SELECT t FROM WalletTransaction t "
            + "WHERE t.type = 'REWARD' AND t.redeemed = false AND t.expiryDate IS NOT NULL AND t.expiryDate <= :now")
    List<WalletTransaction> findExpiredUnredeemed(@Param("now") LocalDateTime now);

    // 2. Sum of active, unredeemed rewards in a wallet
    @Query("SELECT SUM(wt.amount) FROM WalletTransaction wt "
            + "WHERE wt.wallet = :wallet AND wt.expiryDate > :now AND wt.redeemed = false")
    Optional<BigDecimal> sumAmountByWalletAndExpiryDateAfterAndRedeemedFalse(
            @Param("wallet") Wallet wallet,
            @Param("now") LocalDateTime now
    );

    // 3. Sum of rewards for user not expired and not redeemed
    @Query("SELECT SUM(wt.amount) FROM WalletTransaction wt "
            + "WHERE wt.users = :user AND wt.expiryDate > :now AND wt.redeemed = false")
    Optional<BigDecimal> sumAvailableByUser(@Param("user") Users user, @Param("now") LocalDateTime now);

    // 4. Sum of credited amounts for a user
    @Query("SELECT SUM(wt.amount) FROM WalletTransaction wt "
            + "WHERE wt.users = :user AND wt.type = 'CREDIT'")
    Optional<BigDecimal> sumCreditsByUser(@Param("user") Users user);

    // 5. Sum of debited amounts for a user
    @Query("SELECT SUM(wt.amount) FROM WalletTransaction wt "
            + "WHERE wt.users = :user AND wt.amount < :amount")
    Optional<BigDecimal> sumDebitsByUser(@Param("user") Users user, @Param("amount") BigDecimal amount);

    // 6. Sum by type and user since a date
    @Query("SELECT SUM(wt.amount) FROM WalletTransaction wt "
            + "WHERE wt.users = :user AND wt.type = :type AND wt.createdAt >= :date")
    Optional<BigDecimal> sumByUserTypeSince(
            @Param("user") Users user,
            @Param("type") TransactionType type,
            @Param("date") LocalDateTime date
    );

    // 7. Expired, unredeemed transactions of a specific type
    List<WalletTransaction> findByExpiryDateBeforeAndRedeemedFalseAndType(LocalDateTime now, TransactionType type);

    // 8. Expired transactions not yet marked as expired
    List<WalletTransaction> findAllByExpiryDateBeforeAndExpiredFalse(LocalDateTime now);

    // 9. Sum by wallet, type, and time
    @Query("SELECT SUM(wt.amount) FROM WalletTransaction wt "
            + "WHERE wt.wallet = :wallet AND wt.type = :type AND wt.createdAt > :after")
    Optional<BigDecimal> sumByWalletTypeSince(
            @Param("wallet") Wallet wallet,
            @Param("type") TransactionType type,
            @Param("after") LocalDateTime after
    );

    // 10. Same as above, defaulting to 0
    @Query("SELECT COALESCE(SUM(wt.amount), 0) FROM WalletTransaction wt "
            + "WHERE wt.wallet = :wallet AND wt.type = :type AND wt.createdAt > :after")
    BigDecimal sumAmountByWalletAndTypeAndCreatedAtAfter(
            @Param("wallet") Wallet wallet,
            @Param("type") TransactionType type,
            @Param("after") LocalDateTime after
    );

    // 11. Sum of non-redeemed user amounts that haven't expired
    @Query("SELECT COALESCE(SUM(wt.amount), 0) FROM WalletTransaction wt "
            + "WHERE wt.users = :user AND wt.expiryDate > :expiryDate AND wt.redeemed = false")
    Optional<BigDecimal> sumAmountByUserAndExpiryDateAfterAndRedeemedFalse(
            @Param("user") Users user,
            @Param("expiryDate") LocalDateTime expiryDate
    );

    // 12. Sum of user's credited transactions (amount >= X)
    @Query("SELECT COALESCE(SUM(wt.amount), 0) FROM WalletTransaction wt "
            + "WHERE wt.users = :user AND wt.amount >= :amount")
    Optional<BigDecimal> sumAmountByUserAndAmountGreaterThanEqual(
            @Param("user") Users user,
            @Param("amount") BigDecimal amount
    );

    // 13. Sum of user's debited transactions (amount < X)
    @Query("SELECT COALESCE(SUM(wt.amount), 0) FROM WalletTransaction wt "
            + "WHERE wt.users = :user AND wt.amount < :amount")
    Optional<BigDecimal> sumAmountByUserAndAmountLessThan(
            @Param("user") Users user,
            @Param("amount") BigDecimal amount
    );

    // 14. Paginated list of user's transactions
    Page<WalletTransaction> findByWallet_UsersAndTypeInAndCreatedAtBetween(
            Users users,
            List<TransactionType> types,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    // 15. List by wallet
    List<WalletTransaction> findByWallet(Wallet wallet);

    // 16. List user's transactions sorted newest first
    List<WalletTransaction> findByWallet_UsersOrderByCreatedAtDesc(Users user);

    // 17. List by user ID
    List<WalletTransaction> findByWallet_Users_Id(Long userId);
}
