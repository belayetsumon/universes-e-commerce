package com.ecommerce.app.finance.services;

import com.ecommerce.app.finance.dto.AdminFinanceDashboardDto;
import com.ecommerce.app.finance.dto.AdminVendorDueRow;
import com.ecommerce.app.finance.dto.VendorFinanceDashboardDto;
import com.ecommerce.app.finance.dto.VendorLedgerRow;
import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.repository.ShipmentRepository;
import com.ecommerce.app.vendor.model.VendorPayout;
import com.ecommerce.app.vendor.model.VendorPayoutStatusEnum;
import com.ecommerce.app.vendor.model.VendorTransaction;
import com.ecommerce.app.vendor.model.VendorTransactionStatusEnum;
import com.ecommerce.app.vendor.model.VendorTransactionTypeEnum;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorPayoutRepository;
import com.ecommerce.app.vendor.repository.VendorTransactionRepository;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class FinanceReportService {

    @Autowired
    private VendorTransactionRepository vendorTransactionRepository;

    @Autowired
    private VendorPayoutRepository vendorPayoutRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private VendorprofileRepository vendorprofileRepository;

    public VendorFinanceDashboardDto getVendorDashboard(Long vendorId) {
        VendorFinanceDashboardDto dto = new VendorFinanceDashboardDto();
        Vendorprofile vendor = vendorprofileRepository.findById(vendorId).orElse(null);
        dto.setVendor(vendor);

        EnumMap<VendorTransactionStatusEnum, BigDecimal> balances = getVendorBalances(vendorId);
        dto.setPendingAmount(balances.get(VendorTransactionStatusEnum.PENDING));
        dto.setAvailableAmount(balances.get(VendorTransactionStatusEnum.AVAILABLE));
        dto.setReservedAmount(balances.get(VendorTransactionStatusEnum.REQUESTED));
        dto.setPaidAmount(balances.get(VendorTransactionStatusEnum.PAID));

        List<Shipment> vendorShipments = shipmentRepository.findByVendorId(vendorId);
        dto.setMarketplaceDueToVendor(sumShipmentVendorPayable(vendorShipments));
        dto.setMarketplaceShare(sumShipmentMarketplacePayable(vendorShipments));
        dto.setRecentShipments(limitShipments(vendorShipments, 8));

        List<VendorPayout> payouts = vendorPayoutRepository.findByVendor_IdOrderByIdDesc(vendorId);
        dto.setRecentPayouts(limitPayouts(payouts, 8));
        dto.setRequestedPayoutAmount(sumPayoutsByStatus(payouts, VendorPayoutStatusEnum.REQUESTED));
        dto.setProcessedPayoutAmount(sumPayoutsByStatus(payouts, VendorPayoutStatusEnum.PROCESSING));
        dto.setPaidPayoutAmount(sumPayoutsByStatus(payouts, VendorPayoutStatusEnum.PAID));
        return dto;
    }

    public List<VendorLedgerRow> getVendorLedger(Long vendorId) {
        List<VendorTransaction> transactions = vendorTransactionRepository.findAll(Sort.by(Sort.Direction.ASC, "created", "id"));
        List<VendorLedgerRow> rows = new ArrayList<>();
        BigDecimal runningBalance = BigDecimal.ZERO;

        for (VendorTransaction transaction : transactions) {
            if (transaction.getVendor() == null || transaction.getVendor().getId() == null
                    || !transaction.getVendor().getId().equals(vendorId)) {
                continue;
            }

            VendorLedgerRow row = new VendorLedgerRow();
            row.setTransaction(transaction);

            BigDecimal amount = defaultAmount(transaction.getAmount());
            if (isDebitType(transaction.getTransactionType())) {
                row.setDebit(amount);
                runningBalance = runningBalance.subtract(amount);
            } else {
                row.setCredit(amount);
                runningBalance = runningBalance.add(amount);
            }

            row.setRunningBalance(runningBalance);
            rows.add(row);
        }

        rows.sort(Comparator.comparing((VendorLedgerRow row) -> row.getTransaction().getCreated(), Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(row -> row.getTransaction().getId(), Comparator.nullsLast(Comparator.reverseOrder())));
        return rows;
    }

    public AdminFinanceDashboardDto getAdminDashboard() {
        AdminFinanceDashboardDto dto = new AdminFinanceDashboardDto();
        List<Vendorprofile> vendors = vendorprofileRepository.findAll(Sort.by(Sort.Direction.ASC, "companyName", "id"));
        List<Shipment> shipments = shipmentRepository.findAll(Sort.by(Sort.Direction.DESC, "created", "id"));

        BigDecimal totalPending = BigDecimal.ZERO;
        BigDecimal totalAvailable = BigDecimal.ZERO;
        BigDecimal totalReserved = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalVendorPayable = BigDecimal.ZERO;
        BigDecimal totalMarketplacePayable = BigDecimal.ZERO;
        BigDecimal totalRequestedPayout = BigDecimal.ZERO;
        BigDecimal totalProcessedPayout = BigDecimal.ZERO;
        BigDecimal totalPaidPayout = BigDecimal.ZERO;

        List<AdminVendorDueRow> vendorRows = new ArrayList<>();

        for (Vendorprofile vendor : vendors) {
            Long vendorId = vendor.getId();
            EnumMap<VendorTransactionStatusEnum, BigDecimal> balances = getVendorBalances(vendorId);
            List<Shipment> vendorShipments = shipmentRepository.findByVendorId(vendorId);
            List<VendorPayout> payouts = vendorPayoutRepository.findByVendor_IdOrderByIdDesc(vendorId);

            AdminVendorDueRow row = new AdminVendorDueRow();
            row.setVendorId(vendorId);
            row.setVendorCode(vendor.getVendorCode());
            row.setCompanyName(vendor.getCompanyName());
            row.setPendingAmount(balances.get(VendorTransactionStatusEnum.PENDING));
            row.setAvailableAmount(balances.get(VendorTransactionStatusEnum.AVAILABLE));
            row.setReservedAmount(balances.get(VendorTransactionStatusEnum.REQUESTED));
            row.setPaidAmount(balances.get(VendorTransactionStatusEnum.PAID));
            row.setShipmentVendorPayable(sumShipmentVendorPayable(vendorShipments));
            row.setShipmentMarketplacePayable(sumShipmentMarketplacePayable(vendorShipments));
            row.setRequestedPayoutAmount(sumPayoutsByStatus(payouts, VendorPayoutStatusEnum.REQUESTED));
            row.setProcessedPayoutAmount(sumPayoutsByStatus(payouts, VendorPayoutStatusEnum.PROCESSING));
            row.setPaidPayoutAmount(sumPayoutsByStatus(payouts, VendorPayoutStatusEnum.PAID));
            vendorRows.add(row);

            totalPending = totalPending.add(row.getPendingAmount());
            totalAvailable = totalAvailable.add(row.getAvailableAmount());
            totalReserved = totalReserved.add(row.getReservedAmount());
            totalPaid = totalPaid.add(row.getPaidAmount());
            totalVendorPayable = totalVendorPayable.add(row.getShipmentVendorPayable());
            totalMarketplacePayable = totalMarketplacePayable.add(row.getShipmentMarketplacePayable());
            totalRequestedPayout = totalRequestedPayout.add(row.getRequestedPayoutAmount());
            totalProcessedPayout = totalProcessedPayout.add(row.getProcessedPayoutAmount());
            totalPaidPayout = totalPaidPayout.add(row.getPaidPayoutAmount());
        }

        dto.setTotalVendorPending(totalPending);
        dto.setTotalVendorAvailable(totalAvailable);
        dto.setTotalVendorReserved(totalReserved);
        dto.setTotalVendorPaid(totalPaid);
        dto.setTotalMarketplaceDueToVendors(totalVendorPayable);
        dto.setTotalMarketplaceShare(totalMarketplacePayable);
        dto.setTotalRequestedPayout(totalRequestedPayout);
        dto.setTotalProcessedPayout(totalProcessedPayout);
        dto.setTotalPaidPayout(totalPaidPayout);
        dto.setVendorDueRows(vendorRows);
        dto.setRecentSettlements(limitShipments(shipments, 12));
        return dto;
    }

    public List<Shipment> getSettlementLedger() {
        return shipmentRepository.findAll(Sort.by(Sort.Direction.DESC, "created", "id"));
    }

    public Map<Long, Vendorprofile> getVendorLookup() {
        Map<Long, Vendorprofile> lookup = new HashMap<>();
        for (Vendorprofile vendor : vendorprofileRepository.findAll()) {
            lookup.put(vendor.getId(), vendor);
        }
        return lookup;
    }

    private EnumMap<VendorTransactionStatusEnum, BigDecimal> getVendorBalances(Long vendorId) {
        EnumMap<VendorTransactionStatusEnum, BigDecimal> balances = new EnumMap<>(VendorTransactionStatusEnum.class);
        balances.put(VendorTransactionStatusEnum.PENDING, BigDecimal.ZERO);
        balances.put(VendorTransactionStatusEnum.AVAILABLE, BigDecimal.ZERO);
        balances.put(VendorTransactionStatusEnum.REQUESTED, BigDecimal.ZERO);
        balances.put(VendorTransactionStatusEnum.PAID, BigDecimal.ZERO);

        for (VendorTransaction transaction : vendorTransactionRepository.findByVendor_IdOrderByCreatedDesc(vendorId)) {
            if (transaction.getStatus() == null) {
                continue;
            }

            BigDecimal amount = defaultAmount(transaction.getAmount());
            BigDecimal signedAmount = isDebitType(transaction.getTransactionType()) ? amount.negate() : amount;
            balances.put(transaction.getStatus(), balances.get(transaction.getStatus()).add(signedAmount));
        }
        return balances;
    }

    private BigDecimal sumShipmentVendorPayable(List<Shipment> shipments) {
        BigDecimal total = BigDecimal.ZERO;
        for (Shipment shipment : shipments) {
            total = total.add(defaultAmount(shipment.getVendorPayableAmount()));
        }
        return total;
    }

    private BigDecimal sumShipmentMarketplacePayable(List<Shipment> shipments) {
        BigDecimal total = BigDecimal.ZERO;
        for (Shipment shipment : shipments) {
            total = total.add(defaultAmount(shipment.getMarketplacePayableAmount()));
        }
        return total;
    }

    private BigDecimal sumPayoutsByStatus(List<VendorPayout> payouts, VendorPayoutStatusEnum status) {
        BigDecimal total = BigDecimal.ZERO;
        for (VendorPayout payout : payouts) {
            if (payout.getStatus() == status) {
                total = total.add(defaultAmount(payout.getAmount()));
            }
        }
        return total;
    }

    private boolean isDebitType(VendorTransactionTypeEnum type) {
        return type == VendorTransactionTypeEnum.CASHOUT
                || type == VendorTransactionTypeEnum.DEBIT
                || type == VendorTransactionTypeEnum.REFUND
                || type == VendorTransactionTypeEnum.PAYOUT;
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private List<VendorPayout> limitPayouts(List<VendorPayout> payouts, int limit) {
        if (payouts == null || payouts.isEmpty()) {
            return new ArrayList<>();
        }
        return payouts.subList(0, Math.min(limit, payouts.size()));
    }

    private List<Shipment> limitShipments(List<Shipment> shipments, int limit) {
        if (shipments == null || shipments.isEmpty()) {
            return new ArrayList<>();
        }
        shipments.sort(Comparator.comparing(Shipment::getCreated, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Shipment::getId, Comparator.nullsLast(Comparator.reverseOrder())));
        return new ArrayList<>(shipments.subList(0, Math.min(limit, shipments.size())));
    }
}
