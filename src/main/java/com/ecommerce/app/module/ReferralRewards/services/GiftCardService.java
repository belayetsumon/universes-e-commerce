package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.GiftCard;
import com.ecommerce.app.module.ReferralRewards.enumvalue.GiftCardStatus;
import com.ecommerce.app.module.ReferralRewards.enumvalue.GiftCardTransactionType;
import com.ecommerce.app.module.ReferralRewards.model.GiftCardTransaction;
import com.ecommerce.app.module.ReferralRewards.repository.GiftCardRepository;
import com.ecommerce.app.module.ReferralRewards.repository.GiftCardTransactionRepository;
import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.fraud.model.FraudPostOrderEventType;
import com.ecommerce.app.module.fraud.services.FraudPostOrderMonitoringService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.order.model.SalesOrder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GiftCardService {

    private final GiftCardRepository giftCardRepository;
    private final GiftCardTransactionRepository giftCardTransactionRepository;
    private final FraudPostOrderMonitoringService fraudPostOrderMonitoringService;

    public GiftCardService(GiftCardRepository giftCardRepository, GiftCardTransactionRepository giftCardTransactionRepository,
            FraudPostOrderMonitoringService fraudPostOrderMonitoringService) {
        this.giftCardRepository = giftCardRepository;
        this.giftCardTransactionRepository = giftCardTransactionRepository;
        this.fraudPostOrderMonitoringService = fraudPostOrderMonitoringService;
    }

    @Transactional(readOnly = true)
    public Optional<GiftCard> findUsableGiftCard(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return giftCardRepository.findByCodeIgnoreCase(code.trim())
                .filter(this::isUsableStatus)
                .filter(gc -> !gc.isRedeemed())
                .filter(gc -> gc.getBalance() != null && gc.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .filter(this::isNotExpired);
    }

    @Transactional
    public BigDecimal applyToOrder(GiftCard giftCard, Users user, SalesOrder order, BigDecimal requestedAmount) {
        GiftCardTransaction transaction = applyToOrderWithTransaction(giftCard, user, order, requestedAmount);
        return transaction == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : transaction.getAmount();
    }

    @Transactional
    public GiftCardTransaction applyToOrderWithTransaction(GiftCard giftCard, Users user, SalesOrder order, BigDecimal requestedAmount) {
        if (giftCard == null) {
            throw new IllegalArgumentException("Gift card is required.");
        }
        if (giftCard.getId() == null) {
            throw new IllegalArgumentException("Gift card id is required.");
        }
        if (order == null || order.getId() == null) {
            throw new IllegalArgumentException("Order is required.");
        }
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        FraudGuardResult fraudGuard = fraudPostOrderMonitoringService.checkValueReleaseAllowed(
                FraudPostOrderEventType.GIFT_CARD_USAGE,
                order.getId(),
                user == null ? null : user.getId(),
                order.getVendorId(),
                "ORDER:" + order.getId()
        );
        if (!fraudGuard.isAllowed()) {
            throw new IllegalArgumentException("Gift card redemption is held while fraud review is open for this order.");
        }

        GiftCard locked = giftCardRepository.findById(giftCard.getId())
                .orElseThrow(() -> new IllegalArgumentException("Gift card not found."));

        if (!isUsableStatus(locked) || locked.isRedeemed() || !isNotExpired(locked)) {
            throw new IllegalArgumentException("Gift card is not usable.");
        }
        if (!isAvailableToUser(locked, user)) {
            throw new IllegalArgumentException("Gift card is not available for this customer.");
        }

        String orderId = String.valueOf(order.getId());
        if (giftCardTransactionRepository.existsByGiftCardAndOrderId(locked, orderId)) {
            return null;
        }

        BigDecimal balance = safeMoney(locked.getBalance());
        BigDecimal applyAmount = requestedAmount.setScale(2, RoundingMode.HALF_UP).min(balance).max(BigDecimal.ZERO);
        if (applyAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal remaining = balance.subtract(applyAmount).setScale(2, RoundingMode.HALF_UP);
        locked.setBalance(remaining);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            locked.setRedeemed(true);
            locked.setStatus(GiftCardStatus.REDEEMED);
        } else {
            locked.setRedeemed(false);
            locked.setStatus(GiftCardStatus.PARTIALLY_REDEEMED);
        }
        locked.setLastUsedAt(java.time.LocalDateTime.now());
        giftCardRepository.save(locked);

        GiftCardTransaction txn = new GiftCardTransaction();
        txn.setGiftCard(locked);
        txn.setType(GiftCardTransactionType.REDEEM);
        txn.setAmount(applyAmount);
        txn.setBalanceAfter(remaining);
        txn.setCurrency("BDT");
        txn.setOrderId(orderId);
        txn.setIdempotencyKey("GIFT_CARD:" + locked.getId() + ":ORDER:" + orderId);
        GiftCardTransaction saved = giftCardTransactionRepository.save(txn);
        fraudPostOrderMonitoringService.recordGiftCardUsage(order, applyAmount, locked.getCode(), saved.getId());
        return saved;
    }

    private boolean isUsableStatus(GiftCard giftCard) {
        return giftCard != null
                && (giftCard.getStatus() == GiftCardStatus.ACTIVE
                || giftCard.getStatus() == GiftCardStatus.PARTIALLY_REDEEMED);
    }

    private boolean isNotExpired(GiftCard giftCard) {
        return giftCard == null
                || giftCard.getExpiresAt() == null
                || giftCard.getExpiresAt().isAfter(java.time.LocalDateTime.now());
    }

    private boolean isAvailableToUser(GiftCard giftCard, Users user) {
        if (giftCard == null || giftCard.getIssuedTo() == null) {
            return true;
        }
        return user != null
                && user.getId() != null
                && giftCard.getIssuedTo().getId() != null
                && user.getId().equals(giftCard.getIssuedTo().getId());
    }

    private BigDecimal safeMoney(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : amount.setScale(2, RoundingMode.HALF_UP);
    }
}
