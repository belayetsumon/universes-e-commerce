package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.dto.GiftCardPaymentForm;
import com.ecommerce.app.module.ReferralRewards.dto.GiftCardPurchaseForm;
import com.ecommerce.app.module.ReferralRewards.enumvalue.GiftCardPurchaseStatus;
import com.ecommerce.app.module.ReferralRewards.enumvalue.GiftCardStatus;
import com.ecommerce.app.module.ReferralRewards.enumvalue.GiftCardTransactionType;
import com.ecommerce.app.module.ReferralRewards.model.GiftCard;
import com.ecommerce.app.module.ReferralRewards.model.GiftCardPurchase;
import com.ecommerce.app.module.ReferralRewards.model.GiftCardTransaction;
import com.ecommerce.app.module.ReferralRewards.repository.GiftCardPurchaseRepository;
import com.ecommerce.app.module.ReferralRewards.repository.GiftCardRepository;
import com.ecommerce.app.module.ReferralRewards.repository.GiftCardTransactionRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.order.model.PaymentMethod;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GiftCardPurchaseService {

    private static final BigDecimal MIN_PURCHASE_AMOUNT = new BigDecimal("100.00");
    private static final BigDecimal MAX_PURCHASE_AMOUNT = new BigDecimal("50000.00");
    private static final String DEFAULT_CURRENCY = "BDT";
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_RANDOM_LENGTH = 10;

    private final GiftCardPurchaseRepository giftCardPurchaseRepository;
    private final GiftCardRepository giftCardRepository;
    private final GiftCardTransactionRepository giftCardTransactionRepository;
    private final UsersRepository usersRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public GiftCardPurchaseService(
            GiftCardPurchaseRepository giftCardPurchaseRepository,
            GiftCardRepository giftCardRepository,
            GiftCardTransactionRepository giftCardTransactionRepository,
            UsersRepository usersRepository) {
        this.giftCardPurchaseRepository = giftCardPurchaseRepository;
        this.giftCardRepository = giftCardRepository;
        this.giftCardTransactionRepository = giftCardTransactionRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional(readOnly = true)
    public List<GiftCardPurchase> findPurchasesForCustomer(Users buyer) {
        if (buyer == null || buyer.getId() == null) {
            throw new IllegalArgumentException("Please login to view gift card purchases.");
        }
        return giftCardPurchaseRepository.findByBuyerForList(buyer);
    }

    @Transactional(readOnly = true)
    public GiftCardPurchase getPurchaseForCustomer(String purchaseUuid, Users buyer) {
        validateCustomer(buyer);
        String normalizedUuid = requireText(purchaseUuid, "Gift card purchase reference is required.");
        return giftCardPurchaseRepository.findByUuidAndBuyer(normalizedUuid, buyer)
                .orElseThrow(() -> new IllegalArgumentException("Gift card purchase was not found for this customer."));
    }

    @Transactional
    public GiftCardPurchase createPendingPurchase(Users buyer, GiftCardPurchaseForm form) {
        validateCustomer(buyer);
        if (form == null) {
            throw new IllegalArgumentException("Gift card purchase details are required.");
        }

        BigDecimal amount = normalizeAmount(form.getAmount());
        validatePurchaseAmount(amount);

        GiftCardPurchase purchase = new GiftCardPurchase();
        purchase.setBuyer(buyer);
        purchase.setIssuedTo(resolveIssuedTo(buyer, form.getRecipientEmail()));
        purchase.setAmount(amount);
        purchase.setCurrency(DEFAULT_CURRENCY);
        purchase.setStatus(GiftCardPurchaseStatus.PENDING_PAYMENT);
        purchase.setRecipientName(cleanText(form.getRecipientName()));
        purchase.setRecipientEmail(cleanEmail(form.getRecipientEmail()));
        purchase.setGiftMessage(cleanText(form.getMessage()));
        return giftCardPurchaseRepository.save(purchase);
    }

    @Transactional
    public GiftCardPurchase completePayment(String purchaseUuid, Users buyer, GiftCardPaymentForm form) {
        validateCustomer(buyer);
        if (form == null) {
            throw new IllegalArgumentException("Payment details are required.");
        }

        GiftCardPurchase purchase = giftCardPurchaseRepository.findByUuidAndBuyerForUpdate(
                requireText(purchaseUuid, "Gift card purchase reference is required."),
                buyer
        ).orElseThrow(() -> new IllegalArgumentException("Gift card purchase was not found for this customer."));

        if (purchase.getStatus() == GiftCardPurchaseStatus.PAID) {
            return purchase;
        }
        if (purchase.getStatus() != GiftCardPurchaseStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Only pending gift card purchases can be paid.");
        }

        PaymentMethod paymentMethod = form.getPaymentMethod();
        if (paymentMethod == null || !paymentMethod.isOnlineGateway()) {
            throw new IllegalArgumentException("Please choose SSLCommerz or bKash for gift card payment.");
        }

        String paymentReference = requireText(form.getPaymentReference(), "Payment reference or transaction ID is required.");
        if (giftCardPurchaseRepository.existsByPaymentReferenceIgnoreCase(paymentReference)) {
            throw new IllegalArgumentException("This payment reference is already linked to another gift card purchase.");
        }

        GiftCard issuedCard = issueGiftCard(purchase);

        purchase.setGiftCard(issuedCard);
        purchase.setPaymentMethod(paymentMethod);
        purchase.setPaymentReference(paymentReference);
        purchase.setPaymentNote(cleanText(form.getPaymentNote()));
        purchase.setPaidAt(LocalDateTime.now());
        purchase.setStatus(GiftCardPurchaseStatus.PAID);
        purchase.setFailureMessage(null);
        return giftCardPurchaseRepository.save(purchase);
    }

    private GiftCard issueGiftCard(GiftCardPurchase purchase) {
        BigDecimal amount = normalizeAmount(purchase.getAmount());
        LocalDateTime now = LocalDateTime.now();

        GiftCard giftCard = new GiftCard();
        giftCard.setCode(generateUniqueGiftCardCode());
        giftCard.setInitialValue(amount);
        giftCard.setBalance(amount);
        giftCard.setStatus(GiftCardStatus.ACTIVE);
        giftCard.setIssuedTo(purchase.getIssuedTo());
        giftCard.setPurchasedBy(purchase.getBuyer());
        giftCard.setRedeemed(false);
        giftCard.setIssuedAt(now);
        giftCard.setActivatedAt(now);
        GiftCard savedGiftCard = giftCardRepository.save(giftCard);

        GiftCardTransaction transaction = new GiftCardTransaction();
        transaction.setGiftCard(savedGiftCard);
        transaction.setType(GiftCardTransactionType.ISSUE);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(amount);
        transaction.setCurrency(DEFAULT_CURRENCY);
        transaction.setIdempotencyKey("GIFT_CARD_PURCHASE:" + purchase.getUuid() + ":ISSUE");
        giftCardTransactionRepository.save(transaction);

        return savedGiftCard;
    }

    private String generateUniqueGiftCardCode() {
        for (int attempt = 0; attempt < 12; attempt++) {
            String candidate = "GC" + randomToken(CODE_RANDOM_LENGTH);
            if (!giftCardRepository.existsByCodeIgnoreCase(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not generate a unique gift card code. Please try again.");
    }

    private String randomToken(int length) {
        StringBuilder token = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            token.append(CODE_ALPHABET.charAt(secureRandom.nextInt(CODE_ALPHABET.length())));
        }
        return token.toString();
    }

    private Users resolveIssuedTo(Users buyer, String recipientEmail) {
        String email = cleanEmail(recipientEmail);
        if (email == null || email.equalsIgnoreCase(buyer.getEmail())) {
            return buyer;
        }
        return usersRepository.findByEmail(email).orElse(null);
    }

    private void validateCustomer(Users customer) {
        if (customer == null || customer.getId() == null) {
            throw new IllegalArgumentException("Please login before buying a gift card.");
        }
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : amount.setScale(2, RoundingMode.HALF_UP);
    }

    private void validatePurchaseAmount(BigDecimal amount) {
        if (amount.compareTo(MIN_PURCHASE_AMOUNT) < 0) {
            throw new IllegalArgumentException("Gift card amount must be at least " + MIN_PURCHASE_AMOUNT + ".");
        }
        if (amount.compareTo(MAX_PURCHASE_AMOUNT) > 0) {
            throw new IllegalArgumentException("Gift card amount cannot exceed " + MAX_PURCHASE_AMOUNT + ".");
        }
    }

    private String requireText(String value, String message) {
        String cleaned = cleanText(value);
        if (cleaned == null) {
            throw new IllegalArgumentException(message);
        }
        return cleaned;
    }

    private String cleanEmail(String value) {
        String cleaned = cleanText(value);
        return cleaned == null ? null : cleaned.toLowerCase();
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
