/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.admincustomer.controller;

import com.ecommerce.app.module.user.model.Role;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.RoleRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.order.model.OrderHistory;
import com.ecommerce.app.order.model.OrderStatus;
import com.ecommerce.app.order.model.OrderStatusChangedBy;
import com.ecommerce.app.order.model.SalesOrder;
import com.ecommerce.app.order.repository.BillingAddressRepository;
import com.ecommerce.app.order.repository.OrderHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import com.ecommerce.app.order.repository.ShippingAddressRepository;
import com.ecommerce.app.order.services.OrderItemService;
import com.ecommerce.app.order.services.SalesOrderService;
import com.ecommerce.app.services.BarcodeService;
import com.google.zxing.BarcodeFormat;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

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
    OrderItemService orderItemService;

    @Autowired
    BarcodeService barcodeService;
    @Autowired
    private SpringTemplateEngine templateEngine;

    @RequestMapping(value = {"", "/", "/index"})
    public String customerlist(Model model) {

        Role customer = roleRepository.findBySlug("customer");

        model.addAttribute("customerlist", usersRepository.findByRole(customer));

        return "admin/customer/index";
    }

    @RequestMapping("/orderlist")
    public String orderlist(Model model) {
        model.addAttribute("orderlist", salesOrderService.admin_all_Sales_order_list(null, null));
        // model.addAttribute("orderlist", salesOrderRepository.findAllByOrderByIdDesc());
        return "admin/customer/orderlist";
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
        long customerId = salesOrder.getCustomer().getId();
        model.addAttribute("orderdetails", salesOrderRepository.getReferenceById(oid));
        model.addAttribute("orderitem", orderItemService.item_List_By_SalesOrder(oid));
        model.addAttribute("total_sales_price", orderItemService.getTotalSalesPriceBySalesOrder(oid));
        model.addAttribute("total_quantity", orderItemService.getTotalQuantityBySalesOrder(oid));
        model.addAttribute("total_discount", orderItemService.getTotalDiscountBySalesOrder(oid));
        model.addAttribute("total_company_profit", orderItemService.getTotalCompanyProfitByItemBySalesOrder(oid));
        model.addAttribute("total_vat", orderItemService.getTotalVatlBySalesOrder(oid));
        model.addAttribute("total_vendorAmount", orderItemService.getTotalVendorAmountBySalesOrder(oid));
        model.addAttribute("item_total", orderItemService.getitemTotalBySalesOrder(oid));

        model.addAttribute("statusType", OrderStatus.values());
        List<OrderHistory> history = orderHistoryRepository.findBySalesOrderIdOrderByIdDesc(oid);
        model.addAttribute("orderhistory", history);
        model.addAttribute("billingAddress", billingAddressRepository.findByUserId_Id(customerId).get());
        model.addAttribute("shippingAddress", shippingAddressRepository.findByOrder_Id(oid));
        return "admin/customer/order_details";
    }

    @RequestMapping(value = {"/statuschange"})
    public String statusChange(Model model,
            SalesOrder salesOrder,
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "status", required = false) OrderStatus status,
            @RequestParam(name = "remark", required = false) String remark
    ) {
        salesOrder = salesOrderRepository.getReferenceById(id);

        salesOrder.setStatus(status);

        salesOrderRepository.save(salesOrder);

        OrderHistory orderHistory = new OrderHistory();
        orderHistory.setStatus(status);
        orderHistory.setOrderStatusChanged(OrderStatusChangedBy.MarketPlace);
        orderHistory.setSalesOrder(salesOrder);
        orderHistory.setRemark(remark);
        orderHistoryRepository.save(orderHistory);
        return "redirect:/admin-customer/order-details/" + id;
    }

    @GetMapping("/orders/{id}/pdf")
    public ResponseEntity<byte[]> orderPdf(@PathVariable Long id, SalesOrder salesOrder) throws Exception {

        salesOrder = salesOrderRepository.getReferenceById(id);

        Context ctx = new Context();

        ctx.setVariable("order", salesOrder);

        ctx.setVariable("company", salesOrder.getVendorId());

// Logo as Base64
//        ctx.setVariable("companyLogoBase64", toBase64(salesOrder.getVendorId().getLogoPath()));
// Barcode + QR
        ctx.setVariable("barcodeBase64", barcodeService.generateBarcodeBase64(salesOrder.getOrderCode(), BarcodeFormat.CODE_128, 400, 60));

        ctx.setVariable("qrBase64", barcodeService.generateBarcodeBase64("https://example.com/orders/" + salesOrder.getOrderCode(), BarcodeFormat.QR_CODE, 150, 150));

        String html = templateEngine.process("sales-order-pdf", ctx);

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        PdfRendererBuilder builder = new PdfRendererBuilder();

        builder.withHtmlContent(html, "");

        builder.toStream(os);

        builder.run();

        byte[] pdfBytes = os.toByteArray();

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_PDF);

        headers.setContentDispositionFormData("attachment", salesOrder.getOrderCode() + ".pdf");

        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

}
