/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.vendor.services;

import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.fraud.services.FraudPayoutGuard;
import com.ecommerce.app.module.fraud.services.FraudPostOrderMonitoringService;
import com.ecommerce.app.module.fraud.services.VendorRiskProfileService;
import com.ecommerce.app.module.order.model.OrderItem;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.vendor.model.VendorPayout;
import com.ecommerce.app.vendor.model.VendorPayoutMethod;
import com.ecommerce.app.vendor.model.VendorPayoutStatusEnum;
import com.ecommerce.app.vendor.model.VendorTransaction;
import com.ecommerce.app.vendor.model.VendorTransactionStatusEnum;
import com.ecommerce.app.vendor.model.VendorTransactionTypeEnum;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorPayoutRepository;
import com.ecommerce.app.vendor.repository.VendorTransactionRepository;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author libertyerp_local
 */
@Service
public class VendorFinanceService {

    @Autowired
    private VendorTransactionRepository transactionRepo;
    @Autowired
    private VendorPayoutRepository payoutRepo;
    @Autowired
    private VendorprofileRepository vendorprofileRepository;
    @Autowired
    private FraudPayoutGuard fraudPayoutGuard;
    @Autowired
    private FraudPostOrderMonitoringService fraudPostOrderMonitoringService;
    @Autowired
    private VendorRiskProfileService vendorRiskProfileService;

    public EnumMap<VendorTransactionStatusEnum, BigDecimal> getVendorBalance(Long vendorId) {
        EnumMap<VendorTransactionStatusEnum, BigDecimal> balanceMap = new EnumMap<>(VendorTransactionStatusEnum.class);
        balanceMap.put(VendorTransactionStatusEnum.PENDING, BigDecimal.ZERO);
        balanceMap.put(VendorTransactionStatusEnum.AVAILABLE, BigDecimal.ZERO);
        balanceMap.put(VendorTransactionStatusEnum.REQUESTED, BigDecimal.ZERO);
        balanceMap.put(VendorTransactionStatusEnum.PAID, BigDecimal.ZERO);

        // 2026-04-22: Balance summaries must follow the same credit-minus-debit logic as the vendor ledger.
        for (VendorTransaction transaction : transactionRepo.findByVendor_IdOrderByCreatedDesc(vendorId)) {
            if (transaction.getStatus() == null) {
                continue;
            }

            BigDecimal amount = defaultAmount(transaction.getAmount());
            BigDecimal signedAmount = isDebitType(transaction.getTransactionType()) ? amount.negate() : amount;
            balanceMap.put(transaction.getStatus(), balanceMap.get(transaction.getStatus()).add(signedAmount));
        }
        return balanceMap;
    }

    public void createOrderTransaction(Vendorprofile vendor, SalesOrder order, BigDecimal amount) {
        VendorTransaction tx = new VendorTransaction();
        tx.setVendor(vendor);
        tx.setSalesOrder(order);
        tx.setTransactionType(VendorTransactionTypeEnum.ORDER_EARNING);
        tx.setStatus(VendorTransactionStatusEnum.PENDING);
        tx.setAmount(amount);
        transactionRepo.save(tx);
    }

    @Transactional
    public void createOrderTransactionIfMissing(SalesOrder order) {
        if (order == null || order.getId() == null || order.getVendorId() == null) {
            return;
        }

        BigDecimal amount = defaultAmount(order.getTotalVendorAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0
                || transactionRepo.existsBySalesOrder_IdAndTransactionType(order.getId(), VendorTransactionTypeEnum.ORDER_EARNING)) {
            return;
        }

        Vendorprofile vendor = vendorprofileRepository.findById(order.getVendorId()).orElse(null);
        if (vendor == null) {
            return;
        }

        VendorTransaction tx = new VendorTransaction();
        tx.setVendor(vendor);
        tx.setSalesOrder(order);
        tx.setTransactionType(VendorTransactionTypeEnum.ORDER_EARNING);
        tx.setStatus(VendorTransactionStatusEnum.PENDING);
        tx.setAmount(amount);
        tx.setDescription("ORDER_EARNING|orderId=" + order.getId());
        transactionRepo.save(tx);
    }

