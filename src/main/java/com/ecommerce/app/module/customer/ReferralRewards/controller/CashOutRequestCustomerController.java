/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.customer.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.CashOutRequest;
import com.ecommerce.app.module.ReferralRewards.repository.CashOutRequestRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletTransactionRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.math.BigDecimal;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/cashoutcustomerrequest")
public class CashOutRequestCustomerController {

    @Autowired
    private CashOutRequestRepository cashOutRequestRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("attribute", "value");
        return "customer/referral_rewards/cash-out-request-list";
    }

    public String requestCashout(@RequestParam BigDecimal amount, Principal principal, RedirectAttributes ra) {
//        Users user = usersRepository.findByEmail(principal.getName()).orElseThrow();
//
//        BigDecimal balance = walletTransactionRepository.getUserBalance(user);
//        if (amount.compareTo(balance) > 0) {
//            ra.addFlashAttribute("error", "Insufficient wallet balance.");
//            return "redirect:/wallet";
//        }
//
//        CashOutRequest request = new CashOutRequest();
//        request.setUser(user);
//        request.setAmount(amount);
////        request.setStatus("PENDING");
//
//        cashOutRequestRepository.save(request);
//        ra.addFlashAttribute("success", "Cashout request submitted.");

        return "redirect:/wallet";
    }

}
