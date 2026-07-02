package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.enumvalue.CashbackStatus;
import com.ecommerce.app.module.ReferralRewards.model.CashbackTransaction;
import com.ecommerce.app.module.ReferralRewards.repository.CashbackTransactionRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cashback")
public class CashbackTransactionController {

    private final CashbackTransactionRepository cashbackTransactionRepository;

    public CashbackTransactionController(CashbackTransactionRepository cashbackTransactionRepository) {
        this.cashbackTransactionRepository = cashbackTransactionRepository;
    }

    @GetMapping("/list")
    public String list(Model model) {
        try {
            List<CashbackTransaction> cashbacks = cashbackTransactionRepository.findAllForAdminList();
            model.addAttribute("cashbacks", cashbacks);
            addReportSummary(model, cashbacks);
        } catch (RuntimeException ex) {
            model.addAttribute("cashbacks", Collections.emptyList());
            model.addAttribute("cashbackStatusCounts", Collections.emptyMap());
            model.addAttribute("pendingCashbackCount", 0L);
            model.addAttribute("creditedCashbackCount", 0L);
            model.addAttribute("cancelledCashbackCount", 0L);
            model.addAttribute("failedCashbackCount", 0L);
            model.addAttribute("pendingCashbackTotal", BigDecimal.ZERO);
            model.addAttribute("creditedCashbackTotal", BigDecimal.ZERO);
            model.addAttribute("cancelledCashbackTotal", BigDecimal.ZERO);
            model.addAttribute("errorMessage", "Runtime error while loading cashback transactions: " + ex.getMessage());
        }
        return "admin/referral_rewards/cashback-transaction-list";
    }

    private void addReportSummary(Model model, List<CashbackTransaction> cashbacks) {
        Map<CashbackStatus, Long> statusCounts = new EnumMap<>(CashbackStatus.class);
        BigDecimal pendingTotal = BigDecimal.ZERO;
        BigDecimal creditedTotal = BigDecimal.ZERO;
        BigDecimal cancelledTotal = BigDecimal.ZERO;

        for (CashbackTransaction cashback : cashbacks) {
            if (cashback == null || cashback.getStatus() == null) {
                continue;
            }

            statusCounts.merge(cashback.getStatus(), 1L, Long::sum);
            BigDecimal amount = cashback.getAmount() == null ? BigDecimal.ZERO : cashback.getAmount();
            if (cashback.getStatus() == CashbackStatus.PENDING) {
                pendingTotal = pendingTotal.add(amount);
            } else if (cashback.getStatus() == CashbackStatus.CREDITED) {
                creditedTotal = creditedTotal.add(amount);
            } else if (cashback.getStatus() == CashbackStatus.CANCELLED || cashback.getStatus() == CashbackStatus.FAILED) {
                cancelledTotal = cancelledTotal.add(amount);
            }
        }

        model.addAttribute("cashbackStatusCounts", statusCounts);
        model.addAttribute("pendingCashbackCount", statusCounts.getOrDefault(CashbackStatus.PENDING, 0L));
        model.addAttribute("creditedCashbackCount", statusCounts.getOrDefault(CashbackStatus.CREDITED, 0L));
        model.addAttribute("cancelledCashbackCount", statusCounts.getOrDefault(CashbackStatus.CANCELLED, 0L));
        model.addAttribute("failedCashbackCount", statusCounts.getOrDefault(CashbackStatus.FAILED, 0L));
        model.addAttribute("pendingCashbackTotal", pendingTotal);
        model.addAttribute("creditedCashbackTotal", creditedTotal);
        model.addAttribute("cancelledCashbackTotal", cancelledTotal);
    }
}
