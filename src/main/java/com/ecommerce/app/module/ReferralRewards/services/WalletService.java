package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.enumvalue.WalletStatus;
import com.ecommerce.app.module.ReferralRewards.enumvalue.WalletTransactionStatus;
import com.ecommerce.app.module.ReferralRewards.enumvalue.WalletTransactionType;
import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.model.WalletTransaction;
import com.ecommerce.app.module.ReferralRewards.repository.WalletRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletTransactionRepository;
import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.fraud.model.FraudPostOrderEventType;
import com.ecommerce.app.module.fraud.services.FraudPostOrderMonitoringService;
import com.ecommerce.app.module.user.model.Users;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final FraudPostOrderMonitoringService fraudPostOrderMonitoringService;

    public WalletService(WalletRepository walletRepository, WalletTransactionRepository walletTransactionRepository,
            FraudPostOrderMonitoringService fraudPostOrderMonitoringService) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.fraudPostOrderMonitoringService = fraudPostOrderMonitoringService;
    }

    @Transactional
    public Wallet ensureWallet(Users user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }

        return walletRepository.findByUser(user).orElseGet(() -> {
            Wallet newWallet = new Wallet();
            newWallet.setUser(user);
            newWallet.setBalance(BigDecimal.ZERO);
            newWallet.setCurrency("BDT");
            newWallet.setStatus(WalletStatus.ACTIVE);
            return walletRepository.save(newWallet);
        });
    }

    @Transactional
    public void creditWallet(Users user, BigDecimal amount, String description) {
        creditWallet(user, amount, description, TransactionType.CREDIT, null, "MANUAL_CREDIT", null, null);
    }

    @Transactional
    public void creditWallet(Users user, BigDecimal amount, String description, TransactionType type,
            LocalDateTime expiryDate, String sourceType, String sourceReference, Integer levelNumber) {
        if (user == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid credit amount or user.");
        }

        WalletTransactionType transactionType = resolveCreditType(type, sourceType);
        String idempotencyKey = buildIdempotencyKey(user, transactionType, sourceType, sourceReference, amount);
        if (sourceReference != null && !sourceReference.isBlank()
                && walletTransactionRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            return;
        }

        Wallet wallet = ensureWallet(user);
        BigDecimal normalizedAmount = amount.abs();
        FraudGuardResult fraudGuard = fraudPostOrderMonitoringService.checkValueReleaseAllowed(
                FraudPostOrderEventType.WALLET_CREDIT_RELEASED,
                null,
                user.getId(),
                null,
                sourceReference
        );
        if (!fraudGuard.isAllowed()) {
            WalletTransaction txn = new WalletTransaction();
            txn.setWallet(wallet);
            txn.setAmount(normalizedAmount);
            txn.setType(transactionType);
            txn.setStatus(WalletTransactionStatus.PENDING);
            txn.setIdempotencyKey(idempotencyKey);
            walletTransactionRepository.save(txn);
            fraudPostOrderMonitoringService.recordWalletCredit(user, normalizedAmount, sourceType, sourceReference, true);
            return;
        }

        wallet.setBalance(safeBalance(wallet).add(normalizedAmount));
        wallet.setLastTransactionAt(LocalDateTime.now());
        walletRepository.save(wallet);

        WalletTransaction txn = new WalletTransaction();
        txn.setWallet(wallet);
        txn.setAmount(normalizedAmount);
        txn.setType(transactionType);
        txn.setStatus(WalletTransactionStatus.SUCCESS);
        txn.setIdempotencyKey(idempotencyKey);
        walletTransactionRepository.save(txn);
        fraudPostOrderMonitoringService.recordWalletCredit(user, normalizedAmount, sourceType, sourceReference, false);
    }

    @Transactional
    public boolean debitWallet(Users user, BigDecimal amount, String description, TransactionType type,
            String sourceType, String sourceReference) {
        if (user == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        Wallet wallet = ensureWallet(user);
        BigDecimal normalizedAmount = amount.abs();
        if (safeBalance(wallet).compareTo(normalizedAmount) < 0) {
            return false;
        }

        WalletTransactionType transactionType = resolveDebitType(type, sourceType);
        String idempotencyKey = buildIdempotencyKey(user, transactionType, sourceType, sourceReference, normalizedAmount);
        if (sourceReference != null && !sourceReference.isBlank()
                && walletTransactionRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            return true;
        }

        wallet.setBalance(safeBalance(wallet).subtract(normalizedAmount));
        wallet.setLastTransactionAt(LocalDateTime.now());
        walletRepository.save(wallet);

        WalletTransaction txn = new WalletTransaction();
        txn.setWallet(wallet);
        txn.setAmount(normalizedAmount);
        txn.setType(transactionType);
        txn.setStatus(WalletTransactionStatus.SUCCESS);
        txn.setIdempotencyKey(idempotencyKey);
        walletTransactionRepository.save(txn);
        return true;
    }

    @Transactional
    public void expireRewardTransaction(WalletTransaction transaction) {
        if (transaction == null || transaction.getId() == null) {
            return;
        }
        if (transaction.getStatus() == WalletTransactionStatus.REVERSED) {
            return;
        }

        transaction.setStatus(WalletTransactionStatus.REVERSED);
        walletTransactionRepository.save(transaction);
    }

    private WalletTransactionType resolveCreditType(TransactionType type, String sourceType) {
        String source = normalize(sourceType);
        if ("CASHBACK".equals(source)) {
            return WalletTransactionType.CASHBACK;
        }
        if ("REFUND".equals(source)) {
            return WalletTransactionType.REFUND;
        }
        if (type == TransactionType.DEBIT || type == TransactionType.PURCHASE
                || type == TransactionType.REDEMPTION || type == TransactionType.CASHOUT) {
            return WalletTransactionType.ADJUSTMENT;
        }
        return WalletTransactionType.CREDIT;
    }

    private WalletTransactionType resolveDebitType(TransactionType type, String sourceType) {
        if (type == TransactionType.CREDIT || type == TransactionType.TOPUP || type == TransactionType.REWARD) {
            return WalletTransactionType.ADJUSTMENT;
        }
        return WalletTransactionType.DEBIT;
    }

    private BigDecimal safeBalance(Wallet wallet) {
        return wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance();
    }

    private String buildIdempotencyKey(Users user, WalletTransactionType type, String sourceType,
            String sourceReference, BigDecimal amount) {
        String userId = user.getId() == null ? "new-user" : String.valueOf(user.getId());
        String reference = sourceReference == null || sourceReference.isBlank()
                ? UUID.randomUUID().toString()
                : sourceReference.trim();
        String raw = String.join(":",
                "WALLET",
                userId,
                type.name(),
                normalize(sourceType),
                normalize(reference),
                amount.setScale(4, RoundingMode.HALF_UP).toPlainString());
        return raw.length() <= 100 ? raw : raw.substring(0, 100);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "GENERAL";
        }
        return value.trim().replaceAll("\\s+", "_").toUpperCase(Locale.ENGLISH);
    }
}
