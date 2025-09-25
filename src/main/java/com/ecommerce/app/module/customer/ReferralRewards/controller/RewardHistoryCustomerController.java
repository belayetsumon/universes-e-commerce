package com.ecommerce.app.module.customer.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.ReferralRewards.model.WalletTransaction;
import com.ecommerce.app.module.ReferralRewards.repository.WalletTransactionRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/customerrewards")
public class RewardHistoryCustomerController {

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private UsersRepository usersRepository;

    @RequestMapping("/rewards")
    public String getRewardHistory(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) List<String> types,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal,
            Model model) {

        Users user = usersRepository.findByEmail(principal.getName()).orElseThrow();

        LocalDateTime start;
        LocalDateTime end;

        try {
            start = (startDate != null) ? LocalDateTime.parse(startDate) : LocalDateTime.now().minusMonths(6);
        } catch (Exception e) {
            start = LocalDateTime.now().minusMonths(6);
        }

        try {
            end = (endDate != null) ? LocalDateTime.parse(endDate) : LocalDateTime.now();
        } catch (Exception e) {
            end = LocalDateTime.now();
        }

        List<TransactionType> filterTypes = new ArrayList<>();
        if (types != null && !types.isEmpty()) {
            for (String t : types) {
                try {
                    filterTypes.add(TransactionType.valueOf(t.toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                    // ignore invalid types
                }
            }
        } else {
            filterTypes = Arrays.asList(TransactionType.values());
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<WalletTransaction> transactions = walletTransactionRepository
                .findByWallet_UsersAndTypeInAndCreatedAtBetween(user, filterTypes, start, end, pageable);

        model.addAttribute("transactions", transactions);
        model.addAttribute("types", TransactionType.values());
        model.addAttribute("selectedTypes", filterTypes);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "reward-history";
    }

}
