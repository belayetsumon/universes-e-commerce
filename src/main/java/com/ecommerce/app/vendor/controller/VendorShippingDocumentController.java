package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.module.shipping.model.Carrier;
import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.model.ShippingManifest;
import com.ecommerce.app.module.shipping.services.CarrierService;
import com.ecommerce.app.module.shipping.services.ShipmentService;
import com.ecommerce.app.module.shipping.services.ShippingDocumentService;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vendor/shipping-documents")
public class VendorShippingDocumentController {

    private final ShippingDocumentService shippingDocumentService;
    private final ShipmentService shipmentService;
    private final CarrierService carrierService;
    private final VendorUserContext vendorUserContext;

    public VendorShippingDocumentController(ShippingDocumentService shippingDocumentService,
            ShipmentService shipmentService,
            CarrierService carrierService,
            VendorUserContext vendorUserContext) {
        this.shippingDocumentService = shippingDocumentService;
        this.shipmentService = shipmentService;
        this.carrierService = carrierService;
        this.vendorUserContext = vendorUserContext;
    }

    @GetMapping("/labels")
    public String labels(Model model, RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = requireVendor(redirectAttributes);
        if (activeVendor == null) {
            return "redirect:/vendor-order/index";
        }
        model.addAttribute("labels", shippingDocumentService.getLabelsByVendor(activeVendor.getId()));
        model.addAttribute("shipments", shipmentService.getByVendor(activeVendor.getId()));
        return "vendor/shipping_documents/labels";
    }

    @PostMapping("/labels/generate")
    public String generateLabel(@RequestParam("shipmentId") Long shipmentId,
            RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = requireVendor(redirectAttributes);
        if (activeVendor == null) {
            return "redirect:/vendor-order/index";
        }
        Shipment shipment = getVendorShipment(shipmentId, activeVendor.getId());
        if (shipment == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shipment not found for the active vendor.");
            return "redirect:/vendor/shipping-documents/labels";
        }
        try {
            shippingDocumentService.generateLabelForShipment(shipment.getId(), "Vendor Panel");
            redirectAttributes.addFlashAttribute("successMessage", "Shipping label generated for shipment #" + shipment.getId() + ".");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not generate label: " + ex.getMessage());
        }
        return "redirect:/vendor/shipping-documents/labels";
    }

    @GetMapping("/manifests")
    public String manifests(Model model, RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = requireVendor(redirectAttributes);
        if (activeVendor == null) {
            return "redirect:/vendor-order/index";
        }
        model.addAttribute("manifests", shippingDocumentService.getManifestsByVendor(activeVendor.getId()));
        return "vendor/shipping_documents/manifests";
    }

    @GetMapping("/manifests/create")
    public String createManifest(Model model, RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = requireVendor(redirectAttributes);
        if (activeVendor == null) {
            return "redirect:/vendor-order/index";
        }
        model.addAttribute("manifest", new ShippingManifest());
        model.addAttribute("shipments", shipmentService.getByVendor(activeVendor.getId()));
        model.addAttribute("carriers", carrierService.getAll());
        return "vendor/shipping_documents/manifest_form";
    }

    @PostMapping("/manifests/save")
    public String saveManifest(@RequestParam(name = "shipmentIds", required = false) List<Long> shipmentIds,
            @RequestParam(name = "carrierId", required = false) Long carrierId,
            @RequestParam(name = "closed", defaultValue = "false") boolean closed,
            RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = requireVendor(redirectAttributes);
        if (activeVendor == null) {
            return "redirect:/vendor-order/index";
        }
        if (shipmentIds == null || shipmentIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Select at least one shipment for the manifest.");
            return "redirect:/vendor/shipping-documents/manifests/create";
        }
        List<Shipment> shipments = shipmentIds.stream()
                .map(id -> getVendorShipment(id, activeVendor.getId()))
                .filter(Objects::nonNull)
                .toList();
        if (shipments.isEmpty() || shipments.size() != shipmentIds.size()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Manifest can include only shipments for the active vendor.");
            return "redirect:/vendor/shipping-documents/manifests/create";
        }
        ShippingManifest manifest = new ShippingManifest();
        Carrier carrier = carrierId != null ? carrierService.getById(carrierId) : null;
        manifest.setCarrier(carrier);
        manifest.setShipments(shipments);
        manifest.setClosed(closed);
        shippingDocumentService.saveManifest(manifest);
        redirectAttributes.addFlashAttribute("successMessage", "Manifest created for carrier handover.");
        return "redirect:/vendor/shipping-documents/manifests";
    }

    @GetMapping("/invoices")
    public String invoices(Model model, RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = requireVendor(redirectAttributes);
        if (activeVendor == null) {
            return "redirect:/vendor-order/index";
        }
        model.addAttribute("invoices", shippingDocumentService.getInvoicesByVendor(activeVendor.getId()));
        model.addAttribute("shipments", shipmentService.getByVendor(activeVendor.getId()));
        return "vendor/shipping_documents/invoices";
    }

    @PostMapping("/invoices/generate")
    public String generateInvoice(@RequestParam("shipmentId") Long shipmentId,
            RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = requireVendor(redirectAttributes);
        if (activeVendor == null) {
            return "redirect:/vendor-order/index";
        }
        Shipment shipment = getVendorShipment(shipmentId, activeVendor.getId());
        if (shipment == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shipment not found for the active vendor.");
            return "redirect:/vendor/shipping-documents/invoices";
        }
        shippingDocumentService.generateInvoiceForShipment(shipment.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Shipment invoice generated from settlement snapshot.");
        return "redirect:/vendor/shipping-documents/invoices";
    }

    private Vendorprofile requireVendor(RedirectAttributes redirectAttributes) {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor == null || activeVendor.getId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor context not found.");
            return null;
        }
        return activeVendor;
    }

    private Shipment getVendorShipment(Long shipmentId, Long vendorId) {
        Shipment shipment = shipmentService.getById(shipmentId);
        if (shipment == null || !Objects.equals(shipment.getVendorId(), vendorId)) {
            return null;
        }
        return shipment;
    }
}
