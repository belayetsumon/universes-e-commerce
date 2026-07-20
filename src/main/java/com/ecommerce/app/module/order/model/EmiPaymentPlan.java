package com.ecommerce.app.module.order.model;

import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "loan")
@EntityListeners(AuditingEntityListener.class)
public class EmiPaymentPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users customer;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sales_order_id", unique = true, nullable = false)
    private SalesOrder salesOrder;

    private BigDecimal orderAmount = BigDecimal.ZERO;

    private BigDecimal downPaymentAmount = BigDecimal.ZERO;

    private BigDecimal financedAmount = BigDecimal.ZERO;

    private BigDecimal interestRate = BigDecimal.ZERO;

    private Integer tenureMonths;

    private BigDecimal installmentAmount = BigDecimal.ZERO;

    private BigDecimal totalPayableAmount = BigDecimal.ZERO;

    private BigDecimal remainingBalance = BigDecimal.ZERO;

    @Column(length = 100)
    private String providerName;

    @Column(length = 150)
    private String providerReference;

    @Column(length = 500)
    private String providerDecisionNote;

    private LocalDateTime providerRequestedOn;

    private LocalDateTime providerRespondedOn;

    private BigDecimal merchantSettledAmount = BigDecimal.ZERO;

    private LocalDateTime merchantSettledOn;

    @Enumerated(EnumType.STRING)
    private EmiStatus status = EmiStatus.PENDING_PROVIDER;

    @OneToMany(mappedBy = "emiPaymentPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmiInstallment> installments = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getCustomer() {
        return customer;
    }

    public void setCustomer(Users customer) {
        this.customer = customer;
    }

    public SalesOrder getSalesOrder() {
        return salesOrder;
    }

    public void setSalesOrder(SalesOrder salesOrder) {
        this.salesOrder = salesOrder;
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }

    public BigDecimal getDownPaymentAmount() {
        return downPaymentAmount;
    }

    public void setDownPaymentAmount(BigDecimal downPaymentAmount) {
        this.downPaymentAmount = downPaymentAmount;
    }

    public BigDecimal getFinancedAmount() {
        return financedAmount;
    }

    public void setFinancedAmount(BigDecimal financedAmount) {
        this.financedAmount = financedAmount;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public BigDecimal getInstallmentAmount() {
        return installmentAmount;
    }

    public void setInstallmentAmount(BigDecimal installmentAmount) {
        this.installmentAmount = installmentAmount;
    }

    public BigDecimal getTotalPayableAmount() {
        return totalPayableAmount;
    }

    public void setTotalPayableAmount(BigDecimal totalPayableAmount) {
        this.totalPayableAmount = totalPayableAmount;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public EmiStatus getStatus() {
        return status;
    }

    public void setStatus(EmiStatus status) {
        this.status = status;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public void setProviderReference(String providerReference) {
        this.providerReference = providerReference;
    }

    public String getProviderDecisionNote() {
        return providerDecisionNote;
    }

    public void setProviderDecisionNote(String providerDecisionNote) {
        this.providerDecisionNote = providerDecisionNote;
    }

    public LocalDateTime getProviderRequestedOn() {
        return providerRequestedOn;
    }

    public void setProviderRequestedOn(LocalDateTime providerRequestedOn) {
        this.providerRequestedOn = providerRequestedOn;
    }

    public LocalDateTime getProviderRespondedOn() {
        return providerRespondedOn;
    }

    public void setProviderRespondedOn(LocalDateTime providerRespondedOn) {
        this.providerRespondedOn = providerRespondedOn;
    }

    public BigDecimal getMerchantSettledAmount() {
        return merchantSettledAmount;
    }

    public void setMerchantSettledAmount(BigDecimal merchantSettledAmount) {
        this.merchantSettledAmount = merchantSettledAmount;
    }

    public LocalDateTime getMerchantSettledOn() {
        return merchantSettledOn;
    }

    public void setMerchantSettledOn(LocalDateTime merchantSettledOn) {
        this.merchantSettledOn = merchantSettledOn;
    }

    public boolean isProviderPending() {
        return status == EmiStatus.PENDING_PROVIDER;
    }

    public boolean isProviderApproved() {
        return status == EmiStatus.APPROVED_BY_PROVIDER
                || status == EmiStatus.ACTIVE
                || status == EmiStatus.PAID
                || status == EmiStatus.DEFAULTED
                || status == EmiStatus.FORECLOSED;
    }

    public boolean isProviderRejected() {
        return status == EmiStatus.REJECTED_BY_PROVIDER;
    }

    public boolean isCancelledPlan() {
        return status == EmiStatus.CANCELLED;
    }

    public boolean isBlocksDirectPayment() {
        return isProviderPending() || isProviderApproved();
    }

    public boolean getBlocksDirectPayment() {
        return isBlocksDirectPayment();
    }

    public String getStatusLabel() {
        if (status == null) {
            return "Unknown";
        }

        switch (status) {
            case PENDING_PROVIDER:
                return "Pending Provider Approval";
            case APPROVED_BY_PROVIDER:
                return "Approved By Provider";
            case REJECTED_BY_PROVIDER:
                return "Rejected By Provider";
            case ACTIVE:
                return "Active";
            case PAID:
                return "Paid";
            case DEFAULTED:
                return "Defaulted";
            case FORECLOSED:
                return "Foreclosed";
            case CANCELLED:
                return "Cancelled";
            default:
                return status.name();
        }
    }

    public List<EmiInstallment> getInstallments() {
        return installments;
    }

    public void setInstallments(List<EmiInstallment> installments) {
        this.installments = installments;
    }

    public void addInstallment(EmiInstallment installment) {
        if (installment == null) {
            return;
        }
        installment.setEmiPaymentPlan(this);
        this.installments.add(installment);
    }
}
