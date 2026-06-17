package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.CashbackStatus;
import com.ecommerce.app.module.ReferralRewards.model.CashbackTransaction;
import com.ecommerce.app.module.ReferralRewards.model.GiftCard;
import com.ecommerce.app.module.ReferralRewards.model.RewardAccount;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.repository.CashbackTransactionRepository;
import com.ecommerce.app.module.ReferralRewards.repository.GiftCardRepository;
import com.ecommerce.app.module.ReferralRewards.repository.RewardAccountRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletRepository;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionReportingService {

    private final WalletRepository walletRepository;
    private final RewardAccountRepository rewardAccountRepository;
    private final CashbackTransactionRepository cashbackTransactionRepository;
    private final GiftCardRepository giftCardRepository;

    public PromotionReportingService(
            WalletRepository walletRepository,
            RewardAccountRepository rewardAccountRepository,
            CashbackTransactionRepository cashbackTransactionRepository,
            GiftCardRepository giftCardRepository) {
        this.walletRepository = walletRepository;
        this.rewardAccountRepository = rewardAccountRepository;
        this.cashbackTransactionRepository = cashbackTransactionRepository;
        this.giftCardRepository = giftCardRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> currentLiabilitySummary() {
        Map<String, BigDecimal> summary = new LinkedHashMap<>();
        summary.put("walletMoneyLiability", walletRepository.findAll().stream()
                .map(Wallet::getBalance)
                .map(this::safeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.put("rewardPointLiability", rewardAccountRepository.findAll().stream()
                .map(RewardAccount::getBalance)
                .map(this::safeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.put("pendingCashbackLiability", cashbackTransactionRepository.findAll().stream()
                .filter(transaction -> transaction.getStatus() == CashbackStatus.PENDING)
                .map(CashbackTransaction::getAmount)
                .map(this::safeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.put("outstandingGiftCardBalance", giftCardRepository.findAll().stream()
                .filter(giftCard -> !giftCard.isRedeemed())
                .map(GiftCard::getBalance)
                .map(this::safeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return summary;
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
