/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

import com.ecommerce.app.module.ReferralRewards.enumvalue.WalletTransactionStatus;
import com.ecommerce.app.module.ReferralRewards.enumvalue.WalletTransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author libertyerp_local
 */
// import javax.persistence.*;
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "promotions_wallet_transaction", uniqueConstraints = {
    @UniqueConstraint(name = "uk_wallet_tx_idempotency", columnNames = "idempotency_key")
})
public class WalletTransaction extends BaseEntityPromotions {

    @NotNull(message = "Wallet is required.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @NotNull(message = "Transaction amount is required.")
    @DecimalMin(value = "0.01", message = "Transaction amount must be greater than zero.")
    @Digits(integer = 15, fraction = 4, message = "Transaction amount must have maximum 15 digits and 4 decimal places.")
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WalletTransactionType type;

    @NotNull(message = "Transaction status is required.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WalletTransactionStatus status;

    @NotBlank(message = "Idempotency key is required.")
    @Size(max = 100, message = "Idempotency key must not exceed 100 characters.")
    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    public WalletTransaction() {
    }

    public WalletTransaction(Wallet wallet, BigDecimal amount, WalletTransactionType type, WalletTransactionStatus status, String idempotencyKey) {
        this.wallet = wallet;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public WalletTransactionType getType() {
        return type;
    }

    public void setType(WalletTransactionType type) {
        this.type = type;
    }

    public WalletTransactionStatus getStatus() {
        return status;
    }

    public void setStatus(WalletTransactionStatus status) {
        this.status = status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

}
