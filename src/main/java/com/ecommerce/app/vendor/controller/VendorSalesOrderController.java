/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.services.ShipmentService;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.order.model.OrderHistory;
import com.ecommerce.app.order.model.OrderItemReturnStatus;
import com.ecommerce.app.order.model.OrderStatus;
import com.ecommerce.app.order.model.OrderStatusChangedBy;
import com.ecommerce.app.order.dto.ReturnRefundReportDto;
import com.ecommerce.app.order.model.SalesOrder;
import com.ecommerce.app.order.repository.BillingAddressRepository;
import com.ecommerce.app.order.repository.OrderHistoryRepository;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import com.ecommerce.app.order.services.ReturnRefundReportService;
import com.ecommerce.app.order.repository.ShippingAddressRepository;
import com.ecommerce.app.order.services.OrderItemService;
import com.ecommerce.app.order.services.SalesOrderPdfService;
import com.ecommerce.app.order.services.SalesOrderService;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/vendor-order")
//@PreAuthorize("hasAuthority('vendor-order')")
public class VendorSalesOrderController {

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    SalesOrderRepository salesOrderRepository;

    @Autowired
    OrderHistoryRepository orderHistoryRepository;

    @Autowired
    ShippingAddressRepository shippingAddressRepository;

    @Autowired
    BillingAddressRepository billingAddressRepository;

    @Autowired
    SalesOrderService salesOrderService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    SalesOrderPdfService salesOrderPdfService;

    @Autowired
    ReturnRefundReportService returnRefundReportService;

