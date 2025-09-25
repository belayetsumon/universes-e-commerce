package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.RewardRedemption;
import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.model.WalletTransaction;
import com.ecommerce.app.module.ReferralRewards.repository.RewardRedemptionRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletTransactionRepository;
import com.ecommerce.app.module.user.model.Users;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RedemptionService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private RewardRedemptionRepository rewardRedemptionRepository;

    @Transactional
    public void updateWalletBalance(Wallet wallet) {
        BigDecimal balance = walletTransactionRepository
                .sumAmountByWalletAndExpiryDateAfterAndRedeemedFalse(wallet, LocalDateTime.now())
                .orElse(BigDecimal.ZERO);
        wallet.setBalance(balance);
        walletRepository.save(wallet);
    }

    @Transactional
    public boolean redeemPoints(Users user, BigDecimal pointsToRedeem, String type, String details) {
        Optional<Wallet> optionalWallet = walletRepository.findByUsers(user);
        if (optionalWallet.isEmpty()) {
            return false;
        }
        Wallet wallet = optionalWallet.get();

        BigDecimal eligibleBalance = walletTransactionRepository
                .sumAmountByWalletAndExpiryDateAfterAndRedeemedFalse(wallet, LocalDateTime.now())
                .orElse(BigDecimal.ZERO);

        if (eligibleBalance.compareTo(pointsToRedeem) < 0) {
            return false;
        }

        WalletTransaction txn = new WalletTransaction();
        txn.setWallet(wallet);
        txn.setUsers(user);
        txn.setAmount(pointsToRedeem.negate());
        txn.setDescription("Redeemed for " + type + ": " + details);
        txn.setCreatedAt(LocalDateTime.now());
        txn.setType(TransactionType.REDEMPTION);
        txn.setRedeemed(true);
        txn.setExpiryDate(null);
        walletTransactionRepository.save(txn);

        RewardRedemption redemption = new RewardRedemption();
        redemption.setUsers(user);
        redemption.setPointsUsed(pointsToRedeem);
        redemption.setRedemptionType(type);
        redemption.setDetails(details);
        redemption.setRedeemedAt(LocalDateTime.now());
        rewardRedemptionRepository.save(redemption);

        updateWalletBalance(wallet);

        return true;
    }
}
