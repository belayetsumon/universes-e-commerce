/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.order.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author libertyerp_local
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
public class EMIPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;  // The loan associated with this EMI payment

    private Double emiAmount;  // The EMI amount for this installment
    private LocalDate paymentDate;  // Date the payment is due
    private Boolean isPaid;  // Whether the payment is made or not

    private LocalDate paidOn;  // The actual date when the payment was made (if applicable)

    private Double penaltyAmount;

    private int gracePeriod;

    public EMIPayment() {
    }

    public EMIPayment(Long id, Loan loan, Double emiAmount, LocalDate paymentDate, Boolean isPaid, LocalDate paidOn, Double penaltyAmount, int gracePeriod) {
        this.id = id;
        this.loan = loan;
        this.emiAmount = emiAmount;
        this.paymentDate = paymentDate;
        this.isPaid = isPaid;
        this.paidOn = paidOn;
        this.penaltyAmount = penaltyAmount;
        this.gracePeriod = gracePeriod;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    public Double getEmiAmount() {
        return emiAmount;
    }

    public void setEmiAmount(Double emiAmount) {
        this.emiAmount = emiAmount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }

    public LocalDate getPaidOn() {
        return paidOn;
    }

    public void setPaidOn(LocalDate paidOn) {
        this.paidOn = paidOn;
    }

    public Double getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(Double penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public int getGracePeriod() {
        return gracePeriod;
    }

    public void setGracePeriod(int gracePeriod) {
        this.gracePeriod = gracePeriod;
    }

    // Method to calculate penalty (if overdue and grace period is exceeded)
    public void applyPenalty(double penaltyRate) {
        if (!isPaid && paymentDate.isBefore(LocalDate.now())) {
            // Calculate the number of days the payment is overdue, excluding the grace period
            long overdueDays = LocalDate.now().toEpochDay() - paymentDate.toEpochDay();
            if (overdueDays > gracePeriod) {
                // If overdue days exceed grace period, apply penalty
                long penaltyDays = overdueDays - gracePeriod;
                penaltyAmount = emiAmount * penaltyRate * penaltyDays / 100;  // Daily penalty calculation
            }
        }
    }

}
