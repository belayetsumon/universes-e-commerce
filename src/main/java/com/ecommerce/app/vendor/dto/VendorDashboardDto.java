package com.ecommerce.app.vendor.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class VendorDashboardDto {

    private String vendorName;
    private String vendorCode;
    private int totalProducts;
    private int activeProducts;
    private int featuredProducts;
    private int lowStockProducts;
    private int totalOrders;
    private int openOrders;
    private int completedOrders;
    private int returnIssueOrders;
    private BigDecimal pendingBalance = BigDecimal.ZERO;
    private BigDecimal availableBalance = BigDecimal.ZERO;
    private BigDecimal requestedBalance = BigDecimal.ZERO;
    private BigDecimal paidBalance = BigDecimal.ZERO;
    private List<String> monthlyLabels = new ArrayList<>();
    private List<Double> monthlyGrossSales = new ArrayList<>();
    private List<Double> monthlyVendorEarnings = new ArrayList<>();
    private List<Integer> monthlyOrderCounts = new ArrayList<>();
    private List<String> orderStatusLabels = new ArrayList<>();
    private List<Integer> orderStatusCounts = new ArrayList<>();
    private List<String> productCategoryLabels = new ArrayList<>();
    private List<Integer> productCategoryCounts = new ArrayList<>();
    private List<String> balanceLabels = new ArrayList<>();
    private List<Double> balanceAmounts = new ArrayList<>();

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getActiveProducts() {
        return activeProducts;
    }

    public void setActiveProducts(int activeProducts) {
        this.activeProducts = activeProducts;
    }

    public int getFeaturedProducts() {
        return featuredProducts;
    }

    public void setFeaturedProducts(int featuredProducts) {
        this.featuredProducts = featuredProducts;
    }

    public int getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(int lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getOpenOrders() {
        return openOrders;
    }

    public void setOpenOrders(int openOrders) {
        this.openOrders = openOrders;
    }

    public int getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(int completedOrders) {
        this.completedOrders = completedOrders;
    }

    public int getReturnIssueOrders() {
        return returnIssueOrders;
    }

    public void setReturnIssueOrders(int returnIssueOrders) {
        this.returnIssueOrders = returnIssueOrders;
    }

    public BigDecimal getPendingBalance() {
        return pendingBalance;
    }

    public void setPendingBalance(BigDecimal pendingBalance) {
        this.pendingBalance = pendingBalance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getRequestedBalance() {
        return requestedBalance;
    }

    public void setRequestedBalance(BigDecimal requestedBalance) {
        this.requestedBalance = requestedBalance;
    }

    public BigDecimal getPaidBalance() {
        return paidBalance;
    }

    public void setPaidBalance(BigDecimal paidBalance) {
        this.paidBalance = paidBalance;
    }

    public List<String> getMonthlyLabels() {
        return monthlyLabels;
    }

    public void setMonthlyLabels(List<String> monthlyLabels) {
        this.monthlyLabels = monthlyLabels;
    }

    public List<Double> getMonthlyGrossSales() {
        return monthlyGrossSales;
    }

    public void setMonthlyGrossSales(List<Double> monthlyGrossSales) {
        this.monthlyGrossSales = monthlyGrossSales;
    }

    public List<Double> getMonthlyVendorEarnings() {
        return monthlyVendorEarnings;
    }

    public void setMonthlyVendorEarnings(List<Double> monthlyVendorEarnings) {
        this.monthlyVendorEarnings = monthlyVendorEarnings;
    }

    public List<Integer> getMonthlyOrderCounts() {
        return monthlyOrderCounts;
    }

    public void setMonthlyOrderCounts(List<Integer> monthlyOrderCounts) {
        this.monthlyOrderCounts = monthlyOrderCounts;
    }

    public List<String> getOrderStatusLabels() {
        return orderStatusLabels;
    }

    public void setOrderStatusLabels(List<String> orderStatusLabels) {
        this.orderStatusLabels = orderStatusLabels;
    }

    public List<Integer> getOrderStatusCounts() {
        return orderStatusCounts;
    }

    public void setOrderStatusCounts(List<Integer> orderStatusCounts) {
        this.orderStatusCounts = orderStatusCounts;
    }

    public List<String> getProductCategoryLabels() {
        return productCategoryLabels;
    }

    public void setProductCategoryLabels(List<String> productCategoryLabels) {
        this.productCategoryLabels = productCategoryLabels;
    }

    public List<Integer> getProductCategoryCounts() {
        return productCategoryCounts;
    }

    public void setProductCategoryCounts(List<Integer> productCategoryCounts) {
        this.productCategoryCounts = productCategoryCounts;
    }

    public List<String> getBalanceLabels() {
        return balanceLabels;
    }

    public void setBalanceLabels(List<String> balanceLabels) {
        this.balanceLabels = balanceLabels;
    }

    public List<Double> getBalanceAmounts() {
        return balanceAmounts;
    }

    public void setBalanceAmounts(List<Double> balanceAmounts) {
        this.balanceAmounts = balanceAmounts;
    }
}
