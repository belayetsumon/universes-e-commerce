/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.admincustomer.controller;

import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.services.ShipmentService;
import com.ecommerce.app.module.user.model.Role;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.RoleRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.order.model.OrderHistory;
import com.ecommerce.app.module.order.model.OrderPaymentPlan;
import com.ecommerce.app.module.order.model.OrderPaymentState;
import com.ecommerce.app.module.order.model.OrderStatus;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.dto.SalesOrderListResult;
import com.ecommerce.app.module.order.repository.BillingAddressRepository;
import com.ecommerce.app.module.order.repository.OrderHistoryRepository;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import com.ecommerce.app.module.order.repository.ShippingAddressRepository;
import com.ecommerce.app.module.order.services.OrderItemService;
import com.ecommerce.app.module.order.services.SalesOrderPdfService;
import com.ecommerce.app.module.order.services.SalesOrderListViewService;
import com.ecommerce.app.module.order.services.SalesOrderService;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/admin-customer")
//@PreAuthorize("hasAuthority('admin-customer')")
public class AdminCustomerController {

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    RoleRepository roleRepository;

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
    SalesOrderListViewService salesOrderListViewService;

    @Autowired
    VendorprofileRepository vendorprofileRepository;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    SalesOrderPdfService salesOrderPdfService;

    @Autowired
    private ShipmentService shipmentService;

    @RequestMapping(value = {"", "/", "/index"})
    public String customerlist(Model model) {

        Role customer = roleRepository.findBySlug("customer");

        model.addAttribute("customerlist", usersRepository.findByRole(customer));

        return "admin/customer/index";
    }

