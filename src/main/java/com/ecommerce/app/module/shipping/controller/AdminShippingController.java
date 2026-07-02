/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.shipping.controller;

import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.repository.CarrierRepository;
import com.ecommerce.app.module.shipping.repository.DeliveryPersonRepository;
import com.ecommerce.app.module.shipping.repository.ShipmentRepository;
import com.ecommerce.app.module.shipping.services.ShipmentService;
import com.ecommerce.app.module.shipping.services.ShipmentTrackingService;
import com.ecommerce.app.module.shipping.services.PickupAddressService;
import com.ecommerce.app.module.shipping.services.ShippingDocumentService;
import com.ecommerce.app.order.model.SalesOrder;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import com.ecommerce.app.vendor.services.VendorprofileService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/admin/shipments")
public class AdminShippingController {

    @Autowired
    private ShipmentRepository shipmentRepo;
    @Autowired
    private CarrierRepository carrierRepo; // assuming you have this
    @Autowired
    private DeliveryPersonRepository deliveryPersonRepo; // optional
    @Autowired
    private VendorprofileService vendorprofileService;
    @Autowired
    private SalesOrderRepository salesOrderRepository;
    @Autowired
    private ShipmentService shipmentService;
    @Autowired
    private ShipmentTrackingService shipmentTrackingService;
    @Autowired
    private PickupAddressService pickupAddressService;
    @Autowired
    private ShippingDocumentService shippingDocumentService;

    @GetMapping("/list")
    public String list(Model model) {
        try {
            List<Shipment> shipments = shipmentService.getAll();
            model.addAttribute("shipments", shipments);
            model.addAttribute("trackingEventsByShipment", shipmentTrackingService.getEventsByShipmentIds(
                    shipments.stream().map(Shipment::getId).filter(Objects::nonNull).toList()
            ));
        } catch (Exception ex) {
            model.addAttribute("shipments", List.of());
            model.addAttribute("trackingEventsByShipment", Map.of());
            model.addAttribute("errorMessage", "Runtime error while loading shipments: " + ex.getMessage());
        }
        return "admin/shipping/shipment_list";
    }

