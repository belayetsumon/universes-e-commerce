/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.CashOutRequest;
import com.ecommerce.app.module.ReferralRewards.model.CashOutStatus;
import com.ecommerce.app.module.ReferralRewards.model.WalletTransaction;
import com.ecommerce.app.module.ReferralRewards.repository.CashOutRequestRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletTransactionRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/cashoutrequest")
public class CashOutRequestController {

    @Autowired
    private CashOutRequestRepository cashOutRequestRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("pendingRequests", cashOutRequestRepository.findByStatus("PENDING"));
        return "admin/cashout-list";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id) {
        CashOutRequest request = cashOutRequestRepository.findById(id).orElseThrow();
        request.setStatus(CashOutStatus.APPROVED);
        request.setProcessedAt(LocalDateTime.now());
        cashOutRequestRepository.save(request);
        return "redirect:/admin/cashouts";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id) {
        CashOutRequest request = cashOutRequestRepository.findById(id).orElseThrow();
        request.setStatus(CashOutStatus.REJECTED);
        request.setProcessedAt(LocalDateTime.now());
        cashOutRequestRepository.save(request);
        return "redirect:/admin/cashouts";
    }

    @PostMapping("/{id}/mark-paid")
    @Transactional
    public String markPaid(@PathVariable Long id) {
        CashOutRequest request = cashOutRequestRepository.findById(id).orElseThrow();

        if (request.getStatus() != CashOutStatus.APPROVED) {
            // Optional: You can only mark as PAID if it's approved
            return "redirect:/admin/cashouts";
        }

        // Mark as paid
        request.setStatus(CashOutStatus.PAID);
        request.setProcessedAt(LocalDateTime.now());

        // Create wallet transaction (debit)
        WalletTransaction tx = new WalletTransaction();
//        tx.setUser(request.getUser());
        tx.setAmount(request.getAmount().negate()); // money going out
        tx.setDescription("Cashout paid - Request #" + request.getId());

        walletTransactionRepository.save(tx);
        cashOutRequestRepository.save(request);

        return "redirect:/admin/cashouts";
    }

}