    @Transactional
    public BigDecimal createRefundTransactionIfNeeded(SalesOrder order, String reason) {
        if (order == null || order.getId() == null || order.getVendorId() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal amount = defaultAmount(order.getTotalVendorAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        String descriptionPrefix = buildOrderRefundDescriptionPrefix(order);
        if (transactionRepo.existsByVendor_IdAndTransactionTypeAndDescriptionStartingWith(
                order.getVendorId(),
                VendorTransactionTypeEnum.REFUND,
                descriptionPrefix
        )) {
            return BigDecimal.ZERO;
        }

        createOrderTransactionIfMissing(order);

        VendorTransaction earningTx = transactionRepo
                .findFirstBySalesOrder_IdAndTransactionTypeOrderByCreatedDesc(order.getId(), VendorTransactionTypeEnum.ORDER_EARNING)
                .orElse(null);
        Vendorprofile vendor = vendorprofileRepository.findById(order.getVendorId()).orElse(null);

        if (earningTx == null || vendor == null) {
            return BigDecimal.ZERO;
        }

        VendorTransaction tx = new VendorTransaction();
        tx.setVendor(vendor);
        tx.setSalesOrder(order);
        tx.setTransactionType(VendorTransactionTypeEnum.REFUND);
        tx.setStatus(earningTx.getStatus() != null ? earningTx.getStatus() : VendorTransactionStatusEnum.PENDING);
        tx.setAmount(amount);
        tx.setDescription(buildOrderRefundDescription(order, reason));
        transactionRepo.save(tx);
        return amount;
    }

    @Transactional
    public BigDecimal createItemRefundTransactionIfNeeded(SalesOrder order, OrderItem orderItem, String reason) {
        if (order == null || order.getId() == null || order.getVendorId() == null
                || orderItem == null || orderItem.getId() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal amount = defaultAmount(orderItem.getVendorAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        String descriptionPrefix = buildItemRefundDescriptionPrefix(order, orderItem);
        if (transactionRepo.existsByVendor_IdAndTransactionTypeAndDescriptionStartingWith(
                order.getVendorId(),
                VendorTransactionTypeEnum.REFUND,
                descriptionPrefix
        )) {
            return BigDecimal.ZERO;
        }

        createOrderTransactionIfMissing(order);

        VendorTransaction earningTx = transactionRepo
                .findFirstBySalesOrder_IdAndTransactionTypeOrderByCreatedDesc(order.getId(), VendorTransactionTypeEnum.ORDER_EARNING)
                .orElse(null);
        Vendorprofile vendor = vendorprofileRepository.findById(order.getVendorId()).orElse(null);

        if (earningTx == null || vendor == null) {
            return BigDecimal.ZERO;
        }

        VendorTransaction tx = new VendorTransaction();
        tx.setVendor(vendor);
        tx.setSalesOrder(order);
        tx.setTransactionType(VendorTransactionTypeEnum.REFUND);
        tx.setStatus(earningTx.getStatus() != null ? earningTx.getStatus() : VendorTransactionStatusEnum.PENDING);
        tx.setAmount(amount);
        tx.setDescription(buildItemRefundDescription(order, orderItem, reason));
        transactionRepo.save(tx);
        return amount;
    }

    public void markOrderAsAvailable(Long transactionId) {
        VendorTransaction tx = transactionRepo.findById(transactionId).orElseThrow();
        tx.setStatus(VendorTransactionStatusEnum.AVAILABLE);
        transactionRepo.save(tx);
    }

    // 4️⃣ Mark payout as processed
    @Transactional
    public void markPayoutAsPaid(Long payoutId, String gatewayRef) {
        VendorPayout payout = payoutRepo.findById(payoutId).orElseThrow();
        enforcePayoutAllowed(payout, VendorPayoutStatusEnum.PAID);
        payout.setStatus(VendorPayoutStatusEnum.PAID);
        payout.setPayoutReference(gatewayRef);
        payout.setPaidAt(LocalDateTime.now());
        payoutRepo.save(payout);
        recordPayoutEvent(payout, false, null);
    }

    @Transactional
    public VendorPayout requestPayout(Vendorprofile vendor, VendorPayoutMethod method) {
        Long vendorId = vendor == null ? null : vendor.getId();
        FraudGuardResult fraudGuard = fraudPayoutGuard.checkVendorPayoutAllowed(vendorId);
        if (!fraudGuard.isAllowed()) {
            vendorRiskProfileService.refreshVendorProfile(vendorId);
            fraudPostOrderMonitoringService.recordVendorPayout(vendorId, null, null,
                    VendorPayoutStatusEnum.REQUESTED.name(), true, fraudGuard.getReason());
            throw new IllegalStateException(fraudGuard.getReason());
        }
        BigDecimal available = getVendorBalance(vendor.getId()).get(VendorTransactionStatusEnum.AVAILABLE);
        if (available.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("No available balance.");
        }

        VendorPayout payout = new VendorPayout();
        payout.setVendor(vendor);
        payout.setAmount(available);
        payout.setStatus(VendorPayoutStatusEnum.REQUESTED);
        payout.setPayoutMethod(method);
        payoutRepo.save(payout);
        recordPayoutEvent(payout, false, null);
        vendorRiskProfileService.refreshVendorProfile(vendor.getId());

        // 2026-04-22: Reserve withdrawable earnings without marking them paid before transfer completion.
        transactionRepo.markAvailableAsRequested(vendor.getId());
        return payout;
    }

    @Transactional
    public void approvePayout(Long payoutId, String ref, String note) {
        VendorPayout payout = payoutRepo.findById(payoutId).orElseThrow();
        enforcePayoutAllowed(payout, VendorPayoutStatusEnum.PROCESSING);
        payout.setStatus(VendorPayoutStatusEnum.PROCESSING);
        payout.setPayoutReference(ref);
        payout.setAdminNote(note);
        payout.setProcessedAt(LocalDateTime.now());
        payoutRepo.save(payout);
        recordPayoutEvent(payout, false, null);
    }

    @Transactional
    public void PaymentSent(Long payoutId, String ref, String note) {
        VendorPayout payout = payoutRepo.findById(payoutId).orElseThrow();
        enforcePayoutAllowed(payout, VendorPayoutStatusEnum.PAID);
        payout.setStatus(VendorPayoutStatusEnum.PAID);
        payout.setPayoutReference(ref);
        payout.setAdminNote(note);
        payout.setPaidAt(LocalDateTime.now());
        payoutRepo.save(payout);
        transactionRepo.markRequestedAsPaid(payout.getVendor().getId());
        createPayoutLedgerTransactionIfMissing(payout);
        recordPayoutEvent(payout, false, null);
    }

    @Transactional
    public void cancelPayout(Long payoutId, String note) {
        VendorPayout payout = payoutRepo.findById(payoutId).orElseThrow();
        payout.setStatus(VendorPayoutStatusEnum.CANCELLED);
        payout.setAdminNote(note);
        payout.setProcessedAt(LocalDateTime.now());
        payoutRepo.save(payout);
        transactionRepo.markRequestedAsAvailable(payout.getVendor().getId());
    }

    private void createPayoutLedgerTransactionIfMissing(VendorPayout payout) {
        if (payout == null || payout.getVendor() == null || payout.getVendor().getId() == null) {
            return;
        }

        String description = buildPayoutLedgerDescription(payout);
        boolean alreadyExists = transactionRepo.existsByVendor_IdAndTransactionTypeAndDescription(
                payout.getVendor().getId(),
                VendorTransactionTypeEnum.PAYOUT,
                description
        );

        if (alreadyExists) {
            return;
        }

        // 2026-04-22: Create one payout debit ledger row so payout history matches vendor ledger without duplicates.
        VendorTransaction tx = new VendorTransaction();
        tx.setVendor(payout.getVendor());
        tx.setTransactionType(VendorTransactionTypeEnum.PAYOUT);
        tx.setStatus(VendorTransactionStatusEnum.PAID);
        tx.setAmount(payout.getAmount() != null ? payout.getAmount() : BigDecimal.ZERO);
        tx.setDescription(description);
        transactionRepo.save(tx);
    }

    private void enforcePayoutAllowed(VendorPayout payout, VendorPayoutStatusEnum targetStatus) {
        Long vendorId = payout == null || payout.getVendor() == null ? null : payout.getVendor().getId();
        FraudGuardResult fraudGuard = fraudPayoutGuard.checkVendorPayoutAllowed(vendorId);
        if (!fraudGuard.isAllowed()) {
            fraudPostOrderMonitoringService.recordVendorPayout(
                    vendorId,
                    payout == null ? null : payout.getId(),
                    payout == null ? null : payout.getAmount(),
                    targetStatus == null ? null : targetStatus.name(),
                    true,
                    fraudGuard.getReason()
            );
            vendorRiskProfileService.refreshVendorProfile(vendorId);
            throw new IllegalStateException(fraudGuard.getReason());
        }
    }

    private void recordPayoutEvent(VendorPayout payout, boolean held, String reason) {
        if (payout == null) {
            return;
        }
        fraudPostOrderMonitoringService.recordVendorPayout(
                payout.getVendor() == null ? null : payout.getVendor().getId(),
                payout.getId(),
                payout.getAmount(),
                payout.getStatus() == null ? null : payout.getStatus().name(),
                held,
                reason
        );
        if (payout.getVendor() != null) {
            vendorRiskProfileService.refreshVendorProfile(payout.getVendor().getId());
        }
    }

    private String buildPayoutLedgerDescription(VendorPayout payout) {
        String reference = payout.getPayoutReference() != null ? payout.getPayoutReference() : "N/A";
        String note = payout.getAdminNote() != null ? payout.getAdminNote() : "";
        return "PAYOUT_LEDGER_ENTRY|payoutId=" + payout.getId()
                + "|reference=" + reference
                + "|note=" + note;
    }

    private String buildOrderRefundDescription(SalesOrder order, String reason) {
        String note = reason == null ? "" : reason.trim();
        return buildOrderRefundDescriptionPrefix(order) + "|note=" + note;
    }

    private String buildOrderRefundDescriptionPrefix(SalesOrder order) {
        return "ORDER_REFUND|orderId=" + order.getId() + "|scope=ALL_REMAINING";
    }

    private String buildItemRefundDescription(OrderItem orderItem, String reason) {
        String note = reason == null ? "" : reason.trim();
        return buildItemRefundDescriptionPrefix(orderItem) + "|note=" + note;
    }

    private String buildItemRefundDescription(SalesOrder order, OrderItem orderItem, String reason) {
        String note = reason == null ? "" : reason.trim();
        return buildItemRefundDescriptionPrefix(order, orderItem) + "|note=" + note;
    }

    private String buildItemRefundDescriptionPrefix(OrderItem orderItem) {
        if (orderItem == null || orderItem.getSalesOrder() == null) {
            return "ITEM_REFUND";
        }
        return buildItemRefundDescriptionPrefix(orderItem.getSalesOrder(), orderItem);
    }

    private String buildItemRefundDescriptionPrefix(SalesOrder order, OrderItem orderItem) {
        return "ITEM_REFUND|orderId=" + order.getId() + "|itemId=" + orderItem.getId();
    }

    private boolean isDebitType(VendorTransactionTypeEnum type) {
        return type == VendorTransactionTypeEnum.CASHOUT
                || type == VendorTransactionTypeEnum.DEBIT
                || type == VendorTransactionTypeEnum.REFUND
                || type == VendorTransactionTypeEnum.PAYOUT;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }
}
