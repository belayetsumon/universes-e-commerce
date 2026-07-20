/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.model.ShipmentStatus;
import com.ecommerce.app.module.shipping.services.CarrierService;
import com.ecommerce.app.module.shipping.services.DeliveryPersonService;
import com.ecommerce.app.module.shipping.services.PickupAddressService;
import com.ecommerce.app.module.shipping.services.ShipmentService;
import com.ecommerce.app.module.shipping.services.ShipmentTrackingService;
import com.ecommerce.app.module.shipping.services.ShippingDocumentService;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import com.ecommerce.app.module.order.services.SalesOrderService;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/vendor/shipments")
//@PreAuthorize("""
//        @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.shipping.manage')
//        or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//        or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//        or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//        """)
public class Vendor_ShipmentController {

    private final ShipmentService shipmentService;
    private final CarrierService carrierService;
    private final DeliveryPersonService deliveryPersonService;
    private final SalesOrderService salesOrderService;
    private final SalesOrderRepository salesOrderRepository;
    private final VendorUserContext vendorUserContext;
    private final ShipmentTrackingService shipmentTrackingService;
    private final PickupAddressService pickupAddressService;
    private final ShippingDocumentService shippingDocumentService;

    public Vendor_ShipmentController(ShipmentService shipmentService,
            CarrierService carrierService,
            DeliveryPersonService deliveryPersonService,
            SalesOrderService salesOrderService,
            SalesOrderRepository salesOrderRepository,
            VendorUserContext vendorUserContext,
            ShipmentTrackingService shipmentTrackingService,
            PickupAddressService pickupAddressService,
            ShippingDocumentService shippingDocumentService) {
        this.shipmentService = shipmentService;
        this.carrierService = carrierService;
        this.deliveryPersonService = deliveryPersonService;
        this.salesOrderService = salesOrderService;
        this.salesOrderRepository = salesOrderRepository;
        this.vendorUserContext = vendorUserContext;
        this.shipmentTrackingService = shipmentTrackingService;
        this.pickupAddressService = pickupAddressService;
        this.shippingDocumentService = shippingDocumentService;
    }

