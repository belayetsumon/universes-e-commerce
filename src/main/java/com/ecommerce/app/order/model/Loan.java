/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.order.model;

import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.List;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author libertyerp_local
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public Users user;  // Customer who took the loan

    private Double loanAmount;
    private Double interestRate;  // Annual Interest Rate
    private Integer loanTenureMonths;  // Loan tenure in months
    private Double emiAmount;  // Calculated EMI amount
    
     private Double totalLoanAmount; // Total loan amount at the beginning

    private Double remainingBalance; // Remaining amount to be paid


    @Enumerated(EnumType.STRING)
    private LoanStatus loanStatus;  // Status of the loan (Active, Paid, Defaulted)

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL)
    private List<EMIPayment> emiPayments;  // Track monthly EMI payments 

    public Loan() {
    }

    public Loan(Long id, Users user, Double loanAmount, Double interestRate, Integer loanTenureMonths, Double emiAmount, Double totalLoanAmount, Double remainingBalance, LoanStatus loanStatus, List<EMIPayment> emiPayments) {
        this.id = id;
        this.user = user;
        this.loanAmount = loanAmount;
        this.interestRate = interestRate;
        this.loanTenureMonths = loanTenureMonths;
        this.emiAmount = emiAmount;
        this.totalLoanAmount = totalLoanAmount;
        this.remainingBalance = remainingBalance;
        this.loanStatus = loanStatus;
        this.emiPayments = emiPayments;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Double getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(Double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getLoanTenureMonths() {
        return loanTenureMonths;
    }

    public void setLoanTenureMonths(Integer loanTenureMonths) {
        this.loanTenureMonths = loanTenureMonths;
    }

    public Double getEmiAmount() {
        return emiAmount;
    }

    public void setEmiAmount(Double emiAmount) {
        this.emiAmount = emiAmount;
    }

    public Double getTotalLoanAmount() {
        return totalLoanAmount;
    }

    public void setTotalLoanAmount(Double totalLoanAmount) {
        this.totalLoanAmount = totalLoanAmount;
    }

    public Double getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(Double remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public LoanStatus getLoanStatus() {
        return loanStatus;
    }

    public void setLoanStatus(LoanStatus loanStatus) {
        this.loanStatus = loanStatus;
    }

    public List<EMIPayment> getEmiPayments() {
        return emiPayments;
    }

    public void setEmiPayments(List<EMIPayment> emiPayments) {
        this.emiPayments = emiPayments;
    }

    
}
