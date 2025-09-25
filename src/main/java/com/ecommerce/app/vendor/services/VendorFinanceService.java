/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.vendor.services;

import com.ecommerce.app.order.model.SalesOrder;
import com.ecommerce.app.vendor.model.VendorPayout;
import com.ecommerce.app.vendor.model.VendorPayoutMethod;
import com.ecommerce.app.vendor.model.VendorPayoutStatusEnum;
import com.ecommerce.app.vendor.model.VendorTransaction;
import com.ecommerce.app.vendor.model.VendorTransactionStatusEnum;
import com.ecommerce.app.vendor.model.VendorTransactionTypeEnum;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorPayoutRepository;
import com.ecommerce.app.vendor.repository.VendorTransactionRepository;
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

    public EnumMap<VendorTransactionStatusEnum, BigDecimal> getVendorBalance(Long vendorId) {
        EnumMap<VendorTransactionStatusEnum, BigDecimal> balanceMap = new EnumMap<>(VendorTransactionStatusEnum.class);

        balanceMap.put(VendorTransactionStatusEnum.PENDING, transactionRepo.sumPendingAmount(vendorId));
        balanceMap.put(VendorTransactionStatusEnum.AVAILABLE, transactionRepo.sumAvailableAmount(vendorId));
        balanceMap.put(VendorTransactionStatusEnum.PAID, transactionRepo.sumPaidAmount(vendorId));

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

    public void markOrderAsAvailable(Long transactionId) {
        VendorTransaction tx = transactionRepo.findById(transactionId).orElseThrow();
        tx.setStatus(VendorTransactionStatusEnum.AVAILABLE);
        transactionRepo.save(tx);
    }

    // 4️⃣ Mark payout as processed
    @Transactional
    public void markPayoutAsPaid(Long payoutId, String gatewayRef) {
        VendorPayout payout = payoutRepo.findById(payoutId).orElseThrow();
        payout.setStatus(VendorPayoutStatusEnum.PAID);
        payout.setPayoutReference(gatewayRef);
        payout.setPaidAt(LocalDateTime.now());
        payoutRepo.save(payout);
    }

    @Transactional
    public VendorPayout requestPayout(Vendorprofile vendor, VendorPayoutMethod method) {
        BigDecimal available = transactionRepo.sumAvailableAmount(vendor.getId());
        if (available.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("No available balance.");
        }

        VendorPayout payout = new VendorPayout();
        payout.setVendor(vendor);
        payout.setAmount(available);
        payout.setStatus(VendorPayoutStatusEnum.REQUESTED);
        payout.setPayoutMethod(method);
        payoutRepo.save(payout);

        transactionRepo.markAvailableAsPaid(vendor.getId());
        return payout;
    }

    @Transactional
    public void approvePayout(Long payoutId, String ref, String note) {
        VendorPayout payout = payoutRepo.findById(payoutId).orElseThrow();
        payout.setStatus(VendorPayoutStatusEnum.PROCESSING);
        payout.setPayoutReference(ref);
        payout.setAdminNote(note);
        payout.setProcessedAt(LocalDateTime.now());
        payoutRepo.save(payout);
    }

    @Transactional
    public void PaymentSent(Long payoutId, String ref, String note) {
        VendorPayout payout = payoutRepo.findById(payoutId).orElseThrow();
        payout.setStatus(VendorPayoutStatusEnum.PAID);
        payout.setPayoutReference(ref);
        payout.setAdminNote(note);
        payout.setPaidAt(LocalDateTime.now());
        payoutRepo.save(payout);
    }

    @Transactional
    public void cancelPayout(Long payoutId, String note) {
        VendorPayout payout = payoutRepo.findById(payoutId).orElseThrow();
        payout.setStatus(VendorPayoutStatusEnum.CANCELLED);
        payout.setAdminNote(note);
        payout.setProcessedAt(LocalDateTime.now());
        payoutRepo.save(payout);
        // Revert transactions if needed (optional)
    }
}