    // List shipments for vendor
    @GetMapping
    public String list(Model model) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            model.addAttribute("shipments", List.of());
            model.addAttribute("trackingEventsByShipment", Map.of());
            model.addAttribute("errorMessage", "Vendor context not found.");
            return "vendor/shipments/list";
        }

        List<Shipment> shipments = shipmentService.getByVendor(activeVendor.getId());
        model.addAttribute("shipments", shipments);
        model.addAttribute("trackingEventsByShipment", shipmentTrackingService.getEventsByShipmentIds(
                shipments.stream().map(Shipment::getId).filter(Objects::nonNull).toList()
        ));
        return "vendor/shipments/list";
    }

    // Create form
    @GetMapping("/new")
    public String createForm(@RequestParam(name = "orderId", required = false) Long orderId,
            @RequestParam(name = "orderUuid", required = false) String orderUuid,
            Model model,
            RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return "redirect:/vendor-order/index";
        }

        Shipment shipment = new Shipment();
        shipment.setVendorId(activeVendor.getId());
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setShippingCost(BigDecimal.ZERO);
        shipment.setPickupAddress(pickupAddressService.getDefaultForVendor(activeVendor.getId()));

        orderId = resolveOrderId(orderId, orderUuid);
        if (orderId != null) {
            String prefillError = prefillShipmentFromVendorOrder(shipment, activeVendor.getId(), orderId, true);
            if (prefillError != null) {
                model.addAttribute("errorMessage", prefillError);
            }
        }

        model.addAttribute("shipment", shipment);
        model.addAttribute("trackingEvents", List.of());
        populateFormOptions(model, activeVendor.getId(), shipment.getSalesOrderId());
        return "vendor/shipments/form";
    }

    // Save shipment
    @PostMapping
    public String save(@Valid @ModelAttribute Shipment shipment,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return "redirect:/vendor-order/index";
        }

        shipment.setVendorId(activeVendor.getId());
        Shipment existingShipment = shipment.getId() != null ? shipmentService.getById(shipment.getId()) : null;
        validateVendorOrder(shipment, activeVendor.getId(), existingShipment, result);

        if (shipment.getCarrier() == null && shipment.getDeliveryPerson() == null) {
            result.reject("error.shipment", "Select at least Carrier or Delivery Person");
        }

        if (shipment.getDeliveryPerson() != null
                && shipment.getDeliveryPerson().getVendorId() != null
                && !Objects.equals(shipment.getDeliveryPerson().getVendorId(), activeVendor.getId())) {
            result.rejectValue("deliveryPerson", "error.shipment", "Selected delivery person does not belong to the active vendor");
        }

        if (shipment.getPickupAddress() != null
                && shipment.getPickupAddress().getVendorId() != null
                && !Objects.equals(shipment.getPickupAddress().getVendorId(), activeVendor.getId())) {
            result.rejectValue("pickupAddress", "error.shipment", "Selected pickup address does not belong to the active vendor");
        }

        if (result.hasErrors()) {
            populateFormOptions(model, activeVendor.getId(), shipment.getSalesOrderId());
            return "vendor/shipments/form";
        }

        try {
            Shipment savedShipment = shipmentService.save(shipment);
            String successMessage = savedShipment != null && savedShipment.getId() != null
                    ? "Shipment #" + savedShipment.getId() + " saved successfully."
                    : "Shipment saved successfully.";
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/vendor/shipments";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute("errorMessage", "Shipment could not be saved because the order or tracking reference is already used.");
        } catch (Exception ex) {
            model.addAttribute("errorMessage", resolveShipmentSaveError(ex));
        }

        populateFormOptions(model, activeVendor.getId(), shipment.getSalesOrderId());
        return "vendor/shipments/form";
    }

    @PostMapping("/{id}/collect-cod")
    public String collectCod(@PathVariable Long id,
            @RequestParam("amount") BigDecimal amount,
            RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return "redirect:/vendor-order/index";
        }

        try {
            shipmentService.collectVendorPayment(activeVendor.getId(), id, amount);
            redirectAttributes.addFlashAttribute("successMessage", "COD payment collected successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendor/shipments";
    }

    @PostMapping("/{id}/generate-label")
    public String generateLabel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return "redirect:/vendor-order/index";
        }

        Shipment shipment = shipmentService.getById(id);
        if (shipment == null || !Objects.equals(shipment.getVendorId(), activeVendor.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shipment not found for the active vendor.");
            return "redirect:/vendor/shipments";
        }

        try {
            shippingDocumentService.generateLabelForShipment(id, "Vendor Panel");
            redirectAttributes.addFlashAttribute("successMessage", "Shipping label generated and linked with shipment.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not generate shipping label: " + ex.getMessage());
        }
        return "redirect:/vendor/shipments";
    }

    // Edit shipment
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return "redirect:/vendor-order/index";
        }

        Shipment shipment = shipmentService.getById(id);
        if (shipment == null || !Objects.equals(shipment.getVendorId(), activeVendor.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shipment not found for the active vendor.");
            return "redirect:/vendor/shipments";
        }

        shipmentService.syncCodFromOrder(shipment);
        model.addAttribute("shipment", shipment);
        model.addAttribute("trackingEvents", shipmentTrackingService.getEvents(shipment.getId()));
        populateFormOptions(model, activeVendor.getId(), shipment.getSalesOrderId());
        return "vendor/shipments/form";
    }

    // Delete shipment
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return "redirect:/vendor-order/index";
        }

        Shipment shipment = shipmentService.getById(id);
        if (shipment == null || !Objects.equals(shipment.getVendorId(), activeVendor.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shipment not found for the active vendor.");
            return "redirect:/vendor/shipments";
        }

        shipmentService.delete(id);
        return "redirect:/vendor/shipments";
    }

    private void populateFormOptions(Model model, Long vendorId, Long includeOrderId) {
        model.addAttribute("carriers", carrierService.getAll());
        model.addAttribute("deliveryPersons", deliveryPersonService.getByVendor(vendorId));
        model.addAttribute("pickupAddresses", pickupAddressService.getByVendor(vendorId));
        model.addAttribute("salesOrders", buildSalesOrderOptions(vendorId, includeOrderId));
        model.addAttribute("activeVendor", vendorUserContext.getActiveVendor());
        model.addAttribute("selectedOrderCode", resolveOrderCode(includeOrderId));
    }

    private List<Map<String, Object>> buildSalesOrderOptions(Long vendorId, Long includeOrderId) {
        List<Map<String, Object>> options = new ArrayList<>();
        for (SalesOrder order : shipmentService.getEligibleOrders(vendorId, includeOrderId)) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", order.getId());
            option.put("orderCode", order.getOrderCode());
            option.put("district", resolveShippingDistrict(order));
            option.put("shippingCost", resolveShippingCost(order));
            option.put("grandTotal", order.getGrandTotal() != null ? order.getGrandTotal() : BigDecimal.ZERO);
            options.add(option);
        }
        return options;
    }

    private SalesOrder validateVendorOrder(Shipment shipment, Long vendorId, Shipment existingShipment, BindingResult result) {
        if (shipment.getSalesOrderId() == null) {
            return null;
        }

        SalesOrder salesOrder;
        try {
            salesOrder = salesOrderService.getVendorOrderForVendor(shipment.getSalesOrderId(), vendorId);
        } catch (IllegalArgumentException ex) {
            result.rejectValue("salesOrderId", "error.shipment", ex.getMessage());
            return null;
        }

        boolean sameOrderAsExisting = existingShipment != null
                && Objects.equals(existingShipment.getSalesOrderId(), shipment.getSalesOrderId());

        if (!sameOrderAsExisting) {
            Shipment shipmentForOrder = shipmentService.getLatestByOrderId(salesOrder.getId());
            if (shipmentForOrder != null
                    && (shipment.getId() == null || !shipmentForOrder.getId().equals(shipment.getId()))) {
                result.rejectValue("salesOrderId", "error.shipment", "A shipment already exists for the selected sales order");
            }

            String blockReason = shipmentService.getShipmentBlockReason(salesOrder);
            if (blockReason != null) {
                result.rejectValue("salesOrderId", "error.shipment", blockReason);
            }
        }

        String orderDistrict = resolveShippingDistrict(salesOrder);
        if (orderDistrict.isBlank()) {
            result.rejectValue("district", "error.shipment", "Selected sales order has no shipping address district");
        } else {
            shipment.setDistrict(orderDistrict);
        }

        shipment.setVendorId(vendorId);
        shipmentService.syncCodFromOrder(shipment);
        return salesOrder;
    }

    private String prefillShipmentFromVendorOrder(Shipment shipment, Long vendorId, Long orderId, boolean enforceEligibility) {
        SalesOrder salesOrder;
        try {
            salesOrder = salesOrderService.getVendorOrderForVendor(orderId, vendorId);
        } catch (IllegalArgumentException ex) {
            return ex.getMessage();
        }

        if (shipmentService.hasShipmentForOrder(orderId)) {
            Shipment existingShipment = shipmentService.getLatestByOrderId(orderId);
            if (existingShipment != null) {
                return "A shipment already exists for this sales order. Please edit shipment #" + existingShipment.getId() + ".";
            }
        }

        if (enforceEligibility) {
            String blockReason = shipmentService.getShipmentBlockReason(salesOrder);
            if (blockReason != null) {
                return blockReason;
            }
        }

        shipment.setSalesOrderId(orderId);
        shipment.setVendorId(vendorId);
        shipmentService.syncCodFromOrder(shipment);
        return null;
    }

    private String resolveShippingDistrict(SalesOrder order) {
        if (order == null || order.getShippingAddress() == null || order.getShippingAddress().getDistrict() == null) {
            return "";
        }
        return order.getShippingAddress().getDistrict().trim();
    }

    private BigDecimal resolveShippingCost(SalesOrder order) {
        if (order == null || order.getDeliveryCharge() == null) {
            return BigDecimal.ZERO;
        }
        return order.getDeliveryCharge();
    }

    private String resolveOrderCode(Long orderId) {
        if (orderId == null) {
            return null;
        }
        return salesOrderRepository.findById(orderId)
                .map(SalesOrder::getOrderCode)
                .orElse(null);
    }

    private Long resolveOrderId(Long orderId, String orderUuid) {
        if (orderId != null || orderUuid == null || orderUuid.isBlank()) {
            return orderId;
        }
        SalesOrder order = salesOrderRepository.findByUuid(orderUuid.trim());
        return order != null ? order.getId() : null;
    }

    private String resolveShipmentSaveError(Exception ex) {
        String message = ex != null && ex.getMessage() != null ? ex.getMessage() : "";
        String normalizedMessage = message.toLowerCase();
        if (normalizedMessage.contains("constraint")
                || normalizedMessage.contains("duplicate")
                || normalizedMessage.contains("could not execute statement")) {
            return "Shipment could not be saved because the order or tracking reference is already used.";
        }
        return "Runtime error while saving shipment: " + message;
    }

}
