package com.ecommerce.app.order.services;

import com.ecommerce.app.order.model.BillingAddress;
import com.ecommerce.app.order.model.OrderHistory;
import com.ecommerce.app.order.model.SalesOrder;
import com.ecommerce.app.order.model.ShippingAddress;
import com.ecommerce.app.order.repository.BillingAddressRepository;
import com.ecommerce.app.order.repository.OrderHistoryRepository;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import com.ecommerce.app.order.repository.ShippingAddressRepository;
import com.ecommerce.app.services.BarcodeService;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import com.google.zxing.BarcodeFormat;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class SalesOrderPdfService {

    private final SpringTemplateEngine templateEngine;
    private final BarcodeService barcodeService;
    private final SalesOrderRepository salesOrderRepository;
    private final OrderItemService orderItemService;
    private final BillingAddressRepository billingAddressRepository;
    private final ShippingAddressRepository shippingAddressRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final VendorprofileRepository vendorprofileRepository;

    public SalesOrderPdfService(SpringTemplateEngine templateEngine,
            BarcodeService barcodeService,
            SalesOrderRepository salesOrderRepository,
            OrderItemService orderItemService,
            BillingAddressRepository billingAddressRepository,
            ShippingAddressRepository shippingAddressRepository,
            OrderHistoryRepository orderHistoryRepository,
            VendorprofileRepository vendorprofileRepository) {
        this.templateEngine = templateEngine;
        this.barcodeService = barcodeService;
        this.salesOrderRepository = salesOrderRepository;
        this.orderItemService = orderItemService;
        this.billingAddressRepository = billingAddressRepository;
        this.shippingAddressRepository = shippingAddressRepository;
        this.orderHistoryRepository = orderHistoryRepository;
        this.vendorprofileRepository = vendorprofileRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generate(SalesOrder order, String issuedBy) {
        return generate(order, issuedBy, "Sales Order", true, true, true, true);
    }

    @Transactional(readOnly = true)
    public byte[] generateForAdmin(SalesOrder order) {
        return generate(order, "Admin Panel", "Admin Sales Order", true, true, true, true);
    }

    @Transactional(readOnly = true)
    public byte[] generateForVendor(SalesOrder order) {
        return generate(order, "Vendor Panel", "Vendor Sales Order", false, true, false, false);
    }

    @Transactional(readOnly = true)
    public byte[] generateForCustomer(SalesOrder order) {
        return generate(order, "Customer Panel", "Customer Invoice", false, false, false, false);
    }

    private byte[] generate(SalesOrder order, String issuedBy, String documentTitle,
            boolean showInternalFinancials, boolean showVendorSettlement,
            boolean showVendorDetails, boolean showOrderHistory) {
        if (order == null || order.getId() == null) {
            throw new IllegalArgumentException("Order not found.");
        }

        Long orderId = order.getId();
        order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));
        Long customerId = order.getCustomer() != null ? order.getCustomer().getId() : null;
        List<Map<String, Object>> orderItems = orderItemService.item_List_By_SalesOrder(orderId);
        BillingAddress billingAddress = customerId != null
                ? billingAddressRepository.findFirstByUserId_IdOrderByIdDesc(customerId).orElse(null)
                : null;
        ShippingAddress shippingAddress = shippingAddressRepository.findByOrder_Id(orderId);
        List<OrderHistory> orderHistory = orderHistoryRepository.findBySalesOrderIdOrderByIdDesc(orderId);
        Vendorprofile vendor = order.getVendorId() != null
                ? vendorprofileRepository.findById(order.getVendorId()).orElse(null)
                : null;

        Context context = new Context();
        context.setVariable("order", order);
        context.setVariable("orderItems", orderItems);
        context.setVariable("billingAddress", billingAddress);
        context.setVariable("shippingAddress", shippingAddress);
        context.setVariable("orderHistory", orderHistory);
        context.setVariable("vendor", vendor);
        context.setVariable("issuedBy", issuedBy);
        context.setVariable("documentTitle", documentTitle);
        context.setVariable("showInternalFinancials", showInternalFinancials);
        context.setVariable("showVendorSettlement", showVendorSettlement);
        context.setVariable("showVendorDetails", showVendorDetails);
        context.setVariable("showOrderHistory", showOrderHistory);
        context.setVariable("issuedAt", LocalDateTime.now());
        context.setVariable("totalSalesPrice", defaultMoney(orderItemService.getTotalSalesPriceBySalesOrder(orderId)));
        context.setVariable("totalQuantity", defaultMoney(orderItemService.getTotalQuantityBySalesOrder(orderId)));
        context.setVariable("totalDiscount", defaultMoney(orderItemService.getTotalDiscountBySalesOrder(orderId)));
        context.setVariable("totalCommission", defaultMoney(orderItemService.getTotalCompanyProfitByItemBySalesOrder(orderId)));
        context.setVariable("totalVendorAmount", defaultMoney(orderItemService.getTotalVendorAmountBySalesOrder(orderId)));
        context.setVariable("totalVat", defaultMoney(orderItemService.getTotalVatlBySalesOrder(orderId)));
        context.setVariable("itemTotal", defaultMoney(orderItemService.getitemTotalBySalesOrder(orderId)));
        context.setVariable("barcodeBase64", barcodeDataUri(
                safeText(order.getOrderCode(), "ORDER-" + orderId),
                BarcodeFormat.CODE_128,
                420,
                70
        ));
        context.setVariable("qrBase64", barcodeDataUri(
                safeText(order.getOrderCode(), "ORDER-" + orderId),
                BarcodeFormat.QR_CODE,
                140,
                140
        ));

        String html = templateEngine.process("order/sales-order-pdf", context)
                .replaceFirst("^\\uFEFF", "")
                .trim();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, "");
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Could not generate sales order PDF.", ex);
        }
    }

    public String filename(SalesOrder order) {
        String orderCode = order != null ? safeText(order.getOrderCode(), "sales-order-" + order.getUuid()) : "sales-order";
        return orderCode.replaceAll("[^A-Za-z0-9._-]", "-") + ".pdf";
    }

    private BigDecimal defaultMoney(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private String safeText(String value, String fallback) {
        return value != null && !value.isBlank() ? value.trim() : fallback;
    }

    private String barcodeDataUri(String value, BarcodeFormat format, int width, int height) {
        try {
            return barcodeService.generateBarcodeBase64(value, format, width, height);
        } catch (Exception ex) {
            return null;
        }
    }
}
