package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.CashbackPolicy;
import com.ecommerce.app.module.ReferralRewards.model.CashbackPolicyStatus;
import com.ecommerce.app.module.ReferralRewards.model.CashbackStatus;
import com.ecommerce.app.module.ReferralRewards.model.CashbackTransaction;
import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.ReferralRewards.repository.CashbackPolicyRepository;
import com.ecommerce.app.module.ReferralRewards.repository.CashbackTransactionRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.order.model.SalesOrder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CashbackService {

    private final CashbackPolicyRepository cashbackPolicyRepository;
    private final CashbackTransactionRepository cashbackTransactionRepository;
    private final WalletService walletService;

    public CashbackService(
            CashbackPolicyRepository cashbackPolicyRepository,
            CashbackTransactionRepository cashbackTransactionRepository,
            WalletService walletService
    ) {
        this.cashbackPolicyRepository = cashbackPolicyRepository;
        this.cashbackTransactionRepository = cashbackTransactionRepository;
        this.walletService = walletService;
    }

    @Transactional(readOnly = true)
    public BigDecimal computeExpectedCashback(SalesOrder order, BigDecimal orderSubtotal) {
        if (order == null || orderSubtotal == null || orderSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        List<CashbackPolicy> activePolicies = cashbackPolicyRepository.findActivePolicies(
                CashbackPolicyStatus.ACTIVE,
                LocalDateTime.now()
        );
        if (activePolicies == null || activePolicies.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        // Simple enterprise-safe default: apply the best available policy by payout amount.
        BigDecimal best = BigDecimal.ZERO;
        BigDecimal subtotal = orderSubtotal.setScale(2, RoundingMode.HALF_UP);
        for (CashbackPolicy policy : activePolicies) {
            if (policy == null) {
                continue;
            }
            BigDecimal minOrder = policy.getMinOrderValue() == null ? BigDecimal.ZERO : policy.getMinOrderValue();
            if (subtotal.compareTo(minOrder) < 0) {
                continue;
            }

            BigDecimal pct = policy.getPercentage() == null ? BigDecimal.ZERO : policy.getPercentage();
            BigDecimal computed = subtotal.multiply(pct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal max = policy.getMaxCashback() == null ? computed : policy.getMaxCashback();
            BigDecimal limited = computed.min(max).max(BigDecimal.ZERO);
            if (limited.compareTo(best) > 0) {
                best = limited;
            }
        }
        return best.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void createPendingCashbackIfMissing(Users user, SalesOrder order, BigDecimal expectedAmount) {
        if (user == null || order == null || order.getId() == null) {
            return;
        }
        BigDecimal amount = expectedAmount == null ? BigDecimal.ZERO : expectedAmount.setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        String orderId = String.valueOf(order.getId());
        if (cashbackTransactionRepository.countByOrderIdAndStatusIn(
                orderId,
                List.of(CashbackStatus.PENDING, CashbackStatus.APPROVED, CashbackStatus.PAID)
        ) > 0) {
            return;
        }

        CashbackTransaction txn = new CashbackTransaction();
        txn.setUser(user);
        txn.setOrderId(orderId);
        txn.setAmount(amount);
        txn.setStatus(CashbackStatus.PENDING);
        txn.setCreditedTo("WALLET");
        cashbackTransactionRepository.save(txn);
    }

    @Transactional
    public void approveAndPayToWalletIfPending(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }
        CashbackTransaction txn = cashbackTransactionRepository.findByOrderId(orderId).orElse(null);
        if (txn == null || txn.getStatus() != CashbackStatus.PENDING) {
            return;
        }

        Users user = txn.getUser();
        BigDecimal amount = txn.getAmount() == null ? BigDecimal.ZERO : txn.getAmount().setScale(2, RoundingMode.HALF_UP);
        if (user == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        txn.setStatus(CashbackStatus.PAID);
        cashbackTransactionRepository.save(txn);

        walletService.creditWallet(
                user,
                amount,
                "Cashback credited for order #" + orderId,
                TransactionType.CREDIT,
                null,
                "CASHBACK",
                "ORDER:" + orderId,
                null
        );
    }
}