    @GetMapping("/create")
    public String newForm(@RequestParam(name = "orderId", required = false) Long orderId,
            @RequestParam(name = "orderUuid", required = false) String orderUuid,
            Model model) {
        Shipment shipment = new Shipment();
        model.addAttribute("shipment", shipment);
        model.addAttribute("trackingEvents", List.of());
        try {
            orderId = resolveOrderId(orderId, orderUuid);
            if (orderId != null) {
                String prefillError = prefillShipmentFromOrder(shipment, orderId, true);
                if (prefillError != null) {
                    model.addAttribute("errorMessage", prefillError);
                }
            }
            populateFormOptions(model, shipment.getSalesOrderId());
        } catch (Exception ex) {
            populateEmptyFormOptions(model);
            model.addAttribute("errorMessage", "Runtime error while preparing shipment form: " + ex.getMessage());
        }
        return "admin/shipping/admin_shipments_form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
        try {
            Shipment shipment = shipmentRepo.findById(id).orElse(null);
            if (shipment == null) {
                redirectAttrs.addFlashAttribute("errorMessage", "Shipment not found");
                return "redirect:/admin/shipments/list";
            }
            shipmentService.syncCodFromOrder(shipment);
            model.addAttribute("shipment", shipment);
            model.addAttribute("trackingEvents", shipmentTrackingService.getEvents(shipment.getId()));
            populateFormOptions(model, shipment.getSalesOrderId());
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("errorMessage", "Runtime error while loading shipment: " + ex.getMessage());
            return "redirect:/admin/shipments/list";
        }
        return "admin/shipping/admin_shipments_form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Shipment shipment,
            BindingResult result,
            RedirectAttributes redirectAttrs,
            Model model) {
        Shipment existingShipment = shipment.getId() != null ? shipmentService.getById(shipment.getId()) : null;
        validateSelectedOrder(shipment, existingShipment, result, true);
        validateShipmentHandler(shipment, result);

        if (result.hasErrors()) {
            populateFormOptions(model, shipment.getSalesOrderId());
            return "admin/shipping/admin_shipments_form";
        }

        try {
            Shipment savedShipment = shipmentService.save(shipment);
            redirectAttrs.addFlashAttribute("successMessage", "Shipment saved successfully");
            if (savedShipment != null && savedShipment.getId() != null) {
                redirectAttrs.addFlashAttribute("successMessage", "Shipment #" + savedShipment.getId() + " saved successfully");
            }
        } catch (IllegalArgumentException ex) {
            populateFormOptions(model, shipment.getSalesOrderId());
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/shipping/admin_shipments_form";
        } catch (DataIntegrityViolationException ex) {
            populateFormOptions(model, shipment.getSalesOrderId());
            model.addAttribute("errorMessage", "Shipment could not be saved because the order or tracking reference is already used.");
            return "admin/shipping/admin_shipments_form";
        } catch (Exception ex) {
            populateFormOptions(model, shipment.getSalesOrderId());
            model.addAttribute("errorMessage", resolveShipmentSaveError(ex));
            return "admin/shipping/admin_shipments_form";
        }
        return "redirect:/admin/shipments/list";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            shipmentRepo.deleteById(id);
            redirectAttrs.addFlashAttribute("successMessage", "Shipment deleted successfully");
        } catch (DataIntegrityViolationException e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Cannot delete shipment. It has related items!");
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("errorMessage", "Runtime error while deleting shipment: " + ex.getMessage());
        }
        return "redirect:/admin/shipments/list";
    }

    @PostMapping("/{id}/generate-label")
    public String generateLabel(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            shippingDocumentService.generateLabelForShipment(id, "Admin Panel");
            redirectAttrs.addFlashAttribute("successMessage", "Shipping label generated and linked with shipment.");
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("errorMessage", "Could not generate shipping label: " + ex.getMessage());
        }
        return "redirect:/admin/shipments/list";
    }

    private void populateFormOptions(Model model, Long includeOrderId) {
        model.addAttribute("carriers", carrierRepo.findAll());
        model.addAttribute("deliveryPersons", deliveryPersonRepo.findAll());
        model.addAttribute("pickupAddresses", pickupAddressService.getAll());
        model.addAttribute("vendors", vendorprofileService.findAll());
        model.addAttribute("salesOrders", buildSalesOrderOptions(includeOrderId));
        model.addAttribute("selectedOrderCode", resolveOrderCode(includeOrderId));
    }

    private void populateEmptyFormOptions(Model model) {
        model.addAttribute("carriers", List.of());
        model.addAttribute("deliveryPersons", List.of());
        model.addAttribute("pickupAddresses", List.of());
        model.addAttribute("vendors", List.of());
        model.addAttribute("salesOrders", List.of());
    }

    private List<Map<String, Object>> buildSalesOrderOptions(Long includeOrderId) {
        List<Map<String, Object>> options = new ArrayList<>();
        for (SalesOrder order : shipmentService.getEligibleOrders(null, includeOrderId)) {
            Map<String, Object> option = new HashMap<>();
            option.put("id", order.getId());
            option.put("orderCode", order.getOrderCode());
            option.put("vendorId", order.getVendorId());
            option.put("district", resolveShippingDistrict(order));
            option.put("shippingCost", resolveShippingCost(order));
            option.put("grandTotal", order.getGrandTotal() != null ? order.getGrandTotal() : BigDecimal.ZERO);
            options.add(option);
        }
        return options;
    }

    private SalesOrder validateSelectedOrder(Shipment shipment, Shipment existingShipment, BindingResult result, boolean enforceVendorMatch) {
        if (shipment.getSalesOrderId() == null) {
            return null;
        }

        SalesOrder salesOrder = salesOrderRepository.findById(shipment.getSalesOrderId()).orElse(null);
        if (salesOrder == null) {
            result.rejectValue("salesOrderId", "error.shipment", "Selected sales order was not found");
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

        if (enforceVendorMatch && shipment.getVendorId() != null && salesOrder.getVendorId() != null
                && !Objects.equals(shipment.getVendorId(), salesOrder.getVendorId())) {
            result.rejectValue("vendorId", "error.shipment", "Selected vendor does not match the sales order vendor");
        }

        String orderDistrict = resolveShippingDistrict(salesOrder);
        if (orderDistrict == null || orderDistrict.isBlank()) {
            result.rejectValue("district", "error.shipment", "Selected sales order has no shipping address district");
        } else {
            shipment.setDistrict(orderDistrict);
        }

        shipmentService.syncCodFromOrder(shipment);
        return salesOrder;
    }

    private void validateShipmentHandler(Shipment shipment, BindingResult result) {
        if (shipment.getCarrier() == null && shipment.getDeliveryPerson() == null) {
            result.reject("error.shipment", "Select at least Carrier or Delivery Person");
        }
        if (shipment.getDeliveryPerson() != null
                && shipment.getDeliveryPerson().getVendorId() != null
                && shipment.getVendorId() != null
                && !Objects.equals(shipment.getDeliveryPerson().getVendorId(), shipment.getVendorId())) {
            result.rejectValue("deliveryPerson", "error.shipment", "Selected delivery person does not belong to the shipment vendor");
        }
        if (shipment.getPickupAddress() != null
                && shipment.getPickupAddress().getVendorId() != null
                && shipment.getVendorId() != null
                && !Objects.equals(shipment.getPickupAddress().getVendorId(), shipment.getVendorId())) {
            result.rejectValue("pickupAddress", "error.shipment", "Selected pickup address does not belong to the shipment vendor");
        }
    }

    private String prefillShipmentFromOrder(Shipment shipment, Long orderId, boolean enforceEligibility) {
        SalesOrder salesOrder = salesOrderRepository.findById(orderId).orElse(null);
        if (salesOrder == null) {
            return "Selected sales order was not found.";
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
        shipmentService.syncCodFromOrder(shipment);
        shipment.setPickupAddress(pickupAddressService.getDefaultForVendor(salesOrder.getVendorId()));
        return null;
    }

    private String resolveShippingDistrict(SalesOrder order) {
        if (order == null || order.getShippingAddress() == null || order.getShippingAddress().getDistrict() == null) {
            return "";
        }
        return order.getShippingAddress().getDistrict().trim();
    }

    private java.math.BigDecimal resolveShippingCost(SalesOrder order) {
        if (order == null || order.getDeliveryCharge() == null) {
            return java.math.BigDecimal.ZERO;
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
