package com.ecommerce.app.module.ReferralRewards.model;

import com.ecommerce.app.module.ReferralRewards.enumvalue.GiftCardPurchaseStatus;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.order.model.PaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions_gift_card_purchase")
public class GiftCardPurchase extends BaseEntityPromotions {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Users buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_to_id")
    private Users issuedTo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gift_card_id")
    private GiftCard giftCard;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "BDT";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GiftCardPurchaseStatus status = GiftCardPurchaseStatus.PENDING_PAYMENT;

    @Column(length = 120)
    private String recipientName;

    @Column(length = 180)
    private String recipientEmail;

    @Column(length = 500)
    private String giftMessage;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PaymentMethod paymentMethod;

    @Column(length = 120)
    private String paymentReference;

    @Column(length = 500)
    private String paymentNote;

    private LocalDateTime paidAt;

    @Column(length = 500)
    private String failureMessage;

    public Users getBuyer() {
        return buyer;
    }

    public void setBuyer(Users buyer) {
        this.buyer = buyer;
    }

    public Users getIssuedTo() {
        return issuedTo;
    }

    public void setIssuedTo(Users issuedTo) {
        this.issuedTo = issuedTo;
    }

    public GiftCard getGiftCard() {
        return giftCard;
    }

    public void setGiftCard(GiftCard giftCard) {
        this.giftCard = giftCard;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public GiftCardPurchaseStatus getStatus() {
        return status;
    }

    public void setStatus(GiftCardPurchaseStatus status) {
        this.status = status;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getGiftMessage() {
        return giftMessage;
    }

    public void setGiftMessage(String giftMessage) {
        this.giftMessage = giftMessage;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getPaymentNote() {
        return paymentNote;
    }

    public void setPaymentNote(String paymentNote) {
        this.paymentNote = paymentNote;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }
}