    @Autowired
    private VendorUserContext vendorUserContext;
    @Autowired
    private ShipmentService shipmentService;

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.order.read')
//            or @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.order.update')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model,
            HttpSession session,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "status", required = false) OrderStatus status,
            @RequestParam(name = "shipment", required = false) String shipment,
            @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        Vendorprofile vendorprofile = vendorUserContext.getActiveVendor();
        List<Map<String, Object>> orderRows = salesOrderService.admin_all_Sales_order_list(vendorprofile.getId(), null);
        List<Map<String, Object>> enrichedRows = shipmentService.enrichOrderRowsWithShipmentData(orderRows);
        List<Map<String, Object>> filteredRows = filterOrderRows(enrichedRows, q, status, shipment, fromDate, toDate);
        model.addAttribute("orderlist", filteredRows);
        model.addAttribute("allOrderCount", enrichedRows.size());
        model.addAttribute("filteredOrderCount", filteredRows.size());
        model.addAttribute("shipmentReadyCount", countShipmentState(enrichedRows, "READY"));
        model.addAttribute("shipmentCreatedCount", countShipmentState(enrichedRows, "CREATED"));
        model.addAttribute("shipmentBlockedCount", countShipmentState(enrichedRows, "BLOCKED"));
        model.addAttribute("filteredGrandTotal", sumAmount(filteredRows, "grandTotal"));
        model.addAttribute("filteredVendorTotal", sumAmount(filteredRows, "totalVendorAmount"));
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedQ", q);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedShipment", shipment);
        model.addAttribute("selectedFromDate", fromDate);
        model.addAttribute("selectedToDate", toDate);

        //  model.addAttribute("orderlist", salesOrderRepository.findByVendorIdOrderByIdDesc(vendorprofile.getId()));
        return "vendor/sales/index";
    }

    @GetMapping("/returns")
    public String returns(
            @RequestParam(required = false) OrderItemReturnStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model,
            RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return "redirect:/vendor-order/index";
        }

        try {
            model.addAttribute("report", returnRefundReportService.buildVendorReturnReport(activeVendor.getId(), status, fromDate, toDate));
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Runtime error while loading vendor returns: " + ex.getMessage());
            model.addAttribute("report", new ReturnRefundReportDto());
        }
        model.addAttribute("statuses", OrderItemReturnStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "vendor/sales/returns";
    }

    @GetMapping("/refunds")
    public String refunds(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model,
            RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return "redirect:/vendor-order/index";
        }

        try {
            model.addAttribute("report", returnRefundReportService.buildVendorRefundReport(activeVendor.getId(), fromDate, toDate));
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Runtime error while loading vendor refunds: " + ex.getMessage());
            model.addAttribute("report", new ReturnRefundReportDto());
        }
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "vendor/sales/refunds";
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.order.read')
//            or @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.order.update')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @RequestMapping(value = {"/details/{oid}"})
    public String details(Model model, @PathVariable Long oid, SalesOrder salesOrder, RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return "redirect:/vendor-order/index";
        }

        try {
            salesOrder = salesOrderService.getVendorOrderForVendor(oid, activeVendor.getId());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/vendor-order/index";
        }

        long customerId = salesOrder.getCustomer().getId();
        Shipment existingShipment = shipmentService.getLatestByOrderId(oid);
        List<Map<String, Object>> orderItems = salesOrderService.enrichOrderItemsWithReturnData(
                orderItemService.item_List_By_SalesOrder(oid),
                salesOrder.getStatus()
        );
        model.addAttribute("orderdetails", salesOrder);
        model.addAttribute("orderitem", orderItems);
        model.addAttribute("total_sales_price", orderItemService.getTotalSalesPriceBySalesOrder(oid));
        model.addAttribute("total_quantity", orderItemService.getTotalQuantityBySalesOrder(oid));
        model.addAttribute("total_discount", orderItemService.getTotalDiscountBySalesOrder(oid));
        model.addAttribute("total_company_profit", orderItemService.getTotalCompanyProfitByItemBySalesOrder(oid));
        model.addAttribute("total_vat", orderItemService.getTotalVatlBySalesOrder(oid));
        model.addAttribute("total_vendorAmount", orderItemService.getTotalVendorAmountBySalesOrder(oid));
        model.addAttribute("item_total", orderItemService.getitemTotalBySalesOrder(oid));
        model.addAttribute("statusType", salesOrderService.getVendorStatusOptions(salesOrder));
        List<OrderHistory> history = orderHistoryRepository.findBySalesOrderIdOrderByIdDesc(oid);
        model.addAttribute("orderhistory", history);
        model.addAttribute("shipmentEligible", shipmentService.canCreateNewShipment(salesOrder));
        model.addAttribute("existingShipmentId", existingShipment != null ? existingShipment.getId() : null);
        model.addAttribute("shipmentBlockReason", existingShipment == null ? shipmentService.getShipmentBlockReason(salesOrder) : null);
        model.addAttribute("vendorCanCancelOrder", salesOrderService.canVendorCancelOrder(salesOrder));
        model.addAttribute("vendorCancellationMessage", salesOrderService.getVendorCancellationPolicyMessage(salesOrder));
        // Date: 2026-04-20: some customers have multiple billing addresses; pick latest.
        model.addAttribute("billingAddress", billingAddressRepository.findFirstByUserId_IdOrderByIdDesc(customerId).orElse(null));
        model.addAttribute("shippingAddress", shippingAddressRepository.findByOrder_Id(oid));
        return "vendor/sales/order_details";
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.order.update')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @PostMapping("/statuschange")
    public String statusChange(Model model,
            SalesOrder salesOrder,
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "status", required = false) OrderStatus status,
            @RequestParam(name = "remark", required = false) String remark,
            RedirectAttributes redirectAttributes
    ) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return "redirect:/vendor-order/index";
        }

        try {
            salesOrderService.changeStatusAsVendor(id, activeVendor.getId(), status, remark);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/vendor-order/details/" + id;

    }

    @PostMapping("/item-return")
    public String itemReturn(
            @RequestParam(name = "orderId") Long orderId,
            @RequestParam(name = "orderItemId") Long orderItemId,
            @RequestParam(name = "remark", required = false) String remark,
            RedirectAttributes redirectAttributes
    ) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return "redirect:/vendor-order/index";
        }

        try {
            salesOrderService.processItemReturnAsVendor(orderId, activeVendor.getId(), orderItemId, remark);
            redirectAttributes.addFlashAttribute("successMessage", "Selected item marked as returned.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/vendor-order/details/" + orderId;
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.order.update')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @GetMapping("/addcharges/{oid}")
    public String addCharges(Model model, @PathVariable Long oid) {
        model.addAttribute("id", oid);
        return "vendor/sales/add_charges";
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.order.update')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @PostMapping("/update-charges")
    @ResponseBody
    public void updateCharges(SalesOrder salesOrder,
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "packingCharge", required = false) BigDecimal packingCharge,
            @RequestParam(name = "deliveryCharge", required = false) BigDecimal deliveryCharge,
            RedirectAttributes ra, HttpServletResponse response) {
        salesOrderService.updateCharges(id, packingCharge, deliveryCharge);
        salesOrderService.updateOrderTotals(id);
        ra.addFlashAttribute("success", "Charges updated successfully!");
        response.setHeader("HX-Refresh", "true");
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.order.read')
//            or @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.order.update')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @GetMapping("/orders/{id}/pdf")
    public ResponseEntity<byte[]> orderPdf(@PathVariable Long id, SalesOrder salesOrder) throws Exception {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            throw new IllegalArgumentException("Vendor context not found.");
        }

        salesOrder = salesOrderService.getVendorOrderForVendor(id, activeVendor.getId());
        byte[] pdfBytes = salesOrderPdfService.generateForVendor(salesOrder);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", salesOrderPdfService.filename(salesOrder));
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    private List<Map<String, Object>> filterOrderRows(List<Map<String, Object>> rows,
            String q,
            OrderStatus status,
            String shipment,
            LocalDate fromDate,
            LocalDate toDate) {
        String normalizedQuery = q != null ? q.trim().toLowerCase(Locale.ROOT) : "";
        String normalizedShipment = shipment != null ? shipment.trim().toUpperCase(Locale.ROOT) : "";
        return rows.stream()
                .filter(row -> normalizedQuery.isBlank() || rowMatchesQuery(row, normalizedQuery))
                .filter(row -> status == null || status.equals(row.get("status")))
                .filter(row -> normalizedShipment.isBlank() || rowMatchesShipmentState(row, normalizedShipment))
                .filter(row -> fromDate == null || !asLocalDate(row.get("created")).isBefore(fromDate))
                .filter(row -> toDate == null || !asLocalDate(row.get("created")).isAfter(toDate))
                .toList();
    }

    private boolean rowMatchesQuery(Map<String, Object> row, String query) {
        String searchable = String.join(" ",
                value(row.get("orderCode")),
                value(row.get("firstName")),
                value(row.get("lastName")),
                value(row.get("vendorName")),
                value(row.get("mobile")),
                value(row.get("email")),
                value(row.get("status"))
        ).toLowerCase(Locale.ROOT);
        return searchable.contains(query);
    }

    private long countShipmentState(List<Map<String, Object>> rows, String shipmentState) {
        return rows.stream().filter(row -> rowMatchesShipmentState(row, shipmentState)).count();
    }

    private boolean rowMatchesShipmentState(Map<String, Object> row, String shipmentState) {
        boolean hasShipment = Boolean.TRUE.equals(row.get("hasShipment"));
        boolean shipmentEligible = Boolean.TRUE.equals(row.get("shipmentEligible"));
        return switch (shipmentState) {
            case "READY" -> shipmentEligible;
            case "CREATED" -> hasShipment;
            case "BLOCKED" -> !shipmentEligible && !hasShipment;
            default -> true;
        };
    }

    private BigDecimal sumAmount(List<Map<String, Object>> rows, String key) {
        return rows.stream()
                .map(row -> asBigDecimal(row.get(key)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal asBigDecimal(Object value) {
        if (value instanceof BigDecimal amount) {
            return amount;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }

    private LocalDate asLocalDate(Object value) {
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toLocalDate();
        }
        if (value instanceof LocalDate date) {
            return date;
        }
        return LocalDate.now();
    }

    private String value(Object value) {
        return value == null ? "" : value.toString();
    }

}
