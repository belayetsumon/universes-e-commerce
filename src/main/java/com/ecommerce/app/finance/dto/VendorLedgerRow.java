package com.ecommerce.app.finance.dto;

import com.ecommerce.app.vendor.model.VendorTransaction;
import java.math.BigDecimal;

public class VendorLedgerRow {

    private VendorTransaction transaction;
    private BigDecimal credit = BigDecimal.ZERO;
    private BigDecimal debit = BigDecimal.ZERO;
    private BigDecimal runningBalance = BigDecimal.ZERO;

    public VendorTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(VendorTransaction transaction) {
        this.transaction = transaction;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public void setDebit(BigDecimal debit) {
        this.debit = debit;
    }

    public BigDecimal getRunningBalance() {
        return runningBalance;
    }

    public void setRunningBalance(BigDecimal runningBalance) {
        this.runningBalance = runningBalance;
    }
}