    @RequestMapping("/orderlist")
    public String orderlist(Model model,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "status", required = false) OrderStatus status,
            @RequestParam(name = "shipment", required = false) String shipment,
            @RequestParam(name = "paymentPlan", required = false) OrderPaymentPlan paymentPlan,
            @RequestParam(name = "paymentState", required = false) OrderPaymentState paymentState,
            @RequestParam(name = "checkoutType", required = false) String checkoutType,
            @RequestParam(name = "vendorId", required = false) Long vendorId,
            @RequestParam(name = "minTotal", required = false) BigDecimal minTotal,
            @RequestParam(name = "maxTotal", required = false) BigDecimal maxTotal,
            @RequestParam(name = "dateRange", required = false, defaultValue = SalesOrderListViewService.DATE_RANGE_TODAY) String dateRange,
            @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "25") int size) {
        List<Map<String, Object>> orderRows = salesOrderService.admin_all_Sales_order_list(null, null);
        List<Map<String, Object>> enrichedRows = shipmentService.enrichOrderRowsWithShipmentData(orderRows);
        SalesOrderListResult result = salesOrderListViewService.build(
                enrichedRows,
                q,
                status,
                shipment,
                paymentPlan,
                paymentState,
                checkoutType,
                vendorId,
                minTotal,
                maxTotal,
                dateRange,
                fromDate,
                toDate,
                page,
                size
        );
        populateOrderListModel(model, result, q, status, shipment, paymentPlan, paymentState, checkoutType, vendorId, minTotal, maxTotal);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("paymentPlans", OrderPaymentPlan.values());
        model.addAttribute("paymentStates", OrderPaymentState.values());
        model.addAttribute("dateRangeOptions", salesOrderListViewService.dateRangeOptions());
        model.addAttribute("vendorOptions", vendorprofileRepository.findAll());
        // model.addAttribute("orderlist", salesOrderRepository.findAllByOrderByIdDesc());
        return "admin/sales/orderlist";
    }

    private void populateOrderListModel(Model model,
            SalesOrderListResult result,
            String q,
            OrderStatus status,
            String shipment,
            OrderPaymentPlan paymentPlan,
            OrderPaymentState paymentState,
            String checkoutType,
            Long vendorId,
            BigDecimal minTotal,
            BigDecimal maxTotal) {
        model.addAttribute("orderlist", result.getOrderRows());
        model.addAttribute("orderPage", result);
        model.addAttribute("orderKpi", result.getKpi());
        model.addAttribute("allOrderCount", result.getKpi().getAllOrderCount());
        model.addAttribute("filteredOrderCount", result.getKpi().getFilteredOrderCount());
        model.addAttribute("shipmentReadyCount", result.getKpi().getShipmentReadyCount());
        model.addAttribute("shipmentCreatedCount", result.getKpi().getShipmentCreatedCount());
        model.addAttribute("shipmentBlockedCount", result.getKpi().getShipmentBlockedCount());
        model.addAttribute("filteredGrandTotal", result.getKpi().getFilteredGrandTotal());
        model.addAttribute("filteredVendorTotal", result.getKpi().getFilteredVendorTotal());
        model.addAttribute("selectedQ", q);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedShipment", shipment);
        model.addAttribute("selectedPaymentPlan", paymentPlan);
        model.addAttribute("selectedPaymentState", paymentState);
        model.addAttribute("selectedCheckoutType", checkoutType);
        model.addAttribute("selectedVendorId", vendorId);
        model.addAttribute("selectedMinTotal", minTotal);
        model.addAttribute("selectedMaxTotal", maxTotal);
        model.addAttribute("selectedDateRange", result.getSelectedDateRange());
        model.addAttribute("selectedFromDate", result.getSelectedFromDate());
        model.addAttribute("selectedToDate", result.getSelectedToDate());
        model.addAttribute("reportDateRangeLabel", result.getReportDateRangeLabel());
        model.addAttribute("size", result.getSize());
    }

    @RequestMapping("/order-by-customer/{cid}")
    public String orderbycustomer(Model model, @PathVariable Long cid) {
        Users customer = new Users();
        customer.setId(cid);
        model.addAttribute("orderlist", salesOrderRepository.findByCustomerOrderByIdDesc(customer));
        return "admin/customer/order-by-customer";
    }

    @RequestMapping("/order-details/{oid}")
    public String order_details(Model model, @PathVariable Long oid) {
        SalesOrder salesOrder = salesOrderRepository.getReferenceById(oid);
        Shipment existingShipment = shipmentService.getLatestByOrderId(oid);
        long customerId = salesOrder.getCustomer().getId();
        List<Map<String, Object>> orderItems = salesOrderService.enrichOrderItemsWithReturnData(
                orderItemService.item_List_By_SalesOrder(oid),
                salesOrder.getStatus()
        );
        model.addAttribute("orderdetails", salesOrderRepository.getReferenceById(oid));
        model.addAttribute("orderitem", orderItems);
        model.addAttribute("total_sales_price", orderItemService.getTotalSalesPriceBySalesOrder(oid));
        model.addAttribute("total_quantity", orderItemService.getTotalQuantityBySalesOrder(oid));
        model.addAttribute("total_discount", orderItemService.getTotalDiscountBySalesOrder(oid));
        model.addAttribute("total_company_profit", orderItemService.getTotalCompanyProfitByItemBySalesOrder(oid));
        model.addAttribute("total_vat", orderItemService.getTotalVatlBySalesOrder(oid));
        model.addAttribute("total_vendorAmount", orderItemService.getTotalVendorAmountBySalesOrder(oid));
        model.addAttribute("item_total", orderItemService.getitemTotalBySalesOrder(oid));

        model.addAttribute(
                "statusType",
                salesOrderService.getMarketplaceStatusOptions(salesOrder)
        );
        List<OrderHistory> history = orderHistoryRepository.findBySalesOrderIdOrderByIdDesc(oid);
        model.addAttribute("orderhistory", history);
        model.addAttribute("shipmentEligible", shipmentService.canCreateNewShipment(salesOrder));
        model.addAttribute("existingShipmentId", existingShipment != null ? existingShipment.getId() : null);
        model.addAttribute("shipmentBlockReason", existingShipment == null ? shipmentService.getShipmentBlockReason(salesOrder) : null);
        model.addAttribute("marketplaceCanCancelOrder", salesOrderService.canMarketplaceCancelOrder(salesOrder));
        model.addAttribute("marketplaceCancellationMessage", salesOrderService.getMarketplaceCancellationPolicyMessage(salesOrder));
        // Date: 2026-04-20: some customers have multiple billing addresses; pick latest.
        model.addAttribute("billingAddress", billingAddressRepository.findFirstByUserId_IdOrderByIdDesc(customerId).orElse(null));
        model.addAttribute("shippingAddress", shippingAddressRepository.findByOrder_Id(oid));
        return "admin/sales/order_details";
    }

    @PostMapping("/statuschange")
    public String statusChange(Model model,
            SalesOrder salesOrder,
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "status", required = false) OrderStatus status,
            @RequestParam(name = "remark", required = false) String remark,
            RedirectAttributes redirectAttributes
    ) {
        try {
            salesOrderService.changeStatusAsMarketplace(id, status, remark);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin-customer/order-details/" + id;
    }

    @PostMapping("/item-return")
    public String itemReturn(
            @RequestParam(name = "orderId") Long orderId,
            @RequestParam(name = "orderItemId") Long orderItemId,
            @RequestParam(name = "remark", required = false) String remark,
            RedirectAttributes redirectAttributes
    ) {
        try {
            salesOrderService.processItemReturnAsMarketplace(orderId, orderItemId, remark);
            redirectAttributes.addFlashAttribute("successMessage", "Selected item marked as returned.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin-customer/order-details/" + orderId;
    }

    @GetMapping({"/orders/{id}/pdf", "/pdf/{id}"})
    public ResponseEntity<byte[]> orderPdf(@PathVariable Long id, SalesOrder salesOrder) throws Exception {
        salesOrder = salesOrderService.getOrderOrThrow(id);
        byte[] pdfBytes = salesOrderPdfService.generateForAdmin(salesOrder);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", salesOrderPdfService.filename(salesOrder));
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

}
