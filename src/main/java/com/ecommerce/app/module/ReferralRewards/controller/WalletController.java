/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.RewardAccount;
import com.ecommerce.app.module.ReferralRewards.model.RewardTransaction;
import com.ecommerce.app.module.ReferralRewards.repository.RewardAccountRepository;
import com.ecommerce.app.module.ReferralRewards.repository.RewardTransactionRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/wallet")
public class WalletController {

    private final UsersRepository usersRepository;
    private final RewardAccountRepository rewardAccountRepository;
    private final RewardTransactionRepository rewardTransactionRepository;

    public WalletController(UsersRepository usersRepository, RewardAccountRepository rewardAccountRepository,
            RewardTransactionRepository rewardTransactionRepository) {
        this.usersRepository = usersRepository;
        this.rewardAccountRepository = rewardAccountRepository;
        this.rewardTransactionRepository = rewardTransactionRepository;
    }

    @GetMapping("/walletlist")
    public String walletList(Model model) {
        try {
            model.addAttribute("walletList", rewardAccountRepository.findAllForAdminList());
        } catch (RuntimeException ex) {
            model.addAttribute("walletList", Collections.emptyList());
            model.addAttribute("errorMessage", "Runtime error while loading wallets: " + ex.getMessage());
        }

        return "admin/referral_rewards/walletlist";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<RewardAccount> optionalWallet = rewardAccountRepository.findById(id);
            if (optionalWallet.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Wallet record not found.");
                return "redirect:/wallet/walletlist";
            }

            RewardAccount wallet = optionalWallet.get();
            if (rewardTransactionRepository.existsByRewardAccount(wallet)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete wallet because it has related transactions.");
                return "redirect:/wallet/walletlist";
            }

            rewardAccountRepository.delete(wallet);
            redirectAttributes.addFlashAttribute("successMessage", "Wallet deleted successfully.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete wallet because it is linked to other records.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Runtime error while deleting wallet: " + ex.getMessage());
        }
        return "redirect:/wallet/walletlist";
    }

    @GetMapping("/wallet")
    public String viewWallet(Model model, Principal principal) {
        // Fetch the user by email from principal
        Users user = usersRepository.findByEmail(principal.getName()).get();
        if (user == null) {
            // Handle user not found scenario (redirect to login page or error page)
            return "redirect:/login";
        }

        // Fetch wallet by user
        RewardAccount wallet = rewardAccountRepository.findByUsers(user).orElse(null);

        // Fetch transactions if wallet exists, else empty list
        List<RewardTransaction> txns = (wallet != null)
                ? rewardTransactionRepository.findByRewardAccount(wallet)
                : List.of();

        model.addAttribute("wallet", wallet);
        model.addAttribute("transactions", txns);

        return "wallet";
    }
}
