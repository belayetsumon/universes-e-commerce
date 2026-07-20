package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.enumvalue.CashbackCreditDestination;
import com.ecommerce.app.module.ReferralRewards.model.CashbackPolicy;
import com.ecommerce.app.module.ReferralRewards.enumvalue.CashbackPolicyStatus;
import com.ecommerce.app.module.ReferralRewards.enumvalue.CashbackStatus;
import com.ecommerce.app.module.ReferralRewards.model.CashbackTransaction;
import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.ReferralRewards.repository.CashbackPolicyRepository;
import com.ecommerce.app.module.ReferralRewards.repository.CashbackTransactionRepository;
import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.fraud.model.FraudPostOrderEventType;
import com.ecommerce.app.module.fraud.services.FraudPostOrderMonitoringService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.order.model.OrderItem;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.product.model.Productcategory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CashbackService {

    private final CashbackPolicyRepository cashbackPolicyRepository;
    private final CashbackTransactionRepository cashbackTransactionRepository;
    private final WalletService walletService;
    private final FraudPostOrderMonitoringService fraudPostOrderMonitoringService;

    public CashbackService(
            CashbackPolicyRepository cashbackPolicyRepository,
            CashbackTransactionRepository cashbackTransactionRepository,
            WalletService walletService,
            FraudPostOrderMonitoringService fraudPostOrderMonitoringService
    ) {
        this.cashbackPolicyRepository = cashbackPolicyRepository;
        this.cashbackTransactionRepository = cashbackTransactionRepository;
        this.walletService = walletService;
        this.fraudPostOrderMonitoringService = fraudPostOrderMonitoringService;
    }

    @Transactional(readOnly = true)
    public BigDecimal computeExpectedCashback(SalesOrder order, BigDecimal orderSubtotal) {
        CashbackQuote quote = resolveBestCashbackQuote(order, orderSubtotal);
        return quote.getAmount().setScale(2, RoundingMode.HALF_UP);
    }

    private CashbackQuote resolveBestCashbackQuote(SalesOrder order, BigDecimal orderSubtotal) {
        if (order == null || orderSubtotal == null || orderSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return CashbackQuote.empty();
        }

        List<CashbackPolicy> activePolicies = cashbackPolicyRepository.findActivePolicies(
                CashbackPolicyStatus.ACTIVE,
                LocalDateTime.now()
        );
        if (activePolicies == null || activePolicies.isEmpty()) {
            return CashbackQuote.empty();
        }

        CashbackPolicy bestPolicy = null;
        BigDecimal best = BigDecimal.ZERO;
        BigDecimal subtotal = orderSubtotal.setScale(2, RoundingMode.HALF_UP);
        for (CashbackPolicy policy : activePolicies) {
            if (policy == null) {
                continue;
            }
            BigDecimal eligibleSubtotal = resolveEligibleSubtotal(policy, order, subtotal);
            if (eligibleSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal minOrder = policy.getMinOrderValue() == null ? BigDecimal.ZERO : policy.getMinOrderValue();
            if (eligibleSubtotal.compareTo(minOrder) < 0) {
                continue;
            }

            BigDecimal pct = policy.getPercentage() == null ? BigDecimal.ZERO : policy.getPercentage();
            BigDecimal computed = eligibleSubtotal.multiply(pct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal max = policy.getMaxCashback() == null ? computed : policy.getMaxCashback();
            BigDecimal limited = computed.min(max).max(BigDecimal.ZERO);
            if (limited.compareTo(best) > 0) {
                best = limited;
                bestPolicy = policy;
            }
        }
        return new CashbackQuote(bestPolicy, best.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal resolveEligibleSubtotal(CashbackPolicy policy, SalesOrder order, BigDecimal fallbackSubtotal) {
        Set<Long> categoryIds = policy.getCategoryIds();
        if (categoryIds == null || categoryIds.isEmpty()) {
            return fallbackSubtotal;
        }
        if (order.getOrderItem() == null || order.getOrderItem().isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : order.getOrderItem()) {
            if (matchesCategoryScope(item, categoryIds)) {
                BigDecimal itemTotal = item.getItemTotal() == null ? BigDecimal.ZERO : item.getItemTotal();
                subtotal = subtotal.add(itemTotal);
            }
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean matchesCategoryScope(OrderItem item, Set<Long> categoryIds) {
        if (item == null || item.getProduct() == null || item.getProduct().getProductcategory() == null) {
            return false;
        }
        Productcategory category = item.getProduct().getProductcategory();
        return category.getId() != null && categoryIds.contains(category.getId());
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
                List.of(CashbackStatus.PENDING, CashbackStatus.APPROVED, CashbackStatus.CREDITED)
        ) > 0) {
            return;
        }

        CashbackQuote quote = resolveBestCashbackQuote(order, resolveOrderSubtotal(order, amount));
        if (quote.getPolicy() == null || quote.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        CashbackTransaction txn = new CashbackTransaction();
        txn.setUser(user);
        txn.setOrderId(orderId);
        txn.setPolicy(quote.getPolicy());
        txn.setAmount(quote.getAmount());
        txn.setCurrency(resolveCurrency(quote.getPolicy()));
        txn.setStatus(CashbackStatus.PENDING);
        txn.setCreditedTo(CashbackCreditDestination.WALLET);
        txn.setIdempotencyKey("CASHBACK:ORDER:" + orderId);
        txn.setAvailableAt(LocalDateTime.now());
        txn.setRemarks("Cashback pending for order #" + orderId);
        cashbackTransactionRepository.save(txn);
    }

    @Transactional
    public void approveAndPayToWalletIfPending(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }
        CashbackTransaction txn = cashbackTransactionRepository
                .findByOrderIdAndStatusForProcessing(orderId, CashbackStatus.PENDING)
                .stream()
                .findFirst()
                .orElse(null);
        if (txn == null || txn.getStatus() != CashbackStatus.PENDING) {
            return;
        }

        Users user = txn.getUser();
        BigDecimal amount = txn.getAmount() == null ? BigDecimal.ZERO : txn.getAmount().setScale(2, RoundingMode.HALF_UP);
        if (user == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        Long numericOrderId = parseOrderId(orderId);
        FraudGuardResult fraudGuard = fraudPostOrderMonitoringService.checkValueReleaseAllowed(
                FraudPostOrderEventType.CASHBACK_RELEASED,
                numericOrderId,
                user.getId(),
                null,
                "ORDER:" + orderId
        );
        if (!fraudGuard.isAllowed()) {
            txn.setRemarks("Cashback held by fraud control for order #" + orderId);
            cashbackTransactionRepository.save(txn);
            fraudPostOrderMonitoringService.recordCashbackRelease(numericOrderId, user.getId(), amount, true, fraudGuard.getReason());
            return;
        }

        txn.setStatus(CashbackStatus.CREDITED);
        txn.setCreditedAt(LocalDateTime.now());
        txn.setRemarks("Cashback credited to wallet for order #" + orderId);
        cashbackTransactionRepository.save(txn);
        fraudPostOrderMonitoringService.recordCashbackRelease(numericOrderId, user.getId(), amount, false, null);

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

    private BigDecimal resolveOrderSubtotal(SalesOrder order, BigDecimal fallbackAmount) {
        if (order != null && order.getGrandTotal() != null) {
            return order.getGrandTotal();
        }
        return fallbackAmount == null ? BigDecimal.ZERO : fallbackAmount;
    }

    private String resolveCurrency(CashbackPolicy policy) {
        if (policy != null && policy.getCurrency() != null && !policy.getCurrency().isBlank()) {
            return policy.getCurrency().trim().toUpperCase();
        }
        return "BDT";
    }

    private Long parseOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(orderId.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static final class CashbackQuote {

        private final CashbackPolicy policy;
        private final BigDecimal amount;

        private CashbackQuote(CashbackPolicy policy, BigDecimal amount) {
            this.policy = policy;
            this.amount = amount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : amount;
        }

        private static CashbackQuote empty() {
            return new CashbackQuote(null, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        private CashbackPolicy getPolicy() {
            return policy;
        }

        private BigDecimal getAmount() {
            return amount;
        }
    }
}
