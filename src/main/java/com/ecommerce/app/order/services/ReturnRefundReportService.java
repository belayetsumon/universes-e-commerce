package com.ecommerce.app.order.services;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.order.dto.ReturnRefundReportDto;
import com.ecommerce.app.order.dto.ReturnRefundReportRow;
import com.ecommerce.app.order.model.OrderItem;
import com.ecommerce.app.order.model.OrderItemReturnStatus;
import com.ecommerce.app.order.model.SalesOrder;
import com.ecommerce.app.order.repository.OrderItemRepository;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReturnRefundReportService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private VendorprofileRepository vendorprofileRepository;

    @Transactional(readOnly = true)
    public ReturnRefundReportDto buildReport(OrderItemReturnStatus status, LocalDate fromDate, LocalDate toDate) {
        return buildReport(status, fromDate, toDate, null, null, false);
    }

    public ReturnRefundReportDto buildReturnReport(OrderItemReturnStatus status, LocalDate fromDate, LocalDate toDate) {
        return buildReport(status, fromDate, toDate, null, null, false);
    }

    public ReturnRefundReportDto buildRefundReport(LocalDate fromDate, LocalDate toDate) {
        return buildReport(null, fromDate, toDate, null, null, true);
    }

    public ReturnRefundReportDto buildVendorReturnReport(Long vendorId, OrderItemReturnStatus status, LocalDate fromDate, LocalDate toDate) {
        return buildReport(status, fromDate, toDate, vendorId, null, false);
    }

    public ReturnRefundReportDto buildVendorRefundReport(Long vendorId, LocalDate fromDate, LocalDate toDate) {
        return buildReport(null, fromDate, toDate, vendorId, null, true);
    }

    public ReturnRefundReportDto buildCustomerReturnReport(Long customerId, OrderItemReturnStatus status, LocalDate fromDate, LocalDate toDate) {
        return buildReport(status, fromDate, toDate, null, customerId, false);
    }

    public ReturnRefundReportDto buildCustomerRefundReport(Long customerId, LocalDate fromDate, LocalDate toDate) {
        return buildReport(null, fromDate, toDate, null, customerId, true);
    }

    private ReturnRefundReportDto buildReport(OrderItemReturnStatus status, LocalDate fromDate, LocalDate toDate,
            Long vendorId, Long customerId, boolean refundsOnly) {
        LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime to = toDate != null ? toDate.atTime(LocalTime.MAX) : null;
        Map<Long, Vendorprofile> vendorLookup = buildVendorLookup();

        List<ReturnRefundReportRow> rows = orderItemRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .filter(item -> isReturnRelated(item))
                .filter(item -> vendorId == null || matchesVendor(item, vendorId))
                .filter(item -> customerId == null || matchesCustomer(item, customerId))
                .filter(item -> !refundsOnly || safeAmount(item.getReturnRefundAmount()).compareTo(BigDecimal.ZERO) > 0)
                .filter(item -> status == null || resolveReturnStatus(item) == status)
                .filter(item -> withinDateRange(resolveReportDate(item), from, to))
                .map(item -> toRow(item, vendorLookup))
                .sorted(Comparator.comparing(ReturnRefundReportRow::getReturnRequestedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ReturnRefundReportRow::getReturnedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ReturnRefundReportRow::getOrderItemId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        ReturnRefundReportDto report = new ReturnRefundReportDto();
        report.setRows(rows);
        report.setTotalReturnRows(rows.size());
        report.setRequestedCount(rows.stream()
                .filter(row -> row.getReturnStatus() == OrderItemReturnStatus.RETURN_REQUESTED)
                .count());
        report.setReturnedCount(rows.stream()
                .filter(row -> row.getReturnStatus() == OrderItemReturnStatus.RETURNED)
                .count());
        report.setTotalRefundAmount(rows.stream()
                .map(ReturnRefundReportRow::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        report.setPendingRefundAmount(rows.stream()
                .filter(row -> row.getReturnStatus() == OrderItemReturnStatus.RETURN_REQUESTED)
                .map(ReturnRefundReportRow::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return report;
    }

    private boolean matchesVendor(OrderItem item, Long vendorId) {
        if (item == null || vendorId == null) {
            return false;
        }
        Long itemVendorId = item.getVendorId();
        if (itemVendorId == null && item.getSalesOrder() != null) {
            itemVendorId = item.getSalesOrder().getVendorId();
        }
        return vendorId.equals(itemVendorId);
    }

    private boolean matchesCustomer(OrderItem item, Long customerId) {
        if (item == null || customerId == null || item.getSalesOrder() == null || item.getSalesOrder().getCustomer() == null) {
            return false;
        }
        return customerId.equals(item.getSalesOrder().getCustomer().getId());
    }

    private boolean isReturnRelated(OrderItem item) {
        return resolveReturnStatus(item) != OrderItemReturnStatus.NONE
                || safeAmount(item.getReturnRefundAmount()).compareTo(BigDecimal.ZERO) > 0;
    }

    private OrderItemReturnStatus resolveReturnStatus(OrderItem item) {
        return item != null && item.getReturnStatus() != null
                ? item.getReturnStatus()
                : OrderItemReturnStatus.NONE;
    }

    private LocalDateTime resolveReportDate(OrderItem item) {
        if (item == null) {
            return null;
        }
        if (item.getReturnRequestedAt() != null) {
            return item.getReturnRequestedAt();
        }
        if (item.getReturnedAt() != null) {
            return item.getReturnedAt();
        }
        return item.getModified() != null ? item.getModified() : item.getCreated();
    }

    private boolean withinDateRange(LocalDateTime dateTime, LocalDateTime from, LocalDateTime to) {
        if (dateTime == null) {
            return from == null && to == null;
        }
        if (from != null && dateTime.isBefore(from)) {
            return false;
        }
        return to == null || !dateTime.isAfter(to);
    }

    private ReturnRefundReportRow toRow(OrderItem item, Map<Long, Vendorprofile> vendorLookup) {
        ReturnRefundReportRow row = new ReturnRefundReportRow();
        SalesOrder order = item.getSalesOrder();
        Product product = item.getProduct();
        Long vendorId = item.getVendorId() != null ? item.getVendorId() : order != null ? order.getVendorId() : null;

        row.setOrderItemId(item.getId());
        row.setOrderId(order != null ? order.getId() : null);
        row.setOrderCode(order != null ? order.getOrderCode() : "-");
        row.setOrderStatus(order != null ? order.getStatus() : null);
        row.setCustomerName(resolveCustomerName(order));
        row.setCustomerEmail(resolveCustomerEmail(order));
        row.setVendorId(vendorId);
        row.setVendorName(resolveVendorName(vendorId, vendorLookup));
        row.setProductTitle(product != null ? product.getTitle() : "Product #" + item.getProductid());
        row.setVariantSummary(item.getVariantSummary());
        row.setQuantity(safeAmount(item.getQuantity()));
        row.setItemTotal(safeAmount(item.getItemTotal()));
        row.setReturnStatus(resolveReturnStatus(item));
        row.setReturnRequestedAt(item.getReturnRequestedAt());
        row.setReturnedAt(item.getReturnedAt());
        row.setRefundAmount(safeAmount(item.getReturnRefundAmount()));
        row.setRequestRemark(item.getReturnRequestRemark());
        row.setProcessedRemark(item.getReturnProcessedRemark());
        return row;
    }

    private String resolveCustomerName(SalesOrder order) {
        Users customer = order != null ? order.getCustomer() : null;
        if (customer == null) {
            return "-";
        }
        String firstName = customer.getFirstName() != null ? customer.getFirstName() : "";
        String lastName = customer.getLastName() != null ? customer.getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? customer.getEmail() : fullName;
    }

    private String resolveCustomerEmail(SalesOrder order) {
        Users customer = order != null ? order.getCustomer() : null;
        return customer != null && customer.getEmail() != null ? customer.getEmail() : "-";
    }

    private String resolveVendorName(Long vendorId, Map<Long, Vendorprofile> vendorLookup) {
        Vendorprofile vendor = vendorId != null ? vendorLookup.get(vendorId) : null;
        if (vendor == null) {
            return vendorId != null ? "Vendor #" + vendorId : "-";
        }
        return vendor.getCompanyName() != null && !vendor.getCompanyName().isBlank()
                ? vendor.getCompanyName()
                : vendor.getVendorCode();
    }

    private Map<Long, Vendorprofile> buildVendorLookup() {
        Map<Long, Vendorprofile> lookup = new HashMap<>();
        for (Vendorprofile vendor : vendorprofileRepository.findAll()) {
            lookup.put(vendor.getId(), vendor);
        }
        return lookup;
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }
}
