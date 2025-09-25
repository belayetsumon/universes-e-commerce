/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.order.controller;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.order.services.LoanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author libertyerp_local
 */
@Controller
public class LoanController {
    
    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    // Create a new loan
    @PostMapping("/create")
    public String createLoan(@RequestParam Users userId, @RequestParam double loanAmount,
                             @RequestParam double annualInterestRate, @RequestParam int tenureMonths) {
        loanService.createLoan(userId, loanAmount, annualInterestRate, tenureMonths);
        return "redirect:/loan/success";
    }

    // Make an EMI payment
    @PostMapping("/pay")
    public String payEMI(@RequestParam Long loanId, @RequestParam Double paymentAmount) {
        loanService.processEMIPayment(loanId, paymentAmount);
        return "redirect:/loan/payment-success";
    }

    // EMI Payment Success Page
    @GetMapping("/payment-success")
    public String paymentSuccess() {
        return "payment-success";  // Return the success page after EMI payment
    }
    
    
    
      // Endpoint for early loan payment
    @PostMapping("/early-repayment")
    public String earlyRepayment(@RequestParam Long loanId, @RequestParam Double paymentAmount, Model model) {
        try {
            loanService.processEarlyRepayment(loanId, paymentAmount);
            model.addAttribute("message", "Loan fully repaid successfully.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "payment-success";
    }
    
}
