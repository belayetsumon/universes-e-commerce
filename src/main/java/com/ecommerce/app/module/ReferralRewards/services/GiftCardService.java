package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.GiftCard;
import com.ecommerce.app.module.ReferralRewards.model.GiftCardStatus;
import com.ecommerce.app.module.ReferralRewards.model.GiftCardTransaction;
import com.ecommerce.app.module.ReferralRewards.repository.GiftCardRepository;
import com.ecommerce.app.module.ReferralRewards.repository.GiftCardTransactionRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.order.model.SalesOrder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GiftCardService {

    private final GiftCardRepository giftCardRepository;
    private final GiftCardTransactionRepository giftCardTransactionRepository;

    public GiftCardService(GiftCardRepository giftCardRepository, GiftCardTransactionRepository giftCardTransactionRepository) {
        this.giftCardRepository = giftCardRepository;
        this.giftCardTransactionRepository = giftCardTransactionRepository;
    }

    @Transactional(readOnly = true)
    public Optional<GiftCard> findUsableGiftCard(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return giftCardRepository.findByCodeIgnoreCase(code.trim())
                .filter(gc -> gc.getStatus() == GiftCardStatus.ACTIVE)
                .filter(gc -> !gc.isRedeemed())
                .filter(gc -> gc.getBalance() != null && gc.getBalance().compareTo(BigDecimal.ZERO) > 0);
    }

    @Transactional
    public BigDecimal applyToOrder(GiftCard giftCard, Users user, SalesOrder order, BigDecimal requestedAmount) {
        GiftCardTransaction transaction = applyToOrderWithTransaction(giftCard, user, order, requestedAmount);
        return transaction == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : transaction.getAmountUsed();
    }

    @Transactional
    public GiftCardTransaction applyToOrderWithTransaction(GiftCard giftCard, Users user, SalesOrder order, BigDecimal requestedAmount) {
        if (giftCard == null) {
            throw new IllegalArgumentException("Gift card is required.");
        }
        if (order == null || order.getId() == null) {
            throw new IllegalArgumentException("Order is required.");
        }
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        GiftCard locked = giftCardRepository.findById(giftCard.getId())
                .orElseThrow(() -> new IllegalArgumentException("Gift card not found."));

        if (locked.getStatus() != GiftCardStatus.ACTIVE || locked.isRedeemed()) {
            throw new IllegalArgumentException("Gift card is not usable.");
        }

        if (giftCardTransactionRepository.existsByGiftCardAndOrder_Id(locked, order.getId())) {
            return null;
        }

        BigDecimal balance = locked.getBalance() == null ? BigDecimal.ZERO : locked.getBalance();
        BigDecimal applyAmount = requestedAmount.setScale(2, RoundingMode.HALF_UP).min(balance).max(BigDecimal.ZERO);
        if (applyAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal remaining = balance.subtract(applyAmount).setScale(2, RoundingMode.HALF_UP);
        locked.setBalance(remaining);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            locked.setRedeemed(true);
            locked.setStatus(GiftCardStatus.USED);
        }
        giftCardRepository.save(locked);

        GiftCardTransaction txn = new GiftCardTransaction();
        txn.setGiftCard(locked);
        txn.setOrder(order);
        txn.setUser(user);
        txn.setUsedAt(LocalDateTime.now());
        txn.setAmountUsed(applyAmount);
        txn.setRemainingBalance(remaining);
        return giftCardTransactionRepository.save(txn);
    }
}
