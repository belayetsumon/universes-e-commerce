/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.order.services;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.order.model.EMIPayment;
import com.ecommerce.app.order.model.Loan;
import com.ecommerce.app.order.model.LoanStatus;
import com.ecommerce.app.order.repository.EMIPaymentRepository;
import com.ecommerce.app.order.repository.LoanRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private EMIPaymentRepository emiPaymentRepository;

    private final double penaltyRate = 2.0;  // 2% daily penalty (can be configurable)

    // Calculate EMI
    public double calculateEMI(double loanAmount, double annualInterestRate, int tenureMonths) {
        double monthlyInterestRate = (annualInterestRate / 100) / 12;
        double emi = loanAmount * monthlyInterestRate * Math.pow(1 + monthlyInterestRate, tenureMonths) /
                     (Math.pow(1 + monthlyInterestRate, tenureMonths) - 1);
        return emi;
    }
// Method to create a new loan with EMI details
    public Loan createLoan(Users userId, double loanAmount, double annualInterestRate, int tenureMonths) {
        double emiAmount = calculateEMI(loanAmount, annualInterestRate, tenureMonths);
        
        Loan loan = new Loan();
        loan.setUser(userId);  // Set the user ID (borrower)
        loan.setLoanAmount(loanAmount);
        loan.setInterestRate(annualInterestRate);
        loan.setLoanTenureMonths(tenureMonths);
        loan.setEmiAmount(emiAmount);
        loan.setLoanStatus(LoanStatus.ACTIVE);

        // Create EMI payments for each month
        List<EMIPayment> emiPayments = new ArrayList<>();
        for (int i = 1; i <= tenureMonths; i++) {
            EMIPayment emiPayment = new EMIPayment();
            emiPayment.setLoan(loan);
            emiPayment.setEmiAmount(emiAmount);
            emiPayment.setPaymentDate(LocalDate.now().plusMonths(i));  // Set due date for the next month
            emiPayment.setIsPaid(false);  // Payment is pending initially
            emiPayments.add(emiPayment);
        }

        loan.setEmiPayments(emiPayments);

        // Save the loan to the database
        return loanRepository.save(loan);
    }

    // Apply penalty for overdue EMIs

    // Method to apply penalties for overdue EMIs considering grace period
    public void applyPenaltiesForOverdueEMIs(Long loanId) {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));

        List<EMIPayment> emiPayments = loan.getEmiPayments();

        for (EMIPayment emiPayment : emiPayments) {
            emiPayment.applyPenalty(penaltyRate);  // Apply penalty considering grace period
            emiPaymentRepository.save(emiPayment);  // Save the updated EMI payment with penalty
        }
    }

    // Method to process EMI payment (with penalty if overdue)
    public void processEMIPayment(Long loanId, Double paymentAmount) {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));

        for (EMIPayment emiPayment : loan.getEmiPayments()) {
            if (!emiPayment.getIsPaid()) {
                emiPayment.applyPenalty(penaltyRate);  // Apply penalty before payment
                if (emiPayment.getEmiAmount() + emiPayment.getPenaltyAmount() <= paymentAmount) {
                    emiPayment.setIsPaid(true);
                    emiPayment.setPaidOn(LocalDate.now());
                    paymentAmount -= (emiPayment.getEmiAmount() + emiPayment.getPenaltyAmount());  // Deduct the full payment
                }
                emiPaymentRepository.save(emiPayment);
            }
        }

        if (loan.getEmiPayments().stream().allMatch(EMIPayment::getIsPaid)) {
            loan.setLoanStatus(LoanStatus.PAID);  // Mark loan as paid if all EMIs are paid
        }

        loanRepository.save(loan);  // Save the updated loan
    }
    
    
    
    
    // Early Loan Repayment (Foreclosure)
    public void processEarlyRepayment(Long loanId, Double paymentAmount) {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));

        // Check how much is remaining
        Double remainingBalance = loan.getRemainingBalance();

        if (paymentAmount >= remainingBalance) {
            // Mark all EMIs as paid
            List<EMIPayment> emiPayments = loan.getEmiPayments();
            for (EMIPayment emiPayment : emiPayments) {
                emiPayment.setIsPaid(true);
                emiPayment.setPaidOn(LocalDate.now());
                emiPayment.setPenaltyAmount(0.0); // Clear any penalties
                emiPaymentRepository.save(emiPayment);
            }

            // Update loan status
            loan.setRemainingBalance(0.0);
            loan.setLoanStatus(LoanStatus.FORECLOSED); // Loan is closed early
            loanRepository.save(loan);
        } else {
            throw new RuntimeException("Payment amount is less than remaining balance.");
        }
    }
}
