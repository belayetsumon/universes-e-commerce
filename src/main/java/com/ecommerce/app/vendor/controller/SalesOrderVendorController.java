/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.module.user.services.LoggedUserService;
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
import com.ecommerce.app.product.model.DeliveryCharge;
import com.ecommerce.app.services.BarcodeService;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import com.google.zxing.BarcodeFormat;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/vendor-order")
//@PreAuthorize("hasAuthority('vendor-order')")
public class SalesOrderVendorController {

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
    private VendorUserContext vendorUserContext;
    @Autowired
    BarcodeService barcodeService;
    @Autowired
    private SpringTemplateEngine templateEngine;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model, HttpSession session) {

        Vendorprofile vendorprofile = vendorUserContext.getActiveVendor();
        model.addAttribute("orderlist", salesOrderService.admin_all_Sales_order_list(vendorprofile.getId(), null));

        //  model.addAttribute("orderlist", salesOrderRepository.findByVendorIdOrderByIdDesc(vendorprofile.getId()));
        return "vendor/sales/index";
    }

    @RequestMapping(value = {"/details/{oid}"})
    public String details(Model model, @PathVariable Long oid, SalesOrder salesOrder) {

        salesOrder = salesOrderRepository.getReferenceById(oid);
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
        return "vendor/sales/order_details";
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
        orderHistory.setOrderStatusChanged(OrderStatusChangedBy.Vendor);
        orderHistory.setRemark(remark);
        orderHistory.setSalesOrder(salesOrder);

        orderHistoryRepository.save(orderHistory);

        return "redirect:/vendor-order/details/" + id;

    }

    @GetMapping("/addcharges/{oid}")
    public String addCharges(Model model, @PathVariable Long oid) {
        model.addAttribute("id", oid);
        return "vendor/sales/add_charges";
    }

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
